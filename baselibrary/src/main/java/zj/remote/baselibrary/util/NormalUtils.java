package zj.remote.baselibrary.util;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.view.View;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.MessageDigest;

import zj.remote.baselibrary.R;

/**
 * Created by hkq325800 on 2016/9/21.
 */

public class NormalUtils {

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
        view.setTag(R.id.tag_avoid, now);
        return now - time < minClickTime;
    }

    public static void killSelf() {
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    /**
     * 判断网络是否可用
     *
     * @param context
     * @return
     */
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
     * 加载本地图片
     * http://bbs.3gstdy.com
     *
     * @param url 路径
     * @return Bitmap
     */
    public static Bitmap getLocalBitmap(String url) {
        try {
            FileInputStream fis = new FileInputStream(url);
            return BitmapFactory.decodeStream(fis);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
