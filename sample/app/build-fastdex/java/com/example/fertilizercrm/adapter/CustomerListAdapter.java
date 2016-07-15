package com.example.fertilizercrm.adapter;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.fertilizercrm.R;
import org.json.JSONObject;
import butterknife.Bind;
import com.example.fertilizercrm.common.view.SmartJSONArrayAdapter;

/**
 * Created by tong on 15/12/11.
 * 我的客户列表adapter
 * @see com.example.fertilizercrm.activity.CustomerListActivity
 */
public class CustomerListAdapter extends SmartJSONArrayAdapter {
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

    private int currentType;

    public CustomerListAdapter(Context context) {
        super(context,R.layout.list_item_customer_list,ViewHolder.class);
    }

    @Override
    public void bindData(int position, Object h, JSONObject obj) {
        ViewHolder holder = (ViewHolder) h;
        if (currentType == TYPE_ERJI || currentType == TYPE_YIJI) {
            holder.tv_name.setText("  " + obj.optString("aname"));
            holder.tv_desc.setText("  " + obj.optString("contacter"));
            holder.tv_detail.setText("  " + obj.optString("province") + obj.optString("district"));
        }
        else if (currentType == TYPE_FARMER) {
            holder.tv_name.setText("  " + obj.optString("cname"));
            holder.tv_desc.setText("  " + obj.optString("contacter"));
            holder.tv_detail.setText("  " + obj.optString("province") + obj.optString("district"));
        }
    }

    public void setType(int currentType) {
        this.currentType = currentType;
    }

    public static final class ViewHolder {
        @Bind(R.id.iv)          ImageView iv;
        @Bind(R.id.tv_name)     TextView tv_name;
        @Bind(R.id.tv_desc)     TextView tv_desc;
        @Bind(R.id.tv_detail)   TextView tv_detail;
        @Bind(R.id.tv_quantity) TextView tv_quantity;
    }
}
