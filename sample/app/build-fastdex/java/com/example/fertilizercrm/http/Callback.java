package com.example.fertilizercrm.http;

import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;
import android.widget.Toast;
import com.example.fertilizercrm.FertilizerApplication;
import com.example.fertilizercrm.activity.LoginActivity;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import com.example.fertilizercrm.common.httpclient.TextHttpResponseHandler;
import com.example.fertilizercrm.common.utils.JsonUtils;
import com.example.fertilizercrm.common.utils.Logger;
import com.example.fertilizercrm.common.utils.ReflectionUtil;


/**
 * 项目中用到的异步请求处理器的基类，封装了一些与业务相关的逻辑
 * 
 * @author tong
 * 
 * @param <T> 服务器返回的数据目标转换类型
 */
public abstract class Callback<T> extends TextHttpResponseHandler {
    public static final String ERROR_CODE_SESSION_TIMEOUT = "-401";
	public Type mType;
	private Gson mGson;

	public Callback() {
		mType = ReflectionUtil.getGenericFirstType(getClass());

		final int sdk = Build.VERSION.SDK_INT;
		if (sdk >= 23)
		{
			GsonBuilder gsonBuilder = new GsonBuilder()
					.excludeFieldsWithModifiers(
							Modifier.FINAL,
							Modifier.TRANSIENT,
							Modifier.STATIC);
			mGson = gsonBuilder.create();
		} else
		{
			mGson = new Gson();
		}
		Logger.d("mType: " + mType);
	}

	/**
	 * Base abstract method, handling defined generic type
	 * 
	 * @param statusCode
	 *            HTTP 状态码
	 * @param headers
	 *            服务器返回信息
	 * @param response
	 *            服务器返回的json数据转换后的JavaBean
	 */
	public abstract void onSuccess(int statusCode, Header[] headers, T response);

    @Override
    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
        Toast.makeText(FertilizerApplication.getInstance(), "网络异常", Toast.LENGTH_SHORT).show();
		throwable.printStackTrace();
    }


    /**
	 * Base abstract method, 处理服务器返回的业务相关的错误信息
	 * 
	 * @param statusCode
	 *            HTTP 状态码
	 * @param headers
	 *            响应头
	 */
	public void onError(int statusCode, Header[] headers, BizError error) {
		Toast.makeText(FertilizerApplication.getInstance(), error.getMessage(), Toast.LENGTH_SHORT).show();
	}

	@Override
	public final void onSuccess(final int statusCode, final Header[] headers,final String responseString) {
		if (statusCode != HttpStatus.SC_NO_CONTENT) {
			try {
				if (!isError(responseString)) {
					T response = null;
					try {
						response = parseResponse(responseString);
					} catch (final Exception e) {
						fireParseFailure(statusCode, headers, responseString, e);
						return;
					}
					onSuccess(statusCode, headers, response);
				} else {
					try {
						final BizError error = parseError(responseString);
                        if (error.getCode().equals(ERROR_CODE_SESSION_TIMEOUT)) {
                            Intent intent = new Intent(FertilizerApplication.getInstance(),LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(LoginActivity.FLAG_TO_LAUNCH_WHEN_BACK);
							FertilizerApplication.getInstance().startActivity(intent);
                        }else{
                        	onError(statusCode, headers, error);
                        }
					} catch (final RuntimeException e) {
						fireParseFailure(statusCode, headers, responseString, e);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				onFailure(statusCode, headers, responseString, e);
			}
		} else {
			onSuccess(statusCode, headers,(T)null);
		}
	}

	private void fireParseFailure(final int statusCode, final Header[] headers,final String response, final Throwable t) {
		onFailure(statusCode, headers, response, t);
	}

	/**
	 * 服务器返回的数据，是不是业务相关的错误信息
	 *
	 * @param response
	 * @return
	 */
	public boolean isError(String response) {
		try {
			return new JSONObject(response).optInt("code") != 200;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 把Json错误信息转换成ResponseError
	 * 
	 * @param response
	 * @return
	 */
	public BizError parseError(String response) {
		BizError error = null;
		try {
			error = JsonUtils.jsonToObject(response, BizError.class);
			if (error == null) {
				throw new RuntimeException("解析出的ResponseError对象为null");
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return error;
	}

	/**
	 * 把服务器返回的json数据转换为类型为type的JavaBean
	 * 
	 * @param response
	 * @return
	 */
	public T parseResponse(String response) {
		try {
			if (TextUtils.isEmpty(response))
				return null;
			JSONObject jsonObject = (JSONObject)(new JSONTokener(response).nextValue());
			String bodyString = jsonObject.optString("body");
			if (mType.equals(JSONObject.class)) {
				JSONObject result = new JSONObject(bodyString);
				if (!result.has("message")) {
					result.put("message",jsonObject.optString("message"));
				}
				if (!result.has("code")) {
					result.put("code",jsonObject.optString("code"));
				}
				return (T)(result);
			}
			if (mType.equals(JSONArray.class)) {
				return (T)(new JSONArray(bodyString));
			}
			else {
				return mGson.fromJson(bodyString, mType);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return null;
	}
}
