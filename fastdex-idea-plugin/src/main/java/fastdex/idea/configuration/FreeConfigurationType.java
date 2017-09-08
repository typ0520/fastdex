package fastdex.idea.configuration;

import com.intellij.execution.configurations.ConfigurationTypeBase;
import fastdex.idea.icons.PluginIcons;

import javax.swing.*;

/**
 * Fastdex run configuration type
 *
 * @author act262@gmail.com
 */
public class FreeConfigurationType extends ConfigurationTypeBase {

    private final static String ID = "com.github.typ0520.fastdex.run";
    private final static String DISPLAY_NAME = "Fastdex Run";
    private final static String DESC = "Fastdex Run Configuration";
    private final static Icon ICON = PluginIcons.ICON_ACTION_RUN;

    protected FreeConfigurationType() {
        super(ID, DISPLAY_NAME, DESC, ICON);
        this.addFactory(new FreeConfigurationFactory(this));
    }
}
