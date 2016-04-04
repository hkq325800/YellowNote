package com.kerchin.yellownote.utilities;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.kerchin.yellownote.global.Config;

public class Trace {
    public static final String TAG = "hkq_trace";

    public static void d(String msg) {
        if (Config.isDebugMode) {
            Log.d(TAG, msg);
        }
    }

    public static void d(String TAG, String msg) {
        if (Config.isDebugMode) {
            Log.d(TAG, msg);
        }
    }

    public static void i(String msg) {
        if (Config.isDebugMode) {
            Log.i(TAG, msg);
        }
    }

    public static void i(String TAG, String msg) {
        if (Config.isDebugMode) {
            Log.i(TAG, msg);
        }
    }

    public static void w(String msg) {
        if (Config.isDebugMode) {
            Log.w(TAG, msg);
        }
    }

    public static void w(String TAG, String msg) {
        if (Config.isDebugMode) {
            Log.w(TAG, msg);
        }
    }

    public static void e(String msg) {
        if (Config.isDebugMode) {
            Log.e(TAG, msg);
        }
    }

    public static void e(String TAG, String msg) {
        if (Config.isDebugMode) {
            Log.e(TAG, msg);
        }
    }

    public static void show(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void show(Context context, String msg, int length) {
        Toast.makeText(context, msg, length).show();
    }

    public static void show(Context context, int id) {
        Toast.makeText(context, context.getResources().getString(id),
                Toast.LENGTH_SHORT).show();
    }

    public static String getErrorMsg(Exception e) {
        if (Config.isDebugMode) {
            if (e.getMessage() != null) {
                return e.getMessage();
            } else {
                return "";
            }
        } else
            return "请检查网络";
    }
}
