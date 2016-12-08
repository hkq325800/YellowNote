/*
 * Copyright (c) 2015 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package com.kerchin.yellownote.ui.activity;

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
 * 设置密码界面
 */
public class SetPatternActivity extends me.zhanghai.android.patternlock.SetPatternActivity {
    @BindView(R.id.mNavigationLeftBtn)
    Button mNavigationLeftBtn;
    @BindView(R.id.mNavigationRightBtn)
    Button mNavigationRightBtn;
    @BindView(R.id.mNavigationTitleEdt)
    EditText mNavigationTitleEdt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(R.anim.push_left_in,
                R.anim.push_left_out);
        ImmergeUtils.immerge(SetPatternActivity.this, R.color.lightSkyBlue);
        DayNightHelper mDayNightHelper = DayNightHelper.getInstance(this);;
        if (mDayNightHelper.isDay()) {
            setTheme(R.style.AppThemePatternDay);
        } else {
            setTheme(R.style.AppThemePatternNight);
        }
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        mNavigationTitleEdt.setText(getResources().getString(R.string.app_name));
        mNavigationLeftBtn.setVisibility(View.INVISIBLE);
        mNavigationTitleEdt.setEnabled(false);
        mNavigationTitleEdt.setFocusable(false);
        mNavigationTitleEdt.setFocusableInTouchMode(false);
//        mMessageText.setTextColor(getResources().getColor(R.color.colorPrimary));
//        AppUtils.setActionBarDisplayUp(this);
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case android.R.id.home:
//                AppUtils.navigateUp(this);
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//    }

    /**
     * startActivityForResult失效
     * @param pattern 手势
     */
//    public static void startMe(Activity activity, int requestCode) {
//        Intent intent = new Intent(activity, SetPatternActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        activity.startActivityForResult(intent, requestCode);
//    }

    //两次绘制相同后返回
    @Override
    protected void onSetPattern(final List<PatternView.Cell> pattern) {
//        PatternLockUtils.setPattern(pattern, this);
        //TODO 数据传输有问题
        SecretMenuActivity.patternFromOthers = PatternLockUtils.getStrFromPattern(pattern);
    }
}
