package fastdex.runtime;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import dalvik.system.PathClassLoader;
import fastdex.common.ShareConstants;
import fastdex.common.utils.FileUtils;
import fastdex.runtime.loader.MonkeyPatcher;
import fastdex.runtime.fd.Restarter;
import fastdex.runtime.fd.Server;
import fastdex.runtime.loader.SystemClassLoaderAdder;
import fastdex.runtime.loader.ResourcePatcher;
import fastdex.runtime.utils.ShareFileLockHelper;
import fastdex.runtime.utils.SharePatchFileUtil;
import fastdex.runtime.utils.Utils;
import static fastdex.runtime.fd.Logging.LOG_TAG;

/**
 * Created by tong on 17/4/29.
 */
public enum  Fastdex {
    INSTANCE;

    private Context applicationContext;

    private File fastdexDirectory;
    private File patchDirectory;
    private File tempDirectory;

    public static Fastdex get() {
        return INSTANCE;
    }

    public Context getApplicationContext() {
        return applicationContext;
    }

    public void initialize(FastdexApplication fastdexApplication, Application realApplication) {
        this.applicationContext = fastdexApplication.getApplicationContext();
        fastdexDirectory = SharePatchFileUtil.getFastdexDirectory(applicationContext);
        patchDirectory = SharePatchFileUtil.getPatchDirectory(applicationContext);
        tempDirectory = SharePatchFileUtil.getPatchTempDirectory(applicationContext);

        if (Utils.isMainProcess(applicationContext)) {
            RuntimeMetaInfo metaInfo = loadRuntimeMetaInfo();
            MonkeyPatcher.monkeyPatchApplication(applicationContext,fastdexApplication,realApplication);

            preparePatch(metaInfo);
            loadPatch(fastdexApplication,metaInfo);

            Restarter.initialize(fastdexApplication);
            Thread.setDefaultUncaughtExceptionHandler(new FastdexUncaughtExceptionHandler(getApplicationContext()));
            registerFastdexReceiver(fastdexApplication);
            fastdexApplication.startService(new Intent(fastdexApplication, FastdexService.class));
        }
    }

    private void registerFastdexReceiver(FastdexApplication fastdexApplication) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(FastdexReceiver.FASTDEX_RECEIVER_ACTION);
        intentFilter.addCategory(fastdexApplication.getPackageName());
        fastdexApplication.registerReceiver(new FastdexReceiver(),intentFilter);
    }

    /**
     * 加载meta-info
     * @return
     */
    private RuntimeMetaInfo loadRuntimeMetaInfo() {
        try {
            RuntimeMetaInfo metaInfo = readRuntimeMetaInfoFromFile();

            RuntimeMetaInfo assetsMetaInfo = null;
            long lastSourceModified = SharePatchFileUtil.getLastSourceModified(applicationContext);
            Log.d(LOG_TAG,"lastSourceModified: " + lastSourceModified);

            InputStream is = applicationContext.getAssets().open(ShareConstants.META_INFO_FILENAME);
            String assetsMetaInfoJson = new String(FileUtils.readStream(is));
            Log.d(LOG_TAG,"load meta-info from assets: \n" + assetsMetaInfoJson);
            assetsMetaInfo = RuntimeMetaInfo.load(assetsMetaInfoJson);
            if (assetsMetaInfo == null) {
                throw new NullPointerException("AssetsMetaInfo can not be null!!!");
            }

            boolean metaInfoChanged = false;
            boolean sourceChanged = false;

            if (metaInfo == null) {
                FileUtils.cleanDir(patchDirectory);

                assetsMetaInfo.setLastSourceModified(lastSourceModified);
                Fastdex.get().saveRuntimeMetaInfo(assetsMetaInfo);
                metaInfo = assetsMetaInfo;
            }
            else if (!(metaInfoChanged = metaInfo.equals(assetsMetaInfo)) || (sourceChanged = metaInfo.getLastSourceModified() != lastSourceModified)) {
                if (metaInfoChanged && sourceChanged) {
                    Log.d(LOG_TAG,"\nmeta-info and source changed, clean patch info\n");
                }
                else if (metaInfoChanged) {
                    Log.d(LOG_TAG,"\nmeta-info changed, clean patch info\n");
                }
                else if (sourceChanged) {
                    Log.d(LOG_TAG,"\nmeta-info changed, clean patch info\n");
                }

                FileUtils.cleanDir(patchDirectory);
                FileUtils.cleanDir(tempDirectory);

                assetsMetaInfo.setLastSourceModified(lastSourceModified);
                Fastdex.get().saveRuntimeMetaInfo(assetsMetaInfo);
                metaInfo = assetsMetaInfo;
            }

            return metaInfo;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 加载补丁
     * @param fastdexApplication
     * @param metaInfo
     */
    private void loadPatch(FastdexApplication fastdexApplication,RuntimeMetaInfo metaInfo) {
        if (metaInfo != null && !TextUtils.isEmpty(metaInfo.getPatchPath())) {
            final File dexDirectory = new File(new File(metaInfo.getPatchPath()),Constants.DEX_DIR);
            final File optDirectory = new File(new File(metaInfo.getPatchPath()),Constants.OPT_DIR);
            final File resourceDirectory = new File(new File(metaInfo.getPatchPath()),Constants.RES_DIR);
            FileUtils.ensumeDir(optDirectory);
            File resourceApkFile = new File(resourceDirectory,Constants.RESOURCE_APK_FILE_NAME);
            if (FileUtils.isLegalFile(resourceApkFile)) {
                Log.d(LOG_TAG,"apply res patch: " + resourceApkFile);
                try {
                    ResourcePatcher.monkeyPatchExistingResources(applicationContext,resourceApkFile.getAbsolutePath());
                } catch (Throwable throwable) {
                    throw new RuntimeException(throwable);
                }
            }

            File mergedPatchDex = new File(dexDirectory,ShareConstants.MERGED_PATCH_DEX);
            File patchDex = new File(dexDirectory,ShareConstants.PATCH_DEX);

            ArrayList<File> dexList = new ArrayList<>();
            if (FileUtils.isLegalFile(patchDex)) {
                dexList.add(patchDex);
            }
            if (FileUtils.isLegalFile(mergedPatchDex)) {
                dexList.add(mergedPatchDex);
            }

            if (!dexList.isEmpty()) {
                PathClassLoader classLoader = (PathClassLoader) Fastdex.class.getClassLoader();
                try {
                    Log.d(LOG_TAG,"apply dex patch: " + dexList);
                    SystemClassLoaderAdder.installDexes(fastdexApplication,classLoader,optDirectory,dexList);
                } catch (Throwable throwable) {
                    throw new RuntimeException(throwable);
                }
            }
        }
    }

    /**
     * 准备补丁
     * @param metaInfo
     */
    private void preparePatch(RuntimeMetaInfo metaInfo) {
        if (metaInfo != null && !TextUtils.isEmpty(metaInfo.getPreparedPatchPath())) {
            if (!TextUtils.isEmpty(metaInfo.getLastPatchPath())) {
                FileUtils.deleteDir(new File(metaInfo.getLastPatchPath()));
            }
            File preparedPatchDir = new File(metaInfo.getPreparedPatchPath());
            File patchDir = patchDirectory;

            FileUtils.deleteDir(patchDir);
            if (!preparedPatchDir.renameTo(patchDir)) {
                //目录移动失败
                throw new RuntimeException("move path fail. from: " + preparedPatchDir.getAbsolutePath() + " ,into: " + patchDir.getAbsolutePath());
            }

            trimPreparedPatchDir(metaInfo,patchDir);
            metaInfo.setLastPatchPath(metaInfo.getPatchPath());
            metaInfo.setPreparedPatchPath(null);
            metaInfo.setPatchPath(patchDir.getAbsolutePath());
            Fastdex.get().saveRuntimeMetaInfo(metaInfo);

            Log.d(LOG_TAG,"apply new patch: " + metaInfo.toJson());
        }
    }

    /**
     * 从文件名中提取版本号并移动文件
     * @param runtimeMetaInfo
     * @param patchDir
     */
    private void trimPreparedPatchDir(RuntimeMetaInfo runtimeMetaInfo, File patchDir) {
        //把文件名中的资源版本号提取出来
        final File dexDirectory = new File(patchDir,Constants.DEX_DIR);
        final File resourceDirectory = new File(patchDir,Constants.RES_DIR);

        File[] resFiles = resourceDirectory.listFiles();
        if (resFiles != null) {
            for (File f : resFiles) {
                if (Server.isResourcePath(f.getName())) {
                    int version = parseVersionFromPath(f.getName());
                    String path = parseNameFromPath(f.getName());

                    File target = new File(f.getParentFile(),path);
                    if (!f.renameTo(target)) {
                        //处理移动失败
                        throw new RuntimeException("move path fail. from: " + f.getAbsolutePath() + " ,into: " + target.getAbsolutePath());
                    }

                    Log.d(LOG_TAG,"file: " + f + " renameTo: " + target);
                    if (path.equals(Constants.RESOURCE_APK_FILE_NAME)) {
                        runtimeMetaInfo.setResourcesVersion(version);
                    }
                    //只会有一个resources.apk
                    break;
                }
            }
        }

        File[] dexFiles = dexDirectory.listFiles();
        if (dexFiles != null) {
            for (File f : dexFiles) {
                if (Server.isDexPath(f.getName())) {
                    int version = parseVersionFromPath(f.getName());
                    String path = parseNameFromPath(f.getName());

                    File target = new File(f.getParentFile(),path);
                    if (!f.renameTo(target)) {
                        //处理移动失败
                        throw new RuntimeException("move path fail. from: " + f.getAbsolutePath() + " ,into: " + target.getAbsolutePath());
                    }

                    Log.d(LOG_TAG,"file: " + f + " renameTo: " + target);
                    if (path.equals(Constants.MERGED_PATCH_DEX)) {
                        runtimeMetaInfo.setMergedDexVersion(version);
                    }
                    else if (path.equals(Constants.PATCH_DEX)) {
                        runtimeMetaInfo.setPatchDexVersion(version);
                    }
                }
            }
        }
    }

    public File getFastdexDirectory() {
        return fastdexDirectory;
    }

    public File getTempDirectory() {
        return tempDirectory;
    }

    public File getPatchDirectory() {
        return patchDirectory;
    }

    public File getMetaInfoFile() {
        File metaInfoFile = new File(getFastdexDirectory(), ShareConstants.META_INFO_FILENAME);
        return metaInfoFile;
    }

    public File getMetaInfoLockFile() {
        File metaInfoLockFile = new File(getFastdexDirectory(), ShareConstants.META_INFO_FILENAME + ".lock");
        return metaInfoLockFile;
    }

    public RuntimeMetaInfo readRuntimeMetaInfoFromFile() {
        return readRuntimeMetaInfoFromFile(true);
    }

    /**
     * 读取meta-info信息
     * @return
     */
    public RuntimeMetaInfo readRuntimeMetaInfoFromFile(boolean printLog) {
        File lockFile = getMetaInfoLockFile();
        RuntimeMetaInfo metaInfo = null;
        File metaInfoFile = Fastdex.get().getMetaInfoFile();
        if (!FileUtils.isLegalFile(metaInfoFile)) {
            return null;
        }
        File lockParentFile = lockFile.getParentFile();
        if (!lockParentFile.exists()) {
            lockParentFile.mkdirs();
        }
        ShareFileLockHelper fileLock = null;
        try {
            fileLock = ShareFileLockHelper.getFileLock(lockFile);
            String assetsMetaInfoJson = new String(FileUtils.readContents(metaInfoFile));
            if (printLog) {
                Log.d(LOG_TAG,"load meta-info from assets: \n" + assetsMetaInfoJson);
            }
            metaInfo = RuntimeMetaInfo.load(assetsMetaInfoJson);
        } catch (IOException e) {
            throw new RuntimeException("readAndCheckPropertyWithLock fail", e);
        } finally {
            try {
                if (fileLock != null) {
                    fileLock.close();
                }
            } catch (IOException e) {
                Log.w(LOG_TAG, "releaseInfoLock error", e);
            }
        }
        return metaInfo;
    }

    public void saveRuntimeMetaInfo(final RuntimeMetaInfo runtimeMetaInfo) {
        saveRuntimeMetaInfo(runtimeMetaInfo,true);
    }

    /**
     * 保存meta-info信息
     * @param runtimeMetaInfo
     */
    public void saveRuntimeMetaInfo(final RuntimeMetaInfo runtimeMetaInfo,boolean printLog) {
        File lockFile = getMetaInfoLockFile();
        File lockParentFile = lockFile.getParentFile();
        if (!lockParentFile.exists()) {
            lockParentFile.mkdirs();
        }
        ShareFileLockHelper fileLock = null;
        try {
            fileLock = ShareFileLockHelper.getFileLock(lockFile);
            runtimeMetaInfo.save(Fastdex.this, printLog);
        } catch (Exception e) {
            throw new RuntimeException("readAndCheckPropertyWithLock fail", e);
        } finally {
            try {
                if (fileLock != null) {
                    fileLock.close();
                }
            } catch (IOException e) {
                Log.w(LOG_TAG, "releaseInfoLock error", e);
            }
        }
    }

    /**
     * (资源版本__资源名字)
     * 分割版本号和资源名
     * @param path
     * @return
     */
    public static String[] splitPatchPath(String path) {
        return path.split(Constants.RES_SPLIT_STR);
    }

    /**
     * 从路径中解析补丁文件版本
     * @param path
     * @return
     */
    public static int parseVersionFromPath(String path) {
        String[] infoArr = Fastdex.splitPatchPath(path);
        int version = Integer.parseInt(infoArr[0]);
        return version;
    }

    /**
     * 从路径中解析补丁文件名字
     * @param path
     * @return
     */
    public static String parseNameFromPath(String path) {
        String[] infoArr = Fastdex.splitPatchPath(path);
        String name = infoArr[1];
        return name;
    }
}
