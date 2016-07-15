package com.example.fertilizercrm.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.example.fertilizercrm.R;
import com.example.fertilizercrm.adapter.ProductListAdapter;
import com.example.fertilizercrm.basic.BaseActivity;
import com.example.fertilizercrm.bean.Product;
import com.example.fertilizercrm.http.Callback;
import com.example.fertilizercrm.http.Params;
import com.example.fertilizercrm.http.Req;
import com.example.fertilizercrm.http.URLText;
import com.example.fertilizercrm.role.Role;
import com.example.fertilizercrm.view.ProductListHeadView;
import org.apache.http.Header;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.example.fertilizercrm.common.utils.Logger;

/**
 * 商品列表
 */
public class ProductListActivity extends BaseActivity {
    /**
     * 打开页面的类型
     */
    public static final String KEY_OPEN_TYPE = "open_type";

    /**
     * 上级用户类型
     */
    public static final String KEY_SUSERID = "suserid";

    /**
     * 上级用户类型
     */
    public static final String KEY_SUSERTYPE = "susertype";

    public static final String DATA_PRODUCT = "product";

    /**
     *  我的产品列表
     */
    public static final int OPEN_TYPE_MY_PRODUCT = 0;

    /**
     *  选择某个我的产品
     */
    public static final int OPEN_TYPE_GET_MY_PRODUCT = 1;

    /**
     *  选择上级代理商的某个商品
     */
    public static final int OPEN_TYPE_GET_AGENT_PRODUCT = 2;

    /**
     * 产品详情
     */
    private static final int REQUEST_CODE_DETAIL = 1;

    private int openType;
    private String suserid;
    private String susertype = Role.defaultRole().getCodeStr();

    @Bind(R.id.lv) ListView lv;
    @Bind(R.id.tv_empty) View tv_empty;
    private ProductListHeadView headView;
    private ProductListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);
        ButterKnife.bind(this);
        openType = getIntent().getIntExtra(KEY_OPEN_TYPE, 0);
        suserid = getIntent().getStringExtra(KEY_SUSERID);
        susertype = getIntent().getStringExtra(KEY_SUSERTYPE);

        adapter = new ProductListAdapter(this);

        if (openType == OPEN_TYPE_MY_PRODUCT
                || openType == OPEN_TYPE_GET_MY_PRODUCT) {
            headView = new ProductListHeadView(this);
            lv.addHeaderView(headView);
            lv.setHeaderDividersEnabled(false);
            headView.setOnEventListener(new ProductListHeadView.OnEventListener() {
                @Override
                public void onEvent(String keyword, String ptype, int ordertype) {
                    loadData();
                }
            });
        }

        lv.setEmptyView(tv_empty);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Product product = null;
                if (openType == OPEN_TYPE_MY_PRODUCT
                        || openType == OPEN_TYPE_GET_MY_PRODUCT) {
                    product = adapter.getItem(position - 1);
                } else if (openType == OPEN_TYPE_GET_AGENT_PRODUCT) {
                    product = adapter.getItem(position);
                }

                if (openType == OPEN_TYPE_MY_PRODUCT) {
                    Intent intent = new Intent(getActivity(), ProductActivity.class);
                    intent.putExtra(ProductActivity.KEY_PRODUCT, product);
                    startActivityForResult(intent, REQUEST_CODE_DETAIL);
                } else if (openType == OPEN_TYPE_GET_AGENT_PRODUCT
                        || openType == OPEN_TYPE_GET_MY_PRODUCT) {
                    Logger.d(">> Get product position: " + position + " openType: " + openType + " product: " + product);
                    Intent data = new Intent();
                    data.putExtra(KEY_OPEN_TYPE, openType);
                    data.putExtra(DATA_PRODUCT, product);
                    setResult(RESULT_OK, data);
                    finish();
                }
            }
        });
        loadData();
    }

    private void loadData() {
        loadPtype();

        Params params = new Params();
        params.put("start", "1");
        params.put("limit", "100");
        if (openType == OPEN_TYPE_MY_PRODUCT
                || openType == OPEN_TYPE_GET_MY_PRODUCT) {
            params.put("sign", "search");
            if (!TextUtils.isEmpty(headView.getKeyword())) {
                params.put("pname",headView.getKeyword());
            }
            if (!(TextUtils.isEmpty(headView.getPtype())
                    || headView.getPtype().equals("全部"))) {
                params.put("ptype",headView.getPtype());
            }
            params.put("ordertype", headView.getOrdertype());
        }
        else if (openType == OPEN_TYPE_GET_AGENT_PRODUCT) {
            params.put("sign", "searchsuper");
            params.put("suserid", suserid);
            params.put("susertype", susertype);
        }
        new Req().url(URLText.productDeal)
                .params(params)
                .get(new DefaultCallback<List<Product>>() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, List<Product> response) {
                        adapter.setData(response);
                    }
                });
    }

    private void loadPtype() {
        if (headView == null) {
            return;
        }
        if (headView.getPtnameList() != null) {
            return;
        }
        new Req().url(Req.productDeal)
                .addParam("sign","searchtype")
                .addParam("start", "1")
                .addParam("limit", "100")
                .get(new Callback<JSONArray>() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                        List<String> ptnameList = new ArrayList<String>();
                        ptnameList.add("全部");
                        for (int i = 0;i < response.length();i++) {
                            ptnameList.add(response.optJSONObject(i).optString("ptname"));
                        }
                        headView.setPtnameList(ptnameList);
                    }
                });
    }

    @Override
    protected boolean onBack() {
        if (headView != null
                && headView.getAlertView() != null
                && headView.getAlertView().isShowing()) {
            headView.getAlertView().dismiss();
            return true;
        }
        return super.onBack();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_DETAIL
                && resultCode == RESULT_OK) {
            loadData();
        }
    }
}
