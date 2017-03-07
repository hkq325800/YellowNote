package com.kerchin.yellownote.mvp;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.avos.avoscloud.AVException;
import com.kerchin.global.Config;
import com.kerchin.yellownote.R;
import com.kerchin.yellownote.base.MyOrmLiteBaseActivity;
import com.kerchin.yellownote.data.bean.PrimaryData;
import com.kerchin.yellownote.utilities.PatternLockUtils;
import com.kerchin.yellownote.utilities.helper.sql.OrmLiteHelper;

import butterknife.BindView;
import butterknife.OnClick;
import zj.remote.baselibrary.util.AnimationUtil;
import zj.remote.baselibrary.util.DialogUtils;
import zj.remote.baselibrary.util.KeyboardUtil;
import zj.remote.baselibrary.util.NormalUtils;
import zj.remote.baselibrary.util.PreferenceUtils;
import zj.remote.baselibrary.util.SoftKeyboardUtils;
import zj.remote.baselibrary.util.SupportSoftKeyboardUtil;
import zj.remote.baselibrary.util.Trace;

/**
 * Created by hkq325800 on 2017/2/23.
 */
@Route(path = "/yellow/login")
public class LoginActivity extends MyOrmLiteBaseActivity<OrmLiteHelper> implements ILoginView, UiCallback {
    @BindView(R.id.mLoginUserEdt)
    EditText mLoginUserEdt;
    @BindView(R.id.mLoginUserTextInput)
    TextInputLayout mLoginUserTextInput;
    @BindView(R.id.mLoginPassTextInput)
    TextInputLayout mLoginPassTextInput;
    @BindView(R.id.mLoginPassEdt)
    EditText mLoginPassEdt;
    @BindView(R.id.mLoginProveEdt)
    EditText mLoginProveEdt;
    @BindView(R.id.mLoginRePassEdt)
    EditText mLoginRePassEdt;
    @BindView(R.id.mLoginSendProvBtn)
    Button mLoginSendProvBtn;
    @BindView(R.id.mLoginBtn)
    Button mLoginBtn;
    @BindView(R.id.mLoginSignUpBtn)
    Button mLoginSignUpBtn;
    @BindView(R.id.mLoginForgetBtn)
    Button mLoginForgetBtn;
    @BindView(R.id.mSignUpRelative)
    RelativeLayout mSignUpRelative;
    @BindView(R.id.mLoginIconImg)
    ImageView mLoginIconImg;
    @BindView(R.id.mLoginMineTxt)
    TextView mLoginMineTxt;
    @BindView(R.id.mLoginIconLiL)
    LinearLayout mLoginIconLiL;
    private static Long mExitTime = (long) 0;//退出时间
    private boolean isEnter = false;
    private int repeatCount = 0;
    private LoginPresenter mPresenter;
    private int height;

    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {//间隔超过2s
                Trace.show(getApplicationContext(), "再点击一次退出应用");
                mExitTime = System.currentTimeMillis();
            } else {
                Trace.i("exit Main");
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void doSthBeforeSetView(Bundle savedInstanceState) {
        super.doSthBeforeSetView(savedInstanceState);
        closeSliding();
    }

    @Override
    protected boolean initCallback(Message msg) {
        return false;
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_login;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        mSignUpRelative.post(new Runnable() {
            @Override
            public void run() {
                height = mSignUpRelative.getHeight();
                mSignUpRelative.setVisibility(View.GONE);
            }
        });
        String str = mLoginMineTxt.getText().toString();
        try {
            str = str + "V" + NormalUtils.getVersionName(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mLoginMineTxt.setText(str);
        SupportSoftKeyboardUtil.addSoftKeyboardListener(this, mLoginUserEdt, mLoginIconLiL);
        String user = PreferenceUtils.getString(Config.KEY_USER, "", mContext);
        if (!TextUtils.isEmpty(user)) {
            mLoginUserEdt.setText(user);
            mLoginUserEdt.setSelection(user.length());
            mLoginPassEdt.requestFocus();
            mLoginPassTextInput.postDelayed(new Runnable() {
                @Override
                public void run() {
                    KeyboardUtil.showKeyboard(mActivity, mLoginPassTextInput);
                }
            }, 800);
            //干脆地使用初始设置 键盘自动弹出
        } else {
            mLoginUserTextInput.postDelayed(new Runnable() {
                @Override
                public void run() {
                    KeyboardUtil.showKeyboard(mActivity, mLoginUserTextInput);
                }
            }, 800);
        }
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        mPresenter = new LoginPresenter(this);
    }

    @Override
    protected void initEvent(Bundle savedInstanceState) {
        super.initEvent(savedInstanceState);
        mLoginPassEdt.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN
                            && mLoginBtn.getText().toString().equals("登录")) {
                        loginClick();
                        return true;
                    }
                    return mLoginBtn.getText().toString().equals("登录");
                }
                return false;
            }
        });
        //禁止scrollview的滑动
//        mLoginScV.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View arg0, MotionEvent arg1) {
//                return true;
//            }
//        });
//        mLoginFunLiL.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
//            @Override
//            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        mLoginScV.smoothScrollTo(0, mLoginScV.getHeight());
//                    }
//                }, 400);
//            }
//        });
        mLoginProveEdt.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (!isEnter && event.getAction() == KeyEvent.ACTION_DOWN) {
                        isEnter = true;
                        if (mLoginSignUpBtn.getText().toString().equals("注册确认")) {
                            signUpClick();
                        } else if (mLoginForgetBtn.getText().toString().equals("找回密码")
                                && mLoginForgetBtn.getVisibility() == View.VISIBLE) {
                            forgetSecretClick();
                        }
                        return true;
                    }
                    return true;
                }
                return false;
            }
        });

//        mLoginPassEdt.setFilters(new InputFilter[]{new InputFilter.LengthFilter(13)});
//        mLoginRePassEdt.setFilters(new InputFilter[]{new InputFilter.LengthFilter(13)});
        mLoginProveEdt.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
        mLoginUserEdt.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)});
    }

    /**
     * ok
     */
    @OnClick(R.id.mLoginBtn)
    public void loginClick() {
        mLoginPassTextInput.setHint("请输入密码");
        //Log.d("md5",NormalUtils.md5(mLoginPassEdt.getText().toString() + MyApplication.SaltKey));
        if (mLoginBtn.getText().toString().equals("返回登录")) {
            AnimationUtil.runAnimator(false, mSignUpRelative, height, 200);//loginClick
//            mSignUpRelative.setVisibility(View.GONE);
            mLoginForgetBtn.setVisibility(View.VISIBLE);
            mLoginForgetBtn.setText("忘记密码");
            mLoginBtn.setText("登录");
            mLoginSignUpBtn.setText("注册");
        } else {
            String txtUser = mLoginUserEdt.getText().toString();
            String txtPass = mLoginPassEdt.getText().toString();
            if (TextUtils.isEmpty(txtUser)) {
                Trace.show(getApplicationContext(), "请输入手机号");
            } else if (TextUtils.isEmpty(txtPass)) {
                Trace.show(getApplicationContext(), "请输入密码");
            } else {
                //登录验证
                SoftKeyboardUtils.hideInputMode(mActivity
                        , (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE));
                dialog = DialogUtils.showIndeterminateProgressDialog(mActivity, false
                        , "登录中", "请稍候").show();
                mPresenter.login(txtUser, txtPass);
            }
        }
    }

    /**
     * ok
     */
    @OnClick(R.id.mLoginSignUpBtn)
    public void signUpClick() {
        mLoginPassTextInput.setHint("请输入密码(长度需大于6位)");
        if (mLoginSignUpBtn.getText().toString().equals("注册")) {
            mLoginBtn.setText("返回登录");
            mLoginForgetBtn.setText("忘记密码");
            mLoginSignUpBtn.setText("注册确认");
            if (mSignUpRelative.getVisibility() == View.INVISIBLE
                    || mSignUpRelative.getVisibility() == View.GONE)
                AnimationUtil.runAnimator(true, mSignUpRelative, height, 200);//signUpClick
//            mSignUpRelative.setVisibility(View.VISIBLE);
        } else {
            String txtUser = mLoginUserEdt.getText().toString();
            String txtPass = mLoginPassEdt.getText().toString();
            String txtRePass = mLoginRePassEdt.getText().toString();
            String txtProv = mLoginProveEdt.getText().toString();
            if (mPresenter.tableCheck(mContext, txtUser, txtPass, txtRePass, txtProv))
                mPresenter.signUp(txtUser, txtPass, txtProv);
            else
                isEnter = false;//signUp tableCheck false
        }
    }

    /**
     * ok
     */
    @OnClick(R.id.mLoginForgetBtn)
    public void forgetSecretClick() {
        if (mLoginForgetBtn.getText().toString().equals("忘记密码")) {
            mLoginBtn.setText("返回登录");
            mLoginForgetBtn.setText("找回密码");
            mLoginSignUpBtn.setText("注册");
            mLoginPassTextInput.setHint("请输入新密码(长度需大于6位)");
            if (mSignUpRelative.getVisibility() == View.INVISIBLE
                    || mSignUpRelative.getVisibility() == View.GONE)
                AnimationUtil.runAnimator(true, mSignUpRelative, height, 200);//forgetSecretClick
//            mSignUpRelative.setVisibility(View.VISIBLE);
            //Toast.makeText(getApplicationContext(), "请输入曾注册的手机号", Toast.LENGTH_SHORT).show();
        } else if (mLoginForgetBtn.getText().toString().equals("找回密码")) {
            String txtUser = mLoginUserEdt.getText().toString();
            String txtPass = mLoginPassEdt.getText().toString();
            String txtRePass = mLoginRePassEdt.getText().toString();
            String txtProv = mLoginProveEdt.getText().toString();
            if (mPresenter.tableCheck(mContext, txtUser, txtPass, txtRePass, txtProv))
                mPresenter.forget(txtUser, txtPass, txtProv);
            else
                isEnter = false;//forgetSecret tableCheck false
        }
    }

    /**
     * ok
     */
    @OnClick(R.id.mLoginSendProvBtn)
    public void sendProvClick() {
        String txtForget = mLoginForgetBtn.getText().toString();
        final String txtUser = mLoginUserEdt.getText().toString();
        if (txtUser.equals("") || txtUser.length() != 11) {
            Trace.show(getApplicationContext(), "请先填写11位手机号");
        } else {
            if (txtForget.equals("找回密码")) {//忘记密码
                mPresenter.forgetSendProv(txtUser, false, Config.timeout_prov);
            } else if (txtForget.equals("忘记密码")) {//注册
                mPresenter.signUpSendProv(txtUser, true, Config.timeout_prov);
            }
        }
    }

    protected void goToMain() {
        //存入shared
        PreferenceUtils.putString(Config.KEY_USER, mLoginUserEdt.getText().toString(), this);
        PreferenceUtils.putBoolean(Config.KEY_ISLOGIN, true, this);
        PreferenceUtils.putString(Config.KEY_PASS, mLoginPassEdt.getText().toString(), this);
        PrimaryData.getInstance(getHelper(), new PrimaryData.DoAfterWithEx() {
            @Override
            public void justNowWithEx(Exception e) {
                dismissDialog();
            }
        });
        handler.post(runnableForData);//goToMain
    }

    private Runnable runnableForData = new Runnable() {
        @Override
        public void run() {
            if (PrimaryData.status != null
                    && PrimaryData.status.isFolderReady
                    && PrimaryData.status.isItemReady
                    && PrimaryData.status.isHeaderReady
                    && PrimaryData.status.isNoteReady) {
                Trace.d("runnableForData done Login");
                dismissDialog();
                ARouter.getInstance().build("/yellow/main").navigation();
                finish();
            } else {
                if (repeatCount * Config.period_runnable <= Config.timeout_runnable) {
                    if (PrimaryData.status != null)
                        Trace.d("folder:" + PrimaryData.status.isFolderReady
                                + "note:" + PrimaryData.status.isNoteReady
                                + "items:" + PrimaryData.status.isItemReady);
                    handler.postDelayed(runnableForData, Config.period_runnable);
                    repeatCount++;
                } else
                    repeatCount = 0;
            }
        }
    };

    @Override
    public void loginSuccess(String txtUser, String user_default_folderId, String user_icon, String user_read_pass) {
        PreferenceUtils.putString(Config.KEY_DEFAULT_FOLDER, user_default_folderId, LoginActivity.this);
        PreferenceUtils.putString(Config.KEY_USERICON, user_icon, LoginActivity.this);
        PatternLockUtils.setPattern(user_read_pass, getApplicationContext());
        goToMain();//正常登陆
    }

    @Override
    public void exception(String s, AVException e) {
        Trace.e(s + Trace.getErrorMsg(e));
        e.printStackTrace();
        dismissDialog();
        Trace.show(mActivity, s + Trace.getErrorMsg(e));
    }

    @Override
    public void saySth(String s) {
        dismissDialog();
        Trace.show(mActivity, s, false);
    }

    @Override
    public CountDownTimer startSendProv() {
        mLoginSendProvBtn.setEnabled(false);
        mLoginUserEdt.setEnabled(false);
        return new CountDownTimer(Config.timeout_prov * 60 * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                String str = "发送成功" + (millisUntilFinished / 1000);
                mLoginSendProvBtn.setText(str);
            }

            @Override
            public void onFinish() {
                mLoginSendProvBtn.setEnabled(true);
                mLoginUserEdt.setEnabled(true);
                mLoginSendProvBtn.setText("发送验证码");
            }
        }.start();
    }

    @Override
    public void sendProvFailed(CountDownTimer cdt) {
        cdt.cancel();
        mLoginSendProvBtn.setEnabled(true);
        mLoginUserEdt.setEnabled(true);
        mLoginSendProvBtn.setText("发送验证码");
        Trace.show(mActivity, "验证码发送失败");
    }

    @Override
    public void signUpSuccess(String txtUser) {
        PreferenceUtils.getString(Config.KEY_DEFAULT_FOLDER, "", mActivity);
        Trace.e("signUpVerify 用户注册完成");
        Trace.show(mActivity, txtUser + "注册成功");
        goToMain();//注册登录
    }

    @Override
    public void changeEnter() {
        isEnter = false;
    }

    @Override
    public void doThisOnUiThread(Runnable runnable) {
        runOnUiThread(runnable);
    }
}
