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
package com.example.fertilizercrm.easemob.chatuidemo.utils;

import com.example.fertilizercrm.easemob.applib.utils.HXPreferenceUtils;

import android.content.Context;

/**
 * 
 * @deprecated this class is deprecated, please use {@link HXPreferenceUtils}
 *
 */
public class PreferenceUtils {

	/**
	 * 保存Preference的name
	 */
	public static final String PREFERENCE_NAME = "saveInfo";
	private static PreferenceUtils mPreferenceUtils;
	private PreferenceUtils() {
	}

	/**
	 * 单例模式，获取instance实例
	 * 
	 * @param cxt
	 * @return
	 */
	public synchronized static PreferenceUtils getInstance(Context cxt) {
		if (mPreferenceUtils == null) {
			mPreferenceUtils = new PreferenceUtils();
			HXPreferenceUtils.init(cxt);
		}
		
		return mPreferenceUtils;
	}

	public void setSettingMsgNotification(boolean paramBoolean) {
		HXPreferenceUtils.getInstance().setSettingMsgNotification(paramBoolean);
	}

	public boolean getSettingMsgNotification() {
		return HXPreferenceUtils.getInstance().getSettingMsgNotification();
	}

	public void setSettingMsgSound(boolean paramBoolean) {
	    HXPreferenceUtils.getInstance().setSettingMsgSound(paramBoolean);
	}

	public boolean getSettingMsgSound() {
	    return HXPreferenceUtils.getInstance().getSettingMsgSound();
	}

	public void setSettingMsgVibrate(boolean paramBoolean) {
	    HXPreferenceUtils.getInstance().setSettingMsgVibrate(paramBoolean);
	}

	public boolean getSettingMsgVibrate() {
		return HXPreferenceUtils.getInstance().getSettingMsgVibrate();
	}

	public void setSettingMsgSpeaker(boolean paramBoolean) {
	    HXPreferenceUtils.getInstance().setSettingMsgSpeaker(paramBoolean);
	}

	public boolean getSettingMsgSpeaker() {
		return HXPreferenceUtils.getInstance().getSettingMsgSpeaker();
	}
}
