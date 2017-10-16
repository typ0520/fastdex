package fastdex.build.task

import fastdex.build.lib.fd.Communicator
import fastdex.build.lib.fd.ServiceCommunicator
import fastdex.build.util.FastdexInstantRun
import fastdex.build.util.FastdexRuntimeException
import fastdex.build.util.FastdexUtils
import fastdex.build.util.MetaInfo
import fastdex.build.variant.FastdexVariant
import fastdex.common.ShareConstants
import fastdex.common.fd.ProtocolConstants
import fastdex.common.utils.FileUtils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Created by tong on 17/3/12.
 */
public class FastdexPatchTask extends DefaultTask {
    FastdexVariant fastdexVariant

    FastdexPatchTask() {
        group = 'fastdex'
    }

    @TaskAction
    void patch() {
        if (!fastdexVariant.hasDexCache) {
            return
        }
        FastdexInstantRun fastdexInstantRun = fastdexVariant.fastdexInstantRun
        if (!fastdexInstantRun.isInstantRunBuild()) {
            fastdexVariant.project.logger.error("==fastdex instant run disable")
            return
        }
        if (fastdexInstantRun.manifestChanged) {
            fastdexVariant.project.logger.error("==fastdex instant run disable, manifest.xml changed")
            return
        }

        boolean nothingChanged = fastdexInstantRun.nothingChanged()
        fastdexInstantRun.preparedDevice()

        def packageName = fastdexVariant.getMergedPackageName()
        ServiceCommunicator serviceCommunicator = new ServiceCommunicator(packageName)
        MetaInfo runtimeMetaInfo = null

        boolean manifestChanged = fastdexInstantRun.manifestChanged
        boolean resourceChanged = fastdexInstantRun.resourceChanged
        boolean sourceChanged = fastdexInstantRun.sourceChanged
        boolean assetsChanged = fastdexInstantRun.assetsChanged

        boolean sendResourcesApk = false
        boolean sendPatchDex = false
        boolean sendMergedDex = false

        project.logger.error("==fastdex pc   resourceChanged: ${fastdexInstantRun.resourceChanged} , assetsChanged: ${fastdexInstantRun.assetsChanged}, sourceChanged: ${fastdexInstantRun.sourceChanged}, manifestChanged: ${manifestChanged}")

        try {
            runtimeMetaInfo = serviceCommunicator.talkToService(fastdexInstantRun.device, new Communicator<MetaInfo>() {
                @Override
                public MetaInfo communicate(DataInputStream input, DataOutputStream output) throws IOException {
                    output.writeInt(ProtocolConstants.MESSAGE_PING_AND_SHOW_TOAST)

                    MetaInfo info = new MetaInfo()
                    info.active = input.readBoolean()
                    info.buildMillis = input.readLong()
                    info.variantName = input.readUTF()

                    if (fastdexVariant.metaInfo.buildMillis != info.buildMillis) {
                        fastdexVariant.project.logger.error("==fastdex buildMillis not equal, install apk")
                        throw new FastdexRuntimeException()
                    }
                    if (!fastdexVariant.metaInfo.variantName.equals(info.variantName)) {
                        fastdexVariant.project.logger.error("==variantName not equal, install apk")
                        throw new FastdexRuntimeException()
                    }

                    info.resourcesVersion = input.readInt()
                    info.patchDexVersion = input.readInt()
                    info.mergedDexVersion = input.readInt()

                    if (resourceChanged || assetsChanged || fastdexVariant.metaInfo.resourcesVersion != info.resourcesVersion) {
                        sendResourcesApk = true
                    }

                    if ((sourceChanged || fastdexVariant.metaInfo.patchDexVersion != info.patchDexVersion) && !fastdexVariant.willExecDexMerge()) {
                        sendPatchDex = true
                    }

                    if (fastdexVariant.metaInfo.mergedDexVersion != info.mergedDexVersion) {
                        sendMergedDex = true
                    }

                    nothingChanged = !sendResourcesApk && !sendPatchDex && !sendMergedDex

                    if (nothingChanged) {
                        output.writeUTF("Source and resource not changed.")
                    }
                    else {
                        output.writeUTF(" ")
                    }
                    return info
                }
            })

        } catch (Throwable e) {
            if (!(e instanceof FastdexRuntimeException)) {
                e.printStackTrace()
                fastdexVariant.project.logger.error("==fastdex ping installed app fail: " + e.message)
            }
            return
        }
        project.logger.error("==fastdex receive: ${runtimeMetaInfo}")
        project.logger.error("==fastdex app  sendResourcesApk: ${sendResourcesApk} , sendPatchDex: ${sendPatchDex}, sendMergedDex: ${sendMergedDex}")

        if (nothingChanged) {
            fastdexInstantRun.setInstallApk(false)

            if (runtimeMetaInfo != null && !runtimeMetaInfo.active) {
                fastdexInstantRun.startTransparentActivity()
            }
            return
        }
        File mergedPatchDex = FastdexUtils.getMergedPatchDex(fastdexVariant.project,fastdexVariant.variantName)
        File patchDex = FastdexUtils.getPatchDexFile(fastdexVariant.project,fastdexVariant.variantName)
        File resourcesApk = null

        int changeCount = 0
        if (sendResourcesApk) {
            resourcesApk = fastdexInstantRun.getResourcesApk()
            changeCount += 1
        }

        if (sendPatchDex) {
            changeCount += 1
        }

        if (sendMergedDex) {
            changeCount += 1
        }

        long start = System.currentTimeMillis()
        try {
            boolean result = serviceCommunicator.talkToService(fastdexInstantRun.device, new Communicator<Boolean>() {
                @Override
                public Boolean communicate(DataInputStream input, DataOutputStream output) throws IOException {
                    output.writeInt(ProtocolConstants.MESSAGE_PATCHES)
                    output.writeLong(ShareConstants.MESSAGE_TOKEN)
                    output.writeInt(changeCount)

                    if (sendResourcesApk) {
                        String path = fastdexVariant.metaInfo.resourcesVersion + ShareConstants.RES_SPLIT_STR + ShareConstants.RESOURCE_APK_FILE_NAME
                        project.logger.error("==fastdex write ${resourcesApk} ,path: " + path)
                        output.writeUTF(path)
                        byte[] bytes = FileUtils.readContents(resourcesApk)
                        output.writeInt(bytes.length)
                        output.write(bytes)
                    }

                    if (sendPatchDex) {
                        String path = fastdexVariant.metaInfo.patchDexVersion + ShareConstants.RES_SPLIT_STR + ShareConstants.PATCH_DEX
                        project.logger.error("==fastdex write ${patchDex}, path: " + path)
                        output.writeUTF(path)
                        byte[] bytes = FileUtils.readContents(patchDex)
                        output.writeInt(bytes.length)
                        output.write(bytes)
                    }

                    if (sendMergedDex) {
                        String path = fastdexVariant.metaInfo.mergedDexVersion + ShareConstants.RES_SPLIT_STR + ShareConstants.MERGED_PATCH_DEX
                        project.logger.error("==fastdex write ${mergedPatchDex} ,path: " + path)
                        output.writeUTF(path)
                        byte[] bytes = FileUtils.readContents(mergedPatchDex)
                        output.writeInt(bytes.length)
                        output.write(bytes)
                    }

                    output.writeInt(ProtocolConstants.UPDATE_MODE_WARM_SWAP)
                    //show toast
                    output.writeBoolean(true)
                    output.writeBoolean(fastdexVariant.configuration.restartAppByCmd)

                    try {
                        return input.readBoolean()
                    } catch (Throwable e) {
                        return false
                    }
                }
            })
            long end = System.currentTimeMillis();
            project.logger.error("==fastdex send patch data success. use: ${end - start}ms")

            //app不在后台、补丁发送失败、补丁中包含dex并且有设置使用命令强制重启
            //当前的activity栈如果只有一个activity并且是启动页activity不使用adb命令强制重启
            if (!runtimeMetaInfo.active || !result || ((sendPatchDex || sendMergedDex) && (fastdexVariant.configuration.restartAppByCmd))) {
                killApp()
                fastdexInstantRun.startBootActivity()
            }
            fastdexInstantRun.setInstallApk(false)
        } catch (Throwable e) {
            e.printStackTrace()
        }
    }

    def killApp() {
        FastdexInstantRun fastdexInstantRun = fastdexVariant.fastdexInstantRun

        //adb shell am force-stop 包名
        def packageName = fastdexVariant.getMergedPackageName()

        List<String> cmdArgs = new ArrayList<>()
        cmdArgs.add(FastdexUtils.getAdbCmdPath(project))
        cmdArgs.add("-s")
        cmdArgs.add(fastdexInstantRun.device.getSerialNumber())
        cmdArgs.add("shell")
        cmdArgs.add("am")
        cmdArgs.add("force-stop")
        cmdArgs.add(packageName)

        FastdexUtils.runCommand(fastdexVariant.project, cmdArgs)
    }
}
