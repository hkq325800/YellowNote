package com.kerchin.yellownote.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.avos.avoscloud.AVException;
import com.kerchin.global.Config;
import com.kerchin.yellownote.R;
import com.kerchin.yellownote.base.BaseSwipeBackActivity;
import com.kerchin.yellownote.data.event.GetPatternEvent;
import com.kerchin.yellownote.data.proxy.SecretService;
import com.kerchin.yellownote.global.MyApplication;
import com.kerchin.yellownote.utilities.PatternLockUtils;
import com.kerchin.yellownote.utilities.helper.DayNightHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import zj.remote.baselibrary.util.NormalUtils;
import zj.remote.baselibrary.util.PreferenceUtils;
import zj.remote.baselibrary.util.ThreadPool.ThreadPool;
import zj.remote.baselibrary.util.Trace;

/**
 * 密码相关
 * Created by Kerchin on 2016/6/6 0006.
 */
@Route(path = "/yellow/secret_menu")
public class SecretMenuActivity extends BaseSwipeBackActivity {
    @BindView(R.id.mNavigationTitleEdt)
    EditText mNavigationTitleEdt;
    @BindView(R.id.mNavigationRightBtn)
    Button mNavigationRightBtn;
    //    @BindView(R.id.mSecretMenuPatternToggle)
//    ToggleButton mSecretMenuPatternToggle;
    @BindView(R.id.mSecretMenuPatternEditTxt)
    TextView mSecretMenuPatternEditTxt;
    @BindView(R.id.mSecretMenuPatternToggleTxt)
    TextView mSecretMenuPatternToggleTxt;
    private final static int requestForPattern = 1;//手势开关
    private final static int requestForForget = 2;//校验用户名密码 然后清除密码并关闭手势密码
    private final static int requestForConfirmPattern = 3;
    private final static int requestForEditPattern = 4;

    private String patternFromOthers;
    //    private SVProgressHUD mSVProgressHUD;
    private boolean hasPattern;
    DayNightHelper mDayNightHelper;

    @Override
    protected void doSthBeforeSetView(Bundle savedInstanceState) {
        super.doSthBeforeSetView(savedInstanceState);
        mDayNightHelper = DayNightHelper.getInstance(this);;
        if (mDayNightHelper.isDay()) {
            setTheme(R.style.TransparentThemeDay);
        } else {
            setTheme(R.style.TransparentThemeNight);
        }
    }

    @Override
    protected boolean initCallback(Message msg) {
        return false;
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_secret_menu;
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        //从本地数据判断手势密码状态
        ThreadPool.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String str = SecretService.getPatternStr(PreferenceUtils.getString(Config.KEY_USER, "", MyApplication.context));
                    PatternLockUtils.setPattern(str, getApplicationContext());
                    hasPattern = !TextUtils.isEmpty(str);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            enablePattern(hasPattern);
//                            mSVProgressHUD.dismissImmediately();
                        }
                    });
                } catch (AVException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mSecretMenuPatternToggleTxt.setClickable(false);
                            mSecretMenuPatternToggleTxt.setTextColor(getResources()
                                    .getColor(R.color.light_gray));
                            Trace.show(getApplicationContext(), "网络离线无法设置手势密码");
                        }
                    });
                }
            }
        });
//        mSecretMenuPatternToggle.setChecked(hasPattern);
    }

    @Override
    protected void initEvent(Bundle savedInstanceState) {
        super.initEvent(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Subscribe
    public void onEvent(GetPatternEvent event){
        patternFromOthers = event.getStrFromPattern();
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        ButterKnife.bind(this);
        mNavigationTitleEdt.setText("密码相关");
        mNavigationRightBtn.setVisibility(View.INVISIBLE);
        mNavigationTitleEdt.setEnabled(false);
        mNavigationTitleEdt.setFocusable(false);
        mNavigationTitleEdt.clearFocus();
        mNavigationTitleEdt.setFocusableInTouchMode(false);
//        mSVProgressHUD = new SVProgressHUD(this);
//        mSVProgressHUD.showWithStatus("获取最新密码信息...", SVProgressHUD.SVProgressHUDMaskType.Clear);
    }

    @Override
    protected void onDestroy() {
        NormalUtils.clearTextLineCache();
        super.onDestroy();
    }

    @OnClick(R.id.mNavigationLeftBtn)
    public void back() {
        finish();
    }

    /**
     * 登录密码
     */
    @OnClick(R.id.mSecretMenuLoginTxt)
    public void gotoSecret() {
        ARouter.getInstance().build("/yellow/secret").navigation();
        overridePendingTransition(R.anim.push_left_in,
                R.anim.push_left_out);
    }

    /**
     * 手势开关
     */
    @OnClick(R.id.mSecretMenuPatternToggleTxt)
    public void gotoSecretSet() {
        mSecretMenuPatternToggleTxt.setClickable(false);
//        mSecretMenuPatternToggle.setChecked(!mSecretMenuPatternToggle.isChecked());
        ThreadPool.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final String str = SecretService.getPatternStr(PreferenceUtils.getString(Config.KEY_USER, "", MyApplication.context));
                    if (hasPattern == !TextUtils.isEmpty(str)) {//与网络相同
                        mSecretMenuPatternToggleTxt.setClickable(true);
                        ARouter.getInstance()
                                .build(hasPattern ? "/yellow/confirm" : "/yellow/set_pattern")
                                .navigation(SecretMenuActivity.this, requestForPattern);
//                        Intent intent = new Intent(SecretMenuActivity.this
//                                , hasPattern ? ConfirmPatternActivity.class : SetPatternActivity.class);
//                        startActivityForResult(intent, requestForPattern);
                    } else {//与网络不同
                        hasPattern = !TextUtils.isEmpty(str);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                PatternLockUtils.setPattern(str, getApplicationContext());
                                enablePattern(hasPattern);
                                mSecretMenuPatternToggleTxt.setClickable(true);
                                Trace.show(getApplicationContext(), "密码在别处被改动,请注意数据安全!");
                            }
                        });
                    }
                } catch (AVException e) {
                    e.printStackTrace();
                    mSecretMenuPatternToggleTxt.setClickable(true);
                }
            }
        });
    }

    /**
     * 手势修改
     */
    @OnClick(R.id.mSecretMenuPatternEditTxt)
    public void gotoSecretModify() {
        mSecretMenuPatternEditTxt.setClickable(false);
        ThreadPool.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final String str = SecretService.getPatternStr(PreferenceUtils.getString(Config.KEY_USER, "", MyApplication.context));
                    if (TextUtils.isEmpty(str)) {//与网络不同
                        hasPattern = false;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                enablePattern(false);
                            }
                        });
                        PatternLockUtils.clearLocalPattern(getApplicationContext());
                        mSecretMenuPatternEditTxt.setClickable(true);
                        Trace.show(getApplicationContext(), "密码在别处被改动,请注意数据安全!");
                    } else {//与网络相同
                        mSecretMenuPatternEditTxt.setClickable(true);
                        ARouter.getInstance().build("/yellow/confirm").navigation(SecretMenuActivity.this, requestForConfirmPattern);
                    }
                } catch (AVException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void enablePattern(boolean enable) {
        mSecretMenuPatternEditTxt.setClickable(enable);
        if (enable)
            mSecretMenuPatternEditTxt.setTextColor(mDayNightHelper.getColorRes(this, DayNightHelper.COLOR_TEXT));
        else
            mSecretMenuPatternEditTxt.setTextColor(getResources()
                    .getColor(R.color.light_gray));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == requestForPattern && resultCode == RESULT_OK) {//创建成功
            ThreadPool.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (!hasPattern) {//开启手势
                            SecretService.setPatternStr(PreferenceUtils.getString(Config.KEY_USER, "", MyApplication.context)
                                    , patternFromOthers);
                            PatternLockUtils.setPattern(patternFromOthers, getApplicationContext());
                            if (Config.isDebugMode)
                                Trace.show(SecretMenuActivity.this, patternFromOthers);
                            hasPattern = true;
                        } else {//关闭手势
                            PatternLockUtils.clearPattern(getApplicationContext());
                            patternFromOthers = "";
                            hasPattern = false;
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                enablePattern(hasPattern);
                            }
                        });
                    } catch (AVException e) {
                        e.printStackTrace();
                        Trace.show(SecretMenuActivity.this, "请在联网状态下进行此操作");
                    } finally {
                        patternFromOthers = "";
                    }
                }
            });
        } else if (requestCode == requestForPattern && resultCode == RESULT_CANCELED) {
            //手势开关失败
//            mSecretMenuPatternToggle.setChecked(!mSecretMenuPatternToggle.isChecked());
        } else if (resultCode == ConfirmPatternActivity.RESULT_FORGOT_PASSWORD) {
            //忘记密码跳转验证
            ARouter.getInstance().build("/yellow/secret").withBoolean("isForget", true).navigation(SecretMenuActivity.this, requestForForget);
//            Intent intent = new Intent(SecretMenuActivity.this, SecretActivity.class);
//            intent.putExtra("isForget", true);
//            startActivityForResult(intent, requestForForget);
        } else if (requestCode == requestForForget && resultCode == RESULT_OK) {
            //忘记密码验证成功清除密码
//            mSecretMenuPatternToggle.setChecked(false);
            ThreadPool.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        PatternLockUtils.clearPattern(getApplicationContext());
                        hasPattern = false;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                enablePattern(false);
                            }
                        });
                    } catch (AVException e) {
                        e.printStackTrace();
                    }
                }
            });
        } else if (requestCode == requestForConfirmPattern && resultCode == RESULT_OK) {
            //修改手势密码验证成功跳转设置
            ARouter.getInstance().build("/yellow/set_pattern").navigation(SecretMenuActivity.this, requestForEditPattern);
//            Intent intent = new Intent(SecretMenuActivity.this, SetPatternActivity.class);
//            startActivityForResult(intent, requestForEditPattern);
        } else if (requestCode == requestForEditPattern && resultCode == RESULT_OK) {
            //修改手势密码成功
            ThreadPool.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        SecretService.setPatternStr(PreferenceUtils.getString(Config.KEY_USER, "", MyApplication.context)
                                , patternFromOthers);
                        PatternLockUtils.setPattern(patternFromOthers, getApplicationContext());
                        if (Config.isDebugMode)
                            Trace.show(SecretMenuActivity.this, patternFromOthers);
                        hasPattern = true;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                enablePattern(true);
                            }
                        });
                    } catch (AVException e) {
                        e.printStackTrace();
                        Trace.show(SecretMenuActivity.this, "请在联网状态下进行此操作");
                    } finally {
                        patternFromOthers = "";
                    }
                }
            });
        }
    }
}
