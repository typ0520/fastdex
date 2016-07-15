package com.example.fertilizercrm.adapter;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.fertilizercrm.R;
import com.example.fertilizercrm.bean.RoleBean;
import java.util.List;
import butterknife.Bind;
import com.example.fertilizercrm.common.utils.Logger;
import com.example.fertilizercrm.common.view.SmartBeanArrayAdapter;

/**
 * Created by tong on 15/12/11.
 */
public class LinkmanAdapter extends SmartBeanArrayAdapter {
    private List selectedList;

    public LinkmanAdapter(Context context) {
        super(context,R.layout.list_item_linkman,ViewHolder.class);
    }

    public void setSelectedList(List selectedList) {
        this.selectedList = selectedList;
    }

    //是否是选择的一项
    public boolean isSelectedItem(Object obj) {
        for (Object item : selectedList) {
            if (item == obj) {
                return true;
            }
        }
        return false;
    }

    //选择或者取消一项
    public void selectOrUnselectItem(int position) {
        Logger.d(">> selectOrUnselectItem: " + position);
        Object obj = getItem(position);
        if (isSelectedItem(obj)) {
            selectedList.remove(obj);
        } else {
            selectedList.add(obj);
        }

        notifyDataSetChanged();
    }

    //全选
    public void selectAllItem() {
        selectedList.clear();
        selectedList.addAll(this.data);
        notifyDataSetChanged();
    }

    //反全选
    public void unselectAllItem() {
        selectedList.clear();
        notifyDataSetChanged();
    }

    @Override
    public void bindData(int position, Object h, Object obj) {
        ViewHolder holder = (ViewHolder) h;
        if (isSelectedItem(obj)) {
            holder.tv_name.setTextColor(mContext.getResources().getColor(R.color.colorPrimaryDark));
            holder.tv_mobile.setTextColor(mContext.getResources().getColor(R.color.colorPrimaryDark));
            holder.iv.setVisibility(View.VISIBLE);
        } else {
            holder.tv_name.setTextColor(mContext.getResources().getColor(R.color.black));
            holder.tv_mobile.setTextColor(mContext.getResources().getColor(R.color.linkman_gray));
            holder.iv.setVisibility(View.INVISIBLE);
        }

        RoleBean roleBean = (RoleBean) obj;
        holder.tv_name.setText(roleBean.getCompany());
        holder.tv_mobile.setText(roleBean.getMobile());
    }

    public static class ViewHolder {
        @Bind(R.id.tv_name)   TextView tv_name;
        @Bind(R.id.tv_mobile) TextView tv_mobile;
        @Bind(R.id.iv)        ImageView iv;
    }
}
