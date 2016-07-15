package com.example.fertilizercrm.common.httpclient.expand;

import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import com.example.fertilizercrm.common.httpclient.RequestParams;
import com.example.fertilizercrm.common.httpclient.ResponseHandlerInterface;

/**
 * 把上传的数据转换为json格式的
 * 仅支持string类型的转换
 * 例如: key=value    ==>     {"key":"value"}
 * 
 * @author tong
 */
public class JsonRequestParams extends RequestParams {
	public static final String CONTENT_TYPE = "application/json";
	private Object mCriteria;
	/**
	 * Constructs a new empty {@code JsonRequestParams} instance.
	 */
	public JsonRequestParams() {
		this((Map<String, String>) null);
	}
	/**
	 * Constructs a new JsonRequestParams instance from a object
	 * @param params map<String,object> or criteria object
	 */
	public JsonRequestParams(Object params) {
		if (params == null)
			return;
		if (params instanceof Map<?, ?>) {
			urlParamsWithObjects.putAll((Map<String, Object>) params);
		} else {
			this.mCriteria = params;
		}
	}

	/**
	 * Constructs a new JsonRequestParams instance and populate it with a single initial key/value
	 * string param.
	 *
	 * @param key   the key name for the intial param.
	 * @param value the value string for the initial param.
	 */
	public JsonRequestParams(final String key, final String value) {
		put(key,value);
	}
	
	   /**
     * Constructs a new RequestParams instance and populate it with multiple initial key/value
     * string param.
     *
     * @param keysAndValues a sequence of keys and values. Objects are automatically converted to
     *                      Strings (including the value {@code null}).
     * @throws IllegalArgumentException if the number of arguments isn't even.
     */
    public JsonRequestParams(Object... keysAndValues) {
        int len = keysAndValues.length;
        if (len % 2 != 0)
            throw new IllegalArgumentException("Supplied arguments must be even");
        for (int i = 0; i < len; i += 2) {
        	if (!(keysAndValues[i] instanceof CharSequence)) {
        		throw new IllegalArgumentException("invalid key,the type of key must be String " + keysAndValues[i]);
        	}
            String key = String.valueOf(keysAndValues[i]);
            put(key, keysAndValues[i + 1]);
        }
    }

	@Override
	public HttpEntity getEntity(ResponseHandlerInterface progressHandler) throws IOException {
		return createFormEntity();
	}

	@Override
	public void put(String key, Object value) {
		urlParamsWithObjects.put(key, value);
	}

	@Override
	public void put(String key, String value) {
		urlParamsWithObjects.put(key, value);
	}
	
	@Override
	public void add(String key, String value) {
		urlParamsWithObjects.put(key, value);
	}

	@Override
	public void put(String key, File file) throws FileNotFoundException {
		throw new RuntimeException("not support param  file");
	}

	@Override
	public void put(String key, File file, String contentType)
			throws FileNotFoundException {
		throw new RuntimeException("not support param  file");
	}
	
	@Override
	public void put(String key, InputStream stream) {
		throw new RuntimeException("not support param  stream");
	}

	@Override
	public void put(String key, InputStream stream, String name) {
		throw new RuntimeException("not support param  stream");
	}

	@Override
	public void put(String key, InputStream stream, String name,
			String contentType) {
		throw new RuntimeException("not support param  stream");
	}

	private HttpEntity createFormEntity() {
		try {
			JSONObject params_list = new JSONObject();
			Map<String,Object> map = null;
			if (mCriteria != null) {
				map = BeanUtils.optPublicFieldKeyValueMap(mCriteria);
			} else {
				map = urlParamsWithObjects;
			}
			if (map != null) {
				for (Map.Entry<String,Object> entry : map.entrySet()) {
					if (entry.getValue() != null) {
						params_list.put(entry.getKey(), entry.getValue());
					}
				}
			}
			StringEntity entity = null;
			if (params_list.length() > 0) {
				entity = new StringEntity(params_list.toString(),	HTTP.UTF_8);
				entity.setContentType(CONTENT_TYPE);
			}
			return entity;
		} catch (Exception e) {
			return null; // Actually cannot happen when using utf-8
		}
	}
}
