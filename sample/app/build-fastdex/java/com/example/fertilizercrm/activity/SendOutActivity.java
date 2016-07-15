package com.example.fertilizercrm.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.OnClick;
import com.example.fertilizercrm.common.utils.Logger;
import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.example.fertilizercrm.R;
import com.example.fertilizercrm.adapter.SendInAdapter;
import com.example.fertilizercrm.adapter.SendOutAdapter;
import com.example.fertilizercrm.basic.ListViewActivity;
import com.example.fertilizercrm.bean.PriceType;
import com.example.fertilizercrm.bean.RoleBean;
import com.example.fertilizercrm.bean.Product;
import com.example.fertilizercrm.http.Params;
import com.example.fertilizercrm.http.Req;
import com.example.fertilizercrm.view.QuantityPriceEditDialog;
import com.example.fertilizercrm.view.SendOutOrInHeadView;
import com.example.fertilizercrm.sdlv.Menu;
import com.example.fertilizercrm.sdlv.MenuItem;
import com.example.fertilizercrm.sdlv.SlideAndDragListView;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;

/**
 * 发货
 */
public class SendOutActivity extends ListViewActivity<SendOutAdapter> {
    /**
     * 选择产品
     */
    private static final int REQUEST_CODE_GET_PRODUCT = 1;
    /**
     * 选择客户
     */
    private static final int REQUEST_CODE_GET_CUSTOMER = 2;
    /**
     * 选择常用地址
     */
    private static final int REQUEST_CODE_COMMON_ADDRESS = 3;

    @Bind(R.id.tv_customer)    TextView tv_customer;
    @Bind(R.id.tv_quantity)    TextView tv_quantity;//数量
    @Bind(R.id.tv_total_sales) TextView tv_total_sales;//销售总金额
    @Bind(R.id.tv_total_should)TextView tv_total_should;//应收总计
    @Bind(R.id.head_view)      SendOutOrInHeadView head_view;

    private RoleBean roleBean;
    private Product selectAddressProduct;
    private int selectAddressAction;
    private String commonAddress;//常用地址第一条数据

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_send_out;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter.setOnItemClickListener(new SendOutAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, int action, Product product) {
                SendOutActivity.this.selectAddressProduct = product;
                SendOutActivity.this.selectAddressAction = action;
                startActivityForResult(new Intent(getActivity(), CommonAddressListActivity.class), REQUEST_CODE_COMMON_ADDRESS);
            }
        });
        head_view.setOnStatusChangeListener(new SendOutOrInHeadView.OnStatusChangeListener() {
            @Override
            public void onStatusChange(PriceType priceType) {
                List<Product> products = adapter.getData();
                if (products != null) {
                    for (Product product : products) {
                        product.setPriceType(priceType);
                    }
                    adapter.notifyDataSetChanged();
                }
            }
        });
        loadData();
    }

    private void loadData() {
        //加载常用地址
        new Req().url(Req.commonaddDeal)
                .addParam("sign","search")
                .addParam("start","1")
                .addParam("limit","1")
                .get(new DefaultCallback<JSONArray>() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                        JSONObject obj = response.optJSONObject(0);
                        if (obj != null) {
                            SendOutActivity.this.commonAddress = obj.optString("address");
                        }
                    }
                });
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
        menu.addItem(new MenuItem.Builder().setWidth((int) getResources().getDimension(R.dimen.send_out_item_width))
                .setBackground(new ColorDrawable(getResources().getColor(R.color.send_out_modify)))
                .setText("修改")
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
                    //删除 itemPosition
                    new AlertView("提示", "确定删除选中的产品吗?", null, new String[]{"确定", "取消"}, null, getActivity(),
                            AlertView.Style.Alert, new OnItemClickListener() {
                        @Override
                        public void onItemClick(Object o, int position) {
                            if (position == 0) {
                                adapter.delete(position);
                                notifyDataSetChanged();
                            }
                        }
                    }).show();
                } else if (buttonPosition == 1) {
                    //编辑 itemPosition
                    showEditInfoDialog(v, itemPosition);
                }
                return Menu.ITEM_SCROLL_BACK;
            }
        });
    }

    private void showEditInfoDialog(View v, final int itemPosition) {
        final Product product = adapter.getItem(itemPosition);
        QuantityPriceEditDialog quantityPriceEditDialog = new QuantityPriceEditDialog(this);
        quantityPriceEditDialog.setProductName(product.getPname());
        quantityPriceEditDialog.setQuantit(product.getNum() + "");
        quantityPriceEditDialog.setPrice(product.getSaleprice());
        quantityPriceEditDialog.setSendOutEventListener(new QuantityPriceEditDialog.SendOutEventListener() {
            @Override
            public boolean onDetermine(String quantit, String price) {
                Logger.d(">> position: " + itemPosition + " ,quantit: " + quantit + " ,price: " + price);

                int count = 1;
                try {
                    count = Integer.valueOf(quantit);
                } catch (Throwable e) {

                }
                if (count < 1) {
                    count = 1;
                }
                product.setNum(count);
                product.setSaleprice(price);

                notifyDataSetChanged();
                return false;
            }
        });
        //设置初始化数据
        quantityPriceEditDialog.show();
    }

    @Override
    protected boolean onBack() {
        if (head_view.isShowing()) {
            head_view.dismiss();
            return true;
        }
        return super.onBack();
    }

    //选择客户
    @OnClick(R.id.rl_customer) void selectCustomer() {
        Intent intent = new Intent(this,CustomerListActivity.class);
        intent.putExtra(CustomerListActivity.KEY_OPEN_TYPE, CustomerListActivity.OPEN_TYPE_GET_CUSTOMER);
        startActivityForResult(intent, REQUEST_CODE_GET_CUSTOMER);
    }

    //添加商品
    @OnClick(R.id.btn_add_product) void addProduct() {
        if (roleBean == null) {
            showLongToast("请先选择客户");
            return;
        }

        Intent intent = new Intent(this,ProductListActivity.class);
        intent.putExtra(ProductListActivity.KEY_OPEN_TYPE, ProductListActivity.OPEN_TYPE_GET_MY_PRODUCT);
        startActivityForResult(intent, REQUEST_CODE_GET_PRODUCT);
    }

    //进货
    @OnClick(R.id.btn_submit) void clickSubmit() {
        if (roleBean == null) {
            showLongToast("请先选择客户");
            return;
        }

        if (adapter.isEmpty()) {
            showLongToast("请选择产品");
            return;
        }

        //token(唯一标识)、userid(当前用户id)、usertype(当前用户类型)、sign(参数值固定为save)、tuserid (目标用户id)、tusertype (目标用户类型)、
        // pdate(进货日期)、ptypes(进货类别-0系统内进货、1系统外进货)、pricerole(价格条款-送到/自提-汉字)、countnumber(总数量)、counttype(总种类)、countweight(总重量)、
        // pid1(产品id) 、pnum1(产品数量)、outaddress1(出货地址)、inaddress1(到货地址)、
        // price1(公司定价)、
        // saleprice1(销售单价)、amount1(应收小计)、saleamount1(销售小计)、
        // amount(应收总计)、saleamount(销售总计)、incomeamount(实收金额)

        //token(唯一标识)、userid(当前用户id)、usertype(当前用户类型)、sign(参数值固定为save)、tuserid (目标用户id)、tusertype (目标用户类型)、
        // sdate(出货日期)、freight(单吨运杂成本)、pricerole(价格条款-送到/自提-汉字)、countnumber(总数量)、counttype(总种类)、countweight(总重量)、
        // pid1(产品id) 、pnum1(产品数量)、outaddress1(出货地址)、inaddress1(到货地址)、price1(公司定价)、saleprice1(销售单价)、amount1(应收小计)、saleamount1(销售小计)、
        // amount(应收总计)、saleamount(销售总计)、incomeamount(实收金额)

        Params params = new Params();
        params.put("sign","save");
        params.put("tuserid", roleBean.getBsoid());
        params.put("tusertype", roleBean.getRoleTypeStr());
        params.put("sdate",head_view.getDateString());

        params.put("freight",head_view.getFreight());//单吨运杂成本
        params.put("ptypes","0");

        params.put("pricerole",head_view.getPricerole());
        params.put("countnumber",adapter.getTotalCount());//总数量
        params.put("counttype", adapter.getData().size());
        params.put("countweight", adapter.getTotalCount());

        String incomeamount = head_view.getIncomeamount();
        BigDecimal bigDecimal = null;
        try {
            bigDecimal = new BigDecimal(incomeamount);
        } catch (Throwable e) {
            bigDecimal = BigDecimal.ZERO;
        }
        incomeamount = new DecimalFormat("0.0").format(bigDecimal);
        if (TextUtils.isEmpty(incomeamount) || "0.0".equals(incomeamount)) {
            showLongToast("请输入实收金额");
            return;
        }

        params.put("amount", adapter.getFormatTotalPrice());
        params.put("saleamount", adapter.getTotalSaleprice());
        params.put("incomeamount", incomeamount);

        List<Product> products = adapter.getData();
        for (int i = 0;i < products.size();i++) {
            int position = i + 1;
            Product product = products.get(i);
            params.put("pid" + position,product.getBsoid());
            params.put("pnum" + position,product.getNum());

            if (product.getOutaddress() != null) {
                params.put("outaddress" + position,product.getOutaddress());
            } else {
                showLongToast("请选择出货地址");
                return;
            }

            if (product.getInaddress() != null) {
                params.put("inaddress" + position,product.getInaddress());
            } else {
                showLongToast("请选择收货地址");
            }

            params.put("price" + position,product.getFormatPrice());
            params.put("saleprice" + position,product.getSaleprice());
            params.put("amount" + position, product.getFormatTotalPrice());
            params.put("saleamount" + position,product.getTotalSaleprice());
        }

        new Req().url(Req.salesDeal)
                .params(params)
                .get(new DefaultCallback<JSONObject>() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        showLongToast("恭喜您，出货操作已完成!");
                        finish();
                    }
                });
    }

    public void notifyDataSetChanged() {
        Logger.d(">>products: " + adapter.getData());

        adapter.notifyDataSetChanged();
        int totalCount = adapter.getTotalCount();
        tv_quantity.setText("数量： " + totalCount);

        String totalSellingAmount = adapter.getTotalSaleprice();
        tv_total_sales.setText("销售总计: " + totalSellingAmount);

        tv_total_should.setText("应收总计: " + adapter.getFormatTotalPrice());
        head_view.setIncomeamount(totalSellingAmount);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_GET_PRODUCT
                && resultCode == RESULT_OK) {
            Product product = (Product) data.getSerializableExtra(ProductListActivity.DATA_PRODUCT);
            onGetNewProduct(product);
            Product p = adapter.getProductByBsoid(product.getBsoid());
            if (p == null) {
                adapter.addData(product);
            }
            notifyDataSetChanged();
        }
        if (requestCode == REQUEST_CODE_GET_CUSTOMER
                && resultCode == RESULT_OK) {
            this.roleBean = (RoleBean)data.getSerializableExtra(CustomerListActivity.KEY_DATA);
            tv_customer.setText(roleBean.getContacter());

            Logger.d("<< roleBean: " + roleBean);
        }

        if (requestCode == REQUEST_CODE_COMMON_ADDRESS
                && resultCode == RESULT_OK) {
            String address = (String)data.getSerializableExtra(CommonAddressListActivity.DATA_ADDRESS);
            if (selectAddressProduct != null) {
                if (selectAddressAction == SendInAdapter.ACTION_SHIPPING_ADDRESS) {
                    selectAddressProduct.setOutaddress(address);
                } else {
                    selectAddressProduct.setInaddress(address);
                }
                adapter.notifyDataSetChanged();
            }
        }
    }

    private void onGetNewProduct(Product product) {
        if (product == null)return;
        if (this.commonAddress != null) {
            product.setInaddress(commonAddress);
            product.setOutaddress(commonAddress);
        }
        product.setSaleprice(product.getFormatPrice(head_view.getPriceType()));
    }
}
