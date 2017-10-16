package fastdex.runtime;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import fastdex.runtime.fd.Restarter;

/**
 * Created by tong on 17/10/14.
 */
public class FastdexReceiver extends BroadcastReceiver {
    public  static final String FASTDEX_RECEIVER_ACTION   = "android.intent.action.FastdexReceiver";

    private static final String EXTAR_CMD                 = "EXTAR_CMD";
    private static final String EXTAR_TEXT                = "EXTAR_TEXT";
    private static final String EXTAR_UPDATE_MODE         = "EXTAR_UPDATE_MODE";
    private static final String EXTAR_HASDEX              = "EXTAR_HASDEX";
    private static final String EXTAR_HAS_RESOURCES       = "EXTAR_HAS_RESOURCES";
    private static final String EXTAR_PREPARED_PATCH_PATH = "EXTAR_PREPARED_PATCH_PATH";
    private static final String EXTAR_RESOURCES_APKP_ATH  = "EXTAR_RESOURCES_APKP_ATH";
    private static final String EXTAR_TOAST               = "EXTAR_TOAST";
    private static final String EXTAR_RESTART_APP_BY_CMD  = "EXTAR_RESTART_APP_BY_CMD";

    private static final int CMD_SHOW_TOAST   = 1;
    private static final int CMD_SHOW_RESTART = 2;

    @Override
    public void onReceive(Context context, Intent intent) {
        int cmd = intent.getIntExtra(EXTAR_CMD,0);
        if (cmd == CMD_SHOW_TOAST) {
            String text = intent.getStringExtra(EXTAR_TEXT);
            if (!TextUtils.isEmpty(text)) {
                Restarter.showToast(text);
            }
        }
        else if (cmd == CMD_SHOW_RESTART) {
            int updateMode = intent.getIntExtra(EXTAR_UPDATE_MODE,0);
            boolean hasDex = intent.getBooleanExtra(EXTAR_HASDEX,false);
            boolean hasResources = intent.getBooleanExtra(EXTAR_HAS_RESOURCES,false);
            String preparedPatchPath = intent.getStringExtra(EXTAR_PREPARED_PATCH_PATH);
            String resourcesApkPath = intent.getStringExtra(EXTAR_RESOURCES_APKP_ATH);
            boolean toast = intent.getBooleanExtra(EXTAR_TOAST,false);
            boolean restartAppByCmd = intent.getBooleanExtra(EXTAR_RESTART_APP_BY_CMD,false);
            Restarter.restart(updateMode,hasDex,hasResources,preparedPatchPath,resourcesApkPath,toast,restartAppByCmd);
        }
    }

    public static void showToast(String text) {
        Intent intent = new Intent();
        intent.setAction(FASTDEX_RECEIVER_ACTION);
        intent.addCategory(Fastdex.get().getApplicationContext().getPackageName());
        intent.putExtra(EXTAR_CMD,CMD_SHOW_TOAST);
        intent.putExtra(EXTAR_TEXT,text);
        Fastdex.get().getApplicationContext().sendBroadcast(intent);
    }

    public static void restart(int updateMode, boolean hasDex, boolean hasResources, String preparedPatchPath, String resourcesApkPath, boolean toast, boolean restartAppByCmd) {
        Intent intent = new Intent();
        intent.setAction(FASTDEX_RECEIVER_ACTION);
        intent.addCategory(Fastdex.get().getApplicationContext().getPackageName());
        intent.putExtra(EXTAR_CMD,CMD_SHOW_RESTART);
        intent.putExtra(EXTAR_UPDATE_MODE,updateMode);
        intent.putExtra(EXTAR_HASDEX,hasDex);
        intent.putExtra(EXTAR_HAS_RESOURCES,hasResources);
        intent.putExtra(EXTAR_PREPARED_PATCH_PATH,preparedPatchPath);
        intent.putExtra(EXTAR_RESOURCES_APKP_ATH,resourcesApkPath);
        intent.putExtra(EXTAR_TOAST,toast);
        intent.putExtra(EXTAR_RESTART_APP_BY_CMD,restartAppByCmd);
        Fastdex.get().getApplicationContext().sendBroadcast(intent);
    }
}