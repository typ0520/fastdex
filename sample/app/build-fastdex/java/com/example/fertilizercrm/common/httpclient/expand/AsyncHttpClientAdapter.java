package com.example.fertilizercrm.common.httpclient.expand;

import android.content.Context;
import android.util.Log;
import org.apache.http.Header;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;

import java.util.Map;

import com.example.fertilizercrm.common.httpclient.AsyncHttpClient;
import com.example.fertilizercrm.common.httpclient.AsyncHttpResponseHandler;
import com.example.fertilizercrm.common.httpclient.RequestHandle;
import com.example.fertilizercrm.common.httpclient.ResponseHandlerInterface;

/**
 * AsyncHttpClient的适配层，用于放置改动的内容
 * @author tong
 *
 */
public class AsyncHttpClientAdapter extends AsyncHttpClient {
	public static final String TAG = AsyncHttpClient.class.getSimpleName();
	public static boolean debug = false;

	public AsyncHttpClientAdapter() {
		super(true, 80, 443);
	}

	public AsyncHttpClientAdapter(int httpPort) {
		super(true, httpPort, 443);
	}

	public AsyncHttpClientAdapter(int httpPort, int httpsPort) {
		super(true, httpPort, httpsPort);
	}

	public static void setDebug(boolean debug) {
		AsyncHttpClientAdapter.debug = debug;
	}

	@Override
	protected RequestHandle sendRequest(DefaultHttpClient client,
			HttpContext httpContext, HttpUriRequest uriRequest,
			String contentType, ResponseHandlerInterface responseHandler,
			Context context) {

		boolean enableLog = true;
		if (responseHandler != null && (responseHandler instanceof AsyncHttpResponseHandler)) {
			enableLog = ((AsyncHttpResponseHandler)responseHandler).isEnableLog();
		}
		if (debug && enableLog) {
			Log.e(TAG, "request start: " + uriRequest.getURI());
			ResponseHandlerLogWrapper handler = new ResponseHandlerLogWrapper(responseHandler);
			handler.setRequestMethod(uriRequest.getMethod());
			if (uriRequest instanceof HttpEntityEnclosingRequestBase) {
				HttpEntityEnclosingRequestBase requestBase = (HttpEntityEnclosingRequestBase) uriRequest;
				if (requestBase.getEntity() != null) {
					FilterHttpEntity httpEntity = new FilterHttpEntity(requestBase.getEntity());
					requestBase.setEntity(httpEntity);
					handler.setBodyHttpEntity(httpEntity);
				}
			}
			return super.sendRequest(client, httpContext, uriRequest, contentType, handler, context);
		}
		return super.sendRequest(client, httpContext, uriRequest, contentType, responseHandler, context);
	}
	
	public static Header[] map2headerArray(Map<String, String> headerMap) {
		Header[] headers = null;
		if (headerMap != null) {
			headers = new Header[headerMap.size()];
			int i = 0;
			for (Map.Entry<String, String> headerKeyValue : headerMap.entrySet()) {
				headers[i++] = new BasicHeader(headerKeyValue.getKey(), headerKeyValue.getValue());
			}
		}
		return headers;
	}
}
