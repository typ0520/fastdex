package com.example.fertilizercrm.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.example.fertilizercrm.R;
import com.example.fertilizercrm.basic.BeanArrayListActivity;
import com.example.fertilizercrm.bean.Factory;
import com.example.fertilizercrm.http.Req;
import com.example.fertilizercrm.http.URLText;

import org.apache.http.Header;

import java.util.List;

import butterknife.Bind;
import com.example.fertilizercrm.common.utils.Logger;

/**
 * 厂家列表
 */
public class FactoryListActivity extends BeanArrayListActivity<FactoryListActivity.ViewHolder,Factory> {
    /**
     * 打开页面的类型
     */
    public static final String KEY_OPEN_TYPE = "open_type";
    public static final String DATA_SUPPLIER = "supplier";

    /**
     * 我的厂家列表
     */
    public static final int OPEN_TYPE_MY_FACTORY = 0;
    /**
     * 选择一个厂家并返回
     */
    public static final int OPEN_TYPE_GET_FACTORY = 1;

    private int openType;

    @Override
    protected int getCellLayoutResId() {
        return R.layout.list_item_factory_list;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTitleView().setTitle("厂家列表");
        openType = getIntent().getIntExtra(KEY_OPEN_TYPE,OPEN_TYPE_MY_FACTORY);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (openType == OPEN_TYPE_MY_FACTORY) {

                } else if (openType == OPEN_TYPE_GET_FACTORY) {
                    Factory factory = adapter.getItem(position);
                    Logger.d(">> Get product position: " + position + " factory: " + factory);
                    Intent data = new Intent();
                    data.putExtra(DATA_SUPPLIER, factory);
                    setResult(RESULT_OK, data);
                    finish();
                }
            }
        });
        loadData();
    }

    @Override
    protected void bindData(int position, ViewHolder holder, Factory factory) {
        holder.tv_name.setText(factory.getContacter());
        holder.tv_mobile.setText(factory.getMobile());
    }

    private void loadData() {
        new Req().url(URLText.suppliersDeal)
                .addParam("sign", "search")
                .addParam("start","1")
                .addParam("limit","10000").get(new DefaultCallback<List<Factory>>() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, List<Factory> response) {
                adapter.setData(response);
            }
        });
    }

    public static final class ViewHolder {
        @Bind(R.id.tv_name)     TextView tv_name;
        @Bind(R.id.tv_mobile)   TextView tv_mobile;
    }
}
