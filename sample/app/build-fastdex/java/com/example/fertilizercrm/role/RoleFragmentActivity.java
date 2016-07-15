package com.example.fertilizercrm.role;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.example.fertilizercrm.R;
import com.example.fertilizercrm.basic.BaseActivity;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by tong on 15/12/7.
 */
public abstract class RoleFragmentActivity extends BaseActivity {
    private Map<Role,Class<? extends RoleFragment>> map = new HashMap<Role,Class<? extends RoleFragment>>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout layout = new FrameLayout(this);
        layout.setId(R.id.fl_content);
        layout.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(layout);
    }

    /**
     * 添加支持的fragment
     * @param role
     * @param clazz
     */
    public RoleFragmentActivity addFragment(Role role,Class<? extends RoleFragment> clazz) {
        map.put(role, clazz);
        return this;
    }

    public RoleFragmentActivity loadFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fl_content,getRoleFragment(Role.currentRole())).commit();
        return this;
    }

    public RoleFragment getRoleFragment(Role role) {
        Class<? extends RoleFragment> clazz = map.get(role);
        try {
            return clazz.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
