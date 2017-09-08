package fastdex.idea.actions;

import fastdex.idea.icons.PluginIcons;

/**
 * Created by pengwei on 16/9/11.
 */
public class DebugRunAction extends FastdexRunAction {

    public DebugRunAction() {
        super(PluginIcons.StartDebugger);
    }

    @Override
    protected String getArgs() {
        return "-d";
    }
}
