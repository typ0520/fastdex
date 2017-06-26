package fastdex.build.lib.snapshoot.string;

import fastdex.build.lib.snapshoot.api.DiffInfo;
import fastdex.build.lib.snapshoot.api.Status;

/**
 * Created by tong on 17/3/31.
 */
public class StringDiffInfo extends DiffInfo<StringNode> {
    public StringDiffInfo() {
    }

    public StringDiffInfo(Status status, StringNode now, StringNode old) {
        super(status, (now != null ? now.getUniqueKey() : old.getUniqueKey()), now, old);
    }
}
