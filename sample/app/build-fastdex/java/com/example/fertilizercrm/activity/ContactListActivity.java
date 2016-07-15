package com.example.fertilizercrm.activity;

import android.os.Bundle;
import com.example.fertilizercrm.easemob.chatuidemo.activity.ContactlistFragment;
import com.example.fertilizercrm.R;
import com.example.fertilizercrm.basic.BaseActivity;

/**
 * 联系人列表
 */
public class ContactListActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);

        ContactlistFragment contactlistFragment = MainActivity.getInstance().getContactListFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.rl_container,contactlistFragment)
                .commit();
    }
}
