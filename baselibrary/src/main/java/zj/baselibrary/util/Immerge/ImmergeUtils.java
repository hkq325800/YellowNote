package zj.baselibrary.util.Immerge;

import android.app.Activity;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.view.View;
import android.view.WindowManager;

/**
 * Created by ucmed on 2016/9/19.
 */
public class ImmergeUtils {

    /**
     * 沉浸式
     *
     * @param activity
     * @param color
     */
    public static void immerge(Activity activity, @ColorRes int color) {
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
//            activity.getWindow().setStatusBarColor(activity.getResources().getColor(color));
            //设置导航栏颜色 。
//            activity.getWindow().setNavigationBarColor(activity.getResources().getColor(color));
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
    private static void setStatusBarColor(Activity activity, @ColorRes int color) {
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
}
