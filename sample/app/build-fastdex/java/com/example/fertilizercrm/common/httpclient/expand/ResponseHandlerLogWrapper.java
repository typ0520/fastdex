package com.example.fertilizercrm.common.httpclient.expand;

import android.text.TextUtils;
import android.util.Log;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.util.ByteArrayBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URI;
import java.net.URLDecoder;
import java.text.DecimalFormat;

import com.example.fertilizercrm.common.httpclient.AsyncHttpResponseHandler;
import com.example.fertilizercrm.common.httpclient.ResponseHandlerInterface;

/**
 * 用于日志打印的包装类
 * @author 佟亚鹏
 *
 */
public class ResponseHandlerLogWrapper implements ResponseHandlerInterface {
	protected String mMarker = "bocop_request";
	private ResponseHandlerInterface mBase;
	private String mRequestMethod;
	private FilterHttpEntity mBodyHttpEntity;
	private long mStartTime,mFinishTime;

	public ResponseHandlerLogWrapper(ResponseHandlerInterface base) {
		this.mBase = base;
		if (base instanceof MarkInterface) {
			String marker  = ((MarkInterface)base).getMarker();
			if (!TextUtils.isEmpty(marker)) 
				this.mMarker = marker;
		}
	}

	@Override
	public void sendResponseMessage(HttpResponse response) throws IOException {
		FilterHttpEntity httpEntity = null;
		try {
			httpEntity = new FilterHttpEntity(response.getEntity());
			response.setEntity(httpEntity);
			mBase.sendResponseMessage(response);
		} finally {
			mFinishTime = System.currentTimeMillis();
			if (!Thread.currentThread().isInterrupted()) {
				StatusLine status = response.getStatusLine();
				byte[] responseBody = httpEntity.toByteArray();
				if (!Thread.currentThread().isInterrupted()) {
					StringWriter writer = new StringWriter();
					if (status.getStatusCode() >= 300) {
						writerLog(status.getStatusCode(),response.getAllHeaders(),responseBody,new HttpResponseException(status.getStatusCode(), status.getReasonPhrase()),writer);
					} else {
						writerLog(status.getStatusCode(),response.getAllHeaders(),responseBody,null,writer);
					}

					String[] strings = writer.toString().split("\n");
					if (strings != null) {
						for (String str : strings) {
							Log.e(AsyncHttpClientAdapter.TAG, str);
						}
					}
				}
			}
		}
	}


	@Override
	public void sendStartMessage() {
		mStartTime = System.currentTimeMillis();
		if (mBase != null) {
			mBase.sendStartMessage();
		}
	}

	@Override
	public void sendFinishMessage() {
		if (mBase != null) {
			mBase.sendFinishMessage();
		}
	}

	@Override
	public void sendProgressMessage(int bytesWritten, int bytesTotal) {
		if (mBase != null) {
			mBase.sendProgressMessage(bytesWritten, bytesTotal);
		}
	}

	@Override
	public void sendSuccessMessage(int statusCode, Header[] headers,byte[] responseBody) {
		if (mBase != null) {
			mBase.sendSuccessMessage(statusCode, headers, responseBody);
		}
	}

	@Override
	public void sendFailureMessage(int statusCode, Header[] headers,byte[] responseBody, Throwable error) {
		mFinishTime = System.currentTimeMillis();
		
		StringWriter writer = new StringWriter();
		writerLog(statusCode,headers,responseBody,error,writer);
		Log.i(AsyncHttpClientAdapter.TAG, writer.getBuffer().toString());

		if (mBase != null) {
			mBase.sendFailureMessage(statusCode, headers, responseBody, error);
		}
	}

	@Override
	public void sendRetryMessage(int retryNo) {
		if (mBase != null) {
			mBase.sendRetryMessage(retryNo);
		}
	}

	@Override
	public URI getRequestURI() {
		return mBase == null ? null : mBase.getRequestURI();
	}

	@Override
	public Header[] getRequestHeaders() {
		return mBase == null ? null : mBase.getRequestHeaders();
	}

	@Override
	public void setRequestURI(URI requestURI) {
		if (mBase != null) {
			mBase.setRequestURI(requestURI);
		}
	}

	@Override
	public void setRequestHeaders(Header[] requestHeaders) {
		if (mBase != null) {
			mBase.setRequestHeaders(requestHeaders);
		}
	}

	@Override
	public void setUseSynchronousMode(boolean useSynchronousMode) {
		if (mBase != null) {
			mBase.setUseSynchronousMode(useSynchronousMode);
		}
	}

	@Override
	public boolean getUseSynchronousMode() {
		return mBase == null ? false : mBase.getUseSynchronousMode();
	}

	public String getRequestMethod() {
		return mRequestMethod;
	}

	public void setRequestMethod(String requestMethod) {
		this.mRequestMethod = requestMethod;
	}

	public FilterHttpEntity getBodyHttpEntity() {
		return mBodyHttpEntity;
	}

	public void setBodyHttpEntity(FilterHttpEntity httpEntity) {
		this.mBodyHttpEntity = httpEntity;
	}

	/**
	 * 设置这个qing请求的标记，方便debug时看日志方便
	 * @param marker
	 */
	public void setMarker(String marker) {
		this.mMarker = marker;
	}

	public String getMarker() {
		return TextUtils.isEmpty(mMarker) ? "" : mMarker;
	}

	/**
	 * 得到请求用的毫秒数
	 * @return
	 */
	public long getRequestTime() {
		return (mFinishTime - mStartTime);
	}

	protected void writerLog(int statusCode, Header[] responseHeaders,byte[] responseBody, Throwable error, StringWriter writer) {
		try {
			writerSplitLine(mMarker,writer);
			writer.write("\n");

			writerRequestUrl(getRequestURI(),writer);
			writer.write("\n");

			writerState(getRequestMethod(),statusCode,error,responseBody,writer);
			writer.write("\n");

			writerRequestHeader(getRequestHeaders(),writer);
			writer.write("\n");

			writerRequestParams(getRequestURI(),writer);
			writer.write("\n");

			if (error != null) {
				writerException(error,writer);
			}
			writerResponse(responseBody,getResponseCharset(responseHeaders),writer);
			writer.write("\n");
			writer.write("---end---");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void writerException(Throwable error, Writer writer) throws IOException {
		writer.write("Exception: \n");
		writer.write(error.getMessage());
		writer.write("\n");
	}

	protected String getResponseCharset(Header[] responseHeaders) {
		if (mBase != null && mBase instanceof AsyncHttpResponseHandler) {
			AsyncHttpResponseHandler responseHandler = (AsyncHttpResponseHandler) mBase;
			return responseHandler.getCharset();
		}
		return "UTF-8";
	}

	private void writerResponse(byte[] responseBody,String charset,Writer writer) throws UnsupportedEncodingException, IOException {
		if (responseBody != null && responseBody.length > 0) {
			writer.write("Response: \n");
			String response = new String(responseBody);
			if (response != null) {
				response = URLDecoder.decode(response,"UTF-8");
			}
			writer.write(response.length() > 2 ? response.substring(0, response.length() - 2) : response);
			writer.write("\n");
		}
	}

	protected void writerSplitLine(String marker, Writer writer) throws IOException {
		writer.write("----------" + marker + " start----------");
	}

	protected void writerRequestUrl(URI requestURI, Writer writer) throws IOException {
		writer.write(requestURI.toString());
	}

	protected void writerRequestHeader(Header[] headers,Writer writer) throws IOException {
		if (headers != null && headers.length > 0) {
			writer.write("Request Headers: \n");
			int idx = 0;
			for (Header header : headers) {
				writer.write(header.getName() + "=" + header.getValue());
				if (idx != headers.length - 1) {
					writer.write(",");
				}
				idx++;
			}
			writer.write("\n");
		}
	}

	protected void writerRequestParams(URI requestURI, Writer writer) throws IOException {
		if ("post".equals(getRequestMethod().toLowerCase()) || "put".equals(getRequestMethod().toLowerCase())) {
			writer.write(getRequestMethod() + " body: \n");
			if (getBodyHttpEntity() != null) {
				String body = new String(getHttpEntityData(getBodyHttpEntity())); 
				writer.write(body);
				writer.write("\n");
			} 
			writer.write("\n");
		} else {
			writer.write(getRequestMethod() + " params: " + (TextUtils.isEmpty(requestURI.getQuery()) ? "empty" : requestURI.getQuery()));
		}
	}

	protected void writerState(String method,int statusCode, Throwable error, byte[] responseBody, Writer writer) throws IOException {
		double dtime = getRequestTime() > 0 ? getRequestTime() : (System.currentTimeMillis() - mStartTime);
		double time = dtime / 1000;
		StringBuffer sb = new StringBuffer();
		sb.append("method: " + method);
		sb.append(",statusCode: " + statusCode);
		if (time > 1.0d) {
			sb.append(",use: " + new DecimalFormat("0.0").format(time) + "s");
		} else {
			sb.append(",use: " + dtime + "ms");
		}
		sb.append(",Request: " + (error == null ? "success  ^_^" : "failure  v_v"));
		writer.write(sb.toString());
	}

	byte[] getHttpEntityData(HttpEntity entity) throws IOException {
		byte[] responseBody = null;
		if (entity != null) {
			InputStream instream = entity.getContent();
			if (instream != null) {
				long contentLength = entity.getContentLength();
				if (contentLength > Integer.MAX_VALUE) {
					throw new IllegalArgumentException("HTTP entity too large to be buffered in memory");
				}
				if (contentLength < 0) {
					contentLength = 4096;
				}
				try {
					ByteArrayBuffer buffer = new ByteArrayBuffer((int) contentLength);
					try {
						byte[] tmp = new byte[4096];
						int len = 0;
						// do not send messages if request has been cancelled
						while ((len = instream.read(tmp)) != -1 && !Thread.currentThread().isInterrupted()) {
							buffer.append(tmp, 0, len);
						}
					} finally {
						instream.close();
					}
					responseBody = buffer.toByteArray();
				} catch (OutOfMemoryError e) {
					System.gc();
					throw new IOException("File too large to fit into available memory");
				}
			}
		}
		return responseBody;
	}
}
