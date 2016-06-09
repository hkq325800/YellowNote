/*
 * Copyright (c) 2015 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package com.kerchin.yellownote.activity;

import android.os.Bundle;

import com.kerchin.yellownote.R;
import com.kerchin.yellownote.utilities.PatternLockUtils;

import java.util.List;

import me.zhanghai.android.patternlock.PatternView;

/**
 * 设置密码界面
 */
public class SetPatternActivity extends me.zhanghai.android.patternlock.SetPatternActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        NormalUtils.immerge(SetPatternActivity.this, R.color.lightSkyBlue);
        super.onCreate(savedInstanceState);
        mMessageText.setTextColor(getResources().getColor(R.color.colorPrimary));
//        AppUtils.setActionBarDisplayUp(this);
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case android.R.id.home://TODO ?
//                AppUtils.navigateUp(this);
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//    }

    /**
     * startActivityForResult失效
     * @param pattern
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
