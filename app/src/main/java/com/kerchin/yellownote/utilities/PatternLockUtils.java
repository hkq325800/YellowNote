/*
 * Copyright (c) 2015 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package com.kerchin.yellownote.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.kerchin.yellownote.global.PreferenceContract;

import java.util.List;

import me.zhanghai.android.patternlock.PatternUtils;
import me.zhanghai.android.patternlock.PatternView;
//import me.zhanghai.android.patternlock.sample.app.ConfirmPatternActivity;
//import me.zhanghai.android.patternlock.sample.app.SetPatternActivity;

public class PatternLockUtils {

    public static final int REQUEST_CODE_CONFIRM_PATTERN = 1214;

    /**
     * 设置密码
     */
    public static void setPattern(List<PatternView.Cell> pattern, Context context) {
        PreferenceUtils.putString(PreferenceContract.KEY_PATTERN_SHA1,
                PatternUtils.patternToSha1String(pattern), context);
    }

    public static String getStrFromPattern(List<PatternView.Cell> pattern){
        return PatternUtils.patternToSha1String(pattern);
    }

    /**
     * 获取保存在Preference中的加密密码
     */
    private static String getPatternSha1(Context context) {
        return PreferenceUtils.getString(PreferenceContract.KEY_PATTERN_SHA1,
                PreferenceContract.DEFAULT_PATTERN_SHA1, context);
    }

    /**
     * 判断是否有密码
     */
    public static boolean hasPattern(Context context) {
        return !TextUtils.isEmpty(getPatternSha1(context));//hasPattern
    }

    /**
     * 校验密码
     */
    public static boolean isPatternCorrect(List<PatternView.Cell> pattern, Context context) {
        return TextUtils.equals(PatternUtils.patternToSha1String(pattern)
                , getPatternSha1(context));//isPatternCorrect
    }

    /**
     * 忘记密码
     */
    public static void clearPattern(Context context) {
        PreferenceUtils.remove(PreferenceContract.KEY_PATTERN_SHA1, context);
    }

    /**
     * 设置密码 SetPatternPreference onClick
     */
//    public static void setPatternByUser(Context context) {
//        context.startActivity(new Intent(context, SetPatternActivity.class));
//    }

    /**
     * 有密码的时候启动ConfirmPatternActivity
     */
    // NOTE: Should only be called when there is a pattern for this account.
//    public static void confirmPattern(Activity activity, int requestCode) {
//        activity.startActivityForResult(new Intent(activity, ConfirmPatternActivity.class),
//                requestCode);
//    }

    /**
     * 确认密码
     */
//    public static void confirmPattern(Activity activity) {
//        confirmPattern(activity, REQUEST_CODE_CONFIRM_PATTERN);
//    }

    /**
     * 先判断是否有密码 有则确认密码
     */
//    public static void confirmPatternIfHas(Activity activity) {
//        if (hasPattern(activity)) {
//            confirmPattern(activity);
//        }
//    }

    private PatternLockUtils() {
    }
}
