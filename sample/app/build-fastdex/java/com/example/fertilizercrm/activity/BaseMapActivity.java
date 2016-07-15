package com.example.fertilizercrm.activity;

import android.os.Bundle;
import android.view.View;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.example.fertilizercrm.R;
import com.example.fertilizercrm.basic.BaseActivity;
import com.example.fertilizercrm.http.Req;

import org.apache.http.Header;
import org.json.JSONObject;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 地图页面
 */
public class BaseMapActivity extends BaseActivity {
    @Bind(R.id.map_view) MapView mMapView;
    // 定位相关
    protected LocationClient mLocClient;
    protected MyLocationListenner myListener = new MyLocationListenner();
    protected BaiduMap mBaiduMap;
    protected boolean isFirstLoc = true;// 是否首次定位
    protected LatLng mLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_sync);

        ButterKnife.bind(this);
        mBaiduMap = mMapView.getMap();

        mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(15.0f));
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        // 定位初始化
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);// 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);
        mLocClient.setLocOption(option);
        mLocClient.start();

        BitmapDescriptor currentMarker = BitmapDescriptorFactory.fromResource(R.drawable.cur_location_marker);
        mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, true, currentMarker));

        getTitleView().setRightText("同步").setRightClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLatLng == null) {
                    showLongToast("正在定位");
                    return;
                }
                //同步位置
                new Req().url(Req.locationDeal)
                        .addParam("sign", "update")
                        .addParam("lat",mLatLng.latitude)
                        .addParam("lon",mLatLng.longitude)
                        .get(new DefaultCallback<JSONObject>() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                showLongToast("恭喜您，操作成功!");
                                setResult(RESULT_OK);
                                finish();
                            }
                        });
            }
        });
    }

    /**
     * 定位SDK监听函数
     */
    public class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            if (location == null || mMapView == null)
                return;
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                            // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
            LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
            if (isFirstLoc) {
                isFirstLoc = false;
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
                mBaiduMap.animateMapStatus(u);
            }

            BaseMapActivity.this.mLatLng = ll;
        }

        public void onReceivePoi(BDLocation poiLocation) {
        }
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        // 退出时销毁定位
        mLocClient.stop();
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        super.onDestroy();
    }
}
