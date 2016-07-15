package com.example.fertilizercrm.http;
import com.example.fertilizercrm.common.httpclient.expand.AsyncHttpClientAdapter;

/**
 *
 */
public class H extends AsyncHttpClientAdapter {
	private static final H INSTANCE = new H();

	public H() {
		//addHeader("Accept", "application/json");
		//addHeader("charset", "UTF-8");
		setTimeout(30 * 1000);
	}

	public static H getInstance() {
		return INSTANCE;
	}
}
