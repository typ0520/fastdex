package com.example.fertilizercrm.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import com.example.fertilizercrm.R;
import com.example.fertilizercrm.basic.JSONArrayListActivity;
import com.example.fertilizercrm.bean.AreaInfo;
import com.example.fertilizercrm.http.Req;
import com.example.fertilizercrm.http.URLText;
import com.google.gson.Gson;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Stack;

import butterknife.Bind;
import com.example.fertilizercrm.common.utils.Logger;

/**
 * 地区选择
 */
public class AreaActivity extends JSONArrayListActivity<AreaActivity.ViewHolder> {
    public static final String EXTRA_KEY_LABEL = "label";
    public static TextView target;
    public static final String KEY_AREA_INFO = "area_info";
    private static final String CACHE_FILE = "area_file";
    private JSONArray areaInfos;
    Stack<JSONObject> areaStack;
    private Handler handler = new Handler();

    @Override
    protected int getCellLayoutResId() {
        return R.layout.list_item_area;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String label = getIntent().getStringExtra(EXTRA_KEY_LABEL);
        if (label == null) {
            label = "地区选择";
        }
        label = label.trim();

        getTitleView().setTitle(label);
        getTitleView().setRightClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        areaStack = new Stack<JSONObject>();
        areaStack.push(new JSONObject() {{
            try {
                put("parentno", 0);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }});
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                JSONObject obj = adapter.getItem(position);
                areaStack.push(obj);

                if (areaStack.size() < 4) {
                    JSONArray result = findByParentId(obj.optInt("areano"));
                    if (areaStack.size() == 3 && (result == null || result.length() == 0)) {
                        //直辖市
                        callback(false);
                    } else {
                        adapter.setData(result);
                    }
                } else {
                    callback(true);
                }
                Logger.d("<<<<栈数据: " + areaStack.size() + " ," + areaStack.toString());
            }
        });
        loadData();
    }

    private void callback(boolean commonProvince) {
        AreaInfo areaInfo = new AreaInfo();
        if (commonProvince) {
            JSONObject province = areaStack.get(1);
            JSONObject city = areaStack.get(2);
            areaInfo.setProvinceId(province.optInt("areano"));
            areaInfo.setProvinceName(province.optString("areaname"));
            areaInfo.setCityId(city.optInt("areano"));
            areaInfo.setCityName(city.optString("areaname"));

            JSONObject area = areaStack.get(3);
            areaInfo.setAreaId(area.optInt("areano"));
            areaInfo.setAreaName(area.optString("areaname"));

            if (target != null) {
                target.setText(areaInfo.getProvinceName() + "-" + areaInfo.getCityName() + "-" + areaInfo.getAreaName());
            }
        }
        else {
            JSONObject province = areaStack.get(1);
            JSONObject city = areaStack.get(2);
            areaInfo.setProvinceId(province.optInt("areano"));
            areaInfo.setProvinceName(province.optString("areaname"));
            areaInfo.setCityId(province.optInt("areano"));
            areaInfo.setCityName(province.optString("areaname"));
            areaInfo.setAreaId(city.optInt("areano"));
            areaInfo.setAreaName(city.optString("areaname"));

            if (target != null) {
                //target.setText(areaInfo.getProvinceName() + "-" + areaInfo.getCityName());
                target.setText(areaInfo.getProvinceName() + "-" + areaInfo.getCityName() + "-" + areaInfo.getAreaName());
            }
        }

        Logger.d("<<<选择完毕: " + new Gson().toJson(areaInfo));
        Intent intent = new Intent();
        intent.putExtra(KEY_AREA_INFO, areaInfo);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void bindData(int position, ViewHolder holder, JSONObject obj) {
        holder.tv.setText(obj.optString("areaname"));
    }

    @Override
    protected boolean onBack() {
        Logger.d("<<<退出: " + areaStack.toString());
        if (areaStack.isEmpty() || areaStack.size() == 1) {
            finish();
        } else {
            JSONObject obj = areaStack.pop();
            loadData(obj.optInt("parentno"));
        }
        return true;
    }

    private void loadData() {
        showProgress();
        loadFromCache(new Runnable() {
            @Override
            public void run() {
                if (areaInfos == null || areaInfos.length() == 0) {
                    new Req().url(URLText.areaUrl).addParam("sign", "search").get(new DefaultCallback<JSONArray>() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                            areaInfos = response;
                            adapter.setData(findByParentId(0));

                            //不为空在缓存
                            if (response.length() > 0) {
                                try {
                                    FileOutputStream fos = new FileOutputStream(new File(getCacheDir(), CACHE_FILE));
                                    fos.write(response.toString().getBytes());
                                    fos.flush();
                                    fos.close();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                } else {
                    hideProgress();
                    adapter.setData(findByParentId(0));
                }
            }
        });
    }

    private void loadData(int parentno) {
        showProgress();
        adapter.setData(findByParentId(parentno));
        hideProgress();
    }

    private JSONArray findByParentId(int parentId) {
        JSONArray result = new JSONArray();
        Logger.d("<<<findByParentId  parentno: " + parentId);
        for (int i = 0;i < areaInfos.length();i++) {
            JSONObject obj = areaInfos.optJSONObject(i);
            if (obj.optInt("parentno") == parentId) {
                result.put(obj);
            }
        }
        Logger.d("<<<结果>>> result: " + result.toString());
        return result;
    }

    private void loadFromCache(final Runnable runnable) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File file = new File(getCacheDir(),CACHE_FILE);
                    if (!file.exists() || !file.isFile()) {
                        return;
                    }
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    FileInputStream fos = new FileInputStream(file);
                    int len = -1;
                    byte[] buffer = new byte[1024];
                    while ((len = fos.read(buffer)) != -1) {
                        bos.write(buffer,0,len);
                    }
                    fos.close();
                    String json = new String(bos.toByteArray());
                    Logger.d(">>>存在缓存数据: " + json);
                    if (!TextUtils.isEmpty(json)) {
                        JSONArray array = new JSONArray(json);
                        areaInfos = array;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    handler.post(runnable);
                }
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        target = null;
    }

    public static Intent createIntent(Context context,String label,TextView target) {
        Intent intent = new Intent(context,AreaActivity.class);
        intent.putExtra(EXTRA_KEY_LABEL,label);
        AreaActivity.target = target;
        return intent;
    }

    public static class ViewHolder {
        @Bind(R.id.tv) TextView tv;
    }
}
