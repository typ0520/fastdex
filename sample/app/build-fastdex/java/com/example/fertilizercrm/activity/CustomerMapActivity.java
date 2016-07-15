package com.example.fertilizercrm.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.ZoomControls;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.example.fertilizercrm.R;
import com.example.fertilizercrm.basic.BaseActivity;
import com.example.fertilizercrm.basic.DataManager;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.example.fertilizercrm.common.utils.Logger;

/**
 * 我的客户地图页面
 */
public class CustomerMapActivity extends BaseActivity {
    private static JSONArray data;
    @Bind(R.id.map_view)  MapView mMapView;
    // 定位相关
    LocationClient mLocClient;
    public MyLocationListenner myListener = new MyLocationListenner();
    BaiduMap mBaiduMap;
    boolean isFirstLoc = true;// 是否首次定位
    View mInfoWindowView;

    TextView titleView;
    TextView contentView;
    private int currentFocusIndex;
    private UiSettings mUiSettings;
    private BitmapDescriptor mCurrentMarker;
    private Map<Marker,Integer> markerMap;
    private int markerHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_map);
        ButterKnife.bind(this);

        mCurrentMarker = BitmapDescriptorFactory.fromResource(R.drawable.cur_location_marker);
        markerHeight = BitmapFactory.decodeResource(getResources(), R.drawable.cur_location_marker).getHeight();
        mBaiduMap = mMapView.getMap();

        mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(15.0f));
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(false);
        mUiSettings = mBaiduMap.getUiSettings();

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

        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Integer index = markerMap.get(marker);
                if (index != null) {
                    focusPoint(index);
                    return true;
                }
                return false;
            }
        });

        //initView();

        displayLocus();
    }

    private void initView() {
        int childCount = mMapView.getChildCount();
        View zoom = null;
        for (int i = 0; i < childCount; i++) {
            View child = mMapView.getChildAt(i);
            if (child instanceof ZoomControls) {
                zoom = child;
                break;
            }
        }
        zoom.setVisibility(View.GONE);
    }

    private void displayLocus() {
        if (data == null || data.length() == 0) {
            return;
        }
        mBaiduMap.clear();
        markerMap = new HashMap<>();
        for (int i = 0;i < data.length();i++) {
            JSONObject obj = data.optJSONObject(i);
            double longitude = obj.optDouble("longitude");
            double latitude = obj.optDouble("latitude");

            LatLng latLng =  new LatLng(latitude,longitude);
            OverlayOptions ooA = new MarkerOptions().position(latLng).icon(mCurrentMarker).zIndex(10000).draggable(true);
            Marker marker = (Marker) mBaiduMap.addOverlay(ooA);
            markerMap.put(marker,i);
        }
        focusPoint(0);
    }

    private void focusPoint(int focusIndex) {
        if (data == null || data.length() == 0) {
            return;
        }
        this.currentFocusIndex = focusIndex;
        JSONObject obj = data.optJSONObject(focusIndex);
        double longitude = obj.optDouble("longitude");
        double latitude = obj.optDouble("latitude");

        LatLng latLng = new LatLng(latitude,longitude);
        Logger.d("current position: " + focusIndex + " " + latLng);
        MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(latLng);
        InfoWindow infoWindow = initInfoWindow(obj, latLng);
        mBaiduMap.hideInfoWindow();
        mBaiduMap.showInfoWindow(infoWindow);
        mBaiduMap.animateMapStatus(u);
    }

    private InfoWindow initInfoWindow(JSONObject obj,LatLng ll) {
        if (mInfoWindowView == null) {
            mInfoWindowView = View.inflate(this,R.layout.view_cur_location,null);
            titleView = (TextView) mInfoWindowView.findViewById(R.id.tv_title);
            contentView = (TextView) mInfoWindowView.findViewById(R.id.tv_content);
        }

        String address = "该客户尚未同步过位置";
        if (!TextUtils.isEmpty(obj.optString("address"))) {
            address = obj.optString("address");
        }
        String title = obj.optString("aname");
        String mobile = obj.optString("mobile");
        if (!TextUtils.isEmpty(mobile)) {
            title = title + "(" + mobile + ")";
        }
        titleView.setText(title);
        contentView.setText(address);
        InfoWindow infoWindow = new InfoWindow(BitmapDescriptorFactory.fromView(mInfoWindowView), ll, -(markerHeight / 2 + 10), null);
        return infoWindow;
    }

    @OnClick(R.id.btn_pre) void pre() {
        if (data == null || data.length() == 0 || currentFocusIndex == 0) {
            return;
        }

        int index = currentFocusIndex - 1;
        Logger.d("pre index: " + index);
        focusPoint(index);
    }

    @OnClick(R.id.btn_next) void next() {
        if (data == null || data.length() == 0 || currentFocusIndex == data.length() - 1) {
            return;
        }

        int index = currentFocusIndex + 1;
        Logger.d("next index: " + index);
        focusPoint(index);
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

        data = null;
        super.onDestroy();
    }

    public static void start(Context context, JSONArray data) {
        CustomerMapActivity.data = data;
        context.startActivity(new Intent(context,CustomerMapActivity.class));
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

                if (data == null || data.length() == 0) {
                    MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
                    mBaiduMap.animateMapStatus(u);
                }
            }
            DataManager.getInstance().setCurrentLatLng(ll);
        }

        public void onReceivePoi(BDLocation poiLocation) {

        }
    }
}
