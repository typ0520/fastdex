package com.example.fertilizercrm.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.fertilizercrm.R;
import com.example.fertilizercrm.fragment.MeFragment;

import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by tong on 16/2/20.
 */
public class MeAdapter extends BaseAdapter {
    private Context context;
    private List<? extends Map<String, ?>> dataList;
    private int msgCount;

    public MeAdapter(Context context) {
        this.context = context;
    }

    public List<? extends Map<String, ?>> getDataList() {
        return dataList;
    }

    public void setDataList(List<? extends Map<String, ?>> dataList) {
        this.dataList = dataList;
    }

    public int getMsgCount() {
        return msgCount;
    }

    public void setMsgCount(int msgCount) {
        this.msgCount = msgCount;

        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return dataList != null ? dataList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return dataList != null ? dataList.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(context,R.layout.list_item_setting,null);
            ViewHolder holder = new ViewHolder();
            ButterKnife.bind(holder,convertView);
            convertView.setTag(holder);
        }

        ViewHolder holder = (ViewHolder) convertView.getTag();
        bindData(position,holder);
        return convertView;
    }

    private void bindData(int position, ViewHolder holder) {
        Map<String,?> map = (Map<String, ?>) getItem(position);
        int resId = (Integer) map.get(MeFragment.KEY_ICON);
        String text = (String) map.get(MeFragment.KEY_TEXT);

        holder.iv.setImageResource(resId);
        holder.tv.setText(text);

        if (position == 0 && msgCount > 0) {
            holder.tv_unread_address_number.setText(String.valueOf(msgCount));
            holder.tv_unread_address_number.setVisibility(View.VISIBLE);
        }
        else {
            holder.tv_unread_address_number.setVisibility(View.INVISIBLE);
        }
    }

    public static class ViewHolder {
        @Bind(R.id.iv)
        ImageView iv;

        @Bind(R.id.tv)
        TextView tv;

        @Bind(R.id.tv_unread_address_number)
        TextView tv_unread_address_number;
    }
}
