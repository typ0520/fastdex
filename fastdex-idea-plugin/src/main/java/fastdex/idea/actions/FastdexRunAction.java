package fastdex.idea.actions;

import com.android.ddmlib.IDevice;
import com.android.tools.idea.run.DeviceChooser;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import fastdex.idea.icons.PluginIcons;
import fastdex.idea.models.FastdexStatus;
import fastdex.idea.utils.FastdexUtil;
import fastdex.idea.utils.NotificationUtils;
import fastdex.idea.utils.Utils;
import fastdex.idea.views.DeviceChooserDialog;
import fastdex.idea.views.FastdexTerminal;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.android.sdk.AndroidPlatform;
import org.jetbrains.android.sdk.AndroidSdkUtils;
import javax.swing.*;
import java.lang.reflect.Method;
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
        run(currentProject);
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

    public static void run(Project currentProject) {
        FastdexStatus status = FastdexUtil.checkInstall(currentProject);
        if (status != null) {

            ArrayList<String> shell = status.getFastdexRunShell(currentProject);
            if (shell == null) {
                NotificationUtils.gradleWrapperNotFound();
            }
            else {
                boolean supportMultipleDevices = status.isSupportMultipleDevices();
                if (supportMultipleDevices) {
                    if (!Utils.hasInitAndroidDebugBridge()) {
                        AndroidSdkUtils.getDebugBridge(currentProject);
                    }

                    AndroidFacet myFacet = AndroidFacet.getInstance(status.getSelectedModule());
                    String value = PropertiesComponent.getInstance(currentProject).getValue(ANDROID_TARGET_DEVICES_PROPERTY);
                    String[] selectedSerials = value != null ? fromString(value) : null;
                    AndroidPlatform platform = myFacet.getConfiguration().getAndroidPlatform();

                    DeviceChooserDialog chooser = new DeviceChooserDialog(myFacet, platform.getTarget(), true, selectedSerials, null);
                    IDevice[] devices = null;
                    try {
                        Method method = DeviceChooser.class.getDeclaredMethod("getFilteredDevices");
                        method.setAccessible(true);
                        devices = (IDevice[]) method.invoke(chooser.getMyDeviceChooser());
                    } catch (Throwable e) {
                        e.printStackTrace();
                        NotificationUtils.errorMsgDialog(e.getMessage());
                    }
                    if (devices == null || devices.length != 1) {
                        chooser.show();
                    }
                    devices = chooser.getSelectedDevices();
                    if (devices == null || devices.length == 0) {
                        return;
                    }
                    shell.add("-PDEVICE_SN=" + devices[0]);
                }

                if (status.hasOneAppplicationModule()) {
                    FastdexTerminal.getInstance(currentProject).initAndExecute(currentProject.getBasePath(),shell);
                }
                else {
                    FastdexTerminal.getInstance(currentProject).initAndExecute(status.getSelectedModule().getModuleFile().getParent().getPath(),shell);
                }
            }
        }
    }
}
