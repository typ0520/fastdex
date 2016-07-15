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
package com.example.fertilizercrm.easemob.chatuidemo.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import com.example.fertilizercrm.easemob.chatuidemo.Constant;
import com.example.fertilizercrm.R;
import com.example.fertilizercrm.easemob.chatuidemo.domain.User;
import com.example.fertilizercrm.easemob.chatuidemo.utils.UserUtils;
import com.easemob.util.EMLog;
import com.example.fertilizercrm.role.Role;

import com.example.fertilizercrm.common.utils.Logger;

/**
 * 简单的好友Adapter实现
 *
 */
public class ContactAdapter extends ArrayAdapter<User>  implements SectionIndexer{
    private static final String TAG = "ContactAdapter";
	List<String> list;
	List<User> userList;
	List<User> copyUserList;
	private LayoutInflater layoutInflater;
	private SparseIntArray positionOfSection;
	private SparseIntArray sectionOfPosition;
	private int res;
	private MyFilter myFilter;
    private boolean notiyfyByFilter;

	public ContactAdapter(Context context, int resource, List<User> objects) {
		super(context, resource, objects);
		this.res = resource;
		this.userList = objects;
		copyUserList = new ArrayList<User>();
		copyUserList.addAll(objects);
		layoutInflater = LayoutInflater.from(context);
	}
	
	private static class ViewHolder {
	    ImageView avatar;
	    TextView unreadMsgView;
	    TextView nameTextview;
	    TextView tvHeader;
		TextView tv_role;
    }

	private Role getRoleByUsername(String username) {
		if (TextUtils.isEmpty(username) || username.length() <= 2) {
			return null;
		}
		//typ_1
		try {

			if (username.substring(username.length() - 2).startsWith("_")
					&& username.substring(username.length() - 1).matches("[1-7]")) {
				int code = Integer.valueOf(username.substring(username.length() - 1));
				return Role.valueOf(code);
			}
		} catch (Throwable e) {

		}
		return null;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
	    ViewHolder holder;
 		if(convertView == null){
 		    holder = new ViewHolder();
			convertView = layoutInflater.inflate(res, null);
			holder.avatar = (ImageView) convertView.findViewById(R.id.avatar);
			holder.unreadMsgView = (TextView) convertView.findViewById(R.id.unread_msg_number);
			holder.nameTextview = (TextView) convertView.findViewById(R.id.name);
			holder.tvHeader = (TextView) convertView.findViewById(R.id.header);
			holder.tv_role = (TextView) convertView.findViewById(R.id.tv_role);
			convertView.setTag(holder);
		}else{
		    holder = (ViewHolder) convertView.getTag();
		}
		
		User user = getItem(position);
		if(user == null)
			Log.d("ContactAdapter", position + "");
		//设置nick，demo里不涉及到完整user，用username代替nick显示
		String username = user.getUsername();

		Role role = getRoleByUsername(username);
		if (role != null) {
			if (holder != null && holder.tv_role != null) {
				holder.tv_role.setText(role.getDescription());
			}
		} else {
			if (holder != null && holder.tv_role != null) {
				holder.tv_role.setText("");
			}
		}
		String header = user.getHeader();
		if (position == 0 || header != null && !header.equals(getItem(position - 1).getHeader())) {
			if (TextUtils.isEmpty(header)) {
			    holder.tvHeader.setVisibility(View.GONE);
			} else {
			    holder.tvHeader.setVisibility(View.VISIBLE);
			    holder.tvHeader.setText(header);
			}
		} else {
		    holder.tvHeader.setVisibility(View.GONE);
		}
		//显示申请与通知item
		if(username.equals(Constant.NEW_FRIENDS_USERNAME)){
		    holder.nameTextview.setText(user.getNick());
		    holder.avatar.setImageResource(R.drawable.new_friends_icon);
			if(user.getUnreadMsgCount() > 0){
			    holder.unreadMsgView.setVisibility(View.VISIBLE);
			    holder.unreadMsgView.setText(user.getUnreadMsgCount() + "");

				Logger.d(TAG, "ContactAdapter 显示申请与通知 count: " + user.getUnreadMsgCount());
			}else{
			    holder.unreadMsgView.setVisibility(View.INVISIBLE);
				Logger.d(TAG, "ContactAdapter 显示申请与通知 count: " + 0);
			}
		}else if(username.equals(Constant.GROUP_USERNAME)){
			//群聊item
		    holder.nameTextview.setText(user.getNick());
		    holder.avatar.setImageResource(R.drawable.groups_icon);
		}else if(username.equals(Constant.CHAT_ROOM)){
            //群聊item
            holder.nameTextview.setText(user.getNick());
            holder.avatar.setImageResource(R.drawable.groups_icon);
		}else if(username.equals(Constant.CHAT_ROBOT)){
			//Robot item
			holder.nameTextview.setText(user.getNick());
			holder.avatar.setImageResource(R.drawable.groups_icon);
		}else{
		    holder.nameTextview.setText(user.getNick());
		    //设置用户头像
			UserUtils.setUserAvatar(getContext(), username, holder.avatar);
			if(holder.unreadMsgView != null)
			    holder.unreadMsgView.setVisibility(View.INVISIBLE);
		}
		
		return convertView;
	}
	
	@Override
	public User getItem(int position) {
		return super.getItem(position);
	}
	
	@Override
	public int getCount() {
		return super.getCount();
	}

	public int getPositionForSection(int section) {
		return positionOfSection.get(section);
	}

	public int getSectionForPosition(int position) {
		return sectionOfPosition.get(position);
	}
	
	@Override
	public Object[] getSections() {
		positionOfSection = new SparseIntArray();
		sectionOfPosition = new SparseIntArray();
		int count = getCount();
		list = new ArrayList<String>();
		list.add(getContext().getString(R.string.search_header));
		positionOfSection.put(0, 0);
		sectionOfPosition.put(0, 0);
		for (int i = 1; i < count; i++) {

			String letter = getItem(i).getHeader();
			EMLog.d(TAG, "contactadapter getsection getHeader:" + letter + " name:" + getItem(i).getUsername());
			int section = list.size() - 1;
			if (list.get(section) != null && !list.get(section).equals(letter)) {
				list.add(letter);
				section++;
				positionOfSection.put(section, i);
			}
			sectionOfPosition.put(i, section);
		}
		return list.toArray(new String[list.size()]);
	}
	
	@Override
	public Filter getFilter() {
		if(myFilter==null){
			myFilter = new MyFilter(userList);
		}
		return myFilter;
	}
	
	private class  MyFilter extends Filter{
        List<User> mOriginalList = null;
		
		public MyFilter(List<User> myList) {
			this.mOriginalList = myList;
		}

		@Override
		protected synchronized FilterResults performFiltering(CharSequence prefix) {
			FilterResults results = new FilterResults();
			if(mOriginalList==null){
			    mOriginalList = new ArrayList<User>();
			}
			EMLog.d(TAG, "contacts original size: " + mOriginalList.size());
			EMLog.d(TAG, "contacts copy size: " + copyUserList.size());
			
			if(prefix==null || prefix.length()==0){
				results.values = copyUserList;
				results.count = copyUserList.size();
			}else{
				String prefixString = prefix.toString();
				final int count = mOriginalList.size();
				final ArrayList<User> newValues = new ArrayList<User>();
				for(int i=0;i<count;i++){
					final User user = mOriginalList.get(i);
					String username = user.getUsername();
					
					if(username.startsWith(prefixString)){
						newValues.add(user);
					}
					else{
						 final String[] words = username.split(" ");
	                     final int wordCount = words.length;
	
	                     // Start at index 0, in case valueText starts with space(s)
	                     for (int k = 0; k < wordCount; k++) {
	                         if (words[k].startsWith(prefixString)) {
	                             newValues.add(user);
	                             break;
	                         }
	                     }
					}
				}
				results.values=newValues;
				results.count=newValues.size();
			}
			EMLog.d(TAG, "contacts filter results size: " + results.count);
			return results;
		}

		@Override
		protected synchronized void publishResults(CharSequence constraint,
				FilterResults results) {
			userList.clear();
			userList.addAll((List<User>)results.values);
			EMLog.d(TAG, "publish contacts filter results size: " + results.count);
			if (results.count > 0) {
			    notiyfyByFilter = true;
				notifyDataSetChanged();
				notiyfyByFilter = false;
			} else {
				notifyDataSetInvalidated();
			}
		}
	}
	
	
	@Override
	public void notifyDataSetChanged() {
	    super.notifyDataSetChanged();
	    if(!notiyfyByFilter){
	        copyUserList.clear();
	        copyUserList.addAll(userList);
	    }
	}
	

}
