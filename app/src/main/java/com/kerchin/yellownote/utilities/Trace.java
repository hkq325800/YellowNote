package com.kerchin.yellownote.utilities;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.kerchin.yellownote.global.Config;

/**
 * Created by Kerchin on 2016/3/1 0005.
 */
public class Trace {
    public static final String TAG = "hkq_trace";

    public static void d(String msg) {
        if (Config.isDebugMode) {
            Log.d(TAG, msg);
        }
    }

    /**
     * @deprecated
     */
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

    public static void show(final Context context, final String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void show(final Activity activity, final String msg) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity.getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void show(final Context context, final String msg, final int length) {
        Toast.makeText(context, msg, length).show();
    }

    public static void show(final Activity context, final String msg, final int length) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, msg, length).show();
            }
        });
    }

    public static String getErrorMsg(Exception e) {
        if (e.getMessage() != null
                && Config.isDebugMode)
            return e.getMessage();
        else
            return "";
    }
}
