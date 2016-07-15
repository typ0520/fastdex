package com.example.fertilizercrm.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.example.fertilizercrm.common.utils.Logger;

import com.example.fertilizercrm.R;
import com.example.fertilizercrm.basic.BaseActivity;
import com.example.fertilizercrm.bean.Product;
import com.example.fertilizercrm.http.Req;
import com.example.fertilizercrm.http.URLText;
import com.example.fertilizercrm.view.ProductInfoSwitchView;
import com.example.fertilizercrm.view.QuantityPriceEditDialog;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class ProductActivity extends BaseActivity {
    /**
     * 产品
     */
    public static final String KEY_PRODUCT = "product";

    @Bind(R.id.switch_view)  ProductInfoSwitchView switch_view;
    @Bind(R.id.ll_container) View ll_container;
    @Bind(R.id.tv_intro)     TextView tv_intro;//介绍

    @Bind(R.id.iv)           ImageView iv;//产品图片
    @Bind(R.id.tv_name)      TextView tv_name;//产品名字
    @Bind(R.id.tv_detail)    TextView tv_detail;//化肥详情
    @Bind(R.id.tv_group_name)TextView tv_group_name;//品牌
    @Bind(R.id.tv_factory)   TextView tv_factory;//厂家
    @Bind(R.id.tv_scale)     TextView tv_scale;//配比
    @Bind(R.id.tv_desc)      TextView tv_desc;//规格
    @Bind(R.id.tv_provider)  TextView tv_provider;//供货商
    @Bind(R.id.tv_inprice)   TextView tv_inprice;//进价
    @Bind(R.id.tv_outprice)  TextView tv_outprice;//送到售价
    @Bind(R.id.tv_outprice2) TextView tv_outprice2;//自提售价
    @Bind(R.id.tv_stock)     TextView tv_stock;//库存
    @Bind(R.id.rl_inprice)   View rl_inprice;

    private Product product;
    private DecimalFormat decimalFormat = new DecimalFormat("0.00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        product = (Product)getIntent().getSerializableExtra(KEY_PRODUCT);
        setContentView(R.layout.activity_product);
        ButterKnife.bind(this);

        if (currentRole().isSalesman()) {
            rl_inprice.setVisibility(View.GONE);
        }
        switch_view.setOnItemClickListener(new ProductInfoSwitchView.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (position == 0) {
                    ll_container.setVisibility(View.VISIBLE);
                    tv_intro.setVisibility(View.GONE);
                } else if (position == 1) {
                    ll_container.setVisibility(View.GONE);
                    tv_intro.setVisibility(View.VISIBLE);
                }
            }
        });
        getTitleView().setRightClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modifyProduct();
            }
        });

        tv_intro.setText("        " + product.getMemo());
        tv_name.setText(product.getPname());
        tv_detail.setText(product.getPtname() + "-" + product.getTname() + "-" + product.getCtname());
        tv_group_name.setText(product.getBrand());
        tv_factory.setText(product.getFactory());
        tv_scale.setText(product.getRatio());
        tv_desc.setText(product.getFormat());
        tv_provider.setText(product.getFactory());//TODO 暂时写成供货商
        tv_inprice.setText("￥" + decimalFormat.format(product.getInprice()));
        tv_outprice.setText("￥" + decimalFormat.format(product.getOutprice1()));
        tv_outprice2.setText("￥" + decimalFormat.format(product.getOutprice2()));
        tv_stock.setText(product.getStock() + "吨");
        Picasso.with(this).load(URLText.imgUrl + product.getPicture1()).placeholder(R.drawable.default_img).into(iv);
    }

    private void modifyProduct() {
        new Req().url(Req.productDeal)
                .addParam("sign","update")
                .addParam("pid",product.getBsoid())
                .addParam("stock",product.getStock())
                .addParam("outprice1",decimalFormat.format(product.getOutprice1()))
                .addParam("outprice2",decimalFormat.format(product.getOutprice2()))
                .get(new DefaultCallback<JSONObject>() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        setResult(RESULT_OK);
                        getTitleView().setRightText("");
                        showLongToast("产品信息修改成功!");
                    }
                });

    }

    @OnClick(R.id.rl_outprice) void modifyOutprice() {
        final String originPrice = decimalFormat.format(product.getOutprice1());
        //送到售价
        QuantityPriceEditDialog quantityPriceEditDialog = new QuantityPriceEditDialog(this);
        quantityPriceEditDialog.setMode(QuantityPriceEditDialog.MODE_PRICE);
        quantityPriceEditDialog.setProductName(product.getPname());
        quantityPriceEditDialog.setPrice(product.getOutprice1() + "");
        quantityPriceEditDialog.setPriceTitle("送到售价");
        quantityPriceEditDialog.setSendOutEventListener(new QuantityPriceEditDialog.SendOutEventListener() {
            @Override
            public boolean onDetermine(String quantit, String price) {
                Logger.d(">> price: " + price);
                BigDecimal decimal = new BigDecimal(price);
                String outprice1 = decimalFormat.format(decimal);
                if (!originPrice.equals(outprice1)) {
                    product.setOutprice1(decimal);
                    tv_outprice.setText("￥" + outprice1);
                    getTitleView().setRightText("保存");
                }
                return false;
            }
        });
        //设置初始化数据
        quantityPriceEditDialog.show();
    }

    @OnClick(R.id.rl_outprice2) void modifyOutprice2() {
        final String originPrice = decimalFormat.format(product.getOutprice2());
        //自提售价
        QuantityPriceEditDialog quantityPriceEditDialog = new QuantityPriceEditDialog(this);
        quantityPriceEditDialog.setMode(QuantityPriceEditDialog.MODE_PRICE);
        quantityPriceEditDialog.setProductName(product.getPname());
        quantityPriceEditDialog.setPrice(product.getOutprice2() + "");
        quantityPriceEditDialog.setPriceTitle("自提售价");
        quantityPriceEditDialog.setSendOutEventListener(new QuantityPriceEditDialog.SendOutEventListener() {
            @Override
            public boolean onDetermine(String quantit, String price) {
                Logger.d(">> price: " + price);
                BigDecimal decimal = new BigDecimal(price);
                String outprice2 = decimalFormat.format(decimal);
                if (!originPrice.equals(outprice2)) {
                    product.setOutprice2(decimal);
                    tv_outprice2.setText("￥" + outprice2);
                    getTitleView().setRightText("保存");
                }
                return false;
            }
        });
        //设置初始化数据
        quantityPriceEditDialog.show();
    }

    @OnClick(R.id.rl_stock) void modifyStock() {
        final int originStock = product.getStock();
        //修改库存
        QuantityPriceEditDialog quantityPriceEditDialog = new QuantityPriceEditDialog(this);
        quantityPriceEditDialog.setMode(QuantityPriceEditDialog.MODE_QUANTITY);
        quantityPriceEditDialog.setProductName(product.getPname());
        quantityPriceEditDialog.setQuantit(product.getStock());
        quantityPriceEditDialog.setSendOutEventListener(new QuantityPriceEditDialog.SendOutEventListener() {
            @Override
            public boolean onDetermine(String quantit, String price) {
                Logger.d(">> quantit: " + quantit);
                int stock = Integer.valueOf(quantit);
                if (originStock != stock) {
                    product.setStock(stock);
                    tv_stock.setText(product.getStock() + "吨");

                    getTitleView().setRightText("保存");
                }
                return false;
            }
        });
        //设置初始化数据
        quantityPriceEditDialog.show();
    }
}
