package com.example.fertilizercrm.common.httpclient.expand;

import android.content.Context;
import android.util.Log;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;

import com.example.fertilizercrm.common.httpclient.AsyncHttpClient;
import com.example.fertilizercrm.common.httpclient.AsyncHttpResponseHandler;
import com.example.fertilizercrm.common.httpclient.RequestHandle;
import com.example.fertilizercrm.common.httpclient.ResponseHandlerInterface;
import com.example.fertilizercrm.common.httpclient.SyncHttpClient;

public class SyncHttpClientAdapter extends SyncHttpClient {
	public static final String TAG = AsyncHttpClient.class.getSimpleName();

	@Override
	protected RequestHandle sendRequest(DefaultHttpClient client,
			HttpContext httpContext, HttpUriRequest uriRequest,
			String contentType, ResponseHandlerInterface responseHandler,
			Context context) {
		boolean enableLog = true;
		if (responseHandler != null && (responseHandler instanceof AsyncHttpResponseHandler)) {
			enableLog = ((AsyncHttpResponseHandler)responseHandler).isEnableLog();
		}
		if (AsyncHttpClientAdapter.debug && enableLog) {
			Log.i(TAG, "request start: " + uriRequest.getURI());
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
			handler.setRequestHeaders(uriRequest.getAllHeaders());
			handler.setRequestURI(uriRequest.getURI());
			return super.sendRequest(client, httpContext, uriRequest, contentType, handler, context);
		}
		return super.sendRequest(client, httpContext, uriRequest, contentType, responseHandler, context);
	}
}
