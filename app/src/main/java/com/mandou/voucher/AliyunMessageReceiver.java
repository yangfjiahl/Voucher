/*
 * Copyright (C) 2019 ~ 2020 itech.com. All Rights Reserved.
 *
 */
package com.mandou.voucher;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import com.alibaba.sdk.android.push.MessageReceiver;
import com.alibaba.sdk.android.push.notification.CPushMessage;

import java.util.Map;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

/**
 * @author calvin
 * @version v1.0
 * @date 2020/12/5 21:01
 * @description 推送通知接收器
 */
public class AliyunMessageReceiver extends MessageReceiver {

	public static final String REC_TAG = "AliyunMessageReceiver";

	public static final String CHANNEL_ID = "CHANNEL_ID_1";

	@Override
	public void onNotification(Context context, String title, String summary,
			Map<String, String> extraMap) {
		// TODO 处理推送通知
		Log.e(REC_TAG, "Receive notification, title: " + title + ", summary: " + summary
				+ ", extraMap: " + extraMap);

		notify(context, title, summary);
	}

	private void notify(Context context, String title, String summary) {
		NotificationManager manager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);

		if (Build.VERSION.SDK_INT >= 26) {
			// 当sdk版本大于26
			int importance = NotificationManager.IMPORTANCE_HIGH;
			NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "推送通知",
					importance);
			manager.createNotificationChannel(channel);
			Notification notification = new Notification.Builder(context, CHANNEL_ID)
					.setCategory(Notification.CATEGORY_MESSAGE)
					.setSmallIcon(R.mipmap.ic_launcher).setContentTitle(title)
					.setContentText(summary).setAutoCancel(true).build();
			manager.notify(1, notification);
		}
		else {
			// 当sdk版本小于26
			Notification notification = new NotificationCompat.Builder(context)
					.setContentTitle(title).setContentText(summary)
					.setSmallIcon(R.mipmap.ic_launcher).build();
			manager.notify(1, notification);
		}
	}

	@Override
	public void onMessage(Context context, CPushMessage cPushMessage) {
		Log.e(REC_TAG,
				"onMessage, messageId: " + cPushMessage.getMessageId() + ", title: "
						+ cPushMessage.getTitle() + ", content:"
						+ cPushMessage.getContent());

		notify(context, cPushMessage.getTitle(), cPushMessage.getContent());
	}

	@Override
	public void onNotificationOpened(Context context, String title, String summary,
			String extraMap) {
		Log.e(REC_TAG, "onNotificationOpened, title: " + title + ", summary: " + summary
				+ ", extraMap:" + extraMap);
	}

	@Override
	protected void onNotificationClickedWithNoAction(Context context, String title,
			String summary, String extraMap) {
		Log.e(REC_TAG, "onNotificationClickedWithNoAction, title: " + title
				+ ", summary: " + summary + ", extraMap:" + extraMap);
	}

	@Override
	protected void onNotificationReceivedInApp(Context context, String title,
			String summary, Map<String, String> extraMap, int openType,
			String openActivity, String openUrl) {
		Log.e(REC_TAG,
				"onNotificationReceivedInApp, title: " + title + ", summary: " + summary
						+ ", extraMap:" + extraMap + ", openType:" + openType
						+ ", openActivity:" + openActivity + ", openUrl:" + openUrl);
	}

	@Override
	protected void onNotificationRemoved(Context context, String messageId) {
		Log.e(REC_TAG, "onNotificationRemoved");
	}
}
