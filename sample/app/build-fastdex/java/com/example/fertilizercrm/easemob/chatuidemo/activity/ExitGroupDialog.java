/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.fertilizercrm.easemob.chatuidemo.activity;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.fertilizercrm.R;

public class ExitGroupDialog extends BaseActivity{
    private TextView text;
    private Button exitBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.logout_actionsheet);
        
        text = (TextView) findViewById(R.id.tv_text);
        exitBtn = (Button) findViewById(R.id.btn_exit);
        
        text.setText(R.string.exit_group_hint);
        String toast = getIntent().getStringExtra("deleteToast");
        if(toast != null)
        	text.setText(toast);
        exitBtn.setText(R.string.exit_group);
    }
    
    public void logout(View view){
    	setResult(RESULT_OK);
        finish();
        
    }
    
    public void cancel(View view) {
        finish();
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        finish();
        return true;
    }
}
