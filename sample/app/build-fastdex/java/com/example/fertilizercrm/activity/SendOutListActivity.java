package com.example.fertilizercrm.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.example.fertilizercrm.R;
import com.example.fertilizercrm.basic.JSONArrayListActivity;
import com.example.fertilizercrm.http.Req;
import com.example.fertilizercrm.sdlv.Menu;
import com.example.fertilizercrm.sdlv.MenuItem;
import com.example.fertilizercrm.sdlv.SlideAndDragListView;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;
import butterknife.Bind;
import com.example.fertilizercrm.common.utils.DimensionPixelUtil;
import com.example.fertilizercrm.common.utils.Logger;

/**
 * 出货查询列表
 */
public class SendOutListActivity extends JSONArrayListActivity<SendOutListActivity.ViewHolder> {
    @Override
    protected int getLayoutResId() {
        return R.layout.activity_slistview;
    }

    @Override
    protected int getCellLayoutResId() {
        return R.layout.list_item_send_out_list;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getTitleView().setTitle("出货查询");
        loadData();
    }

    @Override
    protected void onListViewInit() {
        Menu menu = new Menu((int)DimensionPixelUtil.dip2px(this,60), new ColorDrawable(Color.LTGRAY), true);
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
            public int onMenuItemClick(View v, final int itemPosition, int buttonPosition, int direction) {
                Logger.d(">> itemPosition: " + itemPosition + " buttonPosition: " + buttonPosition);
                if (buttonPosition == 0) {
                    new AlertView("提示", "确定删除选中的发货单吗?", null, new String[]{"确定", "取消"}, null, getActivity(),
                            AlertView.Style.Alert, new OnItemClickListener() {
                        @Override
                        public void onItemClick(Object o, int position) {
                            if (position == 0) {
                                deleteOrder(position);
                            }
                        }
                    }).show();
                }
                return Menu.ITEM_SCROLL_BACK;
            }
        });

        slv.setOnListItemClickListener(new SlideAndDragListView.OnListItemClickListener() {
            @Override
            public void onListItemClick(View v, int position) {
                String numbers = adapter.getItem(position).optString("numbers");
                Intent intent = new Intent(getActivity(),SendOutDetailActivity.class);
                intent.putExtra(SendOutDetailActivity.KEY_OUTNUMBERS,numbers);
                startActivity(intent);
            }
        });
    }

    private void deleteOrder(final int position) {
        String outnumbers = adapter.getItem(position).optString("numbers");
        Logger.d("<< del send out order: " + outnumbers);
        //删除发货单
        new Req().url(Req.salesDeal)
                .addParam("sign", "delete")
                .addParam("outnumbers",outnumbers)
                .post(new DefaultCallback<JSONObject>() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        adapter.delete(position);
                    }
                });
    }

    @Override
    protected void bindData(int position, SendOutListActivity.ViewHolder holder, JSONObject obj) {
        holder.tv_agent_name.setText((String)holder.tv_agent_name.getTag() + obj.optString("numbers"));
        holder.tv_principal.setText((String)holder.tv_principal.getTag() + obj.optString("sdate"));
        holder.tv_maintain.setText((String) holder.tv_maintain.getTag() + obj.optString("tusername"));

        String status = obj.optString("nowStatus");
        if (status.equals("0")) {
            status = "等待上级审批";
        }
        else if (status.equals("1")) {
            status = "等待确认";
        }
        else if (status.equals("2")) {
            status = "等待对方确认";
        }
        else if (status.equals("3")) {
            status = "双方已确认";
        }
        else if (status.equals("3")) {
            status = "对方已取消交易";
        }
        holder.tv_location.setText((String)holder.tv_location.getTag() + status);
    }

    private void loadData() {
        //{"body":[{"amount":10,"bsoid":6,"countnumber":1,"counttype":1,"countweight":1,"createDate":"2015-12-25 14:58:26.0",
        // "creator":"yiji","freight":0,"fuserid":1,"fusertype":1,"incomeamount":10,"memo":"","modifier":"",
        // "modifyDate":"","nowStatus":2,"numbers":"CH20151225145826887","pricerole":"自提","saleamount":10,
        // "sdate":"2015-12-25","status":0,"suserid":0,"tuserid":1,"tusername":"批发商","tusertype":2}]

        new Req().url(Req.salesDeal)
                .addParam("sign", "search")
                .addParam("start","1")
                .addParam("limit","10000")
                .post(new DefaultCallback<JSONArray>() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                        adapter.setData(response);
                    }
                });
    }

    public static final class ViewHolder {
        @Bind(R.id.tv_agent_name) TextView tv_agent_name;//单号
        @Bind(R.id.tv_principal)  TextView tv_principal;//日期
        @Bind(R.id.tv_maintain)   TextView tv_maintain;//收货人
        @Bind(R.id.tv_location)   TextView tv_location;//状态
    }
}
