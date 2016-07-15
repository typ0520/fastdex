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
 * 进货查询列表
 */
public class SendInListActivity extends JSONArrayListActivity<SendInListActivity.ViewHolder> {
    private int ptypes = 0;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_slistview;
    }

    @Override
    protected int getCellLayoutResId() {
        return R.layout.list_item_send_in_list;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getTitleView()
                .setTitle("进货查询")
                .setRightText("系统外")
                .setRightClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ptypes = ptypes == 0 ?  1 : 0;

                        getTitleView().setRightText(ptypes == 0 ? "系统外" : "系统内");
                        loadData();
                    }
                });
        loadData();
    }

    @Override
    protected void onListViewInit() {
        Menu menu = new Menu((int) DimensionPixelUtil.dip2px(this, 60), new ColorDrawable(Color.LTGRAY), true);
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
                    new AlertView("提示", "确定删除选中的进货单吗?", null, new String[]{"确定", "取消"}, null, getActivity(),
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
                Intent intent = new Intent(getActivity(), SendInDetailActivity.class);
                intent.putExtra(SendInDetailActivity.KEY_INNUMBERS, numbers);
                startActivity(intent);
            }
        });
    }

    private void deleteOrder(final int position) {
        String innumbers = adapter.getItem(position).optString("numbers");
        Logger.d("<< del send in order: " + innumbers);
        //删除进货单
        new Req().url(Req.purchaseDeal)
                .addParam("sign", "delete")
                .addParam("innumbers",innumbers)
                .get(new DefaultCallback<JSONObject>() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        adapter.delete(position);
                    }
                });
    }

    private void loadData() {
        new Req().url(Req.purchaseDeal)
                .addParam("sign", "search")
                .addParam("start","1")
                .addParam("ptypes",ptypes)
                .addParam("limit", "10000")
                .get(new DefaultCallback<JSONArray>() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                        adapter.setData(response);
                    }
                });
    }

    @Override
    protected void bindData(int position, ViewHolder holder, JSONObject obj) {
        holder.tv_agent_name.setText((String)holder.tv_agent_name.getTag() + obj.optString("numbers"));
        holder.tv_principal.setText((String)holder.tv_principal.getTag() + obj.optString("pdate"));
        holder.tv_maintain.setText((String) holder.tv_maintain.getTag() + obj.optString("tusername"));

        String status = obj.optString("nowStatus");
        if (status.equals("0")) {
            status = "待对方确认";
        }
        else if (status.equals("1")) {
            status = "待我确认";
        }
        else if (status.equals("2")) {
            status = "双方已确认";
        }
        else if (status.equals("3")) {
            status = "对方已取消交易";
        }
        holder.tv_location.setText((String) holder.tv_location.getTag() + status);
    }

    public static final class ViewHolder {
        @Bind(R.id.tv_agent_name) TextView tv_agent_name;//单号
        @Bind(R.id.tv_principal)  TextView tv_principal;//日期
        @Bind(R.id.tv_maintain)   TextView tv_maintain;//收货人
        @Bind(R.id.tv_location)   TextView tv_location;//状态
    }
}
