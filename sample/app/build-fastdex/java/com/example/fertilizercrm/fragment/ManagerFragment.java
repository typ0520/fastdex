package com.example.fertilizercrm.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.example.fertilizercrm.R;
import com.example.fertilizercrm.activity.CommonWebviewActivity;
import com.example.fertilizercrm.activity.CustomerListActivity;
import com.example.fertilizercrm.activity.LocationSyncActivity;
import com.example.fertilizercrm.activity.MessageExpandActivity;
import com.example.fertilizercrm.activity.ReceivablesActivity;
import com.example.fertilizercrm.activity.SalesmanWorkListActivity;
import com.example.fertilizercrm.activity.SendInActivity;
import com.example.fertilizercrm.activity.SendOutActivity;
import com.example.fertilizercrm.activity.ToDoListActivity;
import com.example.fertilizercrm.basic.BaseFragment;
import com.example.fertilizercrm.basic.DataManager;
import com.example.fertilizercrm.http.Callback;
import com.example.fertilizercrm.http.FerAppUpdateHandler;
import com.example.fertilizercrm.http.Req;
import com.example.fertilizercrm.http.URLText;
import com.example.fertilizercrm.role.Role;
import com.example.fertilizercrm.view.SectorView;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.example.fertilizercrm.common.utils.Logger;
import com.example.fertilizercrm.common.view.AutoImageViewPager;
import com.example.fertilizercrm.common.view.BaseOnPageChangeListener;
import com.example.fertilizercrm.common.view.PagerMarkerView;

/**
 * 管理页面
 */
public class ManagerFragment extends BaseFragment {
    public static final String KEY_ACTION = "action";
    public static final String KEY_ICON = "icon";
    public static final String KEY_TITLE = "title";
    public static final String KEY_DESC = "desc";
    public static final String KEY_MSG_COUNT = "msg_count";

    //    管理
    //    厂家:
    //    发货 收款 待办事项 消息推送 同步我的位置
    //
    //    厂家业务员:
    //    发货 收款 待办事项 日常工作 消息推送
    //
    //    批发商
    //    发货 收款 待办事项 进货 消息推送 同步我的位置
    //
    //    批发商业务员
    //    发货 收款 待办事项 日常工作 消息推送
    //
    //    零售商
    //    发货 收款 待办事项 进货 消息推送 同步我的位置
    //
    //    零售商业务员
    //    发货 收款 日常工作 消息推送

    /**
     * 发货
     */
    public static final int ACTION_SEND_OUT = 1;
    /**
     * 收款
     */
    public static final int ACTION_RECEIPT = 2;
    /**
     * 待办事项
     */
    public static final int ACTION_TO_DO = 3;
    /**
     * 消息推送
     */
    public static final int ACTION_PUSH_MSG = 4;
    /**
     * 同步我的位置
     */
    public static final int ACTION_SYNC_LOCATION = 5;
    /**
     * 日常工作
     */
    public static final int ACTION_ROUTINE = 6;
    /**
     * 进货
     */
    public static final int ACTION_SEND_IN = 7;
    /**
     * 我的客户
     */
    public static final int ACTION_MY_CUSTOMER = 8;
    /**
     * 待办事项请求码
     */
    private static final int REQUEST_CODE_TODOLIST = 1;

    public List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>(){{
        add(new HashMap<String, Object>() {{
            put(KEY_ACTION, ACTION_SEND_OUT);
            put(KEY_ICON, R.drawable.send_out);
            put(KEY_TITLE, "发货");
            put(KEY_DESC, "你的发货清单");
            put(KEY_MSG_COUNT, false);
        }});
        add(new HashMap<String, Object>() {{
            put(KEY_ACTION, ACTION_RECEIPT);
            put(KEY_ICON, R.drawable.ico_get_money2);
            put(KEY_TITLE, "收钱");
            put(KEY_DESC, "你的收款清单");
            put(KEY_MSG_COUNT, false);
        }});
        add(new HashMap<String, Object>() {{
            put(KEY_ACTION, ACTION_TO_DO);
            put(KEY_ICON, R.drawable.gtasks);
            put(KEY_TITLE, "待办事项");
            put(KEY_DESC, "你的待办事项清单");
            put(KEY_MSG_COUNT, true);
        }});
        add(new HashMap<String, Object>() {{
            put(KEY_ACTION,ACTION_PUSH_MSG);
            put(KEY_ICON, R.drawable.push_msg);
            put(KEY_TITLE, "发布消息");
            put(KEY_DESC, "发布你的求购信息");
            put(KEY_MSG_COUNT, false);
        }});
        add(new HashMap<String, Object>() {{
            put(KEY_ACTION,ACTION_SYNC_LOCATION);
            put(KEY_ICON, R.drawable.ico_sync_location);
            put(KEY_TITLE, "同步位置");
            put(KEY_DESC, "同步我的位置");
            put(KEY_MSG_COUNT, false);
        }});
        add(new HashMap<String, Object>() {{
            put(KEY_ACTION,ACTION_ROUTINE);
            put(KEY_ICON, R.drawable.day_work);
            put(KEY_TITLE, "日常工作");
            put(KEY_DESC, "日常工作");
            put(KEY_MSG_COUNT, false);
        }});
        add(new HashMap<String, Object>() {{
            put(KEY_ACTION, ACTION_SEND_IN);
            put(KEY_ICON, R.drawable.send_in);
            put(KEY_TITLE, "进货");
            put(KEY_DESC, "你的进货清单");
            put(KEY_MSG_COUNT, false);
        }});
        add(new HashMap<String, Object>() {{
            put(KEY_ACTION, ACTION_MY_CUSTOMER);
            put(KEY_ICON, R.drawable.ico_wdkh);
            put(KEY_TITLE, "我的客户");
            put(KEY_DESC, "我的客户");
            put(KEY_MSG_COUNT, false);
        }});
    }};

    @Bind(R.id.view_pager) AutoImageViewPager mViewPager;
    @Bind(R.id.pager_marker) PagerMarkerView mPagerMarker;
    @Bind(R.id.grid_view) GridView mGridView;
    @Bind(R.id.sector_view) SectorView sector_view;
    @Bind(R.id.tv_mine) TextView tv_mine;
    @Bind(R.id.tv_other) TextView tv_other;
    @Bind(R.id.ll_scale) View ll_scale;
    private TextView tv_msg_count;

    protected List<Map<String,Object>> dataSourceList;
    protected SimpleAdapter adapter;
    private List<String> urls;

    private Handler handler = new Handler();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View contentView = obtainContentView(R.layout.fragment_manager, container);
        ButterKnife.bind(this, contentView);
        return contentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initBanner(null);
        initMenu();

        if (currentRole() == Role.yiji) {
            if (DataManager.getInstance().getLoginResponse() != null) {
                SectorView.SectorInfo[] sectorInfos = new SectorView.SectorInfo[2];
                sectorInfos[0] = new SectorView.SectorInfo(){{
                    progress = DataManager.getInstance().getLoginResponse().getMPercent();
                    color = R.color.sector_me;
                }};

                sectorInfos[1] = new SectorView.SectorInfo(){{
                    progress = 100.0d - DataManager.getInstance().getLoginResponse().getMPercent();
                    color = R.color.sector_other;
                }};
                ll_scale.setVisibility(View.VISIBLE);
                sector_view.setSectorInfos(sectorInfos);
                tv_mine.setText("我的: " + DataManager.getInstance().getLoginResponse().getMPercent() + "%");
                tv_other.setText("其它: " + (100.0d - DataManager.getInstance().getLoginResponse().getMPercent()) + "%");
            }
        }
        else {
            ll_scale.setVisibility(View.GONE);
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadData();
            }
        }, 1500);
    }

    private void loadData() {
        new Req().url(Req.clientDeal)
                .addParam("sign", "search")
                .get(new Callback<JSONArray>() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                        List<String> imagesUrl = new ArrayList<String>();
                        urls = new ArrayList<String>();

                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.optJSONObject(i);

                            int index = 1;
                            while (index <= 5) {
                                String pic = obj.optString("pic" + index);
                                String url = obj.optString("pic" + index + "url");
                                if (!TextUtils.isEmpty(pic)) {
                                    if (pic.startsWith("http")) {
                                        imagesUrl.add(pic);
                                    } else {
                                        imagesUrl.add(URLText.imgUrl + pic);
                                    }
                                    urls.add(url);
                                }
                                index++;
                            }
                        }
                        Logger.d("imageUrl: " + imagesUrl);
                        Logger.d("urls: " + urls);
                        if (imagesUrl != null && imagesUrl.size() > 0) {
                            initBanner(imagesUrl);
                        }
                    }
                });

        //app更新
        File apkFile = new File(Environment.getExternalStorageDirectory(),getContext().getPackageName() + ".apk");
        if (apkFile.exists()) {
            apkFile.delete();
        }
        new Req().url(Req.clientDeal)
                .addParam("sign","update")
                .addParam("platform","android")
                .get(new FerAppUpdateHandler(getActivity(),apkFile));
    }

    private void initBanner(List<String> imageUrls) {
        Logger.d(">> imageUrls: " + imageUrls);
        if (imageUrls == null || imageUrls.isEmpty()) {
            mViewPager.setImages(R.drawable.banner);
            mViewPager.setOnPageChangeListener(new BaseOnPageChangeListener() {
                @Override
                public void onPageSelected(int position) {

                }
            });
            mPagerMarker.setCount(0);
            mViewPager.setOnClickListener(null);
        }
        else {
            mViewPager.setImageUrls(R.drawable.banner,R.drawable.banner,imageUrls);
            mViewPager.setOnPageChangeListener(new BaseOnPageChangeListener() {
                @Override
                public void onPageSelected(int position) {
                    mPagerMarker.setCurrent(position);
                }
            });
            mPagerMarker.setCount(imageUrls.size());
            mViewPager.setOnTouchListener(new View.OnTouchListener() {
                private float downX,downY;
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        downX = event.getX();
                        downY = event.getY();
                    }
                    else if (event.getAction() == MotionEvent.ACTION_UP) {
                        if (Math.abs(event.getX() - downX) < 50
                                && Math.abs(event.getY() - downY) < 50) {
                            String url = urls.get(mViewPager.getCurrentItem());
                            if (!TextUtils.isEmpty(url)) {
                                CommonWebviewActivity.start(getContext(),"",url);
                                return true;
                            }
                        }
                    }
                    return false;
                }
            });
        }
    }

    protected void initMenu() {
        dataSourceList = getDataSource();
        if (dataSourceList.size() % 2 != 0) {
            dataSourceList.add(new HashMap<String, Object>() {{
                put(KEY_ACTION,-1);
                put(KEY_ICON, -1);
                put(KEY_TITLE, "");
                put(KEY_DESC, "");
                put(KEY_MSG_COUNT, false);
            }});
        }
        String[] from = {KEY_ICON,KEY_TITLE,KEY_DESC,KEY_MSG_COUNT};
        int[] to = {R.id.iv, R.id.tv_title, R.id.tv_desc,R.id.tv_msg_number};
        adapter = new SimpleAdapter(getActivity(),dataSourceList,R.layout.list_item_manager_menu,from,to);
        adapter.setViewBinder(new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data, String textRepresentation) {
                if (data == null || data.equals("")) {
                    Logger.d("<< managerFragment view binder empty");
                    view.setVisibility(View.INVISIBLE);
                }
                if (view.getId() == R.id.tv_msg_number) {
                    if (data != null && (data instanceof Boolean)) {
                        boolean value = (boolean) data;
                        if (value) {
                            view.setVisibility(View.VISIBLE);
                            tv_msg_count = (TextView) view;

                            if (DataManager.getInstance().getLoginResponse() != null) {
                                setMsgCount(DataManager.getInstance().getLoginResponse().getMtodo());
                            }
                            else {
                                setMsgCount(0);
                            }
                        }
                        else {
                            view.setVisibility(View.GONE);
                        }
                    }
                    return true;
                }
                return false;
            }
        });
        mGridView.setAdapter(adapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ManagerFragment.this.onItemClick((Integer) dataSourceList.get(position).get(KEY_ACTION));
            }
        });
    }

    protected void onItemClick(int action) {
        if (action == ACTION_SEND_OUT) {
            //发货
            startActivity(SendOutActivity.class);
        }
        else if (action == ACTION_RECEIPT) {
            //收款
            startActivity(ReceivablesActivity.class);
        }
        else if (action == ACTION_TO_DO) {
            //setMsgCount(0);
            //待办事项
            startActivityForResult(new Intent(getContext(),ToDoListActivity.class), REQUEST_CODE_TODOLIST);
        }
        else if (action == ACTION_PUSH_MSG) {
            //发布信息
            startActivity(MessageExpandActivity.class);
        }
        else if (action == ACTION_SYNC_LOCATION) {
            //同步位置
            startActivity(LocationSyncActivity.class);
        }
        else if (action == ACTION_ROUTINE) {
            //日常工作
            startActivity(SalesmanWorkListActivity.class);
        }
        else if (action == ACTION_SEND_IN) {
            //进货
            alertView = new AlertView(null, null, "取消", null,
                    new String[]{"系统内进货", "系统外进货"}, getActivity(), AlertView.Style.ActionSheet, new OnItemClickListener() {
                @Override
                public void onItemClick(Object o, int position) {
                    if (alertView != null) {
                        alertView.dismiss();
                    }
                    //进货
                    if (position == 0) {
                        Intent intent = new Intent(getActivity(),SendInActivity.class);
                        intent.putExtra(SendInActivity.KEY_OPEN_TYPE, SendInActivity.OPEN_TYPE_DEFAULT);
                        startActivity(intent);
                    }
                    else if (position == 1) {
                        Intent intent = new Intent(getActivity(),SendInActivity.class);
                        intent.putExtra(SendInActivity.KEY_OPEN_TYPE, SendInActivity.OPEN_TYPE_CHANNEL_SYSTEM_OUT);
                        startActivity(intent);
                    }
                }
            });
            alertView.show();
        }
        else if (action == ACTION_MY_CUSTOMER) {
            //我的客户
            startActivity(CustomerListActivity.class);
        }
    }

    protected List<Map<String, Object>> getDataSource() {
        //    管理
        //    厂家:
        //    发货 收款 待办事项 消息推送 同步我的位置
        //
        //    厂家业务员:
        //    发货 收款 待办事项 日常工作 消息推送
        //
        //    批发商
        //    发货 收款 待办事项 进货 消息推送 同步我的位置
        //
        //    批发商业务员
        //    发货 收款 待办事项 日常工作 消息推送
        //
        //    零售商
        //    发货 收款 待办事项 进货 消息推送 同步我的位置
        //
        //    零售商业务员
        //    发货 收款 日常工作 消息推送
        int[] actions = null;
        if (currentRole() == Role.factory) {
            actions = new int[]{ACTION_SEND_OUT,ACTION_RECEIPT,ACTION_TO_DO,ACTION_PUSH_MSG,ACTION_MY_CUSTOMER};
        }
        else if (currentRole() == Role.factory_salesman) {
            actions = new int[]{ACTION_SEND_OUT,ACTION_RECEIPT,ACTION_TO_DO,ACTION_ROUTINE,ACTION_PUSH_MSG,ACTION_MY_CUSTOMER};
        }
        else if (currentRole() == Role.yiji) {
            actions = new int[]{ACTION_SEND_OUT,ACTION_SEND_IN,ACTION_RECEIPT,ACTION_TO_DO,ACTION_PUSH_MSG,ACTION_SYNC_LOCATION,ACTION_MY_CUSTOMER};
        }
        else if (currentRole() == Role.yiji_salesman) {
            actions = new int[]{ACTION_SEND_OUT,ACTION_RECEIPT,ACTION_TO_DO,ACTION_ROUTINE,ACTION_PUSH_MSG,ACTION_MY_CUSTOMER};
        }
        else if (currentRole() == Role.erji) {
            actions = new int[]{ACTION_SEND_OUT,ACTION_SEND_IN,ACTION_RECEIPT,ACTION_TO_DO,ACTION_PUSH_MSG,ACTION_SYNC_LOCATION,ACTION_MY_CUSTOMER};
        }
        else if (currentRole() == Role.erji_salesman) {
            actions = new int[]{ACTION_SEND_OUT,ACTION_RECEIPT,ACTION_ROUTINE,ACTION_PUSH_MSG,ACTION_MY_CUSTOMER};
        }

        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int action : actions) {
            for (Map<String, Object> data : dataList) {
                if ((int) data.get(KEY_ACTION) == action) {
                    list.add(data);
                    break;
                }
            }
        }
        return list;
    }

    /**
     * 设置待办事项未读数量
     * @param count
     */
    public void setMsgCount(int count) {
        if (tv_msg_count == null) {
            return;
        }
        if (count <= 0) {
            tv_msg_count.setVisibility(View.GONE);
        }
        else {
            tv_msg_count.setVisibility(View.VISIBLE);
            tv_msg_count.setText(String.valueOf(count));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_TODOLIST
                && resultCode == ToDoListActivity.RESULT_OK) {
            if (data != null) {
                int count = data.getIntExtra(ToDoListActivity.KEY_DATA,0);
                setMsgCount(count);
            }
        }
    }
}
