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

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMChatRoom;
import com.example.fertilizercrm.R;
import com.example.fertilizercrm.easemob.chatuidemo.adapter.ChatRoomAdapter;

public class ChatRoomActivity extends BaseActivity {
	private ListView chatListView;
	protected List<EMChatRoom> roomList;
	private ChatRoomAdapter chatRoomAdapter;
	private InputMethodManager inputMethodManager;
	public static ChatRoomActivity instance;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_chatroom);

		instance = this;
		inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		roomList = EMChatManager.getInstance().getAllChatRooms();
		chatListView = (ListView) findViewById(R.id.list);
		chatRoomAdapter = new ChatRoomAdapter(this, 1, roomList);
		chatListView.setAdapter(chatRoomAdapter);
		chatListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if(position == 1) {
					// 添加公开群
					startActivityForResult(new Intent(ChatRoomActivity.this, PublicChatRoomsActivity.class), 0);
				} else {
					// 进入群聊
					Intent intent = new Intent(ChatRoomActivity.this, ChatActivity.class);
					// it is group chat
					intent.putExtra("chatType", ChatActivity.CHATTYPE_CHATROOM);
					intent.putExtra("groupId", chatRoomAdapter.getItem(position - 2).getId());
					startActivityForResult(intent, 0);
				}
			}

		});
		
		// TODO: we need more official UI, but for now, for test purpose
		chatListView.setOnItemLongClickListener(new OnItemLongClickListener(){

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                    int position, long id) {
                if(position > 1){
                    final  String roomId = chatRoomAdapter.getItem(position - 2).getId();
                    
                    new Thread(){
                        @Override
                        public void run(){
                            EMChatManager.getInstance().leaveChatRoom(roomId);
                        }
                    }.start();
                    
                    return true;
                }
                return false;
            }
		    
		});
		
		chatListView.setOnTouchListener(new OnTouchListener() {

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

	/**
	 * 进入公开群聊列表
	 */
	public void onPublicGroups(View view) {
		startActivity(new Intent(this, PublicGroupsActivity.class));
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onResume() {
		super.onResume();
		roomList = EMChatManager.getInstance().getAllChatRooms();
		chatRoomAdapter = new ChatRoomAdapter(this, 1, roomList);
		chatListView.setAdapter(chatRoomAdapter);
		chatRoomAdapter.notifyDataSetChanged();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		instance = null;
	}

	/**
	 * 返回
	 * 
	 * @param view
	 */
	public void back(View view) {
		finish();
	}
}
