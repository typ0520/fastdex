package com.example.fertilizercrm.activity;

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
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.example.fertilizercrm.R;
import com.example.fertilizercrm.basic.BaseActivity;
import com.example.fertilizercrm.basic.DataManager;
import com.example.fertilizercrm.bean.LocusInfo;
import com.example.fertilizercrm.http.Params;
import com.example.fertilizercrm.http.Req;
import com.example.fertilizercrm.view.SalesmanSearchHeadView;
import org.apache.http.Header;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.example.fertilizercrm.common.utils.Logger;

/**
 * 轨迹查询页面
 */
public class LocusActivity extends BaseActivity {
    @Bind(R.id.head_view) SalesmanSearchHeadView head_view;
    @Bind(R.id.map_view)  MapView mMapView;
    // 定位相关
    LocationClient mLocClient;
    public MyLocationListenner myListener = new MyLocationListenner();
    BaiduMap mBaiduMap;
    boolean isFirstLoc = true;// 是否首次定位
    private List<LocusInfo> locusInfos;
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
        setContentView(R.layout.activity_locus);
        ButterKnife.bind(this);

        mCurrentMarker = BitmapDescriptorFactory.fromResource(R.drawable.msg_center_point);
        markerHeight = BitmapFactory.decodeResource(getResources(),R.drawable.msg_center_point).getHeight();
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

       head_view.enableSubmit().setOnSearchListener(new SalesmanSearchHeadView.OnSearchListener() {
           @Override
           public void onSearch() {
               loadData();
           }
       });

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

    private void loadData() {
        if (TextUtils.isEmpty(head_view.getBsoid())) {
            showLongToast("请先选择业务员");
            return;
        }
        Params params = new Params();
        params.put("sign", "searchtrack");
        params.put("start", "1");
        params.put("begindate", head_view.getFormatStartDate());
        params.put("enddate", head_view.getFormatEndDate());
        params.put("limit", "1000");
        if (!currentRole().isSalesman()) {
            //厂家、一零售商查看拜访记录需要id
            params.put("sbsoid", head_view.getBsoid());
        }
        new Req().url(Req.dailyWorkDeal)
                .params(params)
                .get(new DefaultCallback<List<LocusInfo>>() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, List<LocusInfo> response) {
                        if (response == null || response.isEmpty()) {
                            showLongToast("未找到数据");
                        } else {
                            head_view.animHide();
                            locusInfos = response;
                            currentFocusIndex = 0;
                            displayLocus();
                        }
                    }
                });
    }

    private void displayLocus() {
        if (this.locusInfos == null || this.locusInfos.isEmpty()) {
            return;
        }
        mBaiduMap.clear();
        markerMap = new HashMap<>();
        List<LatLng> points = new ArrayList<LatLng>();
        int i = 0;
        for (LocusInfo locusInfo : locusInfos) {
            points.add(locusInfo.getLatLng());

            OverlayOptions ooA = new MarkerOptions().position(locusInfo.getLatLng()).icon(mCurrentMarker).zIndex(9).draggable(true);
            Marker marker = (Marker) mBaiduMap.addOverlay(ooA);
            markerMap.put(marker,i);
            i++;
        }
        OverlayOptions ooPolyline = new PolylineOptions().width(8).color(0xAAFF0000).points(points);
        mBaiduMap.addOverlay(ooPolyline);

        focusPoint(0);
    }

    private void focusPoint(int focusIndex) {
        if (this.locusInfos == null || this.locusInfos.isEmpty()) {
            return;
        }
        this.currentFocusIndex = focusIndex;
        LocusInfo locusInfo = locusInfos.get(focusIndex);
        LatLng latLng = locusInfo.getLatLng();
        Logger.d("current position: " + focusIndex + " " + latLng);
        MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(latLng);
        InfoWindow infoWindow = initInfoWindow(locusInfo.getAddress(), latLng);
        mBaiduMap.hideInfoWindow();
        mBaiduMap.showInfoWindow(infoWindow);
        mBaiduMap.animateMapStatus(u);
    }

    private InfoWindow initInfoWindow(String address,LatLng ll) {
        if (mInfoWindowView == null) {
            mInfoWindowView = View.inflate(this,R.layout.view_cur_location,null);
            titleView = (TextView) mInfoWindowView.findViewById(R.id.tv_title);
            contentView = (TextView) mInfoWindowView.findViewById(R.id.tv_content);
        }
        String province = "";
        String city = "";
        if (address != null) {
            try {
                if (address.indexOf("省") != -1) {
                    province = address.substring(0,address.indexOf("省") + 1);
                    if (address.indexOf("市") != -1) {
                        city = address.substring(address.indexOf("省") + 1,address.indexOf("市") + 1);
                    }
                }
                else if (address.indexOf("市") != -1) {
                    province = address.substring(0,address.indexOf("市") + 1);
                    if (address.indexOf("区") != -1) {
                        city = address.substring(address.indexOf("市") + 1,address.indexOf("区") + 1);
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        titleView.setText(province + city);
        contentView.setText(address);
        InfoWindow infoWindow = new InfoWindow(BitmapDescriptorFactory.fromView(mInfoWindowView), ll, -(markerHeight / 2 + 10), null);
        return infoWindow;
    }

    @OnClick(R.id.btn_pre) void pre() {
        if (locusInfos == null || locusInfos.isEmpty() || currentFocusIndex == 0) {
            return;
        }

        int index = currentFocusIndex - 1;
        Logger.d("pre index: " + index);
        focusPoint(index);
    }

    @OnClick(R.id.btn_next) void next() {
        if (locusInfos == null || locusInfos.isEmpty() || currentFocusIndex == locusInfos.size() - 1) {
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
        super.onDestroy();
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
            DataManager.getInstance().setCurrentLatLng(ll);
        }

        public void onReceivePoi(BDLocation poiLocation) {

        }
    }
}
