package com.example.fertilizercrm.common.utils;

import com.google.gson.Gson;

public class JsonUtils {
	public static<T> T jsonToObject(String json,Class<T> clazz) {
		return new Gson().fromJson(json, clazz);
	}
}
