package fastdex.runtime;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import fastdex.common.ShareConstants;
import fastdex.common.utils.FileUtils;
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

    private long lastSourceModified;

    private int mergedDexVersion;

    private int patchDexVersion;

    private int resourcesVersion;

    private boolean active = true;

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

    public long getLastSourceModified() {
        return lastSourceModified;
    }

    public void setLastSourceModified(long lastSourceModified) {
        this.lastSourceModified = lastSourceModified;
    }

    public int getMergedDexVersion() {
        return mergedDexVersion;
    }

    public void setMergedDexVersion(int mergedDexVersion) {
        this.mergedDexVersion = mergedDexVersion;
    }

    public int getPatchDexVersion() {
        return patchDexVersion;
    }

    public void setPatchDexVersion(int patchDexVersion) {
        this.patchDexVersion = patchDexVersion;
    }

    public int getResourcesVersion() {
        return resourcesVersion;
    }

    public void setResourcesVersion(int resourcesVersion) {
        this.resourcesVersion = resourcesVersion;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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

    public void save(Fastdex fastdex) throws IOException {

    }

    public void save(Fastdex fastdex,boolean printLog) throws IOException {
        File metaInfoFile = Fastdex.get().getMetaInfoFile();
        JSONObject jObj = toJson();
        if (printLog) {
            Log.d(Logging.LOG_TAG,"save meta info: \n" + jObj.toString());
        }
        FileOutputStream outputStream = null;
        FileUtils.ensumeDir(metaInfoFile.getParentFile());
        try {
            outputStream = new FileOutputStream(metaInfoFile);
            outputStream.write(jObj.toString().getBytes());
            outputStream.flush();
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }

    public JSONObject toJson() {
        JSONObject jObj = new JSONObject();
        try {
            jObj.put("buildMillis",buildMillis);
            jObj.put("variantName",variantName);
            jObj.put("lastPatchPath",lastPatchPath);
            jObj.put("patchPath",patchPath);
            jObj.put("preparedPatchPath",preparedPatchPath);
            jObj.put("lastSourceModified",lastSourceModified);

            jObj.put("mergedDexVersion",mergedDexVersion);
            jObj.put("patchDexVersion",patchDexVersion);
            jObj.put("resourcesVersion",resourcesVersion);
            jObj.put("active",active);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jObj;
    }

    public static RuntimeMetaInfo load(String json) {
        try {
            return RuntimeMetaInfo.parse(json);
        } catch (Throwable e) {
            Log.e(Logging.LOG_TAG,e.getMessage());
        }

        return null;
    }

    private static RuntimeMetaInfo parse(String json) {
        try {
            JSONObject jObj = new JSONObject(json);
            RuntimeMetaInfo metaInfo = new RuntimeMetaInfo();

            metaInfo.buildMillis = jObj.optLong("buildMillis");
            metaInfo.variantName = jObj.optString("variantName");
            metaInfo.lastPatchPath = jObj.optString("lastPatchPath");
            metaInfo.patchPath = jObj.optString("patchPath");
            metaInfo.preparedPatchPath = jObj.optString("preparedPatchPath");
            metaInfo.lastSourceModified = jObj.optLong("lastSourceModified");

            metaInfo.mergedDexVersion = jObj.optInt("mergedDexVersion");
            metaInfo.patchDexVersion = jObj.optInt("patchDexVersion");
            metaInfo.resourcesVersion = jObj.optInt("resourcesVersion");

            metaInfo.active = jObj.optBoolean("active",true);
            return metaInfo;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
