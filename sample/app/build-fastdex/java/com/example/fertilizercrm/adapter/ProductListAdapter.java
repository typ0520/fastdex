package com.example.fertilizercrm.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.fertilizercrm.R;
import com.example.fertilizercrm.bean.Product;
import com.example.fertilizercrm.http.URLText;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import com.example.fertilizercrm.common.view.SmartBeanArrayAdapter;

/**
 * Created by tong on 15/12/11.
 * 产品列表adapter
 * @see com.example.fertilizercrm.activity.ProductListActivity
 */
public class ProductListAdapter extends SmartBeanArrayAdapter<Product> {

    public ProductListAdapter(Context context) {
        super(context, R.layout.list_item_product_list, ViewHolder.class);
    }

    @Override
    public void bindData(int position, Object h, Product obj) {
        ViewHolder holder = (ViewHolder) h;
        Product product = getItem(position);
        holder.tv_name.setText(product.getPname());
        holder.tv_quantity.setText(product.getStock() + "吨");
        holder.tv_weight.setText(product.getFormat());
        holder.tv_desc.setText(product.getPtname() + "-" + product.getTname() + "-" + product.getCtname());
        holder.tv_detail.setText(product.getRatio());

        if (TextUtils.isEmpty(product.getPicture1())) {
            holder.iv.setImageResource(R.drawable.default_img);
        } else {
            Picasso.with(mContext).load(URLText.imgUrl + product.getPicture1()).placeholder(R.drawable.default_img).into(holder.iv);
        }
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

    public static final class ViewHolder {
        @Bind(R.id.iv)          ImageView iv;
        @Bind(R.id.tv_name)     TextView tv_name;
        @Bind(R.id.tv_weight)   TextView tv_weight;
        @Bind(R.id.tv_desc)     TextView tv_desc;
        @Bind(R.id.tv_detail)   TextView tv_detail;
        @Bind(R.id.tv_quantity) TextView tv_quantity;
    }
}
