package com.dx168.fastdex.build.snapshoot.string;

import com.dx168.fastdex.build.snapshoot.api.DiffInfo;
import com.dx168.fastdex.build.snapshoot.api.Status;

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
