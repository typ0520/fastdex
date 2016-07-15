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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.easemob.EMValueCallBack;
import com.example.fertilizercrm.easemob.applib.controller.HXSDKHelper;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMContact;
import com.example.fertilizercrm.easemob.chatuidemo.DemoHXSDKHelper;
import com.example.fertilizercrm.R;
import com.example.fertilizercrm.easemob.chatuidemo.db.UserDao;
import com.example.fertilizercrm.easemob.chatuidemo.domain.RobotUser;
import com.easemob.exceptions.EaseMobException;

public class RobotsActivity extends BaseActivity {

	public static final String TAG = RobotsActivity.class.getSimpleName();

	private ListView mListView;
	private List<RobotUser> robotList = new ArrayList<RobotUser>();
	private RobotAdapter adapter;
	private InputMethodManager inputMethodManager;
	private View progressBar;
	private SwipeRefreshLayout swipeRefreshLayout;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.fragment_robots);
		inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		mListView = (ListView) findViewById(R.id.list);
		swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
		swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
				android.R.color.holo_orange_light, android.R.color.holo_red_light);
		progressBar = findViewById(R.id.progress_bar);
		swipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {
				getRobotNamesFromServer();
			}
		});
		Map<String, RobotUser> robotMap = ((DemoHXSDKHelper) HXSDKHelper.getInstance()).getRobotList();
		if (robotMap != null) {
			robotList.addAll(robotMap.values());
		} else {
			progressBar.setVisibility(View.VISIBLE);
			getRobotNamesFromServer();
		}
		adapter = new RobotAdapter(this, 1, robotList);
		mListView.setAdapter(adapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				RobotUser user = (RobotUser) parent.getItemAtPosition(position);
				Intent intent = new Intent();
				intent.setClass(RobotsActivity.this, ChatActivity.class);
				intent.putExtra("userId", user.getUsername());
				startActivity(intent);
			}
		});
		mListView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
					if (getCurrentFocus() != null)
						inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
								InputMethodManager.HIDE_NOT_ALWAYS);
				}
				return false;
			}
		});
	}

	private void getRobotNamesFromServer() {
		asyncGetRobotNamesFromServer(new EMValueCallBack<List<EMContact>>() {

			@Override
			public void onSuccess(final List<EMContact> value) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						progressBar.setVisibility(View.GONE);
						swipeRefreshLayout.setRefreshing(false);
						Map<String, RobotUser> mMap = new HashMap<String, RobotUser>();
						for (EMContact item : value) {
							RobotUser user = new RobotUser();
							user.setUsername(item.getUsername());
							user.setNick(item.getNick());
							user.setHeader("#");
							mMap.put(item.getUsername(), user);
						}
						robotList.clear();
						robotList.addAll(mMap.values());
						// 存入内存
						((DemoHXSDKHelper) HXSDKHelper.getInstance()).setRobotList(mMap);
						// 存入db
						UserDao dao = new UserDao(RobotsActivity.this);
						dao.saveRobotUser(robotList);
						adapter.notifyDataSetChanged();
					}
				});
			}

			@Override
			public void onError(int error, String errorMsg) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						swipeRefreshLayout.setRefreshing(false);
						progressBar.setVisibility(View.GONE);
					}
				});
			}
		});
	}

	private void asyncGetRobotNamesFromServer(final EMValueCallBack<List<EMContact>> callback) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					List<EMContact> mList = EMChatManager.getInstance().getRobotsFromServer();
					callback.onSuccess(mList);
				} catch (EaseMobException e) {
					e.printStackTrace();
					callback.onError(e.getErrorCode(), e.toString());
				}
			}
		}).start();
	}

	class RobotAdapter extends ArrayAdapter<RobotUser> {

		private LayoutInflater inflater;

		public RobotAdapter(Context context, int res, List<RobotUser> robots) {
			super(context, res, robots);
			this.inflater = LayoutInflater.from(context);

		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.row_robots, null);
			}
			((TextView) convertView.findViewById(R.id.name)).setText(getItem(position).getNick());
			return convertView;
		}

	}
}
