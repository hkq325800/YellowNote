package com.kerchin.yellownote.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.kerchin.yellownote.R;
import com.kerchin.yellownote.base.BaseHasSwipeActivity;
import com.kerchin.yellownote.global.MyApplication;
import com.kerchin.yellownote.proxy.SecretService;
import com.kerchin.yellownote.utilities.NormalUtils;
import com.kerchin.yellownote.utilities.PatternLockUtils;
import com.kerchin.yellownote.utilities.Trace;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 密码相关
 * Created by Kerchin on 2016/6/6 0006.
 */
public class SecretMenuActivity extends BaseHasSwipeActivity {
    @BindView(R.id.mNavigationTitleEdt)
    EditText mNavigationTitleEdt;
    @BindView(R.id.mNavigationRightBtn)
    Button mNavigationRightBtn;
    //    @BindView(R.id.mSecretMenuPatternToggle)
//    ToggleButton mSecretMenuPatternToggle;
    @BindView(R.id.mSecretMenuPatternEditLiL)
    LinearLayout mSecretMenuPatternEditLiL;
    @BindView(R.id.mSecretMenuPatternEditTxt)
    TextView mSecretMenuPatternEditTxt;
    private final static int requestCodeForPattern = 1;//手势开关
    private final static int requestCodeForForget = 2;//校验用户名密码 然后清除密码并关闭手势密码
    private final static int requestCodeForConfirmPattern = 3;
    private final static int requestCodeForEditPattern = 4;

    boolean hasPattern;

    @Override
    protected void setContentView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_secret_menu);
        NormalUtils.immerge(this, R.color.lightSkyBlue);
    }

    @Override
    protected void initializeEvent(Bundle savedInstanceState) {

    }

    @Override
    protected void initializeData(Bundle savedInstanceState) {
        //从本地数据判断手势密码状态
        hasPattern = PatternLockUtils.hasPattern(getApplicationContext());
        enablePattern(hasPattern);
//        mSecretMenuPatternToggle.setChecked(hasPattern);
    }

    @Override
    protected void initializeView(Bundle savedInstanceState) {
        ButterKnife.bind(this);
        mNavigationTitleEdt.setText("密码相关");
        mNavigationRightBtn.setVisibility(View.INVISIBLE);
        mNavigationTitleEdt.setEnabled(false);
        mNavigationTitleEdt.setFocusable(false);
        mNavigationTitleEdt.clearFocus();
        mNavigationTitleEdt.setFocusableInTouchMode(false);
    }

    public static void startMe(Context context) {
        Intent intent = new Intent(context, SecretMenuActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @OnClick(R.id.mNavigationLeftBtn)
    public void back() {
        finish();
    }

    /**
     * 登录密码
     */
    @OnClick(R.id.mSecretMenuLoginLiL)
    public void gotoSecret() {
        SecretActivity.startMe(getApplicationContext());
    }

    /**
     * 手势开关
     */
//    @OnClick(R.id.mSecretMenuPatternToggle)
//    public void toggleClick() {
//        Intent intent = new Intent(SecretMenuActivity.this
//                , hasPattern ? ConfirmPatternActivity.class : SetPatternActivity.class);
//        startActivityForResult(intent, requestCodeForPattern);
//    }

    /**
     * 手势开关
     */
    @OnClick(R.id.mSecretMenuPatternToggleLiL)
    public void gotoSecretSet() {
//        mSecretMenuPatternToggle.setChecked(!mSecretMenuPatternToggle.isChecked());
        Intent intent = new Intent(SecretMenuActivity.this
                , hasPattern ? ConfirmPatternActivity.class : SetPatternActivity.class);
        startActivityForResult(intent, requestCodeForPattern);
    }

    /**
     * 手势修改
     */
    @OnClick(R.id.mSecretMenuPatternEditLiL)
    public void gotoSecretModify() {
        Intent intent = new Intent(SecretMenuActivity.this, ConfirmPatternActivity.class );
        startActivityForResult(intent, requestCodeForConfirmPattern);
    }

    private void enablePattern(boolean enable) {
        mSecretMenuPatternEditLiL.setClickable(enable);
        mSecretMenuPatternEditTxt.setTextColor(getResources()
                .getColor(enable ? R.color.textContentColor : R.color.light_gray));
    }

    public static String patternFromOthers;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == requestCodeForPattern && resultCode == RESULT_OK) {//创建成功
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (!hasPattern) {//开启手势
                            SecretService.setPatternStr(MyApplication.user, patternFromOthers);
                            PatternLockUtils.setPattern(patternFromOthers, getApplicationContext());
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
            }).start();
        } else if (requestCode == requestCodeForPattern && resultCode == RESULT_CANCELED) {
            //手势开关失败
//            mSecretMenuPatternToggle.setChecked(!mSecretMenuPatternToggle.isChecked());
        } else if (resultCode == ConfirmPatternActivity.RESULT_FORGOT_PASSWORD) {
            //忘记密码跳转验证
            Intent intent = new Intent(SecretMenuActivity.this, SecretActivity.class);
            intent.putExtra("isForget", true);
            startActivityForResult(intent, requestCodeForForget);
        } else if (requestCode == requestCodeForForget && resultCode == RESULT_OK) {
            //忘记密码验证成功清除密码
//            mSecretMenuPatternToggle.setChecked(false);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        PatternLockUtils.clearPattern(getApplicationContext());
                        hasPattern = false;
                        enablePattern(false);
                    } catch (AVException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } else if (requestCode == requestCodeForConfirmPattern && resultCode == RESULT_OK) {
            //修改手势密码验证成功跳转设置
            Intent intent = new Intent(SecretMenuActivity.this, SetPatternActivity.class);
            startActivityForResult(intent, requestCodeForEditPattern);
        } else if(requestCode == requestCodeForEditPattern && resultCode == RESULT_OK){
            //修改手势密码成功
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        SecretService.setPatternStr(MyApplication.user, patternFromOthers);
                        PatternLockUtils.setPattern(patternFromOthers, getApplicationContext());
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
            }).start();
        }
    }
}
