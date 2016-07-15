package com.example.fertilizercrm.component;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.example.fertilizercrm.basic.DataManager;
import com.example.fertilizercrm.http.Params;
import com.example.fertilizercrm.http.Req;
import com.example.fertilizercrm.role.Role;
import org.apache.http.Header;
import com.example.fertilizercrm.common.httpclient.AsyncHttpResponseHandler;
import com.example.fertilizercrm.common.utils.Logger;

/**
 * 业务员位置信息上送服务
 */
public class LocationPushService extends Service {
    /**
     * 每隔多少秒上传一次信息
     */
    private static final int INTERVAL = 30;
    // 定位相关
    private LocationClient mLocClient;
    private LatLng lastUploadLatlng;

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.d("LocationPushService: onCreate");

        // 定位初始化
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(new MyLocationListenner());
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);// 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(INTERVAL * 1000);
        option.setIsNeedAddress(true);
        mLocClient.setLocOption(option);
        mLocClient.start();
        mLocClient.requestLocation();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        mLocClient.start();
    }

    /**
     * 定位SDK监听函数
     */
    public class MyLocationListenner implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location == null)
                return;
            final LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
            if (lastUploadLatlng != null
                    && DistanceUtil.getDistance(lastUploadLatlng,ll) < 50.0f) {
                return;
            }
            DataManager.getInstance().setCurrentLatLng(ll);
            Params params = new Params();
            params.put("lat", ll.latitude);
            params.put("lon", ll.longitude);
            params.put("sign", "position");
            params.put("address", location.getAddrStr());
            new Req().url(Req.locationDeal)
                    .params(params)
                    .get(new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            lastUploadLatlng = ll;
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                        }
                    });

            Logger.e("<<LocationPushService LatLng: " + ll.toString());
        }

        public void onReceivePoi(BDLocation poiLocation) {
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLocClient.stop();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 启动位置上传service
     * @param context
     */
    public static void start(Context context) {
        if (Role.currentRole().isSalesman()) {
            Intent intent = new Intent(context,LocationPushService.class);
            context.startService(intent);
        }
    }

    /**
     * 终止service
     * @param context
     */
    public static void stop(Context context) {
        if (Role.currentRole().isSalesman()) {
            Intent intent = new Intent(context, LocationPushService.class);
            context.stopService(intent);
        }
    }
}
