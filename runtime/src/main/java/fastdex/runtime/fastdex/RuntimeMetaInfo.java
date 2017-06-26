package fastdex.runtime.fastdex;

import android.util.Log;
import com.google.gson.Gson;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import fastdex.common.ShareConstants;
import fastdex.common.utils.FileUtils;
import fastdex.common.utils.SerializeUtils;
import fastdex.runtime.fd.Logging;

/**
 * Created by tong on 17/4/29.
 */
public class RuntimeMetaInfo {
    /**
     * 全量编译完成的时间
     */
    private long buildMillis;

    private String variantName;

    private String lastPatchPath;

    private String patchPath;

    private String preparedPatchPath;

    public long getBuildMillis() {
        return buildMillis;
    }

    public void setBuildMillis(long buildMillis) {
        this.buildMillis = buildMillis;
    }

    public String getVariantName() {
        return variantName;
    }

    public void setVariantName(String variantName) {
        this.variantName = variantName;
    }

    public String getLastPatchPath() {
        return lastPatchPath;
    }

    public void setLastPatchPath(String lastPatchPath) {
        this.lastPatchPath = lastPatchPath;
    }

    public String getPatchPath() {
        return patchPath;
    }

    public void setPatchPath(String patchPath) {
        this.patchPath = patchPath;
    }

    public String getPreparedPatchPath() {
        return preparedPatchPath;
    }

    public void setPreparedPatchPath(String preparedPatchPath) {
        this.preparedPatchPath = preparedPatchPath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RuntimeMetaInfo metaInfo = (RuntimeMetaInfo) o;

        if (buildMillis != metaInfo.buildMillis) return false;
        return variantName != null ? variantName.equals(metaInfo.variantName) : metaInfo.variantName == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (buildMillis ^ (buildMillis >>> 32));
        result = 31 * result + (variantName != null ? variantName.hashCode() : 0);
        return result;
    }

    public void save(Fastdex fastdex) {
        File metaInfoFile = new File(fastdex.fastdexDirectory, ShareConstants.META_INFO_FILENAME);
        try {
            SerializeUtils.serializeTo(metaInfoFile,this);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(Logging.LOG_TAG,e.getMessage());
        }
    }

    public static RuntimeMetaInfo load(Fastdex fastdex) {
        File metaInfoFile = new File(fastdex.fastdexDirectory, ShareConstants.META_INFO_FILENAME);
        try {
            return new Gson().fromJson(new String(FileUtils.readContents(metaInfoFile)),RuntimeMetaInfo.class);
        } catch (Throwable e) {
            Log.e(Logging.LOG_TAG,e.getMessage());
        }

        return null;
    }

    public static RuntimeMetaInfo load(InputStream is) {
        try {
            return new Gson().fromJson(new String(FileUtils.readStream(is)),RuntimeMetaInfo.class);
        } catch (Throwable e) {
            Log.e(Logging.LOG_TAG,e.getMessage());
        }

        return null;
    }

    public static RuntimeMetaInfo load(String json) {
        try {
            return new Gson().fromJson(json,RuntimeMetaInfo.class);
        } catch (Throwable e) {
            Log.e(Logging.LOG_TAG,e.getMessage());
        }

        return null;
    }
}
