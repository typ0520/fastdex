package fastdex.idea.views;

import com.android.ddmlib.IDevice;
import com.android.sdklib.IAndroidTarget;
import com.android.tools.idea.run.DeviceChooser;
import com.android.tools.idea.run.DeviceChooserListener;
import com.google.common.base.Predicate;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.android.util.AndroidBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import javax.swing.*;

/**
 * Created by tong on 17/9/11.
 */
public class DeviceChooserDialog extends DialogWrapper {
    private final DeviceChooser myDeviceChooser;
    private DeviceChooserListener deviceChooserListener;
    private boolean closed;

    public DeviceChooserDialog(@NotNull AndroidFacet facet,
                               @NotNull IAndroidTarget projectTarget,
                               boolean multipleSelection,
                               @Nullable String[] selectedSerials,
                               @Nullable Predicate<IDevice> filter) {
        super(facet.getModule().getProject(), true);
        setTitle(AndroidBundle.message("choose.device.dialog.title"));

        getOKAction().setEnabled(false);

        myDeviceChooser = new DeviceChooser(multipleSelection, getOKAction(), facet, projectTarget, filter);
        Disposer.register(myDisposable, myDeviceChooser);
        myDeviceChooser.addListener(new DeviceChooserListener() {
            @Override
            public void selectedDevicesChanged() {
                updateOkButton();

                if (deviceChooserListener != null) {
                    deviceChooserListener.selectedDevicesChanged();
                }
            }
        });

        init();
        myDeviceChooser.init(selectedSerials);
    }

    private void updateOkButton() {
        IDevice[] devices = getSelectedDevices();
        boolean enabled = devices.length > 0;
        for (IDevice device : devices) {
            if (!device.isOnline()) {
                enabled = false;
            }
        }
        getOKAction().setEnabled(enabled);
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return myDeviceChooser.getPreferredFocusComponent();
    }

    @Override
    protected void doOKAction() {
        myDeviceChooser.finish();
        super.doOKAction();
    }

    @Override
    public void doCancelAction() {
        this.deviceChooserListener = null;

        closed = true;
        super.doCancelAction();
    }

    @Override
    protected String getDimensionServiceKey() {
        return "AndroidDeviceChooserDialog";
    }

    @Override
    protected JComponent createCenterPanel() {
        return myDeviceChooser.getPanel();
    }

    public IDevice[] getSelectedDevices() {
        return myDeviceChooser.getSelectedDevices();
    }

    public DeviceChooser getMyDeviceChooser() {
        return myDeviceChooser;
    }

    public void setDeviceChooserListener(DeviceChooserListener deviceChooserListener) {
        this.deviceChooserListener = deviceChooserListener;
    }

    public DeviceChooserListener getDeviceChooserListener() {
        return deviceChooserListener;
    }

    public boolean isClosed() {
        return closed;
    }
}