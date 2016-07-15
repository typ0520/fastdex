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
import com.example.fertilizercrm.basic.ListViewActivity;
import com.example.fertilizercrm.bean.Agent;
import com.example.fertilizercrm.bean.Factory;
import com.example.fertilizercrm.bean.OutsideSystemChannel;
import com.example.fertilizercrm.bean.PriceType;
import com.example.fertilizercrm.bean.Product;
import com.example.fertilizercrm.bean.RoleBean;
import com.example.fertilizercrm.http.Params;
import com.example.fertilizercrm.http.Req;
import com.example.fertilizercrm.http.URLText;
import com.example.fertilizercrm.role.Role;
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
 * 进货
 */
public class SendInActivity extends ListViewActivity<SendInAdapter> {
    /**
     * 选择产品
     */
    private static final int REQUEST_CODE_GET_PRODUCT = 1;

    /**
     * 选择厂家
     */
    private static final int REQUEST_CODE_GET_FACTORY = 2;

    /**
     * 选择批发商
     */
    private static final int REQUEST_CODE_GET_AGENT = 3;

    /**
     * 选择常用地址
     */
    private static final int REQUEST_CODE_COMMON_ADDRESS = 4;

    /**
     * 选择系统外进货
     */
    private static final int REQUEST_CODE_OUTSIDE_SYSTEM_CHANNEL = 5;

    /**
     * 系统外进货
     */
    public static final int OPEN_TYPE_CHANNEL_SYSTEM_OUT = 1;

    @Bind(R.id.tv_customer)    TextView tv_customer;
    @Bind(R.id.tv_quantity)    TextView tv_quantity;//数量
    @Bind(R.id.tv_total_sales) TextView tv_total_sales;//销售总计
    @Bind(R.id.tv_total_should)TextView tv_total_should;//应收总计
    @Bind(R.id.head_view)      SendOutOrInHeadView head_view;

    private RoleBean supplier;
    private Product selectAddressProduct;
    private int selectAddressAction;
    private String commonAddress;//常用地址第一条数据
    private AlertView alertView;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_send_in;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter.setOnItemClickListener(new SendInAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, int action, Product product) {
                SendInActivity.this.selectAddressProduct = product;
                SendInActivity.this.selectAddressAction = action;
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
        new Req().url(Req.commonaddDeal)
                .addParam("sign","search")
                .addParam("start","1")
                .addParam("limit","1")
                .get(new DefaultCallback<JSONArray>() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                        JSONObject obj = response.optJSONObject(0);
                        if (obj != null) {
                            SendInActivity.this.commonAddress = obj.optString("address");
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
        quantityPriceEditDialog.setQuantit(product.getNum());
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
        if (alertView != null && alertView.isShowing()) {
            alertView.dismiss();
            return true;
        }
        return super.onBack();
    }

    //选择厂家
    @OnClick(R.id.rl_customer) void selectFactory() {
        if (openType == OPEN_TYPE_DEFAULT) {
            if (currentRole() == Role.erji) {
                //零售商从厂家或批发商进货
                final String[] strings = new String[]{"厂家", "批发商"};
                alertView = new AlertView("请选择供货商", null, null, null, strings, getActivity(), AlertView.Style.Alert, new OnItemClickListener() {
                    @Override
                    public void onItemClick(Object o, int position) {
                        if (position == 0) {
                            //选择厂商
                            Intent intent = new Intent(getActivity(),FactoryListActivity.class);
                            intent.putExtra(FactoryListActivity.KEY_OPEN_TYPE, FactoryListActivity.OPEN_TYPE_GET_FACTORY);
                            startActivityForResult(intent, REQUEST_CODE_GET_FACTORY);
                        }
                        else if (position == 1) {
                            //选择批发商
                            Intent intent = new Intent(getActivity(),AgentListActivity.class);
                            startActivityForResult(intent,REQUEST_CODE_GET_AGENT);
                        }
                    }
                });
                alertView.show();
            }
            else {
                //批发商选择厂商进货
                Intent intent = new Intent(getActivity(),FactoryListActivity.class);
                intent.putExtra(FactoryListActivity.KEY_OPEN_TYPE, FactoryListActivity.OPEN_TYPE_GET_FACTORY);
                startActivityForResult(intent, REQUEST_CODE_GET_FACTORY);
            }
        }
        else if (openType == OPEN_TYPE_CHANNEL_SYSTEM_OUT) {
            //选择系统外进货渠道
            Intent intent = new Intent(getActivity(),OutsideSystemChannelActivity.class);
            startActivityForResult(intent, REQUEST_CODE_OUTSIDE_SYSTEM_CHANNEL);
        }
    }

    //添加商品
    @OnClick(R.id.btn_add_product) void addProduct() {
        if (openType == OPEN_TYPE_DEFAULT) {
            if (supplier == null) {
                showLongToast("请先选择供货商");
                return;
            }

            Intent intent = new Intent(this,ProductListActivity.class);
            intent.putExtra(ProductListActivity.KEY_OPEN_TYPE, ProductListActivity.OPEN_TYPE_GET_AGENT_PRODUCT);
            intent.putExtra(ProductListActivity.KEY_SUSERID, supplier.getBsoid() + "");
            intent.putExtra(ProductListActivity.KEY_SUSERTYPE, supplier.getRoleTypeStr());
            startActivityForResult(intent, REQUEST_CODE_GET_PRODUCT);
        }
        else if (openType == OPEN_TYPE_CHANNEL_SYSTEM_OUT) {
            //系统外进货从我的产品列表中选择
            Intent intent = new Intent(this,ProductListActivity.class);
            intent.putExtra(ProductListActivity.KEY_OPEN_TYPE, ProductListActivity.OPEN_TYPE_GET_MY_PRODUCT);
            startActivityForResult(intent, REQUEST_CODE_GET_PRODUCT);
        }
    }

    //进货
    @OnClick(R.id.btn_submit) void clickSubmit() {
        if (supplier == null) {
            showLongToast("请先选择供货商");
            return;
        }

        if (adapter.isEmpty()) {
            showLongToast("请选择产品");
            return;
        }

        if (openType == OPEN_TYPE_CHANNEL_SYSTEM_OUT) {
            outsideSendin();
            return;
        }

        //token(唯一标识)、userid(当前用户id)、usertype(当前用户类型)、sign(参数值固定为save)、tuserid (目标用户id)、tusertype (目标用户类型)、
        // pdate(进货日期)、ptypes(进货类别-0系统内进货、1系统外进货)、pricerole(价格条款-送到/自提-汉字)、countnumber(总数量)、counttype(总种类)、countweight(总重量)、
        // pid1(产品id) 、pnum1(产品数量)、outaddress1(出货地址)、inaddress1(到货地址)、
        // price1(公司定价)、
        // saleprice1(销售单价)、amount1(应收小计)、saleamount1(销售小计)、
        // amount(应收总计)、saleamount(销售总计)、incomeamount(实收金额)

        Params params = new Params();
        params.put("sign","save");
        params.put("tuserid", supplier.getBsoid() + "");
        if (supplier instanceof Agent) {
            params.put("tusertype", Role.yiji.getCode() + "");
        }
        else {
            params.put("tusertype", Role.factory.getCode() + "");
        }
        params.put("pdate",head_view.getDateString());
        params.put("ptypes","0");

        params.put("pricerole",head_view.getPricerole());
        params.put("countnumber",adapter.getTotalCount() + "");//总数量
        params.put("counttype",adapter.getData().size() + "");
        params.put("countweight", adapter.getTotalCount() + "");

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
            params.put("pid" + position,product.getBsoid() + "");
            params.put("pnum" + position,product.getNum() + "");

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

        new Req().url(URLText.purchaseDeal)
                .params(params)
                .get(new DefaultCallback<JSONObject>() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        showLongToast("恭喜您，进货操作已完成!");
                        finish();
                    }
                });
    }

    private void outsideSendin() {
        //token(唯一标识)、userid(当前用户id)、usertype(当前用户类型)、sign(参数值固定为save)、
        // tuserid (目标用户id)、name (进货渠道名称) 、contacter (进货渠道联系人) 、mobile (进货渠道联系电话)、
        // pdate(进货日期)、ptypes(进货类别-0系统内进货、1系统外进货)、pricerole(价格条款-送到/自提-汉字)
        // 、countnumber(总数量)、counttype(总种类)、countweight(总重量)、pid1(产品id) 、pnum1(产品数量)
        // 、outaddress1(出货地址)、inaddress1(到货地址)、price1(公司定价)、saleprice1(销售单价)、
        // amount1(应收小计)、saleamount1(销售小计)、amount(应收总计)、saleamount(销售总计)
        // 、incomeamount(实收金额)


        // pdate(进货日期)、ptypes(进货类别-0系统内进货、1系统外进货)、pricerole(价格条款-送到/自提-汉字)
        // 、countnumber(总数量)、counttype(总种类)、countweight(总重量)、pid1(产品id) 、pnum1(产品数量)
        // 、outaddress1(出货地址)、inaddress1(到货地址)、price1(公司定价)、saleprice1(销售单价)、
        // amount1(应收小计)、saleamount1(销售小计)、amount(应收总计)、saleamount(销售总计)
        // 、incomeamount(实收金额)


        //系统外进货
        OutsideSystemChannel channel = (OutsideSystemChannel) supplier;
        Params params = new Params();
        params.put("sign","saveoutside");
        if (channel.isSelect()) {
            params.put("tuserid",channel.getBsoid());
        }
        else {
            params.put("name",channel.getName());
            params.put("contacter",channel.getContacter());
            params.put("mobile",channel.getMobile());
        }
        params.put("pdate",head_view.getDateString());
        params.put("ptypes","1");

        params.put("pricerole",head_view.getPricerole());
        params.put("countnumber",adapter.getTotalCount() + "");//总数量
        params.put("counttype",adapter.getData().size() + "");
        params.put("countweight", adapter.getTotalCount() + "");

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
            params.put("pid" + position,product.getBsoid() + "");
            params.put("pnum" + position,product.getNum() + "");

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

        new Req().url(URLText.purchaseDeal)
                .params(params)
                .get(new DefaultCallback<JSONObject>() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        showLongToast("系统外进货操作已完成！");
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

        if (requestCode == REQUEST_CODE_GET_FACTORY
                && resultCode == RESULT_OK) {
            this.supplier = (Factory)data.getSerializableExtra(FactoryListActivity.DATA_SUPPLIER);
            tv_customer.setText(supplier.getContacter());

            Logger.d("<< 厂家 supplier: " + supplier);
        }

        if (requestCode == REQUEST_CODE_OUTSIDE_SYSTEM_CHANNEL
                && resultCode == RESULT_OK) {
            this.supplier = (OutsideSystemChannel)data.getSerializableExtra(KEY_DATA);
            tv_customer.setText(supplier.getContacter());
            Logger.d("<< 系统外进货渠道 supplier: " + supplier);
        }

        if (requestCode == REQUEST_CODE_GET_AGENT
                && resultCode == RESULT_OK) {
            this.supplier = (RoleBean)data.getSerializableExtra(AgentListActivity.DATA_AGENT);

            Agent agent = (Agent) supplier;
            tv_customer.setText(agent.getAname());

            Logger.d("<< 批发商 supplier: " + supplier);
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
