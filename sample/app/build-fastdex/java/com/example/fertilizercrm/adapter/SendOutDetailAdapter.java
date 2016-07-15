package com.example.fertilizercrm.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.fertilizercrm.R;
import com.example.fertilizercrm.bean.SendOutOrder;
import com.example.fertilizercrm.utils.FerUtil;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import com.example.fertilizercrm.common.view.BeanArrayAdapter;

/**
 * Created by tong on 15/12/25.
 * 发货单详情
 */
public class SendOutDetailAdapter extends BeanArrayAdapter<SendOutOrder.ProductdataEntity> {
    public SendOutDetailAdapter(Context context) {
        super(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.list_item_send_out,null);
            ViewHolder holder = new ViewHolder();
            ButterKnife.bind(holder, convertView);
            convertView.setTag(holder);
        }
        bindData(position, (ViewHolder) convertView.getTag());
        return convertView;
    }

    private void bindData(final int position, ViewHolder holder) {
        SendOutOrder.ProductdataEntity entity = getItem(position);
        holder.tv_sub_label.setText(entity.getPname());
        holder.tv_quantity.setText(entity.getNum() + "吨");//总吨数
        holder.tv_selling_price.setText(entity.getSaleprice() + "");//售价
        holder.tv_inprice.setText(entity.getPrice() + "");//定价
        holder.tv_origin_price.setText(entity.getAmount() + "");//应收价格
        holder.tv_selling_amount.setText(entity.getSaleamount() + "");//销售金额
        holder.tv_shipping_address.setText(entity.getOutaddress());
        holder.tv_receiving_address.setText(entity.getInaddress());

        if (TextUtils.isEmpty(entity.getPicture1())) {
            holder.iv.setImageResource(R.drawable.default_img);
        } else {
            Picasso.with(mContext).load(FerUtil.getImageUrl(entity.getPicture1())).placeholder(R.drawable.default_img).into(holder.iv);
        }
    }

    static final class ViewHolder {
        @Bind(R.id.iv)
        ImageView iv;
        @Bind(R.id.tv_shipping_address)
        TextView tv_shipping_address;//出货地址
        @Bind(R.id.tv_receiving_address)
        TextView tv_receiving_address;//收货地址
        @Bind(R.id.tv_sub_label)
        TextView tv_sub_label;//化肥名称
        @Bind(R.id.tv_inprice)
        TextView tv_inprice;//定价
        @Bind(R.id.tv_selling_price)
        TextView tv_selling_price;//售价
        @Bind(R.id.tv_origin_price)
        TextView tv_origin_price;//应收定价
        @Bind(R.id.tv_selling_amount)
        TextView tv_selling_amount;//销售金额
        @Bind(R.id.tv_quantity)
        TextView tv_quantity;//总吨数
        @Bind(R.id.ll_shipping_address)
        View ll_shipping_address;//发货地址
        @Bind(R.id.ll_receive_address)
        View ll_receive_address;//收货地址
    }
}