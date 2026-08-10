package com.ikaros.hikvision.mdt201;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.alibaba.fastjson.JSONObject;

import io.dcloud.feature.uniapp.annotation.UniJSMethod;
import io.dcloud.feature.uniapp.bridge.UniJSCallback;
import io.dcloud.feature.uniapp.common.UniModule;

public class PdaScanModule extends UniModule {
    private static final String START_ACTION = "android.intent.ACTION_SCAN_START";
    private static final String STOP_ACTION = "android.intent.ACTION_SCAN_STOP";
    private static final String DEFAULT_RESULT_ACTION = "android.intent.ACTION_SCAN_OUTPUT";
    private static final String DEFAULT_RESULT_DATA_KEY = "barcode";
    private static final String SCAN_RESULT_EVENT = "PdaScan.onScanResult";
    private static final String STATUS_EVENT = "PdaScan.onStatus";
    private static final String TEST_BARCODE = "PDA_SCAN_EVENT_TEST";
    private static final int API_TIRAMISU = 33;
    private static final int RECEIVER_EXPORTED = 0x2;

    private String resultAction = DEFAULT_RESULT_ACTION;
    private String resultDataKey = DEFAULT_RESULT_DATA_KEY;
    private UniJSCallback scanCallback;
    private boolean receiverRegistered;

    private final BroadcastReceiver scanResultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!resultAction.equals(intent.getAction())) {
                return;
            }

            String barcode = intent.getStringExtra(resultDataKey);
            if (barcode == null || barcode.trim().isEmpty()) {
                return;
            }

            emitScanResult(barcode.trim(), false);
        }
    };

    @UniJSMethod(uiThread = true)
    public void startScan(JSONObject options, UniJSCallback callback) {
        applyOptions(options);
        scanCallback = callback;
        Context context = getModuleContext();
        if (context == null) {
            if (callback != null) {
                callback.invoke(errorResult("CONTEXT_UNAVAILABLE", "无法获取 Android Context"));
            }
            return;
        }

        try {
            registerReceiver(context);
            context.sendBroadcast(new Intent(START_ACTION));
        } catch (RuntimeException exception) {
            if (callback != null) {
                callback.invoke(errorResult("START_FAILED", exception.getMessage()));
            }
            return;
        }
        emitStatus("started");
        if (callback != null) {
            callback.invokeAndKeepAlive(statusResult("started"));
        }
    }

    @UniJSMethod(uiThread = true)
    public void stopScan(UniJSCallback callback) {
        Context context = getModuleContext();
        if (context == null) {
            if (callback != null) {
                callback.invoke(errorResult("CONTEXT_UNAVAILABLE", "无法获取 Android Context"));
            }
            return;
        }

        context.sendBroadcast(new Intent(STOP_ACTION));
        unregisterReceiver(context);
        scanCallback = null;
        emitStatus("stopped");
        if (callback != null) {
            callback.invoke(statusResult("stopped"));
        }
    }

    @UniJSMethod(uiThread = true)
    public void configure(JSONObject options, UniJSCallback callback) {
        applyOptions(options);
        Context context = getModuleContext();
        if (context != null && receiverRegistered) {
            unregisterReceiver(context);
            registerReceiver(context);
        }
        if (callback != null) {
            callback.invoke(configurationResult());
        }
    }

    @UniJSMethod(uiThread = true)
    public void startListening(JSONObject options, UniJSCallback callback) {
        applyOptions(options);
        Context context = getModuleContext();
        if (context == null) {
            if (callback != null) {
                callback.invoke(errorResult("CONTEXT_UNAVAILABLE", "无法获取 Android Context"));
            }
            return;
        }

        try {
            registerReceiver(context);
        } catch (RuntimeException exception) {
            if (callback != null) {
                callback.invoke(errorResult("LISTEN_FAILED", exception.getMessage()));
            }
            return;
        }
        emitStatus("listening");
        if (callback != null) {
            callback.invoke(configurationResult());
        }
    }

    @UniJSMethod(uiThread = true)
    public void emitTestEvent(UniJSCallback callback) {
        emitScanResult(TEST_BARCODE, true);
        if (callback != null) {
            JSONObject result = statusResult("test_event_emitted");
            result.put("barcode", TEST_BARCODE);
            callback.invoke(result);
        }
    }

    @UniJSMethod(uiThread = false)
    public JSONObject getConfiguration() {
        return configurationResult();
    }

    @Override
    public void onActivityPause() {
        Context context = getModuleContext();
        if (context != null) {
            unregisterReceiver(context);
        }
        super.onActivityPause();
    }

    @Override
    public void onActivityDestroy() {
        Context context = getModuleContext();
        if (context != null) {
            unregisterReceiver(context);
        }
        scanCallback = null;
        super.onActivityDestroy();
    }

    private void applyOptions(JSONObject options) {
        if (options == null) {
            return;
        }

        String configuredAction = options.getString("resultAction");
        if (configuredAction != null && !configuredAction.trim().isEmpty()) {
            resultAction = configuredAction.trim();
        }
        String configuredDataKey = options.getString("resultDataKey");
        if (configuredDataKey != null && !configuredDataKey.trim().isEmpty()) {
            resultDataKey = configuredDataKey.trim();
        }
    }

    private Context getModuleContext() {
        return mUniSDKInstance == null ? null : mUniSDKInstance.getContext();
    }

    private void registerReceiver(Context context) {
        if (receiverRegistered) {
            return;
        }

        IntentFilter filter = new IntentFilter(resultAction);
        if (Build.VERSION.SDK_INT >= API_TIRAMISU) {
            try {
                Method registerReceiver = Context.class.getMethod(
                        "registerReceiver",
                        BroadcastReceiver.class,
                        IntentFilter.class,
                        int.class
                );
                registerReceiver.invoke(context, scanResultReceiver, filter, RECEIVER_EXPORTED);
            } catch (NoSuchMethodException exception) {
                context.registerReceiver(scanResultReceiver, filter);
            } catch (IllegalAccessException | InvocationTargetException exception) {
                throw new IllegalStateException("无法注册扫码结果广播接收器", exception);
            }
        } else {
            context.registerReceiver(scanResultReceiver, filter);
        }
        receiverRegistered = true;
    }

    private void unregisterReceiver(Context context) {
        if (!receiverRegistered) {
            return;
        }

        context.unregisterReceiver(scanResultReceiver);
        receiverRegistered = false;
    }

    private void emitScanResult(String barcode, boolean isTest) {
        JSONObject result = new JSONObject();
        result.put("type", "scan");
        result.put("barcode", barcode);
        result.put("resultAction", resultAction);
        result.put("resultDataKey", resultDataKey);
        result.put("isTest", isTest);
        if (scanCallback != null) {
            scanCallback.invokeAndKeepAlive(result);
        }
        if (mUniSDKInstance != null) {
            mUniSDKInstance.fireGlobalEventCallback(SCAN_RESULT_EVENT, result);
        }
    }

    private void emitStatus(String status) {
        if (mUniSDKInstance != null) {
            mUniSDKInstance.fireGlobalEventCallback(STATUS_EVENT, statusResult(status));
        }
    }

    private JSONObject statusResult(String status) {
        JSONObject result = configurationResult();
        result.put("type", "status");
        result.put("status", status);
        return result;
    }

    private JSONObject configurationResult() {
        JSONObject result = new JSONObject();
        result.put("resultAction", resultAction);
        result.put("resultDataKey", resultDataKey);
        result.put("isListening", receiverRegistered);
        return result;
    }

    private JSONObject errorResult(String code, String message) {
        JSONObject result = new JSONObject();
        result.put("type", "error");
        result.put("code", code);
        result.put("message", message);
        return result;
    }
}
