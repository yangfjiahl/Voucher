package com.mandou.voucher;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.sdk.android.push.CloudPushService;
import com.alibaba.sdk.android.push.CommonCallback;
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory;
import com.mandou.acp.sdk.AcpClient;
import com.mandou.acp.sdk.AcpClientConfig;
import com.mandou.acs.sdk.AcsClient;
import com.mandou.acs.sdk.AcsClientConfig;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by calvin on 2019/3/30.
 */

public class VoucherApp extends Application {

	private static String TAG;

	private static final String TOKEN = "TOKEN";

	@Override
	public void onCreate() {
		super.onCreate();

		TAG = getClass().getSimpleName();

		PreferenceHelper.init(this);
		SessionHelper.init(this);

		// API: check whether token is expired or not
		checkLogin();

		// API: Statistics navigate pages
		// ActionHelper.init(this);
		// this.registerActivityLifecycleCallbacks(navigateAction());

		// Statistics SDK:
		AcsClient.sharedInstance().init(this, new AcsClientConfig("20190307001", "123"))
				.setLoggingEnabled();

		AcpClient.sharedInstance().init(this, new AcpClientConfig("20190307001", "123"));

		initAliyunPushChannel(this);
	}

	private void initAliyunPushChannel(Context applicationContext) {
		PushServiceFactory.init(applicationContext);
		CloudPushService pushService = PushServiceFactory.getCloudPushService();
		pushService.register(applicationContext, new CommonCallback() {
			@Override
			public void onSuccess(String response) {
				Log.d(TAG, "init cloudchannel success, deviceId=" + pushService.getDeviceId());

				// 添加标签
				pushService.bindTag(CloudPushService.DEVICE_TARGET,
						new String[] { "anonymous" }, null, new CommonCallback() {
							@Override
							public void onSuccess(String s) {

							}

							@Override
							public void onFailed(String s, String s1) {

							}
						});
			}

			@Override
			public void onFailed(String errorCode, String errorMessage) {
				Log.d(TAG, "init cloudchannel failed -- errorcode:" + errorCode
						+ " -- errorMessage:" + errorMessage);
			}
		});
	}

	private void checkLogin() {
		String tokenStr = PreferenceHelper.getValue(TOKEN);
		if (tokenStr != null && !tokenStr.isEmpty()) {
			JSONObject json = JSONObject.parseObject(tokenStr);
			String token = json.getString("token");

			HttpUrl.Builder urlBuilder = HttpUrl.parse(Api.buildUrl(Api.CHECK_LOGIN))
					.newBuilder();
			urlBuilder.addQueryParameter("token", token);

			Request request = new Request.Builder().url(urlBuilder.build())
					.headers(PayToolInfo.headers).build();
			Call call = Api.getClient().newCall(request);
			call.enqueue(new Callback() {

				@Override
				public void onFailure(Call call, IOException e) {

				}

				@Override
				public void onResponse(Call call, Response response) throws IOException {
					String s = response.body().string();

					Log.d(TAG, "onResponse: " + s);

					JSONObject result = JSON.parseObject(s);

					if (!result.getBoolean("data")) {
						// token expired, delete it
						PreferenceHelper.remove(TOKEN);
					}
					else {
						SessionHelper.startSession();
					}
				}
			});
		}
		else {
			Log.d(TAG, "No credential");
		}
	}

	/**
	 * 页面跳转监听统计
	 */
	private static Application.ActivityLifecycleCallbacks navigateAction() {

		Application.ActivityLifecycleCallbacks activityLifecycleCallbacks = new Application.ActivityLifecycleCallbacks() {
			@Override
			public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

			}

			@Override
			public void onActivityStarted(Activity activity) {
			}

			@Override
			public void onActivityResumed(Activity activity) {
				Log.v(TAG, activity.getClass().getName() + "onActivityResumed");

				ActionModel actionModel = new ActionModel();
				actionModel.setPageName(activity.getClass().getSimpleName());

				if (activity instanceof HasPayment) {
					actionModel.setPaymentPage(((HasPayment) activity).isPaymentPage());
				}

				ActionHelper.reportAction(actionModel);
			}

			@Override
			public void onActivityPaused(Activity activity) {

			}

			@Override
			public void onActivityStopped(Activity activity) {

			}

			@Override
			public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

			}

			@Override
			public void onActivityDestroyed(Activity activity) {

			}
		};

		return activityLifecycleCallbacks;
	}
}
