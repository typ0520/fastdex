package com.example.fertilizercrm.easemob.chatuidemo.activity;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.FileMessageBody;
import com.example.fertilizercrm.R;
import com.easemob.util.FileUtils;

public class ShowNormalFileActivity extends BaseActivity {
	private ProgressBar progressBar;
	private File file;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_file);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);

		final FileMessageBody messageBody = getIntent().getParcelableExtra("msgbody");
		file = new File(messageBody.getLocalUrl());
		//set head map
		final Map<String, String> maps = new HashMap<String, String>();
		if (!TextUtils.isEmpty(messageBody.getSecret())) {
			maps.put("share-secret", messageBody.getSecret());
		}
		
		//下载文件
		EMChatManager.getInstance().downloadFile(messageBody.getRemoteUrl(), messageBody.getLocalUrl(), maps,
                new EMCallBack() {
                    
                    @Override
                    public void onSuccess() {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                FileUtils.openFile(file, ShowNormalFileActivity.this);
                                finish();
                            }
                        });
                    }
                    
                    @Override
                    public void onProgress(final int progress,String status) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                progressBar.setProgress(progress);
                            }
                        });
                    }
                    
                    @Override
                    public void onError(int error, final String msg) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                if(file != null && file.exists()&&file.isFile())
                                    file.delete();
                                String str4 = getResources().getString(R.string.Failed_to_download_file);
                                Toast.makeText(ShowNormalFileActivity.this, str4+msg, Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });
                    }
                });
		
	}
}
