/*
 * Copyright (c) 2015 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package com.kerchin.yellownote.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.kerchin.yellownote.R;
import com.kerchin.yellownote.utilities.helper.DayNightHelper;
import com.kerchin.yellownote.utilities.PatternLockUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.zhanghai.android.patternlock.PatternView;
import zj.remote.baselibrary.util.Immerge.ImmergeUtils;

/**
 * 确认密码界面
 * 需要重写方法isStealthModeEnabled isPatternCorrect onForgotPassword
 */
public class ConfirmPatternActivity extends me.zhanghai.android.patternlock.ConfirmPatternActivity {
    boolean isFromLaunch;

    @BindView(R.id.mNavigationLeftBtn)
    Button mNavigationLeftBtn;
    @BindView(R.id.mNavigationRightBtn)
    Button mNavigationRightBtn;
    @BindView(R.id.mNavigationTitleEdt)
    EditText mNavigationTitleEdt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DayNightHelper mDayNightHelper = DayNightHelper.getInstance(this);;
        if (mDayNightHelper.isDay()) {
            setTheme(R.style.AppThemePatternDay);
        } else {
            setTheme(R.style.AppThemePatternNight);
        }
        super.onCreate(savedInstanceState);
        ImmergeUtils.immerge(ConfirmPatternActivity.this, R.color.lightSkyBlue);
        isFromLaunch = getIntent().getBooleanExtra("isFromLaunch", false);
//        if (!isFromLaunch)
////            overridePendingTransition(R.anim.push_left_in,
////                    android.R.anim.fade_out);
////        else
//            overridePendingTransition(R.anim.push_left_in,
//                    R.anim.not_move);
        mMessageText.setText("请输入手势密码");
        ButterKnife.bind(this);
        mNavigationTitleEdt.setText(/*"确认手势"*/getResources().getString(R.string.app_name));
        mNavigationLeftBtn.setVisibility(View.INVISIBLE);
        mNavigationTitleEdt.setEnabled(false);
        mNavigationTitleEdt.setFocusable(false);
        mNavigationTitleEdt.setFocusableInTouchMode(false);
//        ActivityInfo ActivityManager
//        Trace.show(this, "" + getApplicationInfo().loadLabel(ConfirmPatternActivity.this.getPackageManager()));
    }

    @Override
    protected void onConfirmed() {
        mMessageText.setText("手势正确");
        mMessageText.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isFromLaunch) {
                    MainActivity.startMe(ConfirmPatternActivity.this);
                } else {
                    setResult(RESULT_OK);
                }
                finish();
                if (isFromLaunch)
                    overridePendingTransition(R.anim.push_left_in,
                            R.anim.push_left_out);
                else
                    overridePendingTransition(R.anim.push_right_in,
                            R.anim.push_right_out);
            }
        }, isFromLaunch ? 0 : 500);
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
        return PatternLockUtils.isPatternCorrect(pattern, this);
    }

    private final static int requestForForget = 1;//校验用户名密码 然后清除密码并关闭手势密码

    @Override
    protected void onForgotPassword() {
        if (isFromLaunch) {
            //忘记密码跳转验证
            Intent intent = new Intent(ConfirmPatternActivity.this, SecretActivity.class);
            intent.putExtra("isForget", true);
            startActivityForResult(intent, requestForForget);
            overridePendingTransition(R.anim.push_left_in,
                    R.anim.not_move);
        } else
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
//        if (!mMessageText.getText().toString().equals(getResources()
//                .getString(me.zhanghai.android.patternlock.R.string.pl_draw_pattern_to_unlock)))
//            mMessageText.setText("");
        super.onPatternStart();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == requestForForget && resultCode == RESULT_OK) {
            MainActivity.startMe(ConfirmPatternActivity.this);
            finish();
            overridePendingTransition(R.anim.push_left_in,
                    R.anim.push_left_out);
        }
    }
}
