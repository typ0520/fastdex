package com.example.fertilizercrm.common.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 缓存的工具类
 * @author liuweina
 *
 */
public class SharedPreferenceUtil {
	private static Context mContext;
	/*
	 * the default mode, where the created file can only be accessed by the calling application 
	 * (or all applications sharing the same user ID).
	 */
	public static final int MODE_PRIVATE = 0;
	/*
	 * allow all other applications to have read access to the created file
	 * This constant was deprecated in API level 17.
	 */
	public static final int MODE_WORLD_READABLE = 1;
	/*
	 * allow all other applications to have write access to the created file.
	 * This constant was deprecated in API level 17.
	 */
	public static final int MODE_WORLD_WRITEABLE = 2;

	public static void init(Context context) {
		if (context == null)
			throw new IllegalArgumentException("context can not be null");
		mContext = context;
	}

	/**
	 * 将一个String数据存入到缓存中
	 * @param spName 缓存的名字
	 * @param keyStr 要存入缓存中的key
	 * @param valueStr 要存入缓存中的value
	 */
	public static void setStringDataIntoSP(String spName, String keyStr, String valueStr) {
		SharedPreferences sp = mContext.getSharedPreferences(spName, MODE_PRIVATE);
		sp.edit().putString(keyStr, valueStr).commit();
	}

	/**
	 * 将一个Boolean数据存入到缓存中
	 * @param spName 缓存的名字
	 * @param keyStr 要存入缓存中的key
	 * @param valueStr 要存入缓存中的value
	 */
	public static void setBooleanDataIntoSP(String spName, String keyStr, Boolean valueStr) {
		SharedPreferences sp = mContext.getSharedPreferences(spName, MODE_PRIVATE);
		sp.edit().putBoolean(keyStr, valueStr).commit();
	}

	/**
	 * 将一个Int数据存入到缓存中
	 * @param spName 缓存的名字
	 * @param keyStr 要存入缓存中的key
	 * @param valueStr 要存入缓存中的value
	 */
	public static void setIntDataIntoSP(String spName, String keyStr, int valueStr) {
		SharedPreferences sp = mContext.getSharedPreferences(spName, MODE_PRIVATE);
		sp.edit().putInt(keyStr, valueStr).commit();
	}

	/**
	 * 将一个Float数据存入到缓存中
	 * @param spName 缓存的名字
	 * @param keyStr 要存入缓存中的key
	 * @param valueStr 要存入缓存中的value
	 */
	public static void setFloatDataIntoSP(String spName, String keyStr, Float valueStr) {
		SharedPreferences sp = mContext.getSharedPreferences(spName, MODE_PRIVATE);
		sp.edit().putFloat(keyStr, valueStr).commit();
	}

	/**
	 * 将一个Long数据存入到缓存中
	 * @param spName 缓存的名字
	 * @param keyStr 要存入缓存中的key
	 * @param valueStr 要存入缓存中的value
	 */
	public static void setLongDataIntoSP(String spName, String keyStr, Long valueStr) {
		SharedPreferences sp = mContext.getSharedPreferences(spName, MODE_PRIVATE);
		sp.edit().putLong(keyStr, valueStr).commit();
	}

	/**
	 * 获取缓存中的一个String数据
	 * @param spName 缓存的名字
	 * @param keyStr 已存入缓存中的key
	 * @return 缓存中对应参数key的value
	 */
	public static String getStringValueFromSP(String spName, String keyStr) {
		return getStringValueFromSP(spName, keyStr, "");
	}

	/**
	 * 获取缓存中的一个String数据
	 * @param spName 缓存的名字
	 * @param keyStr 已存入缓存中的key
	 * @param defaultValue 缓存中对应参数key的默认值
	 * @return 缓存中对应参数key的value
	 */
	public static String getStringValueFromSP(String spName, String keyStr, String defaultValue) {
		SharedPreferences sp = mContext.getSharedPreferences(spName, MODE_PRIVATE);
		return sp.getString(keyStr, defaultValue);
	}

	/**
	 * 获取缓存中的一个Float数据
	 * @param spName 缓存的名字
	 * @param keyStr 已存入缓存中的key
	 * @return 缓存中对应参数key的value
	 */
	public static Float getFloatValueFromSP(String spName, String keyStr) {
		return getFloatValueFromSP(spName, keyStr, 0.0f);
	}

	/**
	 * 获取缓存中的一个Float数据
	 * @param spName 缓存的名字
	 * @param keyStr 已存入缓存中的key
	 * @param defaultValue 缓存中对应参数key的默认值
	 * @return 缓存中对应参数key的value
	 */
	public static Float getFloatValueFromSP(String spName, String keyStr, Float defaultValue) {
		SharedPreferences sp = mContext.getSharedPreferences(spName, MODE_PRIVATE);
		return sp.getFloat(keyStr, defaultValue);
	}

	/**
	 * 获取缓存中的一个Int数据
	 * @param spName 缓存的名字
	 * @param keyStr 已存入缓存中的key
	 * @return 缓存中对应参数key的value
	 */
	public static int getIntValueFromSP(String spName, String keyStr) {
		return getIntValueFromSP(spName, keyStr, 0);
	}

	/**
	 * 获取缓存中的一个Int数据
	 * @param spName 缓存的名字
	 * @param keyStr 已存入缓存中的key
	 * @param defaultValue 缓存中对应参数key的默认值
	 * @return 缓存中对应参数key的value
	 */
	public static int getIntValueFromSP(String spName, String keyStr, int defaultValue) {
		SharedPreferences sp = mContext.getSharedPreferences(spName, MODE_PRIVATE);
		return sp.getInt(keyStr, defaultValue);
	}

	/**
	 * 获取缓存中的一个Boolean数据
	 * @param spName 缓存的名字
	 * @param keyStr 已存入缓存中的key
	 * @return 缓存中对应参数key的value
	 */
	public static boolean getBooleanValueFromSP(String spName, String keyStr) {
		return getBooleanValueFromSP(spName, keyStr, false);
	}

	/**
	 * 获取缓存中的一个Boolean数据
	 * @param spName 缓存的名字
	 * @param keyStr 已存入缓存中的key
	 * @param defaultValue 缓存中对应参数key的默认值
	 * @return 缓存中对应参数key的value
	 */
	public static boolean getBooleanValueFromSP(String spName, String keyStr, Boolean defaultValue) {
		SharedPreferences sp = mContext.getSharedPreferences(spName, MODE_PRIVATE);
		return sp.getBoolean(keyStr, defaultValue);
	}

	/**
	 * 获取缓存中的一个Long数据
	 * @param spName 缓存的名字
	 * @param keyStr 已存入缓存中的key
	 * @return 缓存中对应参数key的value
	 */
	public static Long getLongValueFromSP(String spName, String keyStr) {
		return getLongValueFromSP(spName, keyStr, 0l);
	}

	/**
	 * 获取缓存中的一个Long数据
	 * @param spName 缓存的名字
	 * @param keyStr 已存入缓存中的key
	 * @param defaultValue 缓存中对应参数key的默认值
	 * @return 缓存中对应参数key的value
	 */
	public static Long getLongValueFromSP(String spName, String keyStr, Long defaultValue) {
		SharedPreferences sp = mContext.getSharedPreferences(spName, MODE_PRIVATE);
		return sp.getLong(keyStr, 0l);
	}

	/**
	 * 将键值对数组，存入到缓存中
	 * @param spName 缓存的名称
	 * @param keyValueMap 要存入缓存中的键值对
	 */
	public static void setDataIntoSP(String spName, HashMap<String, Object> keyValueMap) {
		SharedPreferences sp = mContext.getSharedPreferences(spName, MODE_PRIVATE);
		Editor editor = sp.edit();
		if(keyValueMap != null && !keyValueMap.isEmpty()) {
			Set<String> keySet = keyValueMap.keySet();
			Iterator<String> iterator = keySet.iterator();
			while (iterator.hasNext()) {
				String key = (String) iterator.next();
				Object value = keyValueMap.get(key);
				if(value.getClass() == String.class) {
					editor.putString(key, (String)value);
				} else if(value.getClass() == Integer.class) {
					editor.putInt(key, (Integer)value);
				} else if(value.getClass() == Boolean.class) {
					editor.putBoolean(key, (Boolean)value);
				} else if(value.getClass() == Long.class) {
					editor.putLong(key, (Long)value);
				} else if(value.getClass() == Float.class) {
					editor.putFloat(key, (Float)value);
				}
			}
			editor.commit();
		}
	}

	/**
	 * 获取多个key值对应的values
	 * @param spName 缓存的名字
	 * @param keyValueMap 要获取的缓存中的key值
	 * @return
	 */
	public static List<Object> getValuesFromSP(String spName, HashMap<String, Object> keyValueMap) {
		List<Object> values = new ArrayList<Object>();
		SharedPreferences sp = mContext.getSharedPreferences(spName, MODE_PRIVATE);
		if(keyValueMap != null && !keyValueMap.isEmpty()) {
			Set<String> keySet = keyValueMap.keySet();
			Iterator<String> iterator = keySet.iterator();
			while (iterator.hasNext()) {
				String key = (String) iterator.next();
				Object value = keyValueMap.get(key);
				if(value == String.class) {
					values.add(sp.getString(key, ""));
				} else if(value == Integer.class) {
					values.add(sp.getInt(key, 0));
				} else if(value == Boolean.class) {
					values.add(sp.getBoolean(key, false));
				} else if(value == Long.class) {
					values.add(sp.getLong(key, 0l));
				} else if(value == Float.class) {
					values.add(sp.getFloat(key, 0.0f));
				}
			}
		}
		return values;
	}

	/**
	 * 获取缓存中所有的数据
	 * @param spName 缓存的数据
	 * @return 
	 */
	public static Map<String, ?> getAllFromSP(String spName) {
		SharedPreferences sp = mContext.getSharedPreferences(spName, MODE_PRIVATE);
		return sp.getAll();
	}

	/**
	 * 验证缓存中是否已经有某个key值
	 * @param spName 缓存的名字
	 * @param key 要查询的key值
	 * @return
	 */
	public static boolean hasKeyInSP(String spName, String key) {
		SharedPreferences sp = mContext.getSharedPreferences(spName, MODE_PRIVATE);
		return sp.contains(key);
	}

	/**
	 * 删除缓存中的某个键值对
	 * @param spName 缓存的名字
	 * @param key 欲删除的缓存中的key值
	 */
	public static void deleteValueInSP(String spName, String key) {
		SharedPreferences sp = mContext.getSharedPreferences(spName, MODE_PRIVATE);
		if(sp.contains(key)) {
			sp.edit().remove(key).commit();
		}
	}

	/**
	 * 删除缓存中的所有值
	 * @param spName 缓存的名字
	 */
	public static void deleteAllInSP(String spName) {
		SharedPreferences sp = mContext.getSharedPreferences(spName, MODE_PRIVATE);
		sp.edit().clear().commit();
	}
}
