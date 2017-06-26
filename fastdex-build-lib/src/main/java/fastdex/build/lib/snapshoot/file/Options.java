package fastdex.build.lib.snapshoot.file;

import fastdex.build.lib.snapshoot.api.Status;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by tong on 17/3/30.
 */
public class Options {
    private final Set<String> suffixList = new HashSet<>();
    private Status[] focusStatus = null;

    public static class Builder {
        private final Options options = new Options();

        public Builder addSuffix(String suffix) {
            options.suffixList.add(suffix);
            return this;
        }

        public Builder focusStatus(Status ...focusStatus) {
            if (focusStatus != null) {
                Set set = new HashSet();
                for (Status status : focusStatus) {
                    set.add(status);
                }
                if (set.size() < focusStatus.length) {
                    throw new IllegalStateException("Content can not be repeated !");
                }
            }

            if (focusStatus.length == 0) {
                options.focusStatus = null;
            }
            else {
                options.focusStatus = focusStatus;
            }
            return this;
        }

        public Options build() {
            return options;
        }
    }
}
