package com.dx168.fastdex.build.task

import com.android.ddmlib.AndroidDebugBridge
import com.android.ddmlib.IDevice
import com.dx168.fastdex.build.util.FastdexRuntimeException
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

/**
 * Created by tong on 17/4/27.
 */
public class FastdexConnectDeviceWithAdbTask extends DefaultTask {
    FastdexInstantRunTask fastdexInstantRun

    FastdexConnectDeviceWithAdbTask() {
        group = 'fastdex'
    }

    @TaskAction
    void connect() {
        AndroidDebugBridge.initIfNeeded(false)
        AndroidDebugBridge bridge =
                AndroidDebugBridge.createBridge("/Users/tong/Applications/android-sdk-macosx/platform-tools/adb", false)
        waitForDevice(bridge)
        IDevice[] devices = bridge.getDevices()
        IDevice device = null

        if (devices != null && devices.length > 0) {
            device = devices[0]
        }

        if (device == null) {
            throw new FastdexRuntimeException("Device not found!!")
        }

        if (devices.length > 1) {
            throw new FastdexRuntimeException("Find multiple devices!!")
        }

        fastdexInstantRun.device = device
        project.logger.error("==fastdex device connected ${device.toString()}")
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
                throw new GradleException("Connect adb timeout!!")
            }
        }
    }
}
