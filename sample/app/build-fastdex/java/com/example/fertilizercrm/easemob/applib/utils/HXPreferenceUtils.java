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
package com.example.fertilizercrm.easemob.applib.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class HXPreferenceUtils {
	/**
	 * 保存Preference的name
	 */
	public static final String PREFERENCE_NAME = "saveInfo";
	private static SharedPreferences mSharedPreferences;
	private static HXPreferenceUtils mPreferenceUtils;
	private static SharedPreferences.Editor editor;

	private String SHARED_KEY_SETTING_NOTIFICATION = "shared_key_setting_notification";
	private String SHARED_KEY_SETTING_SOUND = "shared_key_setting_sound";
	private String SHARED_KEY_SETTING_VIBRATE = "shared_key_setting_vibrate";
	private String SHARED_KEY_SETTING_SPEAKER = "shared_key_setting_speaker";

	private static String SHARED_KEY_SETTING_CHATROOM_OWNER_LEAVE = "shared_key_setting_chatroom_owner_leave";
	private static String SHARED_KEY_SETTING_GROUPS_SYNCED = "SHARED_KEY_SETTING_GROUPS_SYNCED";
	private static String SHARED_KEY_SETTING_CONTACT_SYNCED = "SHARED_KEY_SETTING_CONTACT_SYNCED";
	private static String SHARED_KEY_SETTING_BALCKLIST_SYNCED = "SHARED_KEY_SETTING_BALCKLIST_SYNCED";
	
	private static String SHARED_KEY_CURRENTUSER_NICK = "SHARED_KEY_CURRENTUSER_NICK";
	private static String SHARED_KEY_CURRENTUSER_AVATAR = "SHARED_KEY_CURRENTUSER_AVATAR";
	
	private HXPreferenceUtils(Context cxt) {
		mSharedPreferences = cxt.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
		editor = mSharedPreferences.edit();
	}

	public static synchronized void init(Context cxt){
	    if(mPreferenceUtils == null){
	        mPreferenceUtils = new HXPreferenceUtils(cxt);
	    }
	}

	/**
	 * 单例模式，获取instance实例
	 *
	 * @return
	 */
	public static HXPreferenceUtils getInstance() {
		if (mPreferenceUtils == null) {
			throw new RuntimeException("please init first!");
		}
		
		return mPreferenceUtils;
	}
	
	public void setSettingMsgNotification(boolean paramBoolean) {
		editor.putBoolean(SHARED_KEY_SETTING_NOTIFICATION, paramBoolean);
		editor.commit();
	}

	public boolean getSettingMsgNotification() {
		return mSharedPreferences.getBoolean(SHARED_KEY_SETTING_NOTIFICATION, true);
	}

	public void setSettingMsgSound(boolean paramBoolean) {
		editor.putBoolean(SHARED_KEY_SETTING_SOUND, paramBoolean);
		editor.commit();
	}

	public boolean getSettingMsgSound() {

		return mSharedPreferences.getBoolean(SHARED_KEY_SETTING_SOUND, true);
	}

	public void setSettingMsgVibrate(boolean paramBoolean) {
		editor.putBoolean(SHARED_KEY_SETTING_VIBRATE, paramBoolean);
		editor.commit();
	}

	public boolean getSettingMsgVibrate() {
		return mSharedPreferences.getBoolean(SHARED_KEY_SETTING_VIBRATE, true);
	}

	public void setSettingMsgSpeaker(boolean paramBoolean) {
		editor.putBoolean(SHARED_KEY_SETTING_SPEAKER, paramBoolean);
		editor.commit();
	}

	public boolean getSettingMsgSpeaker() {
		return mSharedPreferences.getBoolean(SHARED_KEY_SETTING_SPEAKER, true);
	}
	
	public void setSettingAllowChatroomOwnerLeave(boolean value) {
        editor.putBoolean(SHARED_KEY_SETTING_CHATROOM_OWNER_LEAVE, value);
        editor.commit();
    }
	
	public boolean getSettingAllowChatroomOwnerLeave() {
        return mSharedPreferences.getBoolean(SHARED_KEY_SETTING_CHATROOM_OWNER_LEAVE, true);
    }
	
	public void setGroupsSynced(boolean synced){
	    editor.putBoolean(SHARED_KEY_SETTING_GROUPS_SYNCED, synced);
        editor.commit();
	}
	
	public boolean isGroupsSynced(){
	    return mSharedPreferences.getBoolean(SHARED_KEY_SETTING_GROUPS_SYNCED, false);
	}
	
	public void setContactSynced(boolean synced){
        editor.putBoolean(SHARED_KEY_SETTING_CONTACT_SYNCED, synced);
        editor.commit();
    }
    
    public boolean isContactSynced(){
        return mSharedPreferences.getBoolean(SHARED_KEY_SETTING_CONTACT_SYNCED, false);
    }
    
    public void setBlacklistSynced(boolean synced){
        editor.putBoolean(SHARED_KEY_SETTING_BALCKLIST_SYNCED, synced);
        editor.commit();
    }
    
    public boolean isBacklistSynced(){
        return mSharedPreferences.getBoolean(SHARED_KEY_SETTING_BALCKLIST_SYNCED, false);
    }
    
	public void setCurrentUserNick(String nick) {
		editor.putString(SHARED_KEY_CURRENTUSER_NICK, nick);
		editor.commit();
	}

	public void setCurrentUserAvatar(String avatar) {
		editor.putString(SHARED_KEY_CURRENTUSER_AVATAR, avatar);
		editor.commit();
	}

	public String getCurrentUserNick() {
		return mSharedPreferences.getString(SHARED_KEY_CURRENTUSER_NICK, null);
	}

	public String getCurrentUserAvatar() {
		return mSharedPreferences.getString(SHARED_KEY_CURRENTUSER_AVATAR, null);
	}

	public void removeCurrentUserInfo() {
		editor.remove(SHARED_KEY_CURRENTUSER_NICK);
		editor.remove(SHARED_KEY_CURRENTUSER_AVATAR);
		editor.commit();
	}
}
