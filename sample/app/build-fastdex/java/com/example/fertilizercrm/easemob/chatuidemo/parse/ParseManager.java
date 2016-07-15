package com.example.fertilizercrm.easemob.chatuidemo.parse;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;

import com.easemob.EMValueCallBack;
import com.easemob.chat.EMChatManager;
import com.example.fertilizercrm.easemob.chatuidemo.domain.User;
import com.example.fertilizercrm.easemob.chatuidemo.utils.UserUtils;
import com.easemob.util.EMLog;
import com.easemob.util.HanziToPinyin;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

public class ParseManager {

	private static final String TAG = ParseManager.class.getSimpleName();
	private static final String ParseAppID = "UUL8TxlHwKj7ZXEUr2brF3ydOxirCXdIj9LscvJs";
	private static final String ParseClientKey = "B1jH9bmxuYyTcpoFfpeVslhmLYsytWTxqYqKQhBJ";

	private static final String CONFIG_TABLE_NAME = "hxuser";
	private static final String CONFIG_USERNAME = "username";
	private static final String CONFIG_NICK = "nickname";
	private static final String CONFIG_AVATAR = "avatar";

	private Context appContext;
	private static ParseManager instance = new ParseManager();
	

	private ParseManager() {
	}

	public static ParseManager getInstance() {
		return instance;
	}

	public void onInit(Context context) {
		this.appContext = context.getApplicationContext();
		Parse.enableLocalDatastore(appContext);
		Parse.initialize(context, ParseAppID, ParseClientKey);
	}

	public boolean updateParseNickName(final String nickname) {
		String username = EMChatManager.getInstance().getCurrentUser();
		ParseQuery<ParseObject> pQuery = ParseQuery.getQuery(CONFIG_TABLE_NAME);
		pQuery.whereEqualTo(CONFIG_USERNAME, username);
		ParseObject pUser = null;
		try {
			pUser = pQuery.getFirst();
			if (pUser == null) {
				return false;
			}
			pUser.put(CONFIG_NICK, nickname);
			pUser.save();
			return true;
		} catch (ParseException e) {
			if(e.getCode()==ParseException.OBJECT_NOT_FOUND){
				pUser = new ParseObject(CONFIG_TABLE_NAME);
				pUser.put(CONFIG_USERNAME, username);
				pUser.put(CONFIG_NICK, nickname);
				try {
					pUser.save();
					return true;
				} catch (ParseException e1) {
					e1.printStackTrace();
					EMLog.e(TAG, "parse error " + e1.getMessage());
				}
				
			}
			e.printStackTrace();
			EMLog.e(TAG, "parse error " + e.getMessage());
		}
		return false;
	}

	public void getContactInfos(List<String> usernames, final EMValueCallBack<List<User>> callback) {
		ParseQuery<ParseObject> pQuery = ParseQuery.getQuery(CONFIG_TABLE_NAME);
		pQuery.whereContainedIn(CONFIG_USERNAME, usernames);
		pQuery.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> arg0, ParseException arg1) {
				if (arg0 != null) {
					List<User> mList = new ArrayList<User>();
					for (ParseObject pObject : arg0) {
						User user = new User();
						ParseFile parseFile = pObject.getParseFile(CONFIG_AVATAR);
						if (parseFile != null) {
							user.setAvatar(parseFile.getUrl());
						}
						user.setNick(pObject.getString(CONFIG_NICK));
						user.setUsername(pObject.getString(CONFIG_USERNAME));
						setUserHearder(user);
						mList.add(user);
					}
					callback.onSuccess(mList);
				} else {
					callback.onError(arg1.getCode(), arg1.getMessage());
				}
			}
		});
	}

	/**
     * 设置hearder属性，方便通讯中对联系人按header分类显示，以及通过右侧ABCD...字母栏快速定位联系人
     *
     * @param user
     */
    private static void setUserHearder(User user) {
        String headerName = null;
        if (!TextUtils.isEmpty(user.getNick())) {
            headerName = user.getNick();
        } else {
            headerName = user.getUsername();
        }
        if (Character.isDigit(headerName.charAt(0))) {
            user.setHeader("#");
        } else {
            user.setHeader(HanziToPinyin.getInstance().get(headerName.substring(0, 1)).get(0).target.substring(0, 1)
                    .toUpperCase());
            char header = user.getHeader().toLowerCase().charAt(0);
            if (header < 'a' || header > 'z') {
                user.setHeader("#");
            }
        }
    }
	
	public void asyncGetCurrentUserInfo(final EMValueCallBack<User> callback){
		final String username = EMChatManager.getInstance().getCurrentUser();
		asyncGetUserInfo(username, new EMValueCallBack<User>() {

			@Override
			public void onSuccess(User value) {
				callback.onSuccess(value);
			}

			@Override
			public void onError(int error, String errorMsg) {
				if (error == ParseException.OBJECT_NOT_FOUND) {
					ParseObject pUser = new ParseObject(CONFIG_TABLE_NAME);
					pUser.put(CONFIG_USERNAME, username);
					pUser.saveInBackground(new SaveCallback() {
						
						@Override
						public void done(ParseException arg0) {
							if(arg0==null){
								callback.onSuccess(new User(username));
							}
						}
					});
				}else{
					callback.onError(error, errorMsg);
				}
			}
		});
	}
	
	public void asyncGetUserInfo(final String username,final EMValueCallBack<User> callback){
		ParseQuery<ParseObject> pQuery = ParseQuery.getQuery(CONFIG_TABLE_NAME);
		pQuery.whereEqualTo(CONFIG_USERNAME, username);
		pQuery.getFirstInBackground(new GetCallback<ParseObject>() {
			
			@Override
			public void done(ParseObject pUser, ParseException e) {
				if(pUser!=null){
					String nick = pUser.getString(CONFIG_NICK);
					ParseFile pFile = pUser.getParseFile(CONFIG_AVATAR);
					if(callback!=null){
						User user = UserUtils.getUserInfo(username);
						if(user!=null){
							user.setNick(nick);
							if (pFile != null && pFile.getUrl() != null) {
								user.setAvatar(pFile.getUrl());
							}
						}
						callback.onSuccess(user);
					}
				}else{
					if(callback!=null){
						callback.onError(e.getCode(), e.getMessage());
					}
				}
				
			}
		});
	}

	public String uploadParseAvatar(byte[] data) {
		String username = EMChatManager.getInstance().getCurrentUser();
		ParseQuery<ParseObject> pQuery = ParseQuery.getQuery(CONFIG_TABLE_NAME);
		pQuery.whereEqualTo(CONFIG_USERNAME, username);
		ParseObject pUser = null;
		try {
			pUser = pQuery.getFirst();
			if (pUser == null) {
				pUser = new ParseObject(CONFIG_TABLE_NAME);
				pUser.put(CONFIG_USERNAME, username);
			}
			ParseFile pFile = new ParseFile(data);
			pUser.put(CONFIG_AVATAR, pFile);
			pUser.save();
			return pFile.getUrl();
		} catch (ParseException e) {
			if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
				try {
					pUser = new ParseObject(CONFIG_TABLE_NAME);
					pUser.put(CONFIG_USERNAME, username);
					ParseFile pFile = new ParseFile(data);
					pUser.put(CONFIG_AVATAR, pFile);
					pUser.save();
					return pFile.getUrl();
				} catch (ParseException e1) {
					e1.printStackTrace();
					EMLog.e(TAG, "parse error " + e1.getMessage());
				}
			} else {
				e.printStackTrace();
				EMLog.e(TAG, "parse error " + e.getMessage());
			}
		}
		return null;
	}

	

	
}
