package com.example.fertilizercrm.common.httpclient.expand;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * filter stream
 * @author tong
 *
 */
public class FilterInputStream extends InputStream {
	protected volatile InputStream in; //将要被装饰的字节输入流
	private OutputStream out;

	protected FilterInputStream(InputStream in,OutputStream out) { //通过构造方法传入此被装饰的流
		this.in = in;
		this.out = out;
	} 

	@Override
	public int read() throws IOException {
		int result = in.read();
		out.write(result);
		return result;
	}

	@Override
	public void close() throws IOException {
		in.close();
	}

	@Override
	public int available() throws IOException {
		return in.available();
	}

	@Override
	public void mark(int readlimit) {
		in.mark(readlimit);
	}

	@Override
	public boolean markSupported() {
		return in.markSupported();
	}

	@Override
	public long skip(long byteCount) throws IOException {
		return in.skip(byteCount);
	}
}
