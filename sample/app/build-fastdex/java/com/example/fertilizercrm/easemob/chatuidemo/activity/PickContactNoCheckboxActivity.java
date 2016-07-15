/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.fertilizercrm.easemob.chatuidemo.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.example.fertilizercrm.easemob.applib.controller.HXSDKHelper;
import com.example.fertilizercrm.easemob.chatuidemo.Constant;
import com.example.fertilizercrm.easemob.chatuidemo.DemoHXSDKHelper;
import com.example.fertilizercrm.R;
import com.example.fertilizercrm.easemob.chatuidemo.adapter.ContactAdapter;
import com.example.fertilizercrm.easemob.chatuidemo.domain.User;
import com.example.fertilizercrm.easemob.chatuidemo.widget.Sidebar;

public class PickContactNoCheckboxActivity extends BaseActivity {

	private ListView listView;
	private Sidebar sidebar;
	protected ContactAdapter contactAdapter;
	private List<User> contactList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pick_contact_no_checkbox);
		listView = (ListView) findViewById(R.id.list);
		sidebar = (Sidebar) findViewById(R.id.sidebar);
		sidebar.setListView(listView);
		contactList = new ArrayList<User>();
		// 获取设置contactlist
		getContactList();
		// 设置adapter
		contactAdapter = new ContactAdapter(this, R.layout.row_contact, contactList);
		listView.setAdapter(contactAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				onListItemClick(position);
			}
		});

	}

	protected void onListItemClick(int position) {
//		if (position != 0) {
			setResult(RESULT_OK, new Intent().putExtra("username", contactAdapter.getItem(position)
					.getUsername()));
			finish();
//		}
	}

	public void back(View view) {
		finish();
	}

	private void getContactList() {
		contactList.clear();
		Map<String, User> users = ((DemoHXSDKHelper)HXSDKHelper.getInstance()).getContactList();
		Iterator<Entry<String, User>> iterator = users.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, User> entry = iterator.next();
			if (!entry.getKey().equals(Constant.NEW_FRIENDS_USERNAME) && !entry.getKey().equals(Constant.GROUP_USERNAME) && !entry.getKey().equals(Constant.CHAT_ROOM) && !entry.getKey().equals(Constant.CHAT_ROBOT))
				contactList.add(entry.getValue());
		}
		// 排序
		Collections.sort(contactList, new Comparator<User>() {

			@Override
			public int compare(User lhs, User rhs) {
				return lhs.getUsername().compareTo(rhs.getUsername());
			}
		});
	}

}
