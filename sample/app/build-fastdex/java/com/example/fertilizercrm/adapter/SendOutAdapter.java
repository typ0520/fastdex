package com.example.fertilizercrm.adapter;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import com.example.fertilizercrm.R;
import com.example.fertilizercrm.bean.PriceType;
import com.example.fertilizercrm.bean.Product;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;
import butterknife.Bind;
import com.example.fertilizercrm.common.view.SmartBeanArrayAdapter;

/**
 * Created by tong on 15/12/11.
 * 发货 adapter
 * @see com.example.fertilizercrm.activity.SendOutActivity
 */
public class SendOutAdapter extends SmartBeanArrayAdapter<Product> {
    /**
     * 出货地址选择动作
     */
    public static final int ACTION_SHIPPING_ADDRESS = 1;
    /**
     * 收货地址选择动作
     */
    public static final int ACTION_RECEIVING_ADDRESS = 2;

    DecimalFormat decimalFormat = new DecimalFormat("0.00");

    private PriceType priceType = PriceType.defaultType();

    private OnItemClickListener mOnItemClickListener;

    public SendOutAdapter(Context context) {
        super(context, R.layout.list_item_send_out, ViewHolder.class);
    }

    @Override
    public void bindData(final int position, Object h, Product product) {
        ViewHolder holder = (ViewHolder) h;
        holder.tv_sub_label.setText(product.getPname());
        holder.tv_inprice.setText(product.getFormatPrice() + "");//定价
        holder.tv_origin_price.setText(product.getFormatTotalPrice());//应收价格
        holder.tv_selling_price.setText(product.getSaleprice());//售价
        holder.tv_selling_amount.setText(product.getTotalSaleprice());//销售金额
        holder.tv_quantity.setText(product.getNum() + "吨");//总吨数

        if (product.getOutaddress() != null) {
            holder.tv_shipping_address.setText(product.getOutaddress());
        } else {
            holder.tv_shipping_address.setText("");
        }
        if (product.getInaddress() != null) {
            holder.tv_receiving_address.setText(product.getInaddress());
        } else {
            holder.tv_receiving_address.setText("");
        }
        holder.ll_shipping_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(position,ACTION_SHIPPING_ADDRESS,getItem(position));
                }
            }
        });
        holder.ll_receive_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(position, ACTION_RECEIVING_ADDRESS, getItem(position));
                }
            }
        });
    }

    public Product getProductByBsoid(int bsoid) {
        if (getData() != null) {
            for (Product product : getData()) {
                if (product.getBsoid() == bsoid) {
                    return product;
                }
            }
        }
        return null;
    }

    /**
     * 获取产品总数
     * @return
     */
    public int getTotalCount() {
        int totalCount = 0;
        List<Product> products = getData();
        if (products != null) {
            for (Product product : products) {
                totalCount += product.getNum();
            }
        }
        return totalCount;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    /**
     * 获取总的应收总计
     * @return
     */
    public String getFormatTotalPrice() {
        BigDecimal result = BigDecimal.ZERO;
        try {
            List<Product> products = getData();
            if (products != null) {
                for (Product product : products) {
                    if (product.getPrice() != null) {
                        result = result.add(product.getPrice());
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return decimalFormat.format(result);
    }

    /**
     * 获取总的销售总计
     * @return
     */
    public String getTotalSaleprice() {
        BigDecimal result = BigDecimal.ZERO;
        try {
            List<Product> products = getData();
            if (products != null) {
                for (Product product : products) {
                    BigDecimal amount = new BigDecimal(product.getTotalSaleprice());
                    result = result.add(amount);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return decimalFormat.format(result);
    }

    public static final class ViewHolder {
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

    public interface OnItemClickListener {
        void onItemClick(int position,int action,Product product);
    }
}
