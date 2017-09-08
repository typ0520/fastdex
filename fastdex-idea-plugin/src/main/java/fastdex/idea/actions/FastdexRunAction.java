package fastdex.idea.actions;

import fastdex.idea.icons.PluginIcons;
import fastdex.idea.models.FastdexStatus;
import fastdex.idea.utils.FastdexUtil;
import fastdex.idea.utils.NotificationUtils;
import fastdex.idea.views.FastdexTerminal;
import javax.swing.*;
import java.util.ArrayList;

/**
 * Created by pengwei on 16/9/11.
 */
public class FastdexRunAction extends BaseAction {

    public FastdexRunAction() {
        super(PluginIcons.FastdexIcon);
    }

    public FastdexRunAction(Icon icon) {
        super(icon);
    }

    @Override
    public void actionPerformed() {
//        final RunnerAndConfigurationSettings selectedConfiguration = RunManager.getInstance(currentProject).getSelectedConfiguration();
//        RunConfiguration runConfiguration = selectedConfiguration.getConfiguration();
//
//        if (runConfiguration instanceof AndroidRunConfiguration) {
//            AndroidRunConfiguration androidRunConfiguration = (AndroidRunConfiguration) runConfiguration;
//            NotificationUtils.infoNotification("android: " + runConfiguration.getClass().getName() + " ," + ((AndroidRunConfiguration) runConfiguration).getConfigurationModule().getSelectedModule());
//        }
//        else {
//            NotificationUtils.infoNotification(runConfiguration.getClass().getName() + " ," + runConfiguration.getName());
//        }

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

    /**
     * 设置参数
     *
     * @return
     */
    protected String getArgs() {
        return null;
    }
}
