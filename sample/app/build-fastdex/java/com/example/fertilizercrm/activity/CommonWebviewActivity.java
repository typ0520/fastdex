package com.example.fertilizercrm.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import com.example.fertilizercrm.R;
import com.example.fertilizercrm.basic.BaseActivity;

/**
 * 通用的webview页面
 */
public class CommonWebviewActivity extends BaseActivity {
    public static final String URL_KEY = "url";
    public static final String TITLE_KEY = "title";

    private WebView webview;
    private ProgressBar pb;

    private String mUrl;
    private String mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_common_webview);

        mTitle = getIntent().getStringExtra(TITLE_KEY);
        mUrl = getIntent().getStringExtra(URL_KEY);

        getTitleView().setTitle(mTitle);
        webview = (WebView) findViewById(R.id.webview);
        pb = (ProgressBar) findViewById(R.id.pb);
        webview.loadUrl(mUrl);
        //修改帮助的url
        loadWebViewConfig();
        webview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return pb.getVisibility() == View.VISIBLE;
            }
        });
    }

    protected void loadWebViewConfig() {
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setSaveFormData(true);
        webview.getSettings().setBuiltInZoomControls(true);
        webview.setWebViewClient(new MyWebViewClient());
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            pb.setVisibility(View.GONE);
            super.onPageFinished(view, url);
        }
    }

    public static void start(Context context, String title, String url) {
        Intent intent = new Intent(context,CommonWebviewActivity.class);
        intent.putExtra(CommonWebviewActivity.TITLE_KEY,title);
        intent.putExtra(CommonWebviewActivity.URL_KEY, url);
        context.startActivity(intent);
    }
}
