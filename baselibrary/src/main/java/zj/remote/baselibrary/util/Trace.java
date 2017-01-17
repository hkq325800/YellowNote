package zj.remote.baselibrary.util;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import zj.remote.baselibrary.Config;

/**
 * Created by Kerchin on 2016/3/1 0005.
 * <p/>
 * 包含log toast dialog的工具类
 */
public class Trace {
    public static final String TAG = "MyTrace";

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
            Log.e(TAG, msg == null ? "" : msg);
        }
    }

    public static void e(String TAG, String msg) {
        if (Config.isDebugMode) {
            Log.e(TAG, msg == null ? "" : msg);
        }
    }

    public static void e(String msg, Throwable tr) {
        if (Config.isDebugMode) {
            Log.e(TAG, msg == null ? "" : msg, tr);
        }
    }

    public static void e(String TAG, String msg, Throwable tr) {
        if (Config.isDebugMode) {
            Log.e(TAG, msg == null ? "" : msg, tr);
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

    public static void show(final Context context, final String msg, boolean isShort) {
        Toast.makeText(context, msg, isShort ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG).show();
    }

    public static void show(final Activity activity, final String msg, final boolean isShort) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity.getApplicationContext(), msg, isShort ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG).show();
            }
        });
    }

    public static String getErrorMsg(Exception e) {
        //noinspection PointlessBooleanExpression
        if (e.getMessage() != null && Config.isDebugMode)
            return e.getMessage();
        else
            return "";
    }
}
