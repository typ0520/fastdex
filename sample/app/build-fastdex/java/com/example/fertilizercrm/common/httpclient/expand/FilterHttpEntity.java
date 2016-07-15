package com.example.fertilizercrm.common.httpclient.expand;

import org.apache.http.Header;
import org.apache.http.HttpEntity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * filter content
 * @author tong
 *
 */
public class FilterHttpEntity implements HttpEntity {
	private HttpEntity mBase;
	private ByteArrayOutputStream bos;
	
	public FilterHttpEntity(HttpEntity base) {
		this.mBase = base;
		bos = new ByteArrayOutputStream();
	}

	@Override
	public void consumeContent() throws IOException {
		if (mBase != null) {
			mBase.consumeContent();
		}
	}

	@Override
	public InputStream getContent() throws IOException, IllegalStateException {
		return mBase == null ? null : new FilterInputStream(mBase.getContent(), bos);
	}

	@Override
	public Header getContentEncoding() {
		return mBase == null ? null : mBase.getContentEncoding();
	}

	@Override
	public long getContentLength() {
		return mBase == null ? 0 : mBase.getContentLength();
	}

	@Override
	public Header getContentType() {
		return mBase == null ? null : mBase.getContentType();
	}

	@Override
	public boolean isChunked() {
		return mBase == null ? false : mBase.isChunked();
	}

	@Override
	public boolean isRepeatable() {
		return mBase == null ? false : mBase.isRepeatable();
	}

	@Override
	public boolean isStreaming() {
		return mBase == null ? false : mBase.isStreaming();
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		if (mBase != null) {
			mBase.writeTo(out);
		}
	}
	
	public byte[] toByteArray() {
		return bos.toByteArray();
	}
}
