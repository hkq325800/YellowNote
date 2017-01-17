/*
 * Copyright (c) 2015 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package com.kerchin.yellownote.utilities;

import android.content.Context;
import android.text.TextUtils;

import com.avos.avoscloud.AVException;
import com.kerchin.yellownote.global.Config;
import com.kerchin.yellownote.global.PreferenceContract;
import com.kerchin.yellownote.data.proxy.SecretService;
import com.kerchin.yellownote.global.SampleApplicationLike;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import me.zhanghai.android.patternlock.PatternUtils;
import me.zhanghai.android.patternlock.PatternView;
import zj.remote.baselibrary.util.PreferenceUtils;

public class PatternLockUtils {

    public static final int REQUEST_CODE_CONFIRM_PATTERN = 1214;

    /**
     * 本地设置密码
     */
    public static void setPattern(String patternStr, Context context) {
//        PreferenceUtils.putString(PreferenceContract.KEY_PATTERN_SHA1,
//                PatternUtils.patternToSha1String(pattern), context);
        SimpleDateFormat myFmt = new SimpleDateFormat("yyyyMMdd", Locale.CHINA);
        String dateStr = myFmt.format(new Date());
//        PreferenceUtils.putString(PreferenceContract.KEY_PATTERN_DATE,
//                dateStr, context);
        PreferenceUtils.putString(PreferenceContract.KEY_PATTERN_SHA1,
                patternStr, context);
    }

    public static boolean isStealthModeEnabled(Context context) {
        return !PreferenceUtils.getBoolean(PreferenceContract.KEY_PATTERN_VISIBLE,
                PreferenceContract.DEFAULT_PATTERN_VISIBLE, context);
    }

    public static String getStrFromPattern(List<PatternView.Cell> pattern) {
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
     * 本地+网络清除密码
     */
    public static void clearPattern(final Context context) throws AVException {
        SecretService.setPatternStr(PreferenceUtils.getString(Config.KEY_USER, "", SampleApplicationLike.context), "");
        PreferenceUtils.remove(PreferenceContract.KEY_PATTERN_SHA1, context);
    }

    /**
     * 本地清除密码
     */
    public static void clearLocalPattern(final Context context){
        PreferenceUtils.remove(PreferenceContract.KEY_PATTERN_SHA1, context);
    }

    private PatternLockUtils() {
    }
}
