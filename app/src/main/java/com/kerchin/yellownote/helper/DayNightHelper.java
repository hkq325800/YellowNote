package com.kerchin.yellownote.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.TypedValue;

import com.kerchin.yellownote.R;
import com.kerchin.yellownote.bean.DayNight;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Clock on 2016/8/24.
 */
public class DayNightHelper {

    private final static String FILE_NAME = "settings";
    private final static String MODE = "day_night_mode";
    public final static int COLOR_BACKGROUND = 0;
    private final static int COLOR_BACKGROUND_RES = 1;
    public final static int COLOR_SOFT_BACKGROUND = 2;
    private final static int COLOR_SOFT_BACKGROUND_RES = 3;
    private boolean isToogle = false;

    private SharedPreferences mSharedPreferences;
    private Map<Integer, Integer> map = new HashMap();

    public DayNightHelper(Context context) {
        this.mSharedPreferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }

    /**
     * 保存模式设置
     *
     * @param mode
     * @return
     */
    public boolean setMode(DayNight mode) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(MODE, mode.getName());
        return editor.commit();
    }

    public void toggleThemeSetting(Context context) {
        isToogle = true;
        if (isDay()) {
            setMode(DayNight.NIGHT);
            context.setTheme(R.style.NightTheme);
        } else {
            setMode(DayNight.DAY);
            context.setTheme(R.style.DayTheme);
        }
    }

    /**
     * 夜间模式
     *
     * @return
     */
    public boolean isNight() {
        String mode = mSharedPreferences.getString(MODE, DayNight.DAY.getName());
        if (DayNight.NIGHT.getName().equals(mode)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 日间模式
     *
     * @return
     */
    public boolean isDay() {
        String mode = mSharedPreferences.getString(MODE, DayNight.DAY.getName());
        if (DayNight.DAY.getName().equals(mode)) {
            return true;
        } else {
            return false;
        }
    }

    public int getColorRes(Context context, int colorCode) {
        if (map.get(colorCode + 1) != null && !isToogle)
            return map.get(colorCode + 1);
        Resources.Theme theme = context.getTheme();
        TypedValue typedValue = new TypedValue();
        int attr = 0;
        switch (colorCode) {
            case COLOR_BACKGROUND:
                attr = R.attr.clockBackground;
                break;
            case COLOR_SOFT_BACKGROUND:
                attr = R.attr.clockSoftBackground;
                break;
        }
        theme.resolveAttribute(attr, typedValue, true);
        map.put(colorCode + 1, context.getResources().getColor(typedValue.resourceId));
        return context.getResources().getColor(typedValue.resourceId);
    }

    public int getColorResId(Context context, int colorCode) {
        if (map.get(colorCode) != null && !isToogle)
            return map.get(colorCode);
        Resources.Theme theme = context.getTheme();
        TypedValue typedValue = new TypedValue();
        int attr = 0;
        switch (colorCode) {
            case COLOR_BACKGROUND:
                attr = R.attr.clockBackground;
                break;
            case COLOR_SOFT_BACKGROUND:
                attr = R.attr.clockSoftBackground;
                break;
        }
        theme.resolveAttribute(attr, typedValue, true);
        map.put(colorCode, typedValue.resourceId);
        return typedValue.resourceId;
    }
}
