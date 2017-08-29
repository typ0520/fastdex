package fastdex.build.util

import com.android.ddmlib.AndroidDebugBridge
import com.android.ddmlib.IDevice
import fastdex.build.variant.FastdexVariant
import fastdex.common.utils.FileUtils
import org.gradle.api.Project

/**
 * Created by tong on 17/3/12.
 */
public class FastdexInstantRun {
    FastdexVariant fastdexVariant
    File resourceApFile
    String resDir

    Project project
    boolean fromFastdexInstantRun
    boolean manifestChanged
    boolean resourceChanged
    boolean sourceChanged

    boolean assetsChanged

    IDevice device

    boolean installApk = true

    FastdexInstantRun(FastdexVariant fastdexVariant) {
        this.fastdexVariant = fastdexVariant
        this.project = fastdexVariant.project
    }

    private void waitForDevice(AndroidDebugBridge bridge) {
        int count = 0;
        while (!bridge.hasInitialDeviceList()) {
            try {
                Thread.sleep(100);
                count++;
            } catch (InterruptedException ignored) {
            }
            if (count > 300) {
                throw new FastdexRuntimeException("Connect adb timeout!!")
            }
        }
    }

    def preparedDevice() {
        preparedDevice(false)
    }

    def preparedDevice(boolean background) {
        if (device != null) {
            return
        }
        AndroidDebugBridge.initIfNeeded(false)
        AndroidDebugBridge bridge =
                AndroidDebugBridge.createBridge(FastdexUtils.getAdbCmdPath(fastdexVariant.project), false)
        waitForDevice(bridge)
        IDevice[] devices = bridge.getDevices()
        if (devices != null && devices.length > 0) {
            if (devices.length > 1) {
                String errmsg = "发现了多个Android设备，请拔掉数据线，只留一个设备 V_V "
//                if (background) {
//                    fastdexVariant.project.logger(errmsg)
//                }
//                else {
//                    throw new FastdexRuntimeException(errmsg)
//                }

                throw new FastdexRuntimeException(errmsg)
            }
            device = devices[0]
        }

        if (device == null) {
            String errmsg = "没有发现Android设备，请确认连接是否正常 adb devices"
//            if (background) {
//                fastdexVariant.project.logger(errmsg)
//            }
//            else {
//                throw new FastdexRuntimeException(errmsg)
//            }
            throw new FastdexRuntimeException(errmsg)
        }
        if (!background) {
            fastdexVariant.project.logger.error("==fastdex device connected ${device.toString()}")
        }
    }

    /**
     * 如果资源发生变化会执行processResources
     */
    public void onResourceChanged() {
        resourceChanged = true
    }

    /**
     * 如果执行了dexTransform则代码发生变化
     */
    public void onSourceChanged() {
        sourceChanged = true
    }

    /**
     * 如果assets发生变化会执行mergeAssets任务
     */
    public void onAssetsChanged() {
        assetsChanged = true
    }

    public void onManifestChanged() {

    }

    public void onFastdexPrepare() {
        //ping app
        //如果资源发生变化生成
        if (!isInstantRunBuild()) {
            return
        }
    }

    def isInstantRunBuild() {
//        String launchTaskName = project.gradle.startParameter.taskRequests.get(0).args.get(0).toString()
//        boolean result = launchTaskName.endsWith("fastdex${fastdexVariant.variantName}")
//        return result

        return fromFastdexInstantRun
    }

    def nothingChanged() {
        return !sourceChanged && !manifestChanged && !resourceChanged && !assetsChanged
    }

    def isInstantRunEnable() {
        //启动的任务是fastdexXXX  补丁构建  设备不为空
        return isInstantRunBuild() && fastdexVariant.hasDexCache
    }

    def setInstallApk(boolean installApk) {
        this.installApk = installApk

        if (!installApk) {
            project.tasks.getByName("package${fastdexVariant.variantName}").enabled = false
            project.tasks.getByName("assemble${fastdexVariant.variantName}").enabled = false
        }
    }

    def getResourcesApk() {
        File resourcesApk = FastdexUtils.getResourcesApk(project,fastdexVariant.variantName)
        if (resourceChanged || assetsChanged || !FileUtils.isLegalFile(resourcesApk)) {
            generateResourceApk(resourcesApk)
            fastdexVariant.metaInfo.resourcesVersion += 1
            fastdexVariant.metaInfo.save(fastdexVariant)
        }
        return resourcesApk
    }


    def generateResourceApk(File resourcesApk) {
        long start = System.currentTimeMillis()
        File tempDir = new File(FastdexUtils.getResourceDir(project,fastdexVariant.variantName),"temp")
        FileUtils.cleanDir(tempDir)

        File tempResourcesApk = new File(tempDir,resourcesApk.getName())
        FileUtils.copyFileUsingStream(resourceApFile,tempResourcesApk)

        File assetsPath = fastdexVariant.androidVariant.getVariantData().getScope().getMergeAssetsOutputDir()
        List<String> assetFiles = getAssetFiles(assetsPath)
        if (assetFiles.isEmpty()) {
            return
        }
        File tempAssetsPath = new File(tempDir,"assets")
        FileUtils.copyDir(assetsPath,tempAssetsPath)

        String[] cmds = new String[assetFiles.size() + 4]
        cmds[0] = FastdexUtils.getAaptCmdPath(project)
        cmds[1] = "add"
        cmds[2] = "-f"
        cmds[3] = tempResourcesApk.absolutePath
        for (int i = 0; i < assetFiles.size(); i++) {
            cmds[4 + i] = "assets/${assetFiles.get(i)}";
        }

        ProcessBuilder aaptProcess = new ProcessBuilder(cmds)
        aaptProcess.directory(tempDir)
        def process = aaptProcess.start()
        int status = process.waitFor()
        try {
            process.destroy()
        } catch (Throwable e) {

        }

        tempResourcesApk.renameTo(resourcesApk)
        def cmd = cmds.join(" ")
        if (fastdexVariant.configuration.debug) {
            project.logger.error("==fastdex add asset files into resources.apk. cmd:\n${cmd}")
        }
        if (status != 0) {
            throw new RuntimeException("==fastdex add asset files into resources.apk fail. cmd:\n${cmd}")
        }
        long end = System.currentTimeMillis();
        fastdexVariant.project.logger.error("==fastdex generate resources.apk success: \n==${resourcesApk} use: ${end - start}ms")
    }

    List<String> getAssetFiles(File dir) {
        ArrayList<String> result = new ArrayList<>()
        if (dir == null || !FileUtils.dirExists(dir.getAbsolutePath())) {
            return result
        }
        if (dir.listFiles().length == 0) {
            return result
        }
        for (File file : dir.listFiles()) {
            if (file.isFile() && !file.getName().startsWith(".")) {
                result.add(file.getName())
            }
        }
        return result;
    }

    def startBootActivity() {
        def packageName = fastdexVariant.getMergedPackageName()

        //启动第一个activity
        String bootActivityName = GradleUtils.getBootActivity(fastdexVariant.manifestPath)
        if (bootActivityName) {
            //$ adb shell am start -n "com.dx168.fastdex.sample/com.dx168.fastdex.sample.MainActivity" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER
            def process = new ProcessBuilder(FastdexUtils.getAdbCmdPath(project),"shell","am","start","-n","\"${packageName}/${bootActivityName}\"","-a","android.intent.action.MAIN","-c","android.intent.category.LAUNCHER").start()
            int status = process.waitFor()
            try {
                process.destroy()
            } catch (Throwable e) {

            }

            String cmd = "adb shell am start -n \"${packageName}/${bootActivityName}\" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER"
            if (fastdexVariant.configuration.debug) {
                project.logger.error("${cmd}")
            }
            if (status != 0) {
                throw new RuntimeException("==fastdex start activity fail: \n${cmd}")
            }
        }
    }
}
