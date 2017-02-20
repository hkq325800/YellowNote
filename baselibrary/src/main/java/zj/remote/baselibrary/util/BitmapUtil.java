package zj.remote.baselibrary.util;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by hkq325800 on 2017/2/20.
 */

public class BitmapUtil {

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

    public static void saveBitmap(Bitmap bm, File f) throws IOException {
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
//            if (type.contains("jpg") || type.contains("jpeg"))
            bm.compress(Bitmap.CompressFormat.JPEG, 90, out);
//            else
//                bm.compress(Bitmap.CompressFormat.PNG, 70, out);
            out.flush();
            out.close();
            Trace.i("yes", "已经保存" + f.getPath() + "/" + (f.length() / 1024) + "kb");
        } catch (FileNotFoundException e) {
            Trace.e("saveBitmap", "FileNotFoundException: " + e.getMessage());
        } catch (IOException e) {
            Trace.e("saveBitmap", "IOException: " + e.getMessage());
        }
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

    public static String getPathFromUri(Context context, Uri selectedImage) {
        if(!TextUtils.isEmpty(selectedImage.getEncodedPath()))
            return selectedImage.getEncodedPath();
        String[] filePathColumn = {MediaStore.Images.Media.DATA};

        Cursor cursor = context.getContentResolver().query(selectedImage,
                filePathColumn, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            return picturePath;
        }
        return "";
    }

    /**
     * Intent i = new Intent(
     * Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
     * startActivityForResult(i, RESULT_LOAD_IMAGE);
     */
    //TODO 最终以图像的宽高为准比较好
    public static Bitmap zoomImage(String picturePath) {
        File f = new File(picturePath);
        double size = f.length() / 1024.0;
        BitmapFactory.Options option = new BitmapFactory.Options();
        double result = size / 100.0;
        int multi;
        if (f.getName().contains(".gif"))
            if (result < 3)
                multi = 1;
            else
                multi = 2;
        else {
            if (result < 1)//小于100kb
                multi = 1;
            else if (result < 5)//100-500kb
                multi = 2;
            else if (result < 15)//500kb-1.5mb
                multi = 4;
            else if (result < 60)//1.5-6mb
                multi = 8;
            else//6mb以上
                multi = 16;
        }
        Trace.d("multi " + multi + "/" + size + "kb");
        option.inSampleSize = multi;
        return BitmapFactory.decodeFile(picturePath, option);
    }

    /**
     * http://www.cnblogs.com/fighter/archive/2012/02/20/android-bitmap-drawable.html
     *
     * @param b
     * @return
     */
    public static Bitmap bytes2Bitmap(byte[] b) {
        if (b.length != 0) {
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        } else {
            return null;
        }
    }
}
