/**
 * Copyright (C) 2019 ~ 2020 itech.com. All Rights Reserved.
 */
package com.itech.acs.sdk;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author: YangFeng(calvin)
 * @date: 2020/5/24 20:28
 * @description: 网络线程
 * @version: v1.0
 */
public class ConnectionThread implements Runnable {

    private static final int CONNECT_TIMEOUT_IN_MILLISECONDS = 30000;
    private static final int READ_TIMEOUT_IN_MILLISECONDS = 30000;

    /**
     * 缓冲的事件
     */
    private static final List<Map<String, Object>> storedEvents = new ArrayList<>();

    /**
     * 创建事件
     *
     * @param jsonEvent
     */
    public void createEvent(Map<String, Object> jsonEvent) {
        storedEvents.add(jsonEvent);
    }

    @Override
    public void run() {
        int n = 3;
        while (n-- > 0) {
            if (!storedEvents.isEmpty()) {
                Map<String, Object> params = storedEvents.get(0);

                if (AcsClient.sharedInstance().isLoggingEnabled()) {
                    Log.d(AcsClient.TAG, params.toString());
                }

                String api = (String) params.get(Consts.API);
                params.remove(Consts.API);

                String deviceId = SystemUtil.getDeviceId(AcsClient.sharedInstance().getAcsClientConfig().context);
                params.put("deviceId", deviceId);

                String customerId = PreferenceHelper.getValue(Consts.CUSTOMER_IDENTITY);
                params.put("customerIdentity", customerId == null ? deviceId : customerId);

                JSONObject json = new JSONObject();
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    try {
                        json.put(entry.getKey(), entry.getValue());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                send(json.toString(), api);
            } else {
                try {
                    Thread.sleep(10 * 100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private enum RequestResult {
        OK,
        RETRY,
        REMOVE
    }

    /**
     * 输出日志
     *
     * @param s
     */
    private void log(String s) {
        if (AcsClient.sharedInstance().isLoggingEnabled()) {
            Log.d(AcsClient.TAG, "onActivityCreated," + s);
        }
    }

    /**
     * 发送数据
     *
     * @param eventData
     * @param endPoint
     */
    private void send(String eventData, String endPoint) {
        URLConnection conn = null;
        InputStream connInputStream = null;
        try {
            conn = urlConnectionForServerRequest(eventData, endPoint);
            conn.connect();

            int responseCode = 0;
            String responseString = "";
            if (conn instanceof HttpURLConnection) {
                final HttpURLConnection httpConn = (HttpURLConnection) conn;

                try {
                    connInputStream = httpConn.getInputStream();
                } catch (Exception ex) {
                    connInputStream = httpConn.getErrorStream();
                }

                responseCode = httpConn.getResponseCode();
                responseString = Utils.inputStreamToString(connInputStream);
            }

            if (AcsClient.sharedInstance().isLoggingEnabled()) {
                log("code:[" + responseCode + "], response:[" + responseString + "], request: " + eventData);
            }

            final RequestResult rRes;

            if (responseCode >= 200 && responseCode < 300) {
                if (responseString.isEmpty()) {
                    if (AcsClient.sharedInstance().isLoggingEnabled()) {
                        log("Response was empty, assuming a success");
                    }
                    rRes = RequestResult.OK;
                } else if (responseString.contains("success")) {
                    if (AcsClient.sharedInstance().isLoggingEnabled()) {
                        log("Response was a success");
                    }
                    rRes = RequestResult.OK;
                } else {
                    if (AcsClient.sharedInstance().isLoggingEnabled()) {
                        log("Response was a unknown, will retry request");
                    }
                    rRes = RequestResult.RETRY;
                }
            } else if (responseCode >= 300 && responseCode < 400) {
                if (AcsClient.sharedInstance().isLoggingEnabled()) {
                    log("Encountered redirect, will retry");
                }
                rRes = RequestResult.RETRY;
            } else if (responseCode == 400 || responseCode == 404) {
                if (AcsClient.sharedInstance().isLoggingEnabled()) {
                    log("Bad request, will be dropped");
                }
                rRes = RequestResult.REMOVE;
            } else if (responseCode > 400) {
                if (AcsClient.sharedInstance().isLoggingEnabled()) {
                    log("Server is down, will retry");
                }
                rRes = RequestResult.RETRY;
            } else {
                if (AcsClient.sharedInstance().isLoggingEnabled()) {
                    log("Bad response code, will retry");
                }
                rRes = RequestResult.RETRY;
            }

            switch (rRes) {
                case OK:
                case REMOVE:
                    storedEvents.remove(0);
                    break;

                case RETRY:
                    break;
            }

        } catch (Exception e) {
            if (AcsClient.sharedInstance().isLoggingEnabled()) {
                log("Got exception while trying to submit event data: [" + eventData + "] [" + e + "]");
            }
        } finally {
            if (conn instanceof HttpURLConnection) {
                try {
                    if (connInputStream != null) {
                        connInputStream.close();
                    }
                } catch (Throwable ignored) {
                }

                ((HttpURLConnection) conn).disconnect();
            }
        }
    }

    public URLConnection urlConnectionForServerRequest(final String payload, final String customEndpoint) throws IOException {
        String urlStr = Api.SERVER_URL + customEndpoint;

        final URL url = new URL(urlStr);
        final HttpURLConnection conn;

        conn = (HttpURLConnection) url.openConnection();

        conn.setConnectTimeout(CONNECT_TIMEOUT_IN_MILLISECONDS);
        conn.setReadTimeout(READ_TIMEOUT_IN_MILLISECONDS);
        conn.setUseCaches(false);
        conn.setDoInput(true);
        conn.setRequestMethod("POST");

        AcsClientConfig config = AcsClient.sharedInstance().getAcsClientConfig();
        conn.addRequestProperty("APP_ID", config.appKey);
        conn.addRequestProperty("APP_SECRET", config.appSecret);

        conn.setDoOutput(true);
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        OutputStream os = conn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
        writer.write(payload);
        writer.flush();
        writer.close();
        os.close();
        return conn;
    }
}