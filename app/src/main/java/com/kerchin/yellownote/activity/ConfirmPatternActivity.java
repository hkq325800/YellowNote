/*
 * Copyright (c) 2015 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package com.kerchin.yellownote.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.kerchin.yellownote.global.PreferenceContract;
import com.kerchin.yellownote.utilities.PatternLockUtils;
import com.kerchin.yellownote.utilities.PreferenceUtils;

import java.util.List;

import me.zhanghai.android.patternlock.PatternView;
//import me.zhanghai.android.patternlock.sample.util.PatternLockUtils;
//import me.zhanghai.android.patternlock.sample.util.PreferenceContract;
//import me.zhanghai.android.patternlock.sample.util.PreferenceUtils;
//import me.zhanghai.android.patternlock.sample.util.ThemeUtils;

/**
 * 确认密码界面
 * 需要重写方法isStealthModeEnabled isPatternCorrect onForgotPassword
 */
public class ConfirmPatternActivity extends me.zhanghai.android.patternlock.ConfirmPatternActivity {
    Runnable r = new Runnable() {
        @Override
        public void run() {
            mMessageText.setText("");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

//        ThemeUtils.applyTheme(this);

        super.onCreate(savedInstanceState);
    }

    /**
     * 图案是否可见
     */
    @Override
    protected boolean isStealthModeEnabled() {
        return !PreferenceUtils.getBoolean(PreferenceContract.KEY_PATTERN_VISIBLE,
                PreferenceContract.DEFAULT_PATTERN_VISIBLE, this);
    }

    @Override
    protected boolean isPatternCorrect(List<PatternView.Cell> pattern) {
        mMessageText.removeCallbacks(r);
        boolean flag = PatternLockUtils.isPatternCorrect(pattern, this);
        if (!flag)
            mMessageText.postDelayed(r, 2000);
        return flag;
    }

    @Override
    protected void onForgotPassword() {

//        startActivity(new Intent(this, ResetPatternActivity.class));
        //PatternLockUtils.clearPattern(ResetPatternActivity.this);
        // Finish with RESULT_FORGOT_PASSWORD.
        super.onForgotPassword();
    }

    /**
     * 保证"图案错误请重试"被清除 而不清除"绘制图案以解锁"
     */
    @Override
    public void onPatternStart() {
        if (!mMessageText.getText().toString().equals(getResources()
                .getString(me.zhanghai.android.patternlock.R.string.pl_draw_pattern_to_unlock)))
            mMessageText.setText("");
        super.onPatternStart();
    }
}
