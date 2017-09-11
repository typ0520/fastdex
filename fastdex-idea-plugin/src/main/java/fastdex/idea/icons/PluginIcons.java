package fastdex.idea.icons;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.IconLoader;
import javax.swing.*;

/**
 * Created by pengwei on 16/9/15.
 */
public class PluginIcons {

    public static final Icon FastdexIcon = load("/icons/icon.png");
    public static final Icon Suspend = intellijLoad("/actions/suspend.png");
    public static final Icon GC = intellijLoad("/actions/gc.png");
    public static final Icon GradleSync = load("/icons/gradlesync.png");

    private static Icon load(String path) {
        try {
            return IconLoader.getIcon(path, PluginIcons.class);
        } catch (IllegalStateException e) {
            return null;
        }
    }

    private static Icon intellijLoad(String path) {
        return IconLoader.getIcon(path, AllIcons.class);
    }
}
