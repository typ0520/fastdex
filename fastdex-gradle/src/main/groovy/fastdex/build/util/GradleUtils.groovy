package fastdex.build.util

import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.internal.pipeline.IntermediateFolderUtils
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.builder.model.Version
import com.google.common.collect.Lists
import com.android.build.gradle.internal.transforms.JarMerger
import fastdex.common.utils.FileUtils
import groovy.xml.QName
import org.gradle.api.GradleException
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformInvocation
import org.gradle.api.Project
import com.android.build.api.transform.QualifiedContent.ContentType

/**
 * Created by tong on 17/3/14.
 */
public class GradleUtils {
    public static final String UTF8_BOM = "\uFEFF";

    public static final String ANDROID_GRADLE_PLUGIN_VERSION = Version.ANDROID_GRADLE_PLUGIN_VERSION

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

    private static String removeUTF8BOM(String s) {
        if (s.startsWith(UTF8_BOM)) {
            s = s.substring(1);
        }
        return s;
    }

    /**
     * 获取启动的activity
     * @param manifestPath
     * @return
     */
    public static String getBootActivity(String manifestPath) {
        def bootActivityName = ""
        def xml = parseXml(manifestPath)
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
     * 合并所有的代码到一个jar钟
     * @param project
     * @param transformInvocation
     * @param outputJar             输出路径
     */
    public static void executeMerge(Project project,TransformInvocation transformInvocation, File outputJar) {
        List<JarInput> jarInputs = Lists.newArrayList();
        List<DirectoryInput> dirInputs = Lists.newArrayList();

        for (TransformInput input : transformInvocation.getInputs()) {
            jarInputs.addAll(input.getJarInputs());
        }

        for (TransformInput input : transformInvocation.getInputs()) {
            dirInputs.addAll(input.getDirectoryInputs());
        }

        JarMerger jarMerger = getClassJarMerger(outputJar)
        jarInputs.each { jar ->
            project.logger.error("==fastdex merge jar " + jar.getFile())
            jarMerger.addJar(jar.getFile())
        }
        dirInputs.each { dir ->
            project.logger.error("==fastdex merge dir " + dir)
            jarMerger.addFolder(dir.getFile())
        }
        jarMerger.close()
        if (!FileUtils.isLegalFile(outputJar)) {
            throw new GradleException("merge jar fail: \n jarInputs: ${jarInputs}\n dirInputs: ${dirInputs}\n mergedJar: ${outputJar}")
        }
        project.logger.error("==fastdex merge jar success: ${outputJar}")
    }

    private static JarMerger getClassJarMerger(File jarFile) {
        JarMerger jarMerger = new JarMerger(jarFile)

        Class<?> zipEntryFilterClazz
        try {
            zipEntryFilterClazz = Class.forName("com.android.builder.packaging.ZipEntryFilter")
        } catch (Throwable t) {
            zipEntryFilterClazz = Class.forName("com.android.builder.signing.SignedJarBuilder\$IZipEntryFilter")
        }

        Class<?>[] classArr = new Class[1];
        classArr[0] = zipEntryFilterClazz
        InvocationHandler handler = new InvocationHandler(){
            public Object invoke(Object proxy, Method method, Object[] args)
                    throws Throwable {
                return args[0].endsWith(Constants.CLASS_SUFFIX);
            }
        };
        Object proxy = Proxy.newProxyInstance(zipEntryFilterClazz.getClassLoader(), classArr, handler);

        jarMerger.setFilter(proxy);

        return jarMerger
    }
}
