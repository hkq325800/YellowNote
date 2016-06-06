/*
 * Copyright (c) 2015 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package com.kerchin.yellownote.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

//import com.kerchin.yellownote.utilities.AppUtils;
import com.kerchin.yellownote.utilities.PatternLockUtils;

import java.util.List;

import me.zhanghai.android.patternlock.PatternView;
//import me.zhanghai.android.patternlock.sample.util.ThemeUtils;

/**
 * 设置密码界面
 */
public class SetPatternActivity extends me.zhanghai.android.patternlock.SetPatternActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

//        ThemeUtils.applyTheme(this);

        super.onCreate(savedInstanceState);

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

    public static void startMe(Activity activity, int requestCode) {
        Intent intent = new Intent(activity, SetPatternActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivityForResult(intent, requestCode);
    }

    //两次绘制相同后返回
    @Override
    protected void onSetPattern(List<PatternView.Cell> pattern) {
        PatternLockUtils.setPattern(pattern, this);
    }
}
