/*
 * Copyright (c) 2015 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package com.kerchin.yellownote.activity;

import android.os.Bundle;

import com.avos.avoscloud.AVException;
import com.kerchin.yellownote.global.MyApplication;
import com.kerchin.yellownote.global.PreferenceContract;
import com.kerchin.yellownote.proxy.SecretService;
import com.kerchin.yellownote.utilities.PatternLockUtils;
import com.kerchin.yellownote.utilities.PreferenceUtils;
import com.kerchin.yellownote.utilities.Trace;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import me.zhanghai.android.patternlock.PatternUtils;
import me.zhanghai.android.patternlock.PatternView;

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
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SecretService.setPatternStr(MyApplication.user, PatternLockUtils.getStrFromPattern(pattern));
                    SimpleDateFormat myFmt = new SimpleDateFormat("yyyyMMdd", Locale.CHINA);
                    String dateStr = myFmt.format(new Date());
                    PreferenceUtils.putString(PreferenceContract.KEY_PATTERN_DATE,
                            dateStr, getApplicationContext());
                    PreferenceUtils.putString(PreferenceContract.KEY_PATTERN_SHA1,
                            PatternUtils.patternToSha1String(pattern), getApplicationContext());
                    Trace.show(SetPatternActivity.this, PatternLockUtils.getStrFromPattern(pattern)+"date"+dateStr);
                } catch (AVException e) {
                    e.printStackTrace();
                    Trace.show(SetPatternActivity.this, "保存失败");
                }
            }
        }).start();
    }
}
