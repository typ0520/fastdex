package fastdex.build.util

import com.android.build.gradle.tasks.ProcessAndroidResources
import com.android.utils.ILogger
import fastdex.build.variant.FastdexVariant
import fastdex.common.utils.FileUtils
import java.lang.reflect.Constructor
import java.lang.reflect.Field

/**
 * Created by tong on 17/11/3.
 */
class FixPublicSymbolLogger implements ILogger {
    final File resDir
    final FastdexVariant fastdexVariant
    final ILogger base
    boolean monitoring

    FixPublicSymbolLogger(File resDir, FastdexVariant fastdexVariant,ILogger base) {
        this.resDir = resDir
        this.fastdexVariant = fastdexVariant
        this.base = base
    }

    @Override
    void error(Throwable throwable, String s, Object... objects) {
        if (s != null && monitoring) {
            int index = s.indexOf("error: Public symbol")
            if (index != -1) {
                def project = fastdexVariant.project
                File idsXmlFile = FastdexUtils.getIdxXmlFile(project,fastdexVariant.variantName)
                File publicXmlFile = FastdexUtils.getPublicXmlFile(project,fastdexVariant.variantName)

                File resDirIdsXmlFile = new File(resDir,"/values/ids.xml")
                File resDirPublicXmlFile = new File(resDir,"/values/public.xml")

                FileUtils.deleteFile(idsXmlFile)
                FileUtils.deleteFile(publicXmlFile)
                FileUtils.deleteFile(resDirIdsXmlFile)
                FileUtils.deleteFile(resDirPublicXmlFile)

                project.logger.error("==fastdex some elements of public.xml can not be found. remove: ")
                project.logger.error(idsXmlFile.absolutePath)
                project.logger.error(publicXmlFile.absolutePath)
                project.logger.error(resDirIdsXmlFile.absolutePath)
                project.logger.error(resDirPublicXmlFile.absolutePath)
                project.logger.error("==automatically restored the state")
            }
        }
        this.base.error(throwable,s,objects)
    }

    @Override
    void warning(String s, Object... objects) {
        this.base.warning(s,objects)
    }

    @Override
    void info(String s, Object... objects) {
        this.base.info(s,objects)
    }

    @Override
    void verbose(String s, Object... objects) {
        this.base.verbose(s,objects)
    }

    /**
     如果每次执行processResources任务时都生成public.xml和ids.xml会浪费很多时间，目前对这两个文件做了缓存；
     这样做会在删除某个id时出现类似于下面这样的错误，此方法是拦截错误日志，如果发现下面这样的错误时自动清除缓存的public.xml和ids.xml；
     清除后重新触发编译就不会有这个错误了，在效率和稳定性上做了一个折中

     AGPBI: {"kind":"error","text":"Public symbol layout/fragment_main declared here is not defined.","sources":[{"file":"/Users/tong/Projects/zxgb-android/app/build/intermediates/res/merged/release/values/public.xml","position":{"startLine":708}}],"original":"","tool":"AAPT"}
     AGPBI: {"kind":"error","text":"Public symbol layout/item_trade_signals declared here is not defined.","sources":[{"file":"/Users/tong/Projects/zxgb-android/app/build/intermediates/res/merged/release/values/public.xml","position":{"startLine":881}}],"original":"","tool":"AAPT"}
     AGPBI: {"kind":"error","text":"Public symbol layout/pager_navigator_layout declared here is not defined.","sources":[{"file":"/Users/tong/Projects/zxgb-android/app/build/intermediates/res/merged/release/values/public.xml","position":{"startLine":460}}],"original":"","tool":"AAPT"}
     AGPBI: {"kind":"error","text":"Public symbol layout/pager_navigator_layout_no_scroll declared here is not defined.","sources":[{"file":"/Users/tong/Projects/zxgb-android/app/build/intermediates/res/merged/release/values/public.xml","position":{"startLine":845}}],"original":"","tool":"AAPT"}
     AGPBI: {"kind":"error","text":"Public symbol layout/simple_fragment_layout declared here is not defined.","sources":[{"file":"/Users/tong/Projects/zxgb-android/app/build/intermediates/res/merged/release/values/public.xml","position":{"startLine":605}}],"original":"","tool":"AAPT"}
     AGPBI: {"kind":"error","text":"Public symbol layout/view_stock_friend_header declared here is not defined.","sources":[{"file":"/Users/tong/Projects/zxgb-android/app/build/intermediates/res/merged/release/values/public.xml","position":{"startLine":581}}],"original":"","tool":"AAPT"}
     /Users/tong/Projects/zxgb-android/app/build/intermediates/res/merged/release/values/public.xml:709: error: Public symbol layout/fragment_main declared here is not defined.
     /Users/tong/Projects/zxgb-android/app/build/intermediates/res/merged/release/values/public.xml:882: error: Public symbol layout/item_trade_signals declared here is not defined.
     /Users/tong/Projects/zxgb-android/app/build/intermediates/res/merged/release/values/public.xml:461: error: Public symbol layout/pager_navigator_layout declared here is not defined.
     /Users/tong/Projects/zxgb-android/app/build/intermediates/res/merged/release/values/public.xml:846: error: Public symbol layout/pager_navigator_layout_no_scroll declared here is not defined.
     /Users/tong/Projects/zxgb-android/app/build/intermediates/res/merged/release/values/public.xml:606: error: Public symbol layout/simple_fragment_layout declared here is not defined.
     /Users/tong/Projects/zxgb-android/app/build/intermediates/res/merged/release/values/public.xml:582: error: Public symbol layout/view_stock_friend_header declared here is not defined.
     */
    static void inject(ProcessAndroidResources processResources, File resDir, FastdexVariant fastdexVariant) {
        try {
            Field androidBuilderField = ReflectUtils.getFieldByName(processResources.getClass(),"androidBuilder")
            androidBuilderField.setAccessible(true)
            def base = androidBuilderField.get(processResources)
            Constructor constructor = base.getClass().getConstructors()[0]
            FixPublicSymbolLogger fixPublicSymbolLogger = new FixPublicSymbolLogger(resDir,fastdexVariant,base.mLogger)
            def androidBuilder = constructor.newInstance(base.mProjectId
                    ,base.mCreatedBy
                    ,base.mProcessExecutor
                    ,base.mJavaProcessExecutor
                    ,base.mErrorReporter
                    ,fixPublicSymbolLogger
                    ,base.mVerboseExec)

            Field[] fields = androidBuilder.getClass().getDeclaredFields()
            for (Field field : fields) {
                if (!java.lang.reflect.Modifier.isStatic(field.getModifiers()) && !java.lang.reflect.Modifier.isFinal(field.getModifiers())) {
                    field.setAccessible(true)
                    field.set(androidBuilder,field.get(base))
                }
            }
            androidBuilderField.set(processResources,androidBuilder)

            processResources.doFirst {
                fixPublicSymbolLogger.monitoring = true
            }

            processResources.doLast {
                fixPublicSymbolLogger.monitoring = false
            }
        } catch (Throwable e) {

        }
    }
}
