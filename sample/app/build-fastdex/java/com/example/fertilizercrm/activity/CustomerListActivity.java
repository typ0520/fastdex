package com.example.fertilizercrm.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.example.fertilizercrm.R;
import com.example.fertilizercrm.adapter.CustomerListAdapter;
import com.example.fertilizercrm.basic.ListViewActivity;
import com.example.fertilizercrm.bean.Agent;
import com.example.fertilizercrm.bean.AgentLevel2;
import com.example.fertilizercrm.bean.Farmer;
import com.example.fertilizercrm.http.Params;
import com.example.fertilizercrm.http.Req;
import com.example.fertilizercrm.role.Role;
import com.example.fertilizercrm.view.CustomerTypeSwitchView;
import com.google.gson.Gson;
import com.example.fertilizercrm.sdlv.Menu;
import com.example.fertilizercrm.sdlv.MenuItem;
import com.example.fertilizercrm.sdlv.SlideAndDragListView;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.Serializable;
import butterknife.Bind;
import butterknife.OnClick;
import com.example.fertilizercrm.common.utils.Logger;

/**
 * 我的客户
 */
public class CustomerListActivity extends ListViewActivity<CustomerListAdapter> {

    public static final String KEY_TYPE = "type";

    public static final String KEY_DATA = "data";

    public static final int OPEN_TYPE_GET_CUSTOMER = 1;

    /**
     * 批发商
     */
    public static final int TYPE_YIJI = 0;

    /**
     * 零售商
     */
    public static final int TYPE_ERJI = 1;

    /**
     * 农户
     */
    public static final int TYPE_FARMER = 2;

    private static final int REQUEST_ADD_CUSTOMER = 1;

    private static final int REQUEST_UPDATE_CUSTOMER = 2;

    @Bind(R.id.customer_switch) CustomerTypeSwitchView customer_switch;
    private Gson gson = new Gson();

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_customer_list;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getTitleView().setTitle("我的客户").setRightImageResource(R.drawable.ico_map).setRightClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomerMapActivity.start(getContext(),adapter.getData());
            }
        });
        if (currentRole() == Role.erji
                || currentRole() == Role.erji_salesman) {
            //零售商和对应业务员的客户只有农户
            setEmptyText("暂时没有农户信息");
        }
        slv.setOnListItemClickListener(new SlideAndDragListView.OnListItemClickListener() {
            @Override
            public void onListItemClick(View v, int position) {
                JSONObject obj = adapter.getItem(position);
                Class<?> clazz = null;
                Class<?> editActivityClass = null;
                int type = -1;
                if (customer_switch.getCurrentType() == TYPE_YIJI) {
                    //批发商
                    clazz = Agent.class;
                    editActivityClass = AgentActivity.class;
                    type = TYPE_YIJI;
                } else if (customer_switch.getCurrentType() == TYPE_ERJI) {
                    //零售商
                    clazz = AgentLevel2.class;
                    editActivityClass = AgentActivity.class;
                    type = TYPE_ERJI;
                } else if (customer_switch.getCurrentType() == TYPE_FARMER) {
                    //农户
                    clazz = Farmer.class;
                    editActivityClass = FarmerActivity.class;
                    type = TYPE_FARMER;
                }
                Serializable object = (Serializable) gson.fromJson(obj.toString(), clazz);
                if (openType == OPEN_TYPE_DEFAULT) {
                    Intent intent = new Intent(getActivity(), editActivityClass);
                    if (customer_switch.getCurrentType() == TYPE_YIJI) {
                        //批发商
                        intent.putExtra(AgentActivity.KEY_IS_LEVEL1,true);
                    } else if (customer_switch.getCurrentType() == TYPE_ERJI) {
                        //零售商
                        intent.putExtra(AgentActivity.KEY_IS_LEVEL1,false);
                    }
                    intent.putExtra(KEY_OPEN_TYPE, OPEN_TYPE_EDIT);
                    intent.putExtra(KEY_DATA, object);
                    startActivityForResult(intent, REQUEST_UPDATE_CUSTOMER);
                } else if (openType == OPEN_TYPE_GET_CUSTOMER) {
                    Intent data = new Intent();
                    data.putExtra(KEY_TYPE, type);
                    data.putExtra(KEY_DATA, object);
                    Logger.d("<< type: " + type);
                    Logger.d("<< data: " + data);
                    setResult(RESULT_OK, data);
                    finish();
                }
            }
        });
        customer_switch.setOnItemClickListener(new CustomerTypeSwitchView.OnItemClickListener() {
            @Override
            public void onItemClick(int type,Role role) {
                if (role.isAgent()) {
                    getTitleView().showRight();
                }
                else {
                    getTitleView().hideRight();
                }
                loadData(type);
            }
        });

        loadData(customer_switch.getCurrentType());
    }

    @Override
    protected void onListViewInit() {
        Menu menu = new Menu((int) getResources().getDimension(R.dimen.send_out_item_height), new ColorDrawable(Color.LTGRAY), true);
        menu.addItem(new MenuItem.Builder().setWidth((int) getResources().getDimension(R.dimen.send_out_item_width))
                .setBackground(new ColorDrawable(getResources().getColor(R.color.send_out_del)))
                .setText("删除")
                .setDirection(MenuItem.DIRECTION_RIGHT)
                .setTextColor(Color.WHITE)
                .setTextSize((int) getResources().getDimension(R.dimen.send_out_item_txt_size))
                .build());
        slv.setMenu(menu);
        slv.setOnMenuItemClickListener(new SlideAndDragListView.OnMenuItemClickListener() {
            @Override
            public int onMenuItemClick(View v, int itemPosition, int buttonPosition, int direction) {
                Logger.d(">> itemPosition: " + itemPosition + " buttonPosition: " + buttonPosition);
                if (buttonPosition == 0) {
                    new AlertView("提示", "确定删除吗?", null, new String[]{"确定", "取消"}, null, getActivity(),
                            AlertView.Style.Alert, new OnItemClickListener() {
                        @Override
                        public void onItemClick(Object o, int position) {
                            if (position == 0) {
                                deleteCustomer(position);
                            }
                        }
                    }).show();
                }
                return Menu.ITEM_SCROLL_BACK;
            }
        });
    }

    //删除客户(批发商、零售商、农户)
    private void deleteCustomer(final int position) {
        JSONObject obj = adapter.getItem(position);
        Params params = new Params();
        params.put("bsoid", obj.optString("bsoid"));
        params.put("sign", "delete");

        DefaultCallback<JSONObject> callback = new DefaultCallback<JSONObject>() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                showLongToast("删除成功!");
                adapter.delete(position);
                adapter.notifyDataSetChanged();
            }
        };

        if (customer_switch.getCurrentType() == TYPE_YIJI) {
            //删除批发商
            new Req().url(Req.agentDeal).params(params).addParam("level", "1").get(callback);
        }
        else if (customer_switch.getCurrentType() == TYPE_ERJI) {
            //删除零售商
            new Req().url(Req.agentDeal).params(params).addParam("level", "2").get(callback);
        }
        else if (customer_switch.getCurrentType() == TYPE_FARMER) {
            //删除农户
           new Req().url(Req.customerDeal).params(params).get(callback);
        }
    }

    private void loadData(int type) {
        Params params = new Params();
        params.put("start","1");
        params.put("limit","100");
        params.put("sign","search");

        DefaultCallback<JSONArray> callback = new DefaultCallback<JSONArray>() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                adapter.setType(customer_switch.getCurrentType());
                adapter.setData(response);
            }
        };

        if (type == TYPE_YIJI) {
            new Req().url(Req.agentDeal).params(params).addParam("level", "1").post(callback);
        }
        else if (type == TYPE_ERJI) {
            new Req().url(Req.agentDeal).params(params).addParam("level", "2").post(callback);
        }
        else if (type == TYPE_FARMER) {
            new Req().url(Req.customerDeal).params(params).post(callback);
        }
    }

    @OnClick(R.id.ib_add) void addCustomer() {
        if (customer_switch.getCurrentType() == TYPE_YIJI) {
            //批发商
            Intent intent = new Intent(getActivity(),AgentActivity.class);
            intent.putExtra(AgentActivity.KEY_IS_LEVEL1,true);
            startActivityForResult(intent,REQUEST_ADD_CUSTOMER);
        } else if (customer_switch.getCurrentType() == TYPE_ERJI) {
            //零售商
            Intent intent = new Intent(getActivity(),AgentActivity.class);
            intent.putExtra(AgentActivity.KEY_IS_LEVEL1,false);
            startActivityForResult(intent,REQUEST_ADD_CUSTOMER);
        } else if (customer_switch.getCurrentType() == TYPE_FARMER) {
            //农户
            startActivityForResult(FarmerActivity.class, REQUEST_ADD_CUSTOMER);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ADD_CUSTOMER
                && resultCode == RESULT_OK) {
            loadData(customer_switch.getCurrentType());
        }

        if (requestCode == REQUEST_UPDATE_CUSTOMER
                && resultCode == RESULT_OK) {
            loadData(customer_switch.getCurrentType());
        }
    }
}
