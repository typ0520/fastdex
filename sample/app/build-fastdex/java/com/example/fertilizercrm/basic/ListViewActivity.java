package com.example.fertilizercrm.basic;

import android.content.Context;
import android.os.Bundle;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import java.lang.reflect.Type;
import butterknife.ButterKnife;
import com.example.fertilizercrm.common.utils.ReflectionUtil;
import com.example.fertilizercrm.R;
import com.example.fertilizercrm.sdlv.SlideAndDragListView;

/**
 * listview activity
 */
public class ListViewActivity<ADAPTER extends BaseAdapter> extends BaseActivity {
    protected SlideAndDragListView slv;
    protected ListView lv;
    protected TextView tv_empty;

    protected ADAPTER adapter;
    protected Type adapterType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapterType = ReflectionUtil.getGenericFirstType(getClass());
        setContentView(getLayoutResId());
        lv = (ListView) findViewById(R.id.lv);
        if (lv != null && (lv instanceof SlideAndDragListView)) {
            slv = (SlideAndDragListView) lv;
        }
        onListViewInit();
        tv_empty = (TextView) findViewById(R.id.tv_empty);

        ButterKnife.bind(this);
        adapter = initAdapter();
        lv.setAdapter(adapter);
        if (tv_empty != null) {
            lv.setEmptyView(tv_empty);
        }
    }

    protected int getLayoutResId() {
        return R.layout.activity_listview;
    }

    protected void onListViewInit() {

    }

    protected ADAPTER initAdapter() {
        try {
            Class<ADAPTER> clazz = (Class<ADAPTER>)adapterType;
            return clazz.getConstructor(Context.class).newInstance(this);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * 设置没有数据时显示的提示信息
     * @param emptyText
     */
    public void setEmptyText(String emptyText) {
        if (tv_empty != null) {
            tv_empty.setText(emptyText);
        }
    }
}
