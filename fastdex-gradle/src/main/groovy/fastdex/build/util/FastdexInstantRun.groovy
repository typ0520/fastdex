package fastdex.build.util

import com.android.ddmlib.AndroidDebugBridge
import com.android.ddmlib.IDevice
import fastdex.build.task.FastdexManifestTask
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
            String serial = fastdexVariant.project.properties.get("DEVICE_SN")

            if (fastdexVariant.project.hasProperty("DEVICE_SN")) {
                for (IDevice d : devices) {
                    if (d.getSerialNumber().equals(serial)) {
                        device = d
                        break
                    }
                }

                if (device == null && !background) {
                    throw new FastdexRuntimeException("找不到序列号是${serial}的adb设备")
                }
            }
            else {
                if (background) {
                   if (devices.length == 1) {
                       device = devices[0]
                   }
                }
                else {
                    if (devices.length > 1) {
                        throw new FastdexRuntimeException("发现了多个Android设备，请使用 -PDEVICE_SN= 指定adb设备的序列号")
                    }
                    device = devices[0]
                }
            }
        }

        if (device == null && !background) {
            throw new FastdexRuntimeException("没有发现Android设备，请确认连接是否正常 adb devices")
        }
        if (device != null) {
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

    }

    def isInstantRunBuild() {
        return fromFastdexInstantRun
    }

    def nothingChanged() {
        return !sourceChanged && !manifestChanged && !resourceChanged && !assetsChanged
    }

    def setInstallApk(boolean installApk) {
        this.installApk = installApk

        if (!installApk) {
            try {
                project.tasks.getByName("transformNativeLibsWithMergeJniLibsFor${fastdexVariant.variantName}").enabled = false
            } catch (Throwable e) {

            }
            try {
                project.tasks.getByName("transformResourcesWithMergeJavaResFor${fastdexVariant.variantName}").enabled = false
            } catch (Throwable e) {

            }

            try {
                project.tasks.getByName("merge${fastdexVariant.variantName}JniLibFolders").enabled = false
            } catch (Throwable e) {

            }

            try {
                project.tasks.getByName("process${fastdexVariant.variantName}JavaRes").enabled = false
            } catch (Throwable e) {

            }
            try {
                project.tasks.getByName("validateSigning${fastdexVariant.variantName}").enabled = false
            } catch (Throwable e) {

            }
            try {
                project.tasks.getByName("validate${fastdexVariant.variantName}Signing").enabled = false
            } catch (Throwable e) {

            }
            try {
                project.tasks.getByName("package${fastdexVariant.variantName}").enabled = false
            } catch (Throwable e) {

            }
            try {
                project.tasks.getByName("zipalign${fastdexVariant.variantName}").enabled = false
            } catch (Throwable e) {

            }
            try {
                project.tasks.getByName("assemble${fastdexVariant.variantName}").enabled = false
            } catch (Throwable e) {

            }
        }
    }

    def getResourcesApk() {
        File resourcesApk = FastdexUtils.getResourcesApk(project,fastdexVariant.variantName)
        if (resourceChanged || assetsChanged || !FileUtils.isLegalFile(resourcesApk)) {
            FileUtils.deleteFile(resourcesApk)
            generateResourceApk(resourcesApk)
            fastdexVariant.metaInfo.resourcesVersion += 1
            fastdexVariant.metaInfo.save(fastdexVariant)
        }
        return resourcesApk
    }

    def generateResourceApk(File resourcesApk) {
        if (GradleUtils.getAndroidGradlePluginVersion().compareTo("2.2") >= 0) {
            long start = System.currentTimeMillis()
            File tempDir = new File(FastdexUtils.getResourceDir(project,fastdexVariant.variantName),"temp")
            FileUtils.cleanDir(tempDir)

            File tempResourcesApk = new File(tempDir,resourcesApk.getName())
            FileUtils.copyFileUsingStream(resourceApFile,tempResourcesApk)

            project.logger.error("==fastdex copy resources.ap_ \ncopy : ${resourceApFile} \ninto: ${tempResourcesApk}")
            File assetsPath = fastdexVariant.androidVariant.getVariantData().getScope().getMergeAssetsOutputDir()
            List<String> assetFiles = getAssetFiles(assetsPath)
            File tempAssetsPath = new File(tempDir,"assets")
            FileUtils.copyDir(assetsPath,tempAssetsPath)

            String[] cmds = new String[assetFiles.size() + 4]
            cmds[0] = FastdexUtils.getAaptCmdPath(project)
            cmds[1] = "add"
            cmds[2] = "-f"
            cmds[3] = tempResourcesApk.absolutePath


            for (int i = 0; i < assetFiles.size(); i++) {
                cmds[4 + i] = "assets/" + assetFiles.get(i).toString()
            }

            ProcessBuilder processBuilder = new ProcessBuilder(cmds)
            processBuilder.directory(tempDir)

            def process = processBuilder.start()

            InputStream is = process.getInputStream()
            BufferedReader reader = new BufferedReader(new InputStreamReader(is))
            String line = null
            while ((line = reader.readLine()) != null) {
                println(line)
            }
            reader.close()

            int status = process.waitFor()

            reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            reader.close();
            try {
                process.destroy()
            } catch (Throwable e) {

            }

            def cmd = cmds.join(" ")
            if (fastdexVariant.configuration.debug) {
                project.logger.error("==fastdex add asset files into resources.apk. cmd:\n${cmd}")
            }

            if (status != 0) {
                throw new RuntimeException("==fastdex add asset files into resources.apk fail. cmd:\n${cmd}")
            }
            else {
                tempResourcesApk.renameTo(resourcesApk)
                FileUtils.deleteDir(tempDir)
            }

            long end = System.currentTimeMillis();
            fastdexVariant.project.logger.error("==fastdex generate resources.apk success, use: ${end - start}ms")
        }
        else {
            project.logger.error("==fastdex \ncopy : ${resourceApFile} \ninto: ${resourcesApk}")
            FileUtils.copyFileUsingStream(resourceApFile,resourcesApk)
        }
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
        startBootActivity(false)
    }

    def startBootActivity(boolean background) {
        def packageName = fastdexVariant.getMergedPackageName()

        //启动第一个activity
        String bootActivityName = GradleUtils.getBootActivity(fastdexVariant.manifestPath)
        if (bootActivityName) {

            //$ adb shell am start -n "com.dx168.fastdex.sample/com.dx168.fastdex.sample.MainActivity" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER
            def process = new ProcessBuilder(FastdexUtils.getAdbCmdPath(project),"-s",device.getSerialNumber(),"shell","am","start","-n","\"${packageName}/${bootActivityName}\"","-a","android.intent.action.MAIN","-c","android.intent.category.LAUNCHER").start()
            int status = process.waitFor()
            try {
                process.destroy()
            } catch (Throwable e) {

            }

            String cmd = "adb -s ${device.getSerialNumber()} shell am start -n \"${packageName}/${bootActivityName}\" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER"
            if (!background) {
                project.logger.error("${cmd}")
            }
            if (status != 0 && !background) {
                throw new FastdexRuntimeException("==fastdex start activity fail: \n${cmd}")
            }
        }
    }

    def startTransparentActivity() {
        startTransparentActivity(false)
    }

    def startTransparentActivity(boolean background) {
        def packageName = fastdexVariant.getMergedPackageName()

        //$ adb shell am start -n "com.dx168.fastdex.sample/com.dx168.fastdex.sample.MainActivity" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER
        def process = new ProcessBuilder(FastdexUtils.getAdbCmdPath(project),"-s",device.getSerialNumber(),"shell","am","start","-n","\"${packageName}/${FastdexManifestTask.TRANSPARENT_ACTIVITY}\"").start()
        int status = process.waitFor()
        try {
            process.destroy()
        } catch (Throwable e) {

        }

        String cmd = "adb shell -s ${device.getSerialNumber()} am start -n \"${packageName}/${FastdexManifestTask.TRANSPARENT_ACTIVITY}\""
        if (!background && fastdexVariant.configuration.debug) {
            project.logger.error("${cmd}")
        }

        if (status != 0 && !background) {
            throw new FastdexRuntimeException("==fastdex start activity fail: \n${cmd}")
        }
    }
}
