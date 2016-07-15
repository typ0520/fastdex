package com.example.fertilizercrm.easemob.chatuidemo.receiver;

import com.example.fertilizercrm.easemob.applib.controller.HXSDKHelper;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class GCMPushBroadCast extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("info", "gcmpush onreceive");
		String alert = intent.getStringExtra("alert");
		sendNotification(alert, true);
	}

	protected NotificationManager notificationManager = null;

	protected static int notifyID = 0525; // start notification id
	protected static int foregroundNotifyID = 0555;

	public void sendNotification(String message, boolean isForeground) {

		Context appContext = HXSDKHelper.getInstance().getAppContext();

		if (notificationManager == null) {
			notificationManager = (NotificationManager) appContext
					.getSystemService(Context.NOTIFICATION_SERVICE);
		}

		try {
			String notifyText = message;

			PackageManager packageManager = appContext.getPackageManager();
			String appname = (String) packageManager
					.getApplicationLabel(appContext.getApplicationInfo());

			// notification titile
			String contentTitle = appname;
			String packageName = appContext.getApplicationInfo().packageName;

			Uri defaultSoundUrlUri = RingtoneManager
					.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			// create and send notificaiton
			NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
					appContext)
					.setSmallIcon(appContext.getApplicationInfo().icon)
					.setSound(defaultSoundUrlUri)
					.setWhen(System.currentTimeMillis()).setAutoCancel(true);

			Intent msgIntent = appContext.getPackageManager()
					.getLaunchIntentForPackage(packageName);

			PendingIntent pendingIntent = PendingIntent.getActivity(appContext,
					notifyID, msgIntent, PendingIntent.FLAG_UPDATE_CURRENT);

			mBuilder.setContentTitle(contentTitle);
			mBuilder.setTicker(notifyText);
			mBuilder.setContentText(notifyText);
			mBuilder.setContentIntent(pendingIntent);
			Notification notification = mBuilder.build();

			notificationManager.notify(notifyID, notification);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
