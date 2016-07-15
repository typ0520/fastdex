package com.example.fertilizercrm.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.example.fertilizercrm.common.utils.Logger;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.example.fertilizercrm.R;
import com.example.fertilizercrm.basic.BaseActivity;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.example.fertilizercrm.basic.DataManager;
import com.example.fertilizercrm.bean.SignInfo;
import com.example.fertilizercrm.http.Params;
import com.example.fertilizercrm.http.Req;
import org.apache.http.Header;
import org.json.JSONObject;

/**
 * 同步位置或者签到
 */
public class LocationSyncActivity extends BaseActivity {
    /**
     * 签到
     */
    public static final int OPEN_TYPE_SIGN = 1;

    @Bind(R.id.map_view) MapView mMapView;
    // 定位相关
    LocationClient mLocClient;
    public MyLocationListenner myListener = new MyLocationListenner();
    BaiduMap mBaiduMap;
    boolean isFirstLoc = true;// 是否首次定位
    BitmapDescriptor mCurrentMarker;
    View mInfoWindowView;
    TextView titleView;
    TextView contentView;
    private LatLng mLatLng;
    private String addressStr;
    private SignInfo signInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_sync);
        signInfo = (SignInfo) getIntent().getSerializableExtra(KEY_DATA);
        ButterKnife.bind(this);
        if (openType == OPEN_TYPE_DEFAULT) {
            getTitleView().setRightText("同步");
        }
        else if (openType == OPEN_TYPE_SIGN) {
            getTitleView().setTitle("业务员签到");
            getTitleView().setRightText("签到");
        }
        mCurrentMarker = BitmapDescriptorFactory.fromResource(R.drawable.cur_location_marker);

        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(15.0f));
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(false);

        if (signInfo == null) {
            // 定位初始化
            mLocClient = new LocationClient(this);
            mLocClient.registerLocationListener(myListener);
            LocationClientOption option = new LocationClientOption();
            option.setOpenGps(true);// 打开gps
            option.setCoorType("bd09ll"); // 设置坐标类型
            option.setScanSpan(1000);
            option.setIsNeedAddress(true);
            mLocClient.setLocOption(option);
            mLocClient.start();
            mLocClient.requestLocation();

            getTitleView().setRightClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mLatLng == null) {
                        showLongToast("正在定位");
                        return;
                    }
                    LatLng latLng = DataManager.getInstance().getCurrentLatLng();
                    Params params = new Params();
                    params.put("lat", latLng.latitude);
                    params.put("lon", latLng.longitude);
                    params.put("address", addressStr);
                    if (openType == OPEN_TYPE_DEFAULT) {
                        //同步位置
                        params.put("sign", "update");
                    } else if (openType == OPEN_TYPE_SIGN) {
                        //签到
                        params.put("sign", "sign");
                    }
                    new Req().url(Req.locationDeal)
                            .params(params)
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
        else {
            getTitleView().setRightText("");

            double latitude = 0.d;
            double longtitude = 0.d;
            try {
                latitude = Double.valueOf(signInfo.getLatitude());
                longtitude = Double.valueOf(signInfo.getLongitude());
            } catch (Throwable e) {

            }
            LatLng ll = new LatLng(latitude, longtitude);
            MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
            mBaiduMap.animateMapStatus(u);

            OverlayOptions ooA = new MarkerOptions().position(ll).icon(mCurrentMarker).zIndex(9).draggable(true);
            mBaiduMap.addOverlay(ooA);

            BDLocation location = new BDLocation();
            location.setAddrStr(signInfo.getAddress());
            InfoWindow infoWindow = initInfoWindow(location,ll);
            mBaiduMap.showInfoWindow(infoWindow);
        }
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
                OverlayOptions ooA = new MarkerOptions().position(ll).icon(mCurrentMarker).zIndex(9).draggable(true);
                mBaiduMap.addOverlay(ooA);
            }
            DataManager.getInstance().setCurrentLatLng(ll);
            mLatLng = ll;

            if (TextUtils.isEmpty(location.getProvince())
                    || TextUtils.isEmpty(location.getCity())
                    || TextUtils.isEmpty(location.getAddrStr())) {
                return;
            }
            MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
            mBaiduMap.animateMapStatus(u);

            OverlayOptions ooA = new MarkerOptions().position(ll).icon(mCurrentMarker).zIndex(9).draggable(true);
            mBaiduMap.addOverlay(ooA);

            InfoWindow infoWindow = initInfoWindow(location,ll);
            mBaiduMap.showInfoWindow(infoWindow);
            LocationSyncActivity.this.addressStr = location.getAddrStr();
            Logger.e("<< location: province: " + location.getProvince() + " city: " + location.getCity() + " addr: " + location.getAddrStr());
        }

        public void onReceivePoi(BDLocation poiLocation) {
        }
    }

    private InfoWindow initInfoWindow(BDLocation location,LatLng ll) {
        if (mInfoWindowView == null) {
            mInfoWindowView = View.inflate(this,R.layout.view_cur_location,null);
            titleView = (TextView) mInfoWindowView.findViewById(R.id.tv_title);
            contentView = (TextView) mInfoWindowView.findViewById(R.id.tv_content);
        }

        if (signInfo == null) {
            if (location.getProvince().equals(location.getCity())) {
                titleView.setText(location.getProvince() + location.getDistrict());
            }
            else {
                titleView.setText(location.getProvince() + location.getCity());
            }
            contentView.setText(location.getAddrStr());
        }
        else {
            titleView.setText(signInfo.getProvince() + signInfo.getCity());
            contentView.setText(signInfo.getAddress());
        }
        InfoWindow infoWindow = new InfoWindow(BitmapDescriptorFactory.fromView(mInfoWindowView), ll, -60, null);
        return infoWindow;
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
        if (mLocClient != null) {
            mLocClient.stop();
        }
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        super.onDestroy();
    }
}
