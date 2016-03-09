package com.kerchin.yellownote.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.ImageView;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * com.kerchin.yellownote.utilities
 * Created by hzhuangkeqing on 2015/9/23 0023.
 */
public class NormalUtils {
    public static void goToActivity(Activity context, Class activity) {
        Intent intent = new Intent();
        intent.setClass(context, activity);
        context.startActivity(intent);//首页面转换
        /*context.overridePendingTransition(R.anim.push_left_in,
                R.anim.push_left_out);*/
    }

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
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getDateString(Date date) {
        Calendar now = Calendar.getInstance();
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        SimpleDateFormat formatter = new SimpleDateFormat(" HH:mm:ss", Locale.CHINA);/* HH:mm:ss*/
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
     * convert drawable to bitmap
     *
     * @param drawable
     * @return
     */
    public static Bitmap drawableToBitmap(Drawable drawable) {
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        return bitmap;
    }

    public static String getStringPreview(String str) {
        String preview;
//        if (str.length() < 50) {
        preview = str.replace("\n", " ");
//        } else {
//            preview = str.replace("\n", " ").substring(0, 50) + "...";
//        }
        return preview;
    }

    /**
     * Intent i = new Intent(
     * Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
     * startActivityForResult(i, RESULT_LOAD_IMAGE);
     *
     * @param context
     * @param selectedImage
     * @param widget
     */
    public static void setDrawableToWidget(Context context, Uri selectedImage, ImageView widget) {
        String[] filePathColumn = {MediaStore.Images.Media.DATA};

        Cursor cursor = context.getContentResolver().query(selectedImage,
                filePathColumn, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            widget.setImageBitmap(zoomImage(picturePath));
        }
    }

    private static Bitmap zoomImage(String picturePath) {
        BitmapFactory.Options option = new BitmapFactory.Options();
        option.inSampleSize = 8;
        return BitmapFactory.decodeFile(picturePath, option);
    }
}
