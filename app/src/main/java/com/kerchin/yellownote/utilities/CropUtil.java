package com.kerchin.yellownote.utilities;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;

import com.yalantis.ucrop.UCrop;

import java.io.File;

/**
 * 默认为
 * Created by hkq325800 on 2016/12/6.
 */

public class CropUtil {
    private static final String TAG = "CropUtil";
    private static final String SAMPLE_CROPPED_IMAGE_NAME = "SampleCropImage";
    private static final String cacheDir = Environment.getExternalStorageDirectory().getAbsolutePath();
    private static final int compressQuality = 90;

    public static final int ASPEC_TYPE_ORIGIN = 0;
    public static final int ASPEC_TYPE_SQUARE = 1;
    public static final int ASPEC_TYPE_DYNAMIC = 2;
    public static final int ASPEC_TYPE_XY = 3;
    /**
     * false JPEG
     * true PNG
     */
    private static boolean PNGorJPEG = false;
    private static boolean isHideBottomControls = false;
    private static boolean isFreeStyleCropEnabled = true;
    private static boolean isCircleDimmedLayer = true;
    private static int aspectType = ASPEC_TYPE_DYNAMIC;

    public static int getAspectType() {
        return aspectType;
    }

    public static void setAspectType(int aspectType) {
        CropUtil.aspectType = aspectType;
    }

    public static boolean isPNGorJPEG() {
        return PNGorJPEG;
    }

    public static void setPNGorJPEG(boolean PNGorJPEG) {
        CropUtil.PNGorJPEG = PNGorJPEG;
    }

    public static boolean isHideBottomControls() {
        return isHideBottomControls;
    }

    public static void setIsHideBottomControls(boolean isHideBottomControls) {
        CropUtil.isHideBottomControls = isHideBottomControls;
    }

    public static boolean isFreeStyleCropEnabled() {
        return isFreeStyleCropEnabled;
    }

    public static void setIsFreeStyleCropEnabled(boolean isFreeStyleCropEnabled) {
        CropUtil.isFreeStyleCropEnabled = isFreeStyleCropEnabled;
    }

    /**
     * @param activity
     * @param uri      //     * @param compressionSet mRadioGroupCompressionSettings.getCheckedRadioButtonId()
     *                 //     * @param aspectChecked  mRadioGroupAspectRatio.getCheckedRadioButtonId()
     */
    public static void startCropActivity(Activity activity, @NonNull Uri uri) {
        String destinationFileName = SAMPLE_CROPPED_IMAGE_NAME;
        if (PNGorJPEG)
            destinationFileName += ".png";
        else
            destinationFileName += ".jpg";

        UCrop uCrop = UCrop.of(uri, Uri.fromFile(new File(cacheDir, destinationFileName)));

        uCrop = basisConfig(uCrop, 1, 1);
        uCrop = advancedConfig(uCrop);

        uCrop.start(activity);
    }

    /**
     * In most cases you need only to set crop aspect ration and max size for resulting image.
     *
     * @param uCrop - ucrop builder instance
     * @return - ucrop builder instance
     */
    private static UCrop basisConfig(@NonNull UCrop uCrop, float ratioX, float ratioY) {
        switch (aspectType) {
            case ASPEC_TYPE_ORIGIN:
                uCrop = uCrop.useSourceImageAspectRatio();
                break;
            case ASPEC_TYPE_SQUARE:
                uCrop = uCrop.withAspectRatio(1, 1);
                break;
            case ASPEC_TYPE_DYNAMIC:
                // do nothing
                break;
            case ASPEC_TYPE_XY:
                //xy的比例
                if (ratioX > 0 && ratioY > 0) {
                    uCrop = uCrop.withAspectRatio(ratioX, ratioY);
                }
                break;
        }

        return uCrop;
    }

    /**
     * Sometimes you want to adjust more options, it's done via {@link com.yalantis.ucrop.UCrop.Options} class.
     *
     * @param uCrop - ucrop builder instance
     * @return - ucrop builder instance
     */
    private static UCrop advancedConfig(@NonNull UCrop uCrop) {
        UCrop.Options options = new UCrop.Options();
        options.setCompressionFormat(PNGorJPEG ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG);
        //jpeg quality 90
        options.setCompressionQuality(compressQuality);//mSeekBarQuality.getProgress()

        //isHideBottomControls
        options.setHideBottomControls(isHideBottomControls);//mCheckBoxHideBottomControls.isChecked()
        //isFreeStyle
        options.setFreeStyleCropEnabled(isFreeStyleCropEnabled);//mCheckBoxFreeStyleCrop.isChecked()

        /*
        If you want to configure how gestures work for all UCropActivity tabs

        options.setAllowedGestures(UCropActivity.SCALE, UCropActivity.ROTATE, UCropActivity.ALL);
        * */

        /*
        This sets max size for bitmap that will be decoded from source Uri.
        More size - more memory allocation, default implementation uses screen diagonal.

        options.setMaxBitmapSize(640);
        * */


        // Tune everything (ﾉ◕ヮ◕)ﾉ*:･ﾟ✧

        options.setMaxScaleMultiplier(5);
        options.setImageToCropBoundsAnimDuration(666);
//        options.setDimmedLayerColor(Color.CYAN);
        options.setCircleDimmedLayer(isCircleDimmedLayer);//是否圆形
        options.setShowCropFrame(true);//外围边线
        //栅格线
//        options.setCropGridStrokeWidth(20);
//        options.setCropGridColor(Color.GREEN);
        options.setCropGridColumnCount(0);
        options.setCropGridRowCount(0);
        //工具栏
//        options.setToolbarCropDrawable(R.drawable.your_crop_icon);
//        options.setToolbarCancelDrawable(R.drawable.your_cancel_icon);
/*
        // Color palette
        options.setToolbarColor(ContextCompat.getColor(this, R.color.your_color_res));
        options.setStatusBarColor(ContextCompat.getColor(this, R.color.your_color_res));
        options.setActiveWidgetColor(ContextCompat.getColor(this, R.color.your_color_res));
        options.setToolbarWidgetColor(ContextCompat.getColor(this, R.color.your_color_res));
        options.setRootViewBackgroundColor(ContextCompat.getColor(this, R.color.your_color_res));

        // Aspect ratio options
        options.setAspectRatioOptions(1,
            new AspectRatio("WOW", 1, 2),
            new AspectRatio("MUCH", 3, 4),
            new AspectRatio("RATIO", CropImageView.DEFAULT_ASPECT_RATIO, CropImageView.DEFAULT_ASPECT_RATIO),
            new AspectRatio("SO", 16, 9),
            new AspectRatio("ASPECT", 1, 1));

       */

        return uCrop.withOptions(options);
    }
}
