package com.kerchin.yellownote.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TextInputLayout;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.kerchin.yellownote.R;
import com.kerchin.yellownote.base.BaseSwipeBackActivity;
import com.kerchin.yellownote.global.Config;
import com.kerchin.yellownote.global.SampleApplicationLike;
import com.kerchin.yellownote.utilities.helper.DayNightHelper;
import com.kerchin.yellownote.data.proxy.LoginService;
import com.kerchin.yellownote.data.proxy.SecretService;
import com.kerchin.yellownote.utilities.NormalUtils;
import com.kerchin.yellownote.utilities.PatternLockUtils;
import zj.remote.baselibrary.util.ThreadPool.ThreadPool;
import com.kerchin.yellownote.utilities.Trace;
import com.securepreferences.SecurePreferences;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import zj.remote.baselibrary.util.SoftKeyboardUtils;

/**
 * Created by Kerchin on 2016/4/9 0009.
 * More Code on hkq325800@163.com
 */
public class SecretActivity extends BaseSwipeBackActivity {
    @BindView(R.id.mNavigationTitleEdt)
    EditText mNavigationTitleEdt;
    @BindView(R.id.mNavigationRightBtn)
    Button mNavigationRightBtn;
    @BindView(R.id.mSecretPassEdt)
    EditText mSecretPassEdt;
    @BindView(R.id.mSecretNewPassEdt)
    EditText mSecretNewPassEdt;
    @BindView(R.id.mSecretNewPassAgainEdt)
    EditText mSecretNewPassAgainEdt;
    @BindView(R.id.mSecretPassTextInput)
    TextInputLayout mSecretPassTextInput;
    @BindView(R.id.mSecretRePassTextInput)
    TextInputLayout mSecretRePassTextInput;
    @BindView(R.id.mSecretRePassAgainTextInput)
    TextInputLayout mSecretRePassAgainTextInput;
    boolean isForget;

    @Override
    protected void doSthBeforeSetView(Bundle savedInstanceState) {
        super.doSthBeforeSetView(savedInstanceState);
        DayNightHelper mDayNightHelper = new DayNightHelper(this);
        if (mDayNightHelper.isDay()) {
            setTheme(R.style.TransparentThemeDay);
        } else {
            setTheme(R.style.TransparentThemeNight);
        }
        isForget = getIntent().getBooleanExtra("isForget", false);
    }

    @Override
    protected void initEvent(Bundle savedInstanceState) {
    }

    @Override
    protected boolean initCallback(Message msg) {
        return false;
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_secret;
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        mNavigationTitleEdt.setText(isForget ? "忘记密码" : "登录密码修改");
        mNavigationRightBtn.setText("提交");
        if (isForget) {
            mSecretPassTextInput.setHint("输入用户名");
            //noinspection ConstantConditions
            mSecretPassTextInput.getEditText().setInputType(InputType.TYPE_CLASS_PHONE);
            mSecretRePassTextInput.setHint("输入密码");
            mSecretRePassAgainTextInput.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        ButterKnife.bind(this);
//        setSlidingModeRight();
        mSecretPassEdt.setFilters(new InputFilter[]{new InputFilter.LengthFilter(13)});
        mSecretNewPassEdt.setFilters(new InputFilter[]{new InputFilter.LengthFilter(13)});
        mSecretNewPassAgainEdt.setFilters(new InputFilter[]{new InputFilter.LengthFilter(13)});
        mNavigationRightBtn.setVisibility(View.VISIBLE);
        mNavigationTitleEdt.setEnabled(false);
        mNavigationTitleEdt.setFocusable(false);
        mNavigationTitleEdt.setFocusableInTouchMode(true);
    }

    public static void startMe(Context context) {
        Intent intent = new Intent(context, SecretActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        NormalUtils.clearTextLineCache();
        super.onDestroy();
    }

    @OnClick(R.id.mNavigationLeftBtn)
    public void back() {
        SoftKeyboardUtils.KeyBoardCancel(this);
        finish();
    }

    @Override
    public void onOpened(){
        back();
    }

    @OnClick(R.id.mNavigationRightBtn)
    public void submit() {
        if (tableCheck(isForget)) {
            mNavigationRightBtn.setEnabled(false);
            ThreadPool.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        AVObject avObjects = isForget
                                ? LoginService.loginVerify(mSecretPassEdt.getText().toString(), mSecretNewPassEdt.getText().toString())
                                : LoginService.loginVerify(SampleApplicationLike.user, mSecretPassEdt.getText().toString());
                        if (avObjects != null) {
                            Trace.d("查询 用户" + avObjects.get("user_tel") + "旧密码正确");
                            boolean isFrozen = avObjects.getBoolean("isFrozen");
                            if (isFrozen) {//冻结修改不成功
                                Message message = Message.obtain();//由于账户冻结重新登陆
                                message.what = reLogForFrozen;
                                handler.sendMessage(message);
                            } else {
                                if (isForget) {
                                    setResult(RESULT_OK);
                                    PatternLockUtils.clearPattern(getApplicationContext());
                                    SoftKeyboardUtils.KeyBoardCancel(SecretActivity.this);
                                    finish();
                                } else {
                                    SecretService.alterSecret(SampleApplicationLike.user, mSecretNewPassEdt.getText().toString());
                                    //存入shared
                                    SecurePreferences.Editor editor = (SecurePreferences.Editor) SampleApplicationLike.getDefaultShared().edit();
                                    editor.putString(Config.KEY_USER, SampleApplicationLike.user);
                                    editor.putString(Config.KEY_PASS, mSecretNewPassEdt.getText().toString());
                                    editor.apply();
                                    //密码正确进行修改
                                    Message message = Message.obtain();//直接进入
                                    message.what = trueForSecret;
                                    handler.sendMessage(message);
                                }
                            }
                        } else {
                            //缓存错误重新登录
                            Message message = Message.obtain();//由于密码错误重新登陆
                            message.what = secretError;
                            handler.sendMessage(message);
                        }
                    } catch (AVException e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mNavigationRightBtn.setEnabled(true);
                            }
                        });
                        Trace.show(getApplicationContext(), "请检查网络后单击图标重试" + Trace.getErrorMsg(e), Toast.LENGTH_LONG);
                    }
                }
            });
        }
    }

    private final static byte secretError = 0;
    private final static byte reLogForFrozen = 1;
    private final static byte trueForSecret = 2;
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case secretError:
                    Trace.show(getApplicationContext(), "你输入的密码不对哦");
                    mNavigationRightBtn.setEnabled(true);
                    break;
                case reLogForFrozen:
                    Trace.show(getApplicationContext(), "您操作的账号已被冻结,请联系 hkq325800@163.com", Toast.LENGTH_LONG);
                    mNavigationRightBtn.setEnabled(true);
                    break;
                case trueForSecret:
                    Trace.show(getApplicationContext(), "密码修改成功");
                    mNavigationRightBtn.setEnabled(true);
                    mSecretNewPassEdt.setText("");
                    mSecretPassEdt.setText("");
                    mSecretNewPassAgainEdt.setText("");
                    mSecretPassEdt.requestFocus();
                    break;
            }
        }
    };

    private boolean tableCheck(boolean isForget) {
        if (isForget) {
            if (TextUtils.isEmpty(mSecretPassEdt.getText())
                    || TextUtils.isEmpty(mSecretNewPassEdt.getText())) {
                Trace.show(getApplicationContext(), "请将信息填写完整");
                return false;
            } else if (!mSecretPassEdt.getText().toString().equals(SampleApplicationLike.user)) {
                Trace.show(getApplicationContext(), "用户名与本地不同");
                return false;
            }
            return true;
        } else {
            if (mSecretPassEdt.getText().toString().equals("")
                    || mSecretNewPassEdt.getText().toString().equals("")
                    || mSecretNewPassAgainEdt.getText().toString().equals("")) {
                Trace.show(getApplicationContext(), "请将信息填写完整");
                return false;
            } else if (!mSecretNewPassEdt.getText().toString().equals(mSecretNewPassAgainEdt.getText().toString())) {
                Trace.show(getApplicationContext(), "两次填写的新密码不一致");
                return false;
            } else if (mSecretNewPassEdt.getText().toString().length() < 6) {
                Trace.show(getApplicationContext(), "密码长度需大于6位");
                return false;
            }
            return true;
        }
    }
}
