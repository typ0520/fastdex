package fastdex.build.util

import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.internal.pipeline.IntermediateFolderUtils
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.builder.model.Version
import fastdex.common.utils.FileUtils
import groovy.xml.QName
import java.lang.reflect.Field
import com.android.build.api.transform.TransformInvocation
import org.gradle.api.Project
import com.android.build.api.transform.QualifiedContent.ContentType

/**
 * Created by tong on 17/3/14.
 */
public class GradleUtils {
    public static final String UTF8_BOM = "\uFEFF";

    /**
     * 获取指定variant的依赖列表
     * @param project
     * @param applicationVariant
     * @return
     */
    public static Set<String> getCurrentDependList(Project project,Object applicationVariant) {
        String buildTypeName = applicationVariant.getBuildType().buildType.getName()

        Set<String> result = new HashSet<>()

        project.configurations.compile.each { File file ->
            result.add(file.getAbsolutePath())
        }

        project.configurations."${buildTypeName}Compile".each { File file ->
            result.add(file.getAbsolutePath())
        }
        return result
    }

    /**
     * 获取transformClassesWithDexFor${variantName}任务的dex输出目录
     * @param transformInvocation
     * @return
     */
    public static File getDexOutputDir(TransformInvocation transformInvocation) {
        File location = com.android.utils.FileUtils.join(transformInvocation.getOutputProvider().rootLocation,
                IntermediateFolderUtils.FOLDERS,
                typesToString(TransformManager.CONTENT_DEX))

        return location
    }

    /**
     * 获取transformClassesWithDexFor${variantName}任务的dex输出目录
     * @param transformInvocation
     * @return
     */
    public static File getDexOutputDir(ApplicationVariant variant) {
        File location = com.android.utils.FileUtils.join(variant.getVariantData().getOutputs().get(0).getScope().getVariantScope().getDexOutputFolder(),
                IntermediateFolderUtils.FOLDERS,
                typesToString(TransformManager.CONTENT_DEX))

        return location
    }

    private static String typesToString(Set<ContentType> types) {
        int value = 0;
        for (ContentType type : types) {
            value += type.getValue();
        }

        return String.format("%x", value);
    }

    /**
     * 解析xml
     * @param xmlPath
     * @return
     */
    public static Object parseXml(String xmlPath) {
        byte[] bytes = FileUtils.readContents(new File(xmlPath))
        try {
            def xml = new XmlParser().parse(new InputStreamReader(new ByteArrayInputStream(bytes), "utf-8"))
            return xml
        } catch (org.xml.sax.SAXParseException e) {
            String msg = e.getMessage();
            //从eclipse转过来的项目可能会有这个问题
            if (msg != null && msg.contains("Content is not allowed in prolog.")) {
                BufferedReader r = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes), "UTF8"));

                ByteArrayOutputStream fos = new ByteArrayOutputStream();
                Writer w = new BufferedWriter(new OutputStreamWriter(fos, "Cp1252"));
                boolean firstLine = true
                for (String s = ""; (s = r.readLine()) != null;) {
                    if (firstLine) {
                        s = removeUTF8BOM(s);
                        firstLine = false;
                    }
                    w.write(s + System.getProperty("line.separator"));
                    w.flush();
                }

                def xml = new XmlParser().parse(new InputStreamReader(new ByteArrayInputStream(fos.toByteArray()), "utf-8"))
                return xml
            }
            else {
                throw new RuntimeException(e)
            }
        }
    }

    private static String removeUTF8BOM(String s) {
        if (s.startsWith(UTF8_BOM)) {
            s = s.substring(1);
        }
        return s;
    }

    /**
     * 获取AndroidManifest.xml文件package属性值
     * @param manifestPath
     * @return
     */
    public static String getPackageName(String manifestPath) {
        def xml = parseXml(manifestPath)
        String packageName = xml.attribute('package')
        return packageName
    }

    /**
     * 获取启动的activity
     * @param manifestPath
     * @return
     */
    public static String getBootActivity(String manifestPath) {
        def xml = parseXml(manifestPath)
        return getBootActivityByXmlNode(xml)
    }

    /**
     * 获取启动的activity
     * @param xml
     * @return
     */
    public static String getBootActivityByXmlNode(Node xml) {
        def bootActivityName = ""
        def application = xml.application[0]

        if (application) {
            def activities = application.activity
            QName androidNameAttr = new QName("http://schemas.android.com/apk/res/android", 'name', 'android');

            try {
                activities.each { activity->
                    def activityName = activity.attribute(androidNameAttr)

                    if (activityName) {
                        def intentFilters = activity."intent-filter"
                        if (intentFilters) {
                            intentFilters.each { intentFilter->
                                def actions = intentFilter.action
                                def categories = intentFilter.category
                                if (actions && categories) {
                                    //android.intent.action.MAIN
                                    //android.intent.category.LAUNCHER

                                    boolean hasMainAttr = false
                                    boolean hasLauncherAttr = false

                                    actions.each { action ->
                                        def attr = action.attribute(androidNameAttr)
                                        if ("android.intent.action.MAIN".equals(attr.toString())) {
                                            hasMainAttr = true
                                        }
                                    }

                                    categories.each { categoriy ->
                                        def attr = categoriy.attribute(androidNameAttr)
                                        if ("android.intent.category.LAUNCHER".equals(attr.toString())) {
                                            hasLauncherAttr = true
                                        }
                                    }

                                    if (hasMainAttr && hasLauncherAttr) {
                                        bootActivityName = activityName
                                        throw new JumpException()
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (JumpException e) {

            }
        }
        return bootActivityName
    }

    /**
     * 获取android gradle插件版本
     * @return
     */
    public static String getAndroidGradlePluginVersion() {
        return Version.ANDROID_GRADLE_PLUGIN_VERSION
    }

    /**
     * 获取application工程apt输出目录
     * @param variant
     * @return
     */
    public static File getAptOutputDir(ApplicationVariant variant) {
        if (GradleUtils.getAndroidGradlePluginVersion().compareTo("2.2") >= 0) {
            //2.2.0以后才有getAnnotationProcessorOutputDir()这个api
            return variant.getVariantData().getScope().getAnnotationProcessorOutputDir()
        }
        else {
            return new File(variant.getVariantData().getScope().getGlobalScope().getGeneratedDir(), "/source/apt/${variant.dirName}")
        }
    }

    /**
     * 动态添加属性
     * @param project
     * @param key
     * @param value
     */
    public static void addDynamicProperty(Project project,Object key, Object value) {
        Class defaultConventionClass = null
        try {
            defaultConventionClass = Class.forName("org.gradle.api.internal.plugins.DefaultConvention")
        } catch (ClassNotFoundException e) {

        }

        if (defaultConventionClass != null) {
            Field extensionsStorageField = null
            try {
                extensionsStorageField = defaultConventionClass.getDeclaredField("extensionsStorage")
                extensionsStorageField.setAccessible(true)
            } catch (Throwable e) {

            }

            if (extensionsStorageField != null) {
                try {
                    project.rootProject.allprojects.each {
                        Object extensionsStorage = extensionsStorageField.get(it.getAsDynamicObject().getConvention())
                        extensionsStorage.add(key,value)
                        it.getAsDynamicObject().getDynamicProperties().set(key,value)
                    }
                    project.gradle.startParameter.projectProperties.put(key,value)
                } catch (Throwable e) {

                }
            }
        }
    }
}