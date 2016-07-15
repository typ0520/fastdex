package com.example.fertilizercrm.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.fertilizercrm.common.utils.Logger;
import com.example.fertilizercrm.R;
import com.example.fertilizercrm.adapter.ProductListAdapter;
import com.example.fertilizercrm.basic.ListViewActivity;
import com.example.fertilizercrm.bean.Product;
import com.example.fertilizercrm.http.Req;
import org.apache.http.Header;
import org.json.JSONObject;
import java.util.List;

/**
 * 业务员分配的产品列表
 */
public class SalesmanProductListActivity extends ListViewActivity<ProductListAdapter> {
    //选择产品
    private static final int REQUEST_CODE_ADD_PRODUCT = 1;
    /**
     * 业务员id
     */
    public static final String KEY_BSOID = "bsoid";

    private String bsoid;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_salesman_product_list;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bsoid = getIntent().getStringExtra(KEY_BSOID);
        Logger.d("<<业务员Id: " + bsoid);

        getTitleView().setRightClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),ProductListActivity.class);
                intent.putExtra(KEY_OPEN_TYPE, ProductListActivity.OPEN_TYPE_GET_MY_PRODUCT);
                startActivityForResult(intent,REQUEST_CODE_ADD_PRODUCT);
            }
        });
        loadData();
    }

    private void loadData() {
        new Req().url(Req.salespersonDeal)
                .addParam("sign", "searchproduct")
                .addParam("start","1")
                .addParam("limit","100")
                .addParam("bsoid",bsoid)
                .get(new DefaultCallback<List<Product>>() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, List<Product> response) {
                        adapter.setData(response);
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_ADD_PRODUCT
                && resultCode == RESULT_OK) {
            Product product = (Product) data.getSerializableExtra(ProductListActivity.DATA_PRODUCT);
            Product p = adapter.getProductByBsoid(product.getBsoid());
            if (p == null) {
                assignProduct(product);
            }
        }
    }

    //分配产品
    private void assignProduct(Product product) {
        new Req().url(Req.salespersonDeal)
                .addParam("sign", "saveproduct")
                .addParam("bsoid",bsoid)
                .addParam("count",1)
                .addParam("pid1",product.getBsoid())
                .get(new DefaultCallback<JSONObject>() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        loadData();
                    }
                });
    }
}
