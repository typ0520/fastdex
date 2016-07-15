package com.example.fertilizercrm.easemob.chatuidemo.activity;

import com.easemob.chat.EMChatManager;
import com.example.fertilizercrm.R;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class OfflinePushNickActivity extends BaseActivity {

	private EditText inputNickName;
	private Button saveNickName;
	private TextView nicknameDescription;
	private ProgressDialog dialog;

	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		setContentView(R.layout.activity_offline_push);

		inputNickName = (EditText) findViewById(R.id.et_input_nickname);
		saveNickName = (Button) findViewById(R.id.btn_save);
		nicknameDescription = (TextView) findViewById(R.id.tv_nickname_description);

		saveNickName.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				dialog = ProgressDialog.show(OfflinePushNickActivity.this, "update nickname...", "waiting...");
				new Thread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						boolean updatenick = EMChatManager.getInstance().updateCurrentUserNick(
								inputNickName.getText().toString());
						if (!updatenick) {
							runOnUiThread(new Runnable() {
								public void run() {
									Toast.makeText(OfflinePushNickActivity.this, "update nickname failed!",
											Toast.LENGTH_SHORT).show();
									dialog.dismiss();
								}
							});
						} else {
							runOnUiThread(new Runnable() {
								public void run() {
									dialog.dismiss();
									Toast.makeText(OfflinePushNickActivity.this, "update nickname success!",
											Toast.LENGTH_SHORT).show();
								}
							});
							finish();
						}
					}
				}).start();
			}
		});

		inputNickName.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				if (s.toString().length() > 0) {
					nicknameDescription.setTextColor(Color.RED);
				}else{
					nicknameDescription.setTextColor(Color.parseColor("#cccccc"));
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub

			}
		});
	}
}
