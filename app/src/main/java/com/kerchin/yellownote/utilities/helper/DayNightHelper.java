package com.kerchin.yellownote.utilities.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.util.SparseIntArray;
import android.util.TypedValue;

import com.kerchin.yellownote.R;
import com.kerchin.yellownote.data.bean.DayNight;

import java.util.ArrayList;

/**
 * 例：setTextColor为@ColorInt 需要getResource
 * 而backgroundColorRes为@ColorRes
 * Created by hkq325800 on 2016/11/24.
 */
public class DayNightHelper {

    private final static String FILE_NAME = "dayNightSettings";
    private final static String MODE = "day_night_mode";
    //每个colorCode为其res预留位置为-colorCode
    public final static int COLOR_BACKGROUND = R.attr.clockBackground;
    public final static int COLOR_SOFT_BACKGROUND = R.attr.clockSoftBackground;
    public final static int COLOR_TEXT = R.attr.clockTextColor;
    public final static int COLOR_NAV_BACKGROUND = R.attr.clockNavBackground;

    private final static int COLOR_BACKGROUND_RES = -COLOR_BACKGROUND;
    private final static int COLOR_SOFT_BACKGROUND_RES = -COLOR_SOFT_BACKGROUND;
    private final static int COLOR_TEXT_RES = -COLOR_TEXT;
    public final static int COLOR_NAV_BACKGROUND_RES = -COLOR_NAV_BACKGROUND;

    private SharedPreferences mSharedPreferences;
    //缓存节省了resolveAttribute的时间
    private SparseIntArray caches = new SparseIntArray();
    private ArrayList<Integer> attrs = new ArrayList<>();

    private static DayNightHelper instance;

    public static DayNightHelper getInstance(Context context) {
        if (instance == null) {
            synchronized (DayNightHelper.class) {
                if (instance == null) {
                    instance = new DayNightHelper(context);
                    return instance;
                } else
                    return instance;
            }
        } else
            return instance;
    }

    private DayNightHelper(Context context) {
        this.mSharedPreferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        //需要attrs.xml、styles.xml支持
        attrs.add(COLOR_BACKGROUND);
        attrs.add(COLOR_SOFT_BACKGROUND);
        attrs.add(COLOR_TEXT);
        attrs.add(COLOR_NAV_BACKGROUND);
    }

    /**
     * 保存模式设置
     *
     * @param mode
     * @return
     */
    private boolean setMode(DayNight mode) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(MODE, mode.getName());
        return editor.commit();
    }

    /**
     * 交换日夜模式
     * 每次更换设置都会清除缓存
     *
     * @param context
     */
    public void toggleThemeSetting(Context context) {
        for (Integer i : attrs) {
            caches.put(i, 0);
            caches.put(-i, 0);
        }
        if (isDay()) {
            setMode(DayNight.NIGHT);
            context.setTheme(R.style.NightTheme);
        } else {
            setMode(DayNight.DAY);
            context.setTheme(R.style.DayTheme);
        }
    }

    /**
     * 是否夜间模式
     *
     * @return
     */
    public boolean isNight() {
        String mode = mSharedPreferences.getString(MODE, DayNight.DAY.getName());
        return DayNight.NIGHT.getName().equals(mode);
    }

    /**
     * 是否日间模式
     *
     * @return
     */
    public boolean isDay() {
        String mode = mSharedPreferences.getString(MODE, DayNight.DAY.getName());
        return DayNight.DAY.getName().equals(mode);
    }

    /**
     * 得到getResources()之后的int资源
     * 推荐只使用这个方法和getColorResId()中的一种
     *
     * @param context
     * @param attrRes
     * @return @ColorInt
     */
    public
    @ColorInt
    int getColorRes(Context context, @AttrRes int attrRes) {
        if (caches.get(-attrRes) != 0)
            return caches.get(-attrRes);
        Resources.Theme theme = context.getTheme();
        TypedValue typedValue = new TypedValue();
        theme.resolveAttribute(attrRes, typedValue, true);
        int res = context.getResources().getColor(typedValue.resourceId);
        //加入缓存
        caches.put(-attrRes, res);
        return res;
    }

    /**
     * 得到R.color的int资源
     * 推荐只使用这个方法和getColorRes()中的一种
     *
     * @param context
     * @param attrRes
     * @return @ColorRes
     */
    public
    @ColorRes
    int getColorResId(Context context, @AttrRes int attrRes) {
        if (caches.get(attrRes) != 0)
            return caches.get(attrRes);
        Resources.Theme theme = context.getTheme();
        TypedValue typedValue = new TypedValue();
        theme.resolveAttribute(attrRes, typedValue, true);
        //加入缓存
        caches.put(attrRes, typedValue.resourceId);
        return typedValue.resourceId;
    }
}
