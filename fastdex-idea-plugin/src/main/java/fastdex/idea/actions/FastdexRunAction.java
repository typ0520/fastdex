package fastdex.idea.actions;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.android.tools.idea.run.AndroidRunConfiguration;
import com.android.tools.idea.run.DeviceChooser;
import com.android.tools.idea.run.DeviceChooserDialog;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import fastdex.idea.icons.PluginIcons;
import fastdex.idea.models.FastdexStatus;
import fastdex.idea.utils.FastdexUtil;
import fastdex.idea.utils.NotificationUtils;
import fastdex.idea.views.FastdexTerminal;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.android.sdk.AndroidPlatform;

import javax.swing.*;
import java.util.ArrayList;

/**
 * Created by pengwei on 16/9/11.
 */
public class FastdexRunAction extends BaseAction {
    private static final String ANDROID_TARGET_DEVICES_PROPERTY = "AndroidTargetDevices";


    public FastdexRunAction() {
        super(PluginIcons.FastdexIcon);
    }

    public FastdexRunAction(Icon icon) {
        super(icon);
    }

    @Override
    public void actionPerformed() {
//        AndroidDebugBridge bridge = AndroidDebugBridge.getBridge();
//        if (bridge == null || !bridge.isConnected()) {
//            \
//        }
//
//        final RunnerAndConfigurationSettings selectedConfiguration = RunManager.getInstance(currentProject).getSelectedConfiguration();
//        AndroidRunConfiguration runConfiguration = (AndroidRunConfiguration) selectedConfiguration.getConfiguration();
//
//        AndroidFacet myFacet = AndroidFacet.getInstance(runConfiguration.getConfigurationModule().getModule());
//        final Project project = myFacet.getModule().getProject();
//        String value = PropertiesComponent.getInstance(project).getValue(ANDROID_TARGET_DEVICES_PROPERTY);
//        String[] selectedSerials = value != null ? fromString(value) : null;
//        AndroidPlatform platform = myFacet.getConfiguration().getAndroidPlatform();
//
//        DeviceChooserDialog chooser = new DeviceChooserDialog(myFacet, platform.getTarget(), true, selectedSerials, null);
//        IDevice[] devices = chooser.getSelectedDevices();
//
//        if (devices == null || devices.length != 1) {
//            chooser.show();
//            devices = chooser.getSelectedDevices();
//        }
//
////        chooser.show();
////        IDevice[] devices = chooser.getSelectedDevices();
//        NotificationUtils.infoNotification(runConfiguration.getClass().getName() + " ," + toString(devices));

        FastdexStatus status = FastdexUtil.checkInstall(currentProject);
        if (status != null) {
            ArrayList<String> shell = status.getFastdexRunShell(currentProject);
            if (shell == null) {
                NotificationUtils.gradleWrapperNotFound();
            }
            else {
                if (status.hasOneAppplicationModule()) {
                    FastdexTerminal.getInstance(currentProject).initAndExecute(currentProject.getBasePath(),shell);
                }
                else {
                    FastdexTerminal.getInstance(currentProject).initAndExecute(status.getSelectedModule().getModuleFile().getParent().getPath(),shell);
                }
            }
        }
    }

    public static String toString(IDevice[] devices) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0, n = devices.length; i < n; i++) {
            builder.append(devices[i].getSerialNumber());
            if (i < n - 1) {
                builder.append(' ');
            }
        }
        return builder.toString();
    }

    /**
     * 设置参数
     *
     * @return
     */
    protected String getArgs() {
        return null;
    }

    private static String[] fromString( String s) {
        return s.split(" ");
    }
}
