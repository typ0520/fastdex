package com.example.fertilizercrm;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.baidu.mapapi.SDKInitializer;
import com.easemob.EMCallBack;
import com.example.fertilizercrm.easemob.chatuidemo.DemoHXSDKHelper;
import com.example.fertilizercrm.basic.DataManager;
import com.example.fertilizercrm.component.LocationPushService;
import com.tencent.bugly.crashreport.CrashReport;


import java.util.ArrayList;
import java.util.List;

import cn.jpush.android.api.JPushInterface;
import com.example.fertilizercrm.common.httpclient.expand.AsyncHttpClientAdapter;
import com.example.fertilizercrm.common.imageloader.cache.disc.naming.Md5FileNameGenerator;
import com.example.fertilizercrm.common.imageloader.cache.memory.impl.WeakMemoryCache;
import com.example.fertilizercrm.common.imageloader.core.ImageLoader;
import com.example.fertilizercrm.common.imageloader.core.ImageLoaderConfiguration;
import com.example.fertilizercrm.common.utils.EventEmitter;
import com.example.fertilizercrm.common.utils.Logger;
import com.example.fertilizercrm.common.utils.SharedPreferenceUtil;

/**
 * Created by tong on 15/12/7.
 */
public class FertilizerApplication extends Application {
    private static FertilizerApplication instance;

    public static FertilizerApplication getInstance() {
        return instance;
    }

    // login user name
    public final String PREF_USERNAME = "username";

    /**
     * 当前用户nickname,为了苹果推送不是userid而是昵称
     */
    public static String currentUserNick = "";
    public static DemoHXSDKHelper hxSDKHelper = new DemoHXSDKHelper();

    private EventEmitter eventEmitter;

    private List<Activity> activityList = new ArrayList<Activity>();

    public void addActivity(Activity activity) {
        activityList.add(activity);
    }

    public void removeActivity(Activity activity) {
        activityList.remove(activity);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        FertilizerApplication.instance = this;
        //SDKInitializer.initialize(this);
        SharedPreferenceUtil.init(this);
        try {
            JPushInterface.init(this);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        hxSDKHelper.onInit(this);

        initImageLoader();
        if (BuildConfig.BUILD_TYPE.equals("debug")) {
            JPushInterface.setDebugMode(true);
            AsyncHttpClientAdapter.debug = true;
            //开启日志
            Logger.setDebugOn(true);
        }
        else {
            Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread thread, Throwable throwable) {
                    throwable.printStackTrace();
                    Logger.e("<<关闭所有activity....");
                    for (Activity activity : activityList) {
                        if (activity != null) {
                            activity.finish();
                        }
                    }
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(0);
                }
            });

            CrashReport.initCrashReport(this, "900016732", false);
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    protected void initImageLoader() {
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .threadPoolSize(3)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .memoryCacheSize(1500000) // 1.5 Mb
                .denyCacheImageMultipleSizesInMemory()
                .discCacheFileNameGenerator(new Md5FileNameGenerator())
                .memoryCache(new WeakMemoryCache())
                .build();
        ImageLoader.getInstance().init(config);
    }

    public void logout() {
        try {
            Logger.d("<< logout");
            //关闭上送位置服务
            LocationPushService.stop(this);
            DataManager.getInstance().clear();
            JPushInterface.stopPush(this);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取当前登陆用户名
     *
     * @return
     */
    public String getUserName() {
        return hxSDKHelper.getHXId();
    }

    /**
     * 获取密码
     *
     * @return
     */
    public String getPassword() {
        return hxSDKHelper.getPassword();
    }

    /**
     * 设置用户名
     *
     */
    public void setUserName(String username) {
        hxSDKHelper.setHXId(username);
    }

    /**
     * 设置密码 下面的实例代码 只是demo，实际的应用中需要加password 加密后存入 preference 环信sdk
     * 内部的自动登录需要的密码，已经加密存储了
     *
     * @param pwd
     */
    public void setPassword(String pwd) {
        hxSDKHelper.setPassword(pwd);
    }

    /**
     * 退出登录,清空数据
     */
    public void logout(final boolean isGCM,final EMCallBack emCallBack) {
        // 先调用sdk logout，在清理app中自己的数据
        hxSDKHelper.logout(isGCM, emCallBack);
    }

    public EventEmitter getEventEmitter() {
        if (eventEmitter == null) {
            eventEmitter = new EventEmitter();
        }
        return eventEmitter;
    }
}
