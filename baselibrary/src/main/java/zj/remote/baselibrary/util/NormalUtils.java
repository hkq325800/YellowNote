package zj.remote.baselibrary.util;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.security.MessageDigest;

import zj.remote.baselibrary.R;

/**
 * Created by hkq325800 on 2016/9/21.
 */

public class NormalUtils {

    private NormalUtils() {
    }

    public static String result;
    public static void onlyGetTitleFromUrl(Context context, String url) {
        WebView webView = new WebView(context);
        WebChromeClient wvcc = new WebChromeClient() {
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                result = title;
//                Log.d("ANDROID_LAB", "TITLE="+title);
                //title 就是网页的title
            }
        };
        webView.setWebChromeClient(wvcc);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        webView.loadUrl(url);
    }

    public static String getVersionName(Context context) throws Exception {
        // 获取packageManager的实例
        PackageManager packageManager = context.getPackageManager();
        // getPackageName()是你当前类的包名，0代表是获取版本信息
        PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
        return packInfo.versionName;
    }

    public static boolean isNetworkAvailable(Context context) {
        if (context != null) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = cm.getActiveNetworkInfo();
            if (info != null) {
                return info.isAvailable();
            }
        }
        return false;
    }

    /**
     * 浏览器下载
     *
     * @param context
     * @param versionCode
     * @param serviceClass
     * @param uriStr
     * @param fileName
     */
    public static void downloadByWeb(Context context, String versionCode, Class<?> serviceClass, String uriStr, String fileName) {
        Intent intent = new Intent(context, serviceClass);
        intent.putExtra("uriStr", uriStr);//context.getString(R.string.uri_download)
        intent.putExtra("fileName", fileName);//context.getResources().getString(R.string.app_name) + versionCode + ".apk"
        context.startService(intent);
    }

    /**
     * 内部下载
     *
     * @param context
     * @param uriStr
     */
    public static void downloadByUri(Context context, String uriStr) {
        Uri uri = Uri.parse(uriStr);//指定网址
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);//指定Action
        intent.setData(uri);//设置Uri
        context.startActivity(intent);//启动Activity
    }

    /**
     * reference http://www.jianshu.com/p/b4a8b3d4f587
     * 用easyPermission 代替
     *
     * @param context
     * @param requestCode
     */
    public static boolean requestPermission(Activity context, int requestCode, @RequiresPermission String permission) {
        if (ContextCompat.checkSelfPermission(context, permission)
                != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(context, new String[]{permission},
                    requestCode);
            return false;
        }
        return true;
    }

    /**
     * in order to avoid leak in TextLine.sCached
     * http://stackoverflow.com/questions/30397356/android-memory-leak-on-textview-leakcanary-leak-can-be-ignored
     */
    /*start*/
    private static final Field TEXT_LINE_CACHED;

    static {
        Field textLineCached = null;
        try {
            textLineCached = Class.forName("android.text.TextLine").getDeclaredField("sCached");
            textLineCached.setAccessible(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        TEXT_LINE_CACHED = textLineCached;
    }
    public static void clearTextLineCache() {
        // If the field was not found for whatever reason just return.
        if (TEXT_LINE_CACHED == null) return;

        Object cached = null;
        try {
            // Get reference to the TextLine sCached array.
            cached = TEXT_LINE_CACHED.get(null);
        } catch (Exception ex) {
            //
        }
        if (cached != null) {
            // Clear the array.
            for (int i = 0, size = Array.getLength(cached); i < size; i++) {
                Array.set(cached, i, null);
            }
        }
    }
    /*end*/

    /**
     * 快捷发送通知
     */
    private static final int DOWNLOAD_NOTIFICATION_ID_DONE = 911;
    public static void sendNotification(Activity activity, Intent intent, String contentTitle, String contentText, String ticker, @DrawableRes int icon){
        NotificationCompat.Builder mNotification = new NotificationCompat.Builder(activity);
        mNotification
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setTicker(ticker)//提示
                .setSmallIcon(icon)
                .setOngoing(false)
                .setContentIntent(PendingIntent.getActivity(activity, 0, intent, 0))
                .setAutoCancel(true);
        ((NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE)).notify(DOWNLOAD_NOTIFICATION_ID_DONE, mNotification.build());
    }

    /**
     * 调用系统自带的图片预览
     * @param context
     * @param file
     */
    public static void gotoPicPreview(Context context, @NonNull File file){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(file), "image/*");
        context.startActivity(intent);
    }

    /**
     * 启动图片选择(先选择文件夹再选图片) 用于选择头像、单张图片
     *
     * @param activity
     * @param REQUEST_SELECT_PICTURE
     */
    public static void yellowPicPicker(Activity activity, int REQUEST_SELECT_PICTURE) {
        Intent i = new Intent(
                Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        activity.startActivityForResult(i, REQUEST_SELECT_PICTURE);
    }

    /**
     * 启动安卓自带的图片选择
     *
     * @param activity
     * @param title
     * @param REQUEST_SELECT_PICTURE
     */
    public static void gotoPicPicker(Activity activity, String title, int REQUEST_SELECT_PICTURE) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        activity.startActivityForResult(Intent.createChooser(intent, title), REQUEST_SELECT_PICTURE);
    }

    /**
     * 计算md5值 返回16位(较短的)md5值
     *
     * @param val 源
     * @return String
     */
    public static String md5(String val) {
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        try {
            byte[] btInput = val.getBytes();
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (byte byte0 : md) {
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            String result = new String(str);
            return result.substring(8, result.length() - 8).toLowerCase();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 防误触
     *
     * @param view
     * @param minClickTime
     * @return true 触发 false 正常
     */
    public static boolean avoidManyClicks(@NonNull View view, long minClickTime) {
        long time;
        try {
            time = (long) view.getTag(R.id.tag_avoid);
        } catch (NullPointerException e) {
            time = 0;
        }
        long now = System.currentTimeMillis();
        if(now - time >= minClickTime)
            view.setTag(R.id.tag_avoid, now);
        return now - time < minClickTime;
    }

    public static void killSelf() {
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
