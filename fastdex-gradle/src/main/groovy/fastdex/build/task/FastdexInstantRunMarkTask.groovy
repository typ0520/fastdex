package fastdex.build.task

import fastdex.build.lib.fd.Communicator
import fastdex.build.lib.fd.ServiceCommunicator
import fastdex.build.util.MetaInfo
import fastdex.build.variant.FastdexVariant
import fastdex.common.fd.ProtocolConstants
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Created by tong on 17/3/12.
 */
public class FastdexInstantRunMarkTask extends DefaultTask {
    FastdexVariant fastdexVariant

    FastdexInstantRunMarkTask() {
        group = 'fastdex'
    }

    @TaskAction
    void mark() {
        fastdexVariant.fastdexInstantRun.fromFastdexInstantRun = true
        project.logger.error("==fastdex fromFastdexInstantRun: true")

        new Thread(new Runnable() {
            @Override
            void run() {
                try {
                    fastdexVariant.fastdexInstantRun.preparedDevice(true)
                } catch (Throwable e) {

                }
                try {
                    def packageName = fastdexVariant.getMergedPackageName()
                    ServiceCommunicator serviceCommunicator = new ServiceCommunicator(packageName)
                    MetaInfo runtimeMetaInfo = serviceCommunicator.talkToService(fastdexInstantRun.device, new Communicator<MetaInfo>() {
                        @Override
                        public MetaInfo communicate(DataInputStream input, DataOutputStream output) throws IOException {
                            output.writeInt(ProtocolConstants.MESSAGE_PING)
                            MetaInfo info = new MetaInfo()
                            info.active = input.readBoolean()
                            return info
                        }
                    })

                    if (runtimeMetaInfo != null && !runtimeMetaInfo.active) {
                        fastdexVariant.fastdexInstantRun.startTransparentActivity(true)
                    }
                } catch (Throwable e) {
                    fastdexVariant.fastdexInstantRun.startBootActivity(true)
                }
            }
        }).start()
    }
}
