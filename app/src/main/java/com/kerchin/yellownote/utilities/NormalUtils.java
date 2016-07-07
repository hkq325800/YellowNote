package com.kerchin.yellownote.utilities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.view.View;
import android.view.WindowManager;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * com.kerchin.yellownote.utilities
 * Created by hzhuangkeqing on 2015/9/23 0023.
 */
public class NormalUtils {
    public static void downloadByUri(Context context, String uriStr) {
        Uri uri = Uri.parse(uriStr);//指定网址
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);//指定Action
        intent.setData(uri);//设置Uri
        context.startActivity(intent);//启动Activity
    }

    public static String getTrueDate(Date date) {
        SimpleDateFormat myFmt = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒", Locale.CHINA);
        return myFmt.format(date);
    }

    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        return wm.getDefaultDisplay().getWidth();
    }

    /**
     * 沉浸式
     *
     * @param activity
     * @param color
     */
    public static void immerge(Activity activity, int color) {
        /**沉浸式状态栏设置部分**/
        //Android5.0版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            setStatusBarColor(activity, color);//阴影绘制
            //设置状态栏颜色
//            getWindow().setStatusBarColor(getResources().getColor(color));
            //设置导航栏颜色
            activity.getWindow().setNavigationBarColor(activity.getResources().getColor(color));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            //透明状态栏
//            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//            //创建状态栏的管理实例
//            SystemBarTintManager tintManager = new SystemBarTintManager(activity);
//            //激活状态栏设置
//            tintManager.setStatusBarTintEnabled(true);
//            //设置状态栏颜色
//            tintManager.setTintResource(color);
            setStatusBarColor(activity, color);//阴影绘制
//            //透明导航栏
//            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
//            //激活导航栏设置
//            tintManager.setNavigationBarTintEnabled(true);
//            //设置导航栏颜色
//            tintManager.setNavigationBarTintResource(color);
        }
    }

    /**
     * 设置状态栏颜色
     * 也就是所谓沉浸式状态栏
     */
    public static void setStatusBarColor(Activity activity, int color) {
        /**
         * Android4.4以上可用
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            SystemBarTintManager tintManager = new SystemBarTintManager(activity);
            tintManager.setStatusBarTintResource(color);
            tintManager.setStatusBarTintEnabled(true);
            //Window window = getWindow();
            //window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            //window.setStatusBarColor(color);
        }
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 dp(像素) 的单位 转成为 px
     */
    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) ((dipValue - 0.5f) * scale);
    }

    /**
     * 跳转activity
     *
     * @param context  上下文
     * @param activity 目标activity
     */
    public static void goToActivity(Activity context, Class activity) {
        Intent intent = new Intent();
        intent.setClass(context, activity);
        context.startActivity(intent);//首页面转换
        /*context.overridePendingTransition(R.anim.push_left_in,
                R.anim.push_left_out);*/
    }

    /**
     * 计算md5值
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
            return new String(str);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 返回Note中的日期字符串
     *
     * @param date Date类型
     * @return String
     */
    public static String getDateString(Date date) {
        Calendar now = Calendar.getInstance();
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        SimpleDateFormat formatter = new SimpleDateFormat(" HH:mm:ss", Locale.CHINA);
        if (c.get(Calendar.YEAR) == now.get(Calendar.YEAR)) {
            int d1 = c.get(Calendar.DAY_OF_YEAR);
            int d2 = now.get(Calendar.DAY_OF_YEAR);
            if (d1 == d2) {
                return "今天" + formatter.format(date);
            } else if (d2 - d1 == 1) {
                return "昨天" + formatter.format(date);
            } else if (d2 - d1 == 2) {
                return "前天" + formatter.format(date);
            }
        }
        formatter = new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA);/* HH:mm:ss*/
        return formatter.format(date);

    }

    /**
     * WaterDropListViewHeader——
     * Map a value within a given range to another range.
     *
     * @param value    the value to map
     * @param fromLow  the low end of the range the value is within
     * @param fromHigh the high end of the range the value is within
     * @param toLow    the low end of the range to map to
     * @param toHigh   the high end of the range to map to
     * @return the mapped value
     */
    public static double mapValueFromRangeToRange(
            double value,
            double fromLow,
            double fromHigh,
            double toLow,
            double toHigh) {
        double fromRangeSize = fromHigh - fromLow;//250
        double toRangeSize = toHigh - toLow;//1
        double valueScale = (value - fromLow) / fromRangeSize;//0.02
        return toLow + (valueScale * toRangeSize);
    }

    /**
     * WaterDropListView——
     * convert drawable to bitmap
     *
     * @param drawable source
     * @return bitmap
     */
    public static Bitmap drawableToBitmap(Drawable drawable) {
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height
                , drawable.getOpacity() != PixelFormat.OPAQUE
                ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * Intent i = new Intent(
     * Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
     * startActivityForResult(i, RESULT_LOAD_IMAGE);
     *
     * @param context       上下文
     * @param selectedImage 选择的图片的uri
     */
    public static Bitmap getZoomBitmapFromUri(Context context, Uri selectedImage) {
        String[] filePathColumn = {MediaStore.Images.Media.DATA};

        Cursor cursor = context.getContentResolver().query(selectedImage,
                filePathColumn, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            return zoomImage(picturePath);
        }
        return null;
    }

    private static Bitmap zoomImage(String picturePath) {
        File f = new File(picturePath);
        long size = f.length();//29832 bytes
        BitmapFactory.Options option = new BitmapFactory.Options();
        double result = size / 1024;
        int multi = 0;
        if (f.getName().contains(".gif"))
            multi = 2;
        else {
            if (result / 100 < 1)//几十kb
                multi = 1;
            else if (result / 100 < 10)//几百kb
                multi = 2;
            else if (result / 100 < 100)//几mb
                multi = 8;
            else
                multi = 16;
        }
        Trace.d("multi " + multi);
        option.inSampleSize = multi;
        return BitmapFactory.decodeFile(picturePath, option);
    }

    /**
     * 获取匹配字符串格式的日期字符串
     *
     * @param matchStr 匹配字符串
     * @return String
     */
    public static String getNowDate(String matchStr) {
        SimpleDateFormat formatter = new SimpleDateFormat(matchStr, Locale.CHINA);
        Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
        return formatter.format(curDate);
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    //encode

    private static byte[] sha1(byte[] input) {
        try {
            return MessageDigest.getInstance("SHA-1").digest(input);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] stringToSha1(String str) {
        return /*sha1(*/str.getBytes(Charset.forName("UTF-8"))/*)*/;
    }

    public static String stringToSha1String(String string) {
        return bytesToString(stringToSha1(string));
    }

    private static String bytesToString(byte[] bytes) {
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    //decode

    private static byte[] stringToBytes(String string) {
        return Base64.decode(string, Base64.DEFAULT);
    }

    public static String sha1StringToString(String string) {
        try {
            return new String(stringToBytes(string), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

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

    /**
     * in order to avoid leak in TextLine.sCached
     * http://stackoverflow.com/questions/30397356/android-memory-leak-on-textview-leakcanary-leak-can-be-ignored
     */
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

    /**
     * reference http://www.jianshu.com/p/b4a8b3d4f587
     *
     * @param context
     * @param requestCode
     */
    public static void requestWritePermission(Activity context, int requestCode) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    requestCode);
        }
    }

    private NormalUtils() {
    }
}
