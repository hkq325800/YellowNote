package zj.remote.baselibrary.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.MessageDigest;

/**
 * Created by ucmed on 2016/9/21.
 */

public class NormalUtils {

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
    public static boolean avoidManyClicks(View view, long minClickTime) {
        long time;
        try {
            time = (long) view.getTag();
        } catch (NullPointerException e) {
            time = 0;
        }
        long now = System.currentTimeMillis();
        view.setTag(now);
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
