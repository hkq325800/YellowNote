/*
 * Copyright (c) 2015 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package com.kerchin.yellownote.activity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.kerchin.yellownote.R;
import com.kerchin.yellownote.utilities.NormalUtils;
import com.kerchin.yellownote.utilities.PatternLockUtils;
import com.kerchin.yellownote.utilities.Trace;

import java.util.List;

import me.zhanghai.android.patternlock.PatternView;

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
    boolean isFromLaunch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        NormalUtils.immerge(ConfirmPatternActivity.this, R.color.lightSkyBlue);
        isFromLaunch = getIntent().getBooleanExtra("isFromLaunch", false);
        super.onCreate(savedInstanceState);
//        ActivityInfo ActivityManager
//        Trace.show(this, "" + getApplicationInfo().loadLabel(ConfirmPatternActivity.this.getPackageManager()));
    }

    @Override
    protected void onConfirmed() {
        if (isFromLaunch) {
            MainActivity.startMe(getApplicationContext());
        } else {
            setResult(RESULT_OK);
        }
        finish();
    }

    /**
     * 图案是否可见
     */
    @Override
    protected boolean isStealthModeEnabled() {
        return PatternLockUtils.isStealthModeEnabled(this);
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
