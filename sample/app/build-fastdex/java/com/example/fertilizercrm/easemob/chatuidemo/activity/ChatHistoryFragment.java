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
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fertilizercrm.easemob.applib.controller.HXSDKHelper;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMContact;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;
import com.easemob.chat.EMMessage;
import com.example.fertilizercrm.easemob.chatuidemo.DemoHXSDKHelper;
import com.example.fertilizercrm.R;
import com.example.fertilizercrm.easemob.chatuidemo.adapter.ChatHistoryAdapter;
import com.example.fertilizercrm.easemob.chatuidemo.db.InviteMessgeDao;
import com.example.fertilizercrm.easemob.chatuidemo.domain.User;
import com.example.fertilizercrm.FertilizerApplication;
import com.example.fertilizercrm.activity.MainActivity;

/**
 * 聊天记录Fragment
 * 
 */
public class ChatHistoryFragment extends Fragment {

	private InputMethodManager inputMethodManager;
	private ListView listView;
	private Map<String, User> contactList;
	private ChatHistoryAdapter adapter;
	private EditText query;
	private ImageButton clearSearch;
	public RelativeLayout errorItem;
	public TextView errorText;
	private boolean hidden;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_conversation_history, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		errorText = (TextView) errorItem.findViewById(R.id.tv_connect_errormsg);
		// contact list
		contactList = ((DemoHXSDKHelper) HXSDKHelper.getInstance()).getContactList();
		listView = (ListView) getView().findViewById(R.id.list);
		adapter = new ChatHistoryAdapter(getActivity(), 1, loadUsersWithRecentChat());
		// 设置adapter
		listView.setAdapter(adapter);
		final String st = getResources().getString(R.string.Cant_chat_with_yourself);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				EMContact emContact = adapter.getItem(position);
				if (adapter.getItem(position).getUsername().equals(FertilizerApplication.getInstance().getUserName()))
					Toast.makeText(getActivity(), st, Toast.LENGTH_SHORT).show();
				else {
					// 进入聊天页面
					  Intent intent = new Intent(getActivity(), ChatActivity.class);
					 if (emContact instanceof EMGroup) {
		                    //it is group chat
		                    intent.putExtra("chatType", ChatActivity.CHATTYPE_GROUP);
		                    intent.putExtra("groupId", ((EMGroup) emContact).getGroupId());
		                } else {
		                    //it is single chat
		                    intent.putExtra("userId", emContact.getUsername());
		                } 
					startActivity(intent);
				}
			}
		});
		// 注册上下文菜单
		registerForContextMenu(listView);

		listView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// 隐藏软键盘
				if (getActivity().getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
					if (getActivity().getCurrentFocus() != null)
						inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
								InputMethodManager.HIDE_NOT_ALWAYS);
				}
				return false;
			}
		});
		// 搜索框
		query = (EditText) getView().findViewById(R.id.query);
		// 搜索框中清除button
		clearSearch = (ImageButton) getView().findViewById(R.id.search_clear);
		query.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
				adapter.getFilter().filter(s);
				if (s.length() > 0) {
					clearSearch.setVisibility(View.VISIBLE);
				} else {
					clearSearch.setVisibility(View.INVISIBLE);
				}
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			public void afterTextChanged(Editable s) {
			}
		});
		clearSearch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				query.getText().clear();

			}
		});

	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		// if(((AdapterContextMenuInfo)menuInfo).position > 0){ m,
		getActivity().getMenuInflater().inflate(R.menu.delete_message, menu);
		// }
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.delete_message) {
			EMContact tobeDeleteUser = adapter.getItem(((AdapterContextMenuInfo) item.getMenuInfo()).position);
			boolean isGroup = false;
			if(tobeDeleteUser instanceof EMGroup)
				isGroup = true;
			// 删除此会话
			EMChatManager.getInstance().deleteConversation(tobeDeleteUser.getUsername(),isGroup);
			InviteMessgeDao inviteMessgeDao = new InviteMessgeDao(getActivity());
			inviteMessgeDao.deleteMessage(tobeDeleteUser.getUsername());
			adapter.remove(tobeDeleteUser);
			adapter.notifyDataSetChanged();

			// 更新消息未读数
			((MainActivity) getActivity()).updateUnreadLabel();

			return true;
		}
		return super.onContextItemSelected(item);
	}

	/**
	 * 刷新页面
	 */
	public void refresh() {
		adapter = new ChatHistoryAdapter(getActivity(), R.layout.row_chat_history, loadUsersWithRecentChat());
		listView.setAdapter(adapter);
		adapter.notifyDataSetChanged();
	}

	
	
	/**
	 * 获取有聊天记录的users和groups
	 *
	 * @return
	 */
	private List<EMContact> loadUsersWithRecentChat() {
		List<EMContact> resultList = new ArrayList<EMContact>();
		//获取有聊天记录的users，不包括陌生人
		for (User user : contactList.values()) {
			EMConversation conversation = EMChatManager.getInstance().getConversation(user.getUsername());
			if (conversation.getMsgCount() > 0) {
				resultList.add(user);
			}
		}
		for(EMGroup group : EMGroupManager.getInstance().getAllGroups()){
			EMConversation conversation = EMChatManager.getInstance().getConversation(group.getGroupId());
			if(conversation.getMsgCount() > 0){
				resultList.add(group);
			}
			
		}
		
		// 排序
		sortUserByLastChatTime(resultList);
		return resultList;
	}

	/**
	 * 根据最后一条消息的时间排序
	 *
	 */
	private void sortUserByLastChatTime(List<EMContact> contactList) {
		Collections.sort(contactList, new Comparator<EMContact>() {
			@Override
			public int compare(final EMContact user1, final EMContact user2) {
				EMConversation conversation1 = EMChatManager.getInstance().getConversation(user1.getUsername());
				EMConversation conversation2 = EMChatManager.getInstance().getConversation(user2.getUsername());

				EMMessage user2LastMessage = conversation2.getLastMessage();
				EMMessage user1LastMessage = conversation1.getLastMessage();
				if (user2LastMessage.getMsgTime() == user1LastMessage.getMsgTime()) {
					return 0;
				} else if (user2LastMessage.getMsgTime() > user1LastMessage.getMsgTime()) {
					return 1;
				} else {
					return -1;
				}
			}

		});
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		this.hidden = hidden;
		if (!hidden) {
			refresh();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!hidden) {
			refresh();
		}
	}
	
	
}
