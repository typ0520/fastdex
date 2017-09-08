package fastdex.idea.models;

import com.intellij.openapi.components.*;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nullable;

/**
 * Created by huangyong on 17/2/14.
 */
@State(
        name = "FastdexConfigurationStorage",
        storages = @Storage(file = "fastdex-configuration.xml", roamingType = RoamingType.DISABLED)
)
public class FastdexConfiguration implements PersistentStateComponent<FastdexConfiguration> {
    @Nullable
    @Override
    public FastdexConfiguration getState() {
        return this;
    }

    @Override
    public void loadState(FastdexConfiguration fastdexConfiguration) {
        XmlSerializerUtil.copyBean(fastdexConfiguration, this);
    }

    public static FastdexConfiguration getInstance() {
        return ServiceManager.getService(FastdexConfiguration.class);
    }
}
