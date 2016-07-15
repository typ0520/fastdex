package com.example.fertilizercrm.easemob.chatuidemo.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
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
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMConversation.EMConversationType;
import com.example.fertilizercrm.easemob.chatuidemo.Constant;
import com.example.fertilizercrm.easemob.chatuidemo.DemoHXSDKHelper;
import com.example.fertilizercrm.R;
import com.example.fertilizercrm.easemob.chatuidemo.adapter.ChatAllHistoryAdapter;
import com.example.fertilizercrm.easemob.chatuidemo.db.InviteMessgeDao;
import com.example.fertilizercrm.easemob.chatuidemo.db.UserDao;
import com.example.fertilizercrm.easemob.chatuidemo.domain.User;
import com.example.fertilizercrm.FertilizerApplication;
import com.example.fertilizercrm.activity.MainActivity;
import com.example.fertilizercrm.basic.BaseFragment;
import com.example.fertilizercrm.view.TitleView;

import com.example.fertilizercrm.common.utils.Logger;

/**
 * 显示所有会话记录，比较简单的实现，更好的可能是把陌生人存入本地，这样取到的聊天记录是可控的
 * 
 */
public class ChatAllHistoryFragment extends BaseFragment implements OnClickListener {

	private InputMethodManager inputMethodManager;
	private ListView listView;
	private ChatAllHistoryAdapter adapter;
	private EditText query;
	private ImageButton clearSearch;
	public RelativeLayout errorItem;

	public TextView errorText;
	private boolean hidden;
	private List<EMConversation> conversationList = new ArrayList<EMConversation>();
		
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		//return inflater.inflate(R.layout.fragment_conversation_history, container, false);
		return obtainContentView(R.layout.fragment_conversation_history, container);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if(savedInstanceState != null && savedInstanceState.getBoolean("isConflict", false))
            return;
		TitleView titleView = (TitleView) findViewById(R.id.title_view);
		titleView.setTitleColor(getResources().getColor(R.color.left_text_color2));
		inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		errorItem = (RelativeLayout) getView().findViewById(R.id.rl_error_item);
		errorText = (TextView) errorItem.findViewById(R.id.tv_connect_errormsg);		
		
		conversationList.addAll(loadConversationsWithRecentChat());
		listView = (ListView) getView().findViewById(R.id.list);
		adapter = new ChatAllHistoryAdapter(getActivity(), 1, conversationList);
		// 设置adapter
		listView.setAdapter(adapter);
				
		
		final String st2 = getResources().getString(R.string.Cant_chat_with_yourself);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				EMConversation conversation = adapter.getItem(position);
				String username = conversation.getUserName();

				Logger.d("<< easemob username: " + username);
				if (username.equals(FertilizerApplication.getInstance().getUserName()))
					Toast.makeText(getActivity(), st2, Toast.LENGTH_SHORT).show();
				else {
				    // 进入聊天页面
				    Intent intent = new Intent(getActivity(), ChatActivity.class);
				    if(conversation.isGroup()){
				        if(conversation.getType() == EMConversationType.ChatRoom){
				         // it is group chat
	                        intent.putExtra("chatType", ChatActivity.CHATTYPE_CHATROOM);
	                        intent.putExtra("groupId", username);
				        }else{
				         // it is group chat
	                        intent.putExtra("chatType", ChatActivity.CHATTYPE_GROUP);
	                        intent.putExtra("groupId", username);
				        }
				        
				    }else{
				        // it is single chat
                        intent.putExtra("userId", username);
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
				hideSoftKeyboard();
				return false;
			}

		});
		// 搜索框
		query = (EditText) getView().findViewById(R.id.query);
		String strSearch = getResources().getString(R.string.search);
		query.setHint(strSearch);
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
				hideSoftKeyboard();
			}
		});


//		try {
//			// ** 第一次登录或者之前logout后再登录，加载所有本地群和回话
//			// ** manually load all local groups and
//			EMGroupManager.getInstance().loadAllGroups();
//			EMChatManager.getInstance().loadAllConversations();
//			// 处理好友和群组
//			initializeContacts();
//		} catch (Exception e) {
//			e.printStackTrace();
//			// 取好友或者群聊失败，不让进入主页面
//			getActivity().runOnUiThread(new Runnable() {
//				public void run() {
//					hideProgress();
//					DemoHXSDKHelper.getInstance().logout(true,null);
//					Toast.makeText(getActivity(), com.example.fertilizercrm.R.string.login_failure_failed, Toast.LENGTH_LONG).show();
//				}
//			});
//			return;
//		}
//		// 更新当前用户的nickname 此方法的作用是在ios离线推送时能够显示用户nick
//		boolean updatenick = EMChatManager.getInstance().updateCurrentUserNick(
//				ProjectApplication.currentUserNick.trim());
//		if (!updatenick) {
//			Log.e("LoginActivity", "update current user nick fail");
//		}
	}

	private void initializeContacts() {
		Map<String, User> userlist = new HashMap<String, User>();
		// 添加user"申请与通知"
		User newFriends = new User();
		newFriends.setUsername(Constant.NEW_FRIENDS_USERNAME);
		String strChat = getResources().getString(
				com.example.fertilizercrm.R.string.Application_and_notify);
		newFriends.setNick(strChat);

		userlist.put(Constant.NEW_FRIENDS_USERNAME, newFriends);
//        // 添加"群聊"
//        User groupUser = new User();
//        String strGroup = getResources().getString(com.example.fertilizercrm.R.string.group_chat);
//        groupUser.setUsername(Constant.GROUP_USERNAME);
//        groupUser.setNick(strGroup);
//        groupUser.setHeader("");
//        userlist.put(Constant.GROUP_USERNAME, groupUser);
//
//        // 添加"Robot"
//        User robotUser = new User();
//        String strRobot = getResources().getString(com.example.fertilizercrm.R.string.robot_chat);
//        robotUser.setUsername(Constant.CHAT_ROBOT);
//        robotUser.setNick(strRobot);
//        robotUser.setHeader("");
//        userlist.put(Constant.CHAT_ROBOT, robotUser);

		// 存入内存
		((DemoHXSDKHelper) HXSDKHelper.getInstance()).setContactList(userlist);
		// 存入db
		UserDao dao = new UserDao(getActivity());
		List<User> users = new ArrayList<User>(userlist.values());
		dao.saveContactList(users);
	}

	void hideSoftKeyboard() {
		if (getActivity().getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
			if (getActivity().getCurrentFocus() != null)
				inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
						InputMethodManager.HIDE_NOT_ALWAYS);
		}
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
		boolean handled = false;
		boolean deleteMessage = false;
		if (item.getItemId() == R.id.delete_message) {
			deleteMessage = true;
			handled = true;
		} else if (item.getItemId() == R.id.delete_conversation) {
			deleteMessage = false;
			handled = true;
		}
		EMConversation tobeDeleteCons = adapter.getItem(((AdapterContextMenuInfo) item.getMenuInfo()).position);
		// 删除此会话
		EMChatManager.getInstance().deleteConversation(tobeDeleteCons.getUserName(), tobeDeleteCons.isGroup(), deleteMessage);
		InviteMessgeDao inviteMessgeDao = new InviteMessgeDao(getActivity());
		inviteMessgeDao.deleteMessage(tobeDeleteCons.getUserName());
		adapter.remove(tobeDeleteCons);
		adapter.notifyDataSetChanged();

		// 更新消息未读数
		((MainActivity) getActivity()).updateUnreadLabel();
		
		return handled ? true : super.onContextItemSelected(item);
	}

	/**
	 * 刷新页面
	 */
	public void refresh() {
		conversationList.clear();
		conversationList.addAll(loadConversationsWithRecentChat());
		if(adapter != null)
		    adapter.notifyDataSetChanged();
	}

	/**
	 * 获取所有会话
	 *
	 * @return
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        +	 */
	private List<EMConversation> loadConversationsWithRecentChat() {
		// 获取所有会话，包括陌生人
		Hashtable<String, EMConversation> conversations = EMChatManager.getInstance().getAllConversations();
		// 过滤掉messages size为0的conversation
		/**
		 * 如果在排序过程中有新消息收到，lastMsgTime会发生变化
		 * 影响排序过程，Collection.sort会产生异常
		 * 保证Conversation在Sort过程中最后一条消息的时间不变 
		 * 避免并发问题
		 */
		List<Pair<Long, EMConversation>> sortList = new ArrayList<Pair<Long, EMConversation>>();
		synchronized (conversations) {
			for (EMConversation conversation : conversations.values()) {
				if (conversation.getAllMessages().size() != 0) {
					//if(conversation.getQueryType() != EMConversationType.ChatRoom){
						sortList.add(new Pair<Long, EMConversation>(conversation.getLastMessage().getMsgTime(), conversation));
					//}
				}
			}
		}
		try {
			// Internal is TimSort algorithm, has bug
			sortConversationByLastChatTime(sortList);
		} catch (Exception e) {
			e.printStackTrace();
		}
		List<EMConversation> list = new ArrayList<EMConversation>();
		for (Pair<Long, EMConversation> sortItem : sortList) {
			list.add(sortItem.second);
		}
		return list;
	}

	/**
	 * 根据最后一条消息的时间排序
	 *
	 */
	private void sortConversationByLastChatTime(List<Pair<Long, EMConversation>> conversationList) {
		Collections.sort(conversationList, new Comparator<Pair<Long, EMConversation>>() {
			@Override
			public int compare(final Pair<Long, EMConversation> con1, final Pair<Long, EMConversation> con2) {

				if (con1.first == con2.first) {
					return 0;
				} else if (con2.first > con1.first) {
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
		if (!hidden && ! ((MainActivity)getActivity()).isConflict) {
			refresh();
		}
	}

	@Override
    public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
        if(((MainActivity)getActivity()).isConflict){
        	outState.putBoolean("isConflict", true);
        }else if(((MainActivity)getActivity()).getCurrentAccountRemoved()){
        	outState.putBoolean(Constant.ACCOUNT_REMOVED, true);
        }
    }

    @Override
    public void onClick(View v) {        
    }
}
