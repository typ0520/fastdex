package com.example.fertilizercrm.component;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.example.fertilizercrm.activity.MessageCenterActivity;

import cn.jpush.android.api.JPushInterface;

/**
 * Created by tong on 15/9/24.
 */
public class JPushReceiver extends BroadcastReceiver {
    private static final String TAG = "JPush";

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        Log.d(TAG, "[MyReceiver] onReceive - " + intent.getAction() + ", extras: " + printBundle(bundle));
        if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
            String content = bundle.getString("cn.jpush.android.ALERT");
            if (!TextUtils.isEmpty(content)) {
                Log.d(TAG,">> 推送的信息: " + content);
            }
        }
        else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
            Log.d(TAG, "[MyReceiver] 用户点击打开了通知");
            //打开自定义的Activity
            Intent i = new Intent(context, MessageCenterActivity.class);
            i.putExtras(bundle);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP );
            context.startActivity(i);
        }

//        onReceive - cn.jpush.android.intent.NOTIFICATION_RECEIVED, extras:
//        key:cn.jpush.android.PUSH_ID, ss value:812186040
//        key:cn.jpush.android.ALERT, ss value:test
//        key:cn.jpush.android.EXTRA, ss value:{}
//        key:cn.jpush.android.NOTIFICATION_ID, value:812186040
//        key:cn.jpush.android.NOTIFICATION_CONTENT_TITLE, ss value:肥肥
//        key:cn.jpush.android.MSG_ID, ss value:812186040


//        if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
//            String regId = bundle.getString(JPushInterface.EXTRA_REGISTRATION_ID);
//            Log.d(TAG, "[MyReceiver] 接收Registration Id : " + regId);
//            //send the Registration Id to your server...
//
//            Toast.makeText(ProjectApplication.getInstance(),regId + "",Toast.LENGTH_LONG).show();
//        } else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
//            Log.d(TAG, "[MyReceiver] 接收到推送下来的自定义消息: " + bundle.getString(JPushInterface.EXTRA_MESSAGE));
//
//            Toast.makeText(ProjectApplication.getInstance(),bundle.getString(JPushInterface.EXTRA_MESSAGE),Toast.LENGTH_LONG).show();
//            processCustomMessage(context, bundle);
//
//        } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
//            Log.d(TAG, "[MyReceiver] 接收到推送下来的通知");
//            int notifactionId = bundle.getInt(JPushInterface.EXTRA_NOTIFICATION_ID);
//            Log.d(TAG, "[MyReceiver] 接收到推送下来的通知的ID: " + notifactionId);
//            Toast.makeText(ProjectApplication.getInstance(),notifactionId + "",Toast.LENGTH_LONG).show();
//
//        } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
//            Log.d(TAG, "[MyReceiver] 用户点击打开了通知");
//
//            //打开自定义的Activity
//            Intent i = new Intent(context, JPushReceiver.class);
//            i.putExtras(bundle);
//            //i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP );
//            context.startActivity(i);
//
//        } else if (JPushInterface.ACTION_RICHPUSH_CALLBACK.equals(intent.getAction())) {
//            Log.d(TAG, "[MyReceiver] 用户收到到RICH PUSH CALLBACK: " + bundle.getString(JPushInterface.EXTRA_EXTRA));
//            //在这里根据 JPushInterface.EXTRA_EXTRA 的内容处理代码，比如打开新的Activity， 打开一个网页等..
//
//        } else if(JPushInterface.ACTION_CONNECTION_CHANGE.equals(intent.getAction())) {
//            boolean connected = intent.getBooleanExtra(JPushInterface.EXTRA_CONNECTION_CHANGE, false);
//            Log.w(TAG, "[MyReceiver]" + intent.getAction() +" connected state change to "+connected);
//        } else {
//            Log.d(TAG, "[MyReceiver] Unhandled intent - " + intent.getAction());
//        }
    }

    // 打印所有的 intent extra 数据
    private static String printBundle(Bundle bundle) {
        StringBuilder sb = new StringBuilder();
        for (String key : bundle.keySet()) {
            if (key.equals(JPushInterface.EXTRA_NOTIFICATION_ID)) {
                sb.append("\nkey:" + key + ", value:" + bundle.getInt(key));
            }else if(key.equals(JPushInterface.EXTRA_CONNECTION_CHANGE)){
                sb.append("\nkey:" + key + ", value:" + bundle.getBoolean(key));
            }
            else {
                sb.append("\nkey:" + key + ", ss value:" + bundle.getString(key));
            }
        }
        return sb.toString();
    }

    //send msg to MainActivity
    private void processCustomMessage(Context context, Bundle bundle) {

    }
}
