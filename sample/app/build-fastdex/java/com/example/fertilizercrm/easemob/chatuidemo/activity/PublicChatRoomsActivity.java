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
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.EMChatRoomChangeListener;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMChatRoom;
import com.easemob.chat.EMCursorResult;
import com.example.fertilizercrm.R;
import com.easemob.exceptions.EaseMobException;

public class PublicChatRoomsActivity extends BaseActivity {
	private ProgressBar pb;
	private TextView title;
	private ListView listView;
	private ChatRoomAdapter adapter;
	
	private List<EMChatRoom> chatRoomList;
	private boolean isLoading;
	private boolean isFirstLoading = true;
	private boolean hasMoreData = true;
	private String cursor;
	private final int pagesize = 20;
    private LinearLayout footLoadingLayout;
    private ProgressBar footLoadingPB;
    private TextView footLoadingText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_public_groups);

		// 搜索框
		pb = (ProgressBar) findViewById(R.id.progressBar);
		listView = (ListView) findViewById(R.id.list);
		title = (TextView)findViewById(R.id.tv_title);
		title.setText(getResources().getString(R.string.chat_room));
		chatRoomList = new ArrayList<EMChatRoom>();
		
		View footView = getLayoutInflater().inflate(R.layout.listview_footer_view, null);
        footLoadingLayout = (LinearLayout) footView.findViewById(R.id.loading_layout);
        footLoadingPB = (ProgressBar)footView.findViewById(R.id.loading_bar);
        footLoadingText = (TextView) footView.findViewById(R.id.loading_text);
        listView.addFooterView(footView, null, false);
        footLoadingLayout.setVisibility(View.GONE);
        
        //获取及显示数据
        loadAndShowData();
        
        EMChatManager.getInstance().addChatRoomChangeListener(new EMChatRoomChangeListener(){
            @Override
            public void onChatRoomDestroyed(String roomId, String roomName) {
                chatRoomList.clear();
                if(adapter != null){
                    runOnUiThread(new Runnable(){

                        @Override
                        public void run() {
                            if(adapter != null){
                                adapter.notifyDataSetChanged();
                                loadAndShowData();
                            }
                        }
                        
                    });
                }
            }

            @Override
            public void onMemberJoined(String roomId, String participant) {                
            }

            @Override
            public void onMemberExited(String roomId, String roomName,
                    String participant) {
                
            }

            @Override
            public void onMemberKicked(String roomId, String roomName,
                    String participant) {
            }
            
        });
        
        //设置item点击事件
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                
                final EMChatRoom room = adapter.getItem(position);
                startActivity(new Intent(PublicChatRoomsActivity.this, ChatActivity.class).putExtra("chatType", 3).
                		putExtra("groupId", room.getId()));
                
            }
        });
        listView.setOnScrollListener(new OnScrollListener() {
            
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if(scrollState == OnScrollListener.SCROLL_STATE_IDLE){
                    if(cursor != null){
                        int lasPos = view.getLastVisiblePosition();
                        if(hasMoreData && !isLoading && lasPos == listView.getCount()-1){
                            loadAndShowData();
                        }
                    }
                }
            }
            
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                
            }
        });
        
	}
	
	private void loadAndShowData(){
		new Thread(new Runnable() {

            public void run() {
                try {
                    isLoading = true;
                    final EMCursorResult<EMChatRoom> result = EMChatManager.getInstance().fetchPublicChatRoomsFromServer(pagesize, cursor);
                    //获取group list
                    final List<EMChatRoom> chatRooms = result.getData();
                    runOnUiThread(new Runnable() {

                        public void run() {
                            chatRoomList.addAll(chatRooms);
                            if(chatRooms.size() != 0){
                                //获取cursor
                                cursor = result.getCursor();
//                                if(chatRooms.size() == pagesize)
//                                    footLoadingLayout.setVisibility(View.VISIBLE);
                            }
                            if(isFirstLoading){
                                pb.setVisibility(View.INVISIBLE);
                                isFirstLoading = false;
                                //设置adapter
                                adapter = new ChatRoomAdapter(PublicChatRoomsActivity.this, 1, chatRoomList);
                                listView.setAdapter(adapter);
                            }else{
                                if(chatRooms.size() < pagesize){
                                    hasMoreData = false;
                                    footLoadingLayout.setVisibility(View.VISIBLE);
                                    footLoadingPB.setVisibility(View.GONE);
                                    footLoadingText.setText(getResources().getString(R.string.no_more_messages));
                                }
                                adapter.notifyDataSetChanged();
                            }
                            isLoading = false;
                        }
                    });
                } catch (EaseMobException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        public void run() {
                            isLoading = false;
                            pb.setVisibility(View.INVISIBLE);
                            footLoadingLayout.setVisibility(View.GONE);
                            Toast.makeText(PublicChatRoomsActivity.this, getResources().getString(R.string.failed_to_load_data), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
	}
	/**
	 * adapter
	 *
	 */
	private class ChatRoomAdapter extends ArrayAdapter<EMChatRoom> {

		private LayoutInflater inflater;

		public ChatRoomAdapter(Context context, int res, List<EMChatRoom> rooms) {
			super(context, res, rooms);
			this.inflater = LayoutInflater.from(context);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.row_group, null);
			}

			((TextView) convertView.findViewById(R.id.name)).setText(getItem(position).getName());

			return convertView;
		}
	}
	
	public void back(View view){
		finish();
	}
}
