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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.chat.EMCursorResult;
import com.easemob.chat.EMGroupInfo;
import com.easemob.chat.EMGroupManager;
import com.example.fertilizercrm.R;
import com.easemob.exceptions.EaseMobException;

public class PublicGroupsActivity extends BaseActivity {
	private ProgressBar pb;
	private ListView listView;
	private GroupsAdapter adapter;
	
	private List<EMGroupInfo> groupsList;
	private boolean isLoading;
	private boolean isFirstLoading = true;
	private boolean hasMoreData = true;
	private String cursor;
	private final int pagesize = 20;
    private LinearLayout footLoadingLayout;
    private ProgressBar footLoadingPB;
    private TextView footLoadingText;
    private Button searchBtn;
    

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_public_groups);

		pb = (ProgressBar) findViewById(R.id.progressBar);
		listView = (ListView) findViewById(R.id.list);
		groupsList = new ArrayList<EMGroupInfo>();
		searchBtn = (Button) findViewById(R.id.btn_search);
		
		View footView = getLayoutInflater().inflate(R.layout.listview_footer_view, null);
        footLoadingLayout = (LinearLayout) footView.findViewById(R.id.loading_layout);
        footLoadingPB = (ProgressBar)footView.findViewById(R.id.loading_bar);
        footLoadingText = (TextView) footView.findViewById(R.id.loading_text);
        listView.addFooterView(footView, null, false);
        footLoadingLayout.setVisibility(View.GONE);
        
        //获取及显示数据
        loadAndShowData();
        
        //设置item点击事件
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(new Intent(PublicGroupsActivity.this, GroupSimpleDetailActivity.class).
                        putExtra("groupinfo", adapter.getItem(position)));
            }
        });
        listView.setOnScrollListener(new OnScrollListener() {
            
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if(scrollState == OnScrollListener.SCROLL_STATE_IDLE){
                    if(listView.getCount() != 0){
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
	
	/**
	 * 搜索
	 * @param view
	 */
	public void search(View view){
	    startActivity(new Intent(this, PublicGroupsSeachActivity.class));
	}
	
	private void loadAndShowData(){
	    new Thread(new Runnable() {

            public void run() {
                try {
                    isLoading = true;
                    final EMCursorResult<EMGroupInfo> result = EMGroupManager.getInstance().getPublicGroupsFromServer(pagesize, cursor);
                    //获取group list
                    final List<EMGroupInfo> returnGroups = result.getData();
                    runOnUiThread(new Runnable() {

                        public void run() {
                            searchBtn.setVisibility(View.VISIBLE);
                            groupsList.addAll(returnGroups);
                            if(returnGroups.size() != 0){
                                //获取cursor
                                cursor = result.getCursor();
                                if(returnGroups.size() == pagesize)
                                    footLoadingLayout.setVisibility(View.VISIBLE);
                            }
                            if(isFirstLoading){
                                pb.setVisibility(View.INVISIBLE);
                                isFirstLoading = false;
                                //设置adapter
                                adapter = new GroupsAdapter(PublicGroupsActivity.this, 1, groupsList);
                                listView.setAdapter(adapter);
                            }else{
                                if(returnGroups.size() < pagesize){
                                    hasMoreData = false;
                                    footLoadingLayout.setVisibility(View.VISIBLE);
                                    footLoadingPB.setVisibility(View.GONE);
                                    footLoadingText.setText("No more data");
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
                            Toast.makeText(PublicGroupsActivity.this, "加载数据失败，请检查网络或稍后重试", 0).show();
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
	private class GroupsAdapter extends ArrayAdapter<EMGroupInfo> {

		private LayoutInflater inflater;

		public GroupsAdapter(Context context, int res, List<EMGroupInfo> groups) {
			super(context, res, groups);
			this.inflater = LayoutInflater.from(context);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.row_group, null);
			}

			((TextView) convertView.findViewById(R.id.name)).setText(getItem(position).getGroupName());

			return convertView;
		}
	}
	
	public void back(View view){
		finish();
	}
}
