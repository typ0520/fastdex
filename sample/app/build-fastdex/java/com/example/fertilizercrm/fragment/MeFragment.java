package com.example.fertilizercrm.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.example.fertilizercrm.R;
import com.example.fertilizercrm.activity.AboutUsActivity;
import com.example.fertilizercrm.activity.CustomerListActivity;
import com.example.fertilizercrm.activity.FactoryListActivity;
import com.example.fertilizercrm.activity.LoginActivity;
import com.example.fertilizercrm.activity.MessageCenterActivity;
import com.example.fertilizercrm.activity.ProductListActivity;
import com.example.fertilizercrm.activity.ProfileActivity;
import com.example.fertilizercrm.activity.SalesmanListActivity;
import com.example.fertilizercrm.adapter.MeAdapter;
import com.example.fertilizercrm.basic.BaseFragment;
import com.example.fertilizercrm.basic.DataManager;
import com.example.fertilizercrm.bean.LoginResponse;
import com.example.fertilizercrm.http.URLText;
import com.example.fertilizercrm.role.Role;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.example.fertilizercrm.common.imageloader.core.DisplayImageOptions;
import com.example.fertilizercrm.common.imageloader.core.ImageLoader;
import com.example.fertilizercrm.common.utils.EventEmitter;

/**
 * 我的页面
 */
public class MeFragment extends BaseFragment {
    /**
     * 我的资料
     */
    private static final int REQUEST_CODE_PROFILE = 1;

    public static final String KEY_ACTION = "action";
    public static final String KEY_ICON = "icon";
    public static final String KEY_TEXT = "text";
    public static final String KEY_UNREAD_ADDRESS_NUMBER = "UNREAD_ADDRESS_NUMBER";

    //    查询
    //    厂家:
    //    我的信息 我的联系人 我的业务员 我的客户 我的产品 消息中心
    //
    //    厂家业务员:
    //    我的信息 我的联系人 我的客户 我的产品 消息中心
    //
    //    批发商
    //    我的信息 我的联系人 我的业务员 我的客户 我的产品 消息中心
    //
    //    批发商业务员
    //    我的信息 我的联系人 我的客户 我的产品 消息中心
    //
    //    零售商
    //    我的信息 我的联系人 我的业务员 我的客户 我的产品 消息中心
    //
    //    零售商业务员
    //    我的信息 我的联系人 我的客户 我的产品 消息中心

    /**
     * 我的联系人
     */
    public static final int ACTION_MY_LINK = 1;
    /**
     * 我的业务员
     */
    public static final int ACTION_MY_SALESMAN = 2;
    /**
     * 我的客户
     */
    public static final int ACTION_MY_CUSTOMER = 3;
    /**
     * 我的产品
     */
    public static final int ACTION_MY_PRODUCT = 4;
    /**
     * 消息中心
     */
    public static final int ACTION_MSG_CENTER = 5;
    /**
     * 我的厂家
     */
    public static final int ACTION_MY_FACTORY = 6;
    /**
     * 关于我们
     */
    public static final int ACTION_ABOUT_ME = 7;


    @Bind(R.id.tv_name)    TextView  tv_name;
    @Bind(R.id.tv_mobile)  TextView  tv_mobile;
    @Bind(R.id.tv_company) TextView  tv_company;
    @Bind(R.id.tv_type)    TextView  tv_type;
    @Bind(R.id.iv_head)    ImageView iv_head;
    @Bind(R.id.lv)         ListView  lv;

    MeAdapter adapter;
    private int count;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View contentView = obtainContentView(R.layout.fragment_me, container);
        ButterKnife.bind(this, contentView);

        initMenu();

        String hiconUrl = null;
        if (DataManager.getInstance().getLoginResponse() != null) {
            hiconUrl = DataManager.getInstance().getLoginResponse().getMImageURL();;
        }
        if (!TextUtils.isEmpty(hiconUrl)) {
            DisplayImageOptions options = new DisplayImageOptions.Builder()
                    .cacheInMemory(false)
                    .cacheOnDisc(true)
                    .considerExifParams(true)
                    .showImageOnFail(R.drawable.default_img)
                    .showImageOnFail(R.drawable.default_img)
                    .bitmapConfig(Bitmap.Config.RGB_565).build();
            ImageLoader.getInstance().displayImage(URLText.imgUrl + hiconUrl, iv_head, options);
        }
        eventEmitter().register(ProfileActivity.EVENT_GET_NEW_HICON, new EventEmitter.OnEventListener() {
            @Override
            public void onEvent(String event, Object data) {
                Bitmap bitmap = BitmapFactory.decodeFile(((File) data).getAbsolutePath());
                if (bitmap != null) {
                    iv_head.setImageBitmap(bitmap);
                }
            }
        });

        return contentView;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (tv_name != null && DataManager.getInstance().getLoginResponse() != null) {
            LoginResponse loginResponse = DataManager.getInstance().getLoginResponse();
            tv_name.setText(loginResponse.getMUserName());
            tv_mobile.setText(loginResponse.getMPhone());
            tv_company.setText(loginResponse.getMCompany());
            tv_type.setText(Role.currentRole().getDescription());
        }
    }

    @OnClick({R.id.ib_setting,R.id.iv_head}) void clickSetting() {
        startActivity(ProfileActivity.class, REQUEST_CODE_PROFILE);
    }

    private void initMenu() {
//        String[] from = {KEY_ICON,KEY_TEXT,KEY_UNREAD_ADDRESS_NUMBER};
//        int[] to = {R.id.iv,R.id.tv,R.id.tv_unread_address_number};
        final List<? extends Map<String, ?>> dataSourceList = getDataSource();
//        SimpleAdapter adapter = new SimpleAdapter(getActivity(),dataSourceList,R.layout.list_item_setting,from,to);
//        adapter.setViewBinder(new SimpleAdapter.ViewBinder() {
//            @Override
//            public boolean setViewValue(View view, Object data, String textRepresentation) {
//                if (view.getId() == R.id.tv_unread_address_number) {
//                    if (data.equals(KEY_UNREAD_ADDRESS_NUMBER)) {
//                        tv_unread_address_number = (TextView) view;
//                        updateUnreadAddressLable(count);
//                        return true;
//                    }
//                    return true;
//                }
//                return false;
//            }
//        });
        adapter = new MeAdapter(getActivity());
        adapter.setDataList(dataSourceList);
        lv.setAdapter(adapter);
        adapter.setMsgCount(count);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MeFragment.this.onItemClick((Integer) dataSourceList.get(position).get(KEY_ACTION));
            }
        });
    }

    protected void onItemClick(int action) {
        if (action == ACTION_MY_LINK) {
            //我的联系人
            //startActivity(ContactListActivity.class);
        }
        else if (action == ACTION_MY_CUSTOMER) {
            //我的客户
            startActivity(CustomerListActivity.class);
        }
        else if (action == ACTION_MY_SALESMAN) {
            //我的业务员
            startActivity(SalesmanListActivity.class);
        }
        else if (action == ACTION_MY_FACTORY) {
            //我的厂家
            startActivity(FactoryListActivity.class);
        }
        else if (action == ACTION_MY_PRODUCT) {
            //我的产品
            startActivity(ProductListActivity.class);
        }
        else if (action == ACTION_MSG_CENTER) {
            //消息中心
            startActivity(MessageCenterActivity.class);
        }
        else if (action == ACTION_ABOUT_ME) {
            //关于我们
            startActivity(AboutUsActivity.class);
        }
    }

    protected List<? extends Map<String, ?>> getDataSource() {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        list.add(new HashMap<String, Object>(){{
            put(KEY_ACTION,ACTION_MY_LINK);
            put(KEY_ICON,R.drawable.ico_link);
            put(KEY_TEXT,"我的联系人");
            put(KEY_UNREAD_ADDRESS_NUMBER,KEY_UNREAD_ADDRESS_NUMBER);
        }});
        list.add(new HashMap<String, Object>(){{
            put(KEY_ACTION,ACTION_MY_CUSTOMER);
            put(KEY_ICON,R.drawable.ico_customer);
            put(KEY_TEXT,"我的客户");
            put(KEY_UNREAD_ADDRESS_NUMBER,"false");
        }});
        if (!currentRole().isSalesman()) {
            list.add(new HashMap<String, Object>(){{
                put(KEY_ACTION,ACTION_MY_SALESMAN);
                put(KEY_ICON,R.drawable.ico_salesman);
                put(KEY_TEXT,"我的业务员");
                put(KEY_UNREAD_ADDRESS_NUMBER,"false");
            }});
        }
        if (currentRole() == Role.yiji || currentRole() == Role.erji) {
            list.add(new HashMap<String, Object>(){{
                put(KEY_ACTION,ACTION_MY_FACTORY);
                put(KEY_ICON,R.drawable.ico_factory);
                put(KEY_TEXT,"我的厂家");
                put(KEY_UNREAD_ADDRESS_NUMBER,"false");
            }});
        }
        list.add(new HashMap<String, Object>(){{
            put(KEY_ACTION,ACTION_MY_PRODUCT);
            put(KEY_ICON,R.drawable.ico_product);
            put(KEY_TEXT,"我的产品");
            put(KEY_UNREAD_ADDRESS_NUMBER,"false");
        }});
        list.add(new HashMap<String, Object>(){{
            put(KEY_ACTION,ACTION_MSG_CENTER);
            put(KEY_ICON,R.drawable.ico_msg_center);
            put(KEY_TEXT,"消息中心");
            put(KEY_UNREAD_ADDRESS_NUMBER,"false");
        }});
        list.add(new HashMap<String, Object>() {{
            put(KEY_ACTION, ACTION_ABOUT_ME);
            put(KEY_ICON, R.drawable.ico_about);
            put(KEY_TEXT, "关于我们");
            put(KEY_UNREAD_ADDRESS_NUMBER,"false");
        }});
        return list;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PROFILE
                && resultCode == ProfileActivity.RESULT_CODE_GO_TO_LOGIN) {
            getActivity().finish();

            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(intent.getFlags() | LoginActivity.FLAG_TO_LAUNCH_WHEN_BACK);
            intent.setFlags(intent.getFlags() | LoginActivity.FLAG_TO_MAIN_PAGE);
            startActivity(intent);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public synchronized void updateUnreadAddressLable(int count) {
        this.count = count;
        if (adapter != null) {
            adapter.setMsgCount(count);
        }
    }
}
