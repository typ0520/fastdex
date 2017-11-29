package fastdex.build.util

import com.android.ddmlib.AndroidDebugBridge
import com.android.ddmlib.IDevice
import fastdex.build.variant.FastdexVariant
import fastdex.common.utils.FileUtils
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Project
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes

/**
 * Created by tong on 17/3/12.
 */
class FastdexInstantRun {
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
        int count = 0
        while (!bridge.hasInitialDeviceList()) {
            try {
                Thread.sleep(100)
                count++
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
    def onResourceChanged() {
        resourceChanged = true
    }

    /**
     * 如果执行了dexTransform则代码发生变化
     */
    def onSourceChanged() {
        sourceChanged = true
    }

    /**
     * 如果assets发生变化会执行mergeAssets任务
     */
    def onAssetsChanged() {
        assetsChanged = true
    }

    def onManifestChanged() {

    }

    def onFastdexPrepare() {

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

            File assetsPath = fastdexVariant.androidVariant.getMergeAssets().outputDir
            List<String> assetFiles = getAssetFiles(assetsPath)

            if (assetFiles.isEmpty()) {
                project.logger.error("==fastdex \ncopy : ${resourceApFile} \ninto: ${resourcesApk}")
                FileUtils.copyFileUsingStream(resourceApFile,resourcesApk)
            }
            else {
                File tempDir = new File(FastdexUtils.getResourceDir(project,fastdexVariant.variantName),"temp")
                FileUtils.cleanDir(tempDir)
                File tempResourcesApk = new File(tempDir,resourcesApk.getName())
                FileUtils.copyFileUsingStream(resourceApFile,tempResourcesApk)
                project.logger.error("==fastdex copy resources.ap_ \ncopy : ${resourceApFile} \ninto: ${tempResourcesApk}")

                File tempAssetsPath = new File(tempDir,"assets")
                FileUtils.copyDir(assetsPath,tempAssetsPath)
                List<String> cmdArgs = new ArrayList<>()
                cmdArgs.add(FastdexUtils.getAaptCmdPath(project))
                cmdArgs.add("add")
                cmdArgs.add("-f")
                cmdArgs.add(tempResourcesApk.absolutePath)

                for (int i = 0; i < assetFiles.size(); i++) {
                    cmdArgs.add("assets/" + assetFiles.get(i).toString())
                }

                FastdexUtils.runCommand(project,cmdArgs,tempDir,false)

                tempResourcesApk.renameTo(resourcesApk)
                FileUtils.deleteDir(tempDir)
            }

            long end = System.currentTimeMillis()
            fastdexVariant.project.logger.error("==fastdex generate resources.apk success, use: ${end - start}ms")
        }
        else {
            project.logger.error("==fastdex \ncopy : ${resourceApFile} \ninto: ${resourcesApk}")
            FileUtils.copyFileUsingStream(resourceApFile,resourcesApk)
        }
    }

    def getAssetFiles(File dir) {
        ArrayList<String> result = new ArrayList<>()
        if (dir == null || !FileUtils.dirExists(dir.getAbsolutePath())) {
            return result
        }
        if (dir.listFiles().length == 0) {
            return result
        }

        boolean isWindows = Os.isFamily(Os.FAMILY_WINDOWS)
        Path path = dir.toPath()
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (!file.toFile().isHidden()) {
                    String relativePath = path.relativize(file).toString()
                    if (isWindows) {
                        relativePath = relativePath.replaceAll("\\\\","/")
                    }
                    result.add(relativePath)
                }
                return FileVisitResult.CONTINUE
            }
        })
        return result
    }

    def startBootActivity() {
        startBootActivity(false)
    }

    def startBootActivity(boolean background) {
        def packageName = fastdexVariant.getMergedPackageName()

        //启动第一个activity
        String bootActivityName = GradleUtils.getBootActivity(fastdexVariant.manifestPath)
        if (bootActivityName) {
            List<String> cmdArgs = new ArrayList<>()
            cmdArgs.add(FastdexUtils.getAdbCmdPath(project))
            cmdArgs.add("-s")
            cmdArgs.add(device.getSerialNumber())
            cmdArgs.add("shell")
            cmdArgs.add("am")
            cmdArgs.add("start")
            cmdArgs.add("-n")
            cmdArgs.add(packageName + "/" + bootActivityName)
            cmdArgs.add("-a")
            cmdArgs.add("android.intent.action.MAIN")
            cmdArgs.add("-c")
            cmdArgs.add("android.intent.category.LAUNCHER")

            //$ adb shell am start -n "com.dx168.fastdex.sample/com.dx168.fastdex.sample.MainActivity" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER

           FastdexUtils.runCommand(project,cmdArgs,background)
        }
    }
}
