package com.kerchin.yellownote.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.InputFilter;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.bigkoo.svprogresshud.SVProgressHUD;
import com.kerchin.yellownote.R;
import com.kerchin.yellownote.base.LoginAbstract;
import com.kerchin.yellownote.bean.PrimaryData;
import com.kerchin.yellownote.global.Config;
import com.kerchin.yellownote.global.MyApplication;
import com.kerchin.yellownote.proxy.LoginService;
import com.kerchin.yellownote.proxy.SecretService;
import com.kerchin.yellownote.utilities.NormalUtils;
import com.kerchin.yellownote.utilities.PatternLockUtils;
import com.kerchin.yellownote.utilities.SoftKeyboardUtils;
import com.kerchin.yellownote.utilities.Trace;
import com.securepreferences.SecurePreferences;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Kerchin on 2015/8/1 0005.
 */
public class LoginActivity extends LoginAbstract {
    @BindView(R.id.mLoginScV)
    ScrollView mLoginScV;
    @BindView(R.id.mLoginUserEdt)
    EditText mLoginUserEdt;
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
    @BindView(R.id.mLoginFunLiL)
    LinearLayout mLoginFunLiL;
    private SVProgressHUD mSVProgressHUD;
    private final static long runnableTimeout = 8000;
    private final static long runnablePeriod = 200;
    private final static byte statusInit = -1;//未查询或查询中
    private final static byte statusFalse = 0;//查询结果为假
    private final static byte statusTrue = 1;//查询结果为真
    private static Long mExitTime = (long) 0;//退出时间
    private boolean isEnter = false;
    private boolean smsStatus = false;
    private byte registerStatus = -1;
    private int repeatCount = 0;
    private Handler handler = new Handler();

    @Override
    protected void setContentView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_login);
        NormalUtils.immerge(LoginActivity.this, R.color.lightSkyBlue);
    }

    @Override
    protected void initializeView(Bundle savedInstanceState) {
        ButterKnife.bind(this);
        mSVProgressHUD = new SVProgressHUD(this);
    }

    @Override
    protected void initializeData(Bundle savedInstanceState) {

    }

    @Override
    protected void initializeEvent(Bundle savedInstanceState) {
        if (!MyApplication.user.equals("")) {
            mLoginUserEdt.setText(MyApplication.user);
            mLoginUserEdt.setSelection(MyApplication.user.length());
            mLoginPassEdt.requestFocus();
        } else
            mLoginUserEdt.requestFocus();
        mLoginPassEdt.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN
                            && mLoginBtn.getText().toString().equals("登录")) {
                        login();
                        return true;
                    }
                    return mLoginBtn.getText().toString().equals("登录");
                }
                return false;
            }
        });
        //禁止scrollview的滑动
        mLoginScV.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                return true;
            }
        });
        mLoginFunLiL.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mLoginScV.smoothScrollTo(0, mLoginScV.getHeight());
                    }
                }, 400);
            }
        });
        mLoginProveEdt.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (!isEnter && event.getAction() == KeyEvent.ACTION_DOWN) {
                        isEnter = true;
                        if (mLoginSignUpBtn.getText().toString().equals("注册确认")) {
                            signUp();
                        } else if (mLoginForgetBtn.getText().toString().equals("找回密码")
                                && mLoginForgetBtn.getVisibility() == View.VISIBLE) {
                            forgetSecret();
                        }
                        return true;
                    }
                    return true;
                }
                return false;
            }
        });
        mLoginPassEdt.setFilters(new InputFilter[]{new InputFilter.LengthFilter(13)});
        mLoginRePassEdt.setFilters(new InputFilter[]{new InputFilter.LengthFilter(13)});
        mLoginProveEdt.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
        mLoginUserEdt.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)});

    }

    @OnClick(R.id.mLoginBtn)
    public void loginClick() {
        //Log.d("md5",NormalUtils.md5(mLoginPassEdt.getText().toString() + MyApplication.SaltKey));
        if (mLoginBtn.getText().toString().equals("返回登录")) {
            mSignUpRelative.setVisibility(View.GONE);
            mLoginForgetBtn.setVisibility(View.VISIBLE);
            mLoginForgetBtn.setText("忘记密码");
            mLoginBtn.setText("登录");
            mLoginSignUpBtn.setText("注册");
        } else {
            login();
        }
    }

    //登录流程起点
    @Override
    protected void login() {
        String txtUser = mLoginUserEdt.getText().toString();
        String txtPass = mLoginPassEdt.getText().toString();
        if (txtUser.equals("")) {
            Trace.show(getApplicationContext(), "请输入手机号");
        } else if (txtPass.equals("")) {
            Trace.show(getApplicationContext(), "请输入密码");
        } else {
            //登录验证
            loginVerify(txtUser, txtPass);
        }
    }

    @OnClick(R.id.mLoginSignUpBtn)
    public void signUpClick() {
        if (mLoginSignUpBtn.getText().toString().equals("注册")) {
            mLoginBtn.setText("返回登录");
            mLoginForgetBtn.setText("忘记密码");
            mLoginForgetBtn.setVisibility(View.GONE);
            mLoginSignUpBtn.setText("注册确认");
            mSignUpRelative.setVisibility(View.VISIBLE);
        } else {
            signUp();
        }
    }

    //注册流程起点 已将isRegistered放在sendProv中判断可通过smsVerify就不再判断
    @Override
    protected void signUp() {
        final String txtUser = mLoginUserEdt.getText().toString();
        final String txtPass = mLoginPassEdt.getText().toString();
        String txtRePass = mLoginRePassEdt.getText().toString();
        final String txtProv = mLoginProveEdt.getText().toString();
        //表格检查
        if (tableCheck(txtUser, txtPass, txtRePass, txtProv)) {
            //验证验证码成功则注册成功以该帐号登录
            if (Config.isDebugMode)
                signUpVerify(txtUser, txtPass);
            else {
                smsVerify(txtProv, txtUser);
                new CountDownTimer(Config.timeout_avod, 250) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        if (smsStatus) {
                            cancel();
                            smsStatus = false;
                            Trace.d("CDTimer signUp server echo " + millisUntilFinished);
                            signUpVerify(txtUser, txtPass);
                        }
                    }

                    @Override
                    public void onFinish() {
                    }
                }.start();
            }
        } else
            isEnter = false;//signUp tableCheck false
    }

    @OnClick(R.id.mLoginForgetBtn)
    public void forgetSecretClick() {
        if (mLoginForgetBtn.getText().toString().equals("忘记密码")) {
            mLoginBtn.setText("返回登录");
            mLoginForgetBtn.setText("找回密码");
            mSignUpRelative.setVisibility(View.VISIBLE);
            //Toast.makeText(getApplicationContext(), "请输入曾注册的手机号", Toast.LENGTH_SHORT).show();
        } else {
            forgetSecret();
        }
    }

    //找回密码流程起点
    @Override
    protected void forgetSecret() {
        final String txtUser = mLoginUserEdt.getText().toString();
        final String txtPass = mLoginPassEdt.getText().toString();
        String txtRePass = mLoginRePassEdt.getText().toString();
        final String txtProv = mLoginProveEdt.getText().toString();
        //表格检查
        if (tableCheck(txtUser, txtPass, txtRePass, txtProv)) {
            //是否注册查询
            isRegistered(txtUser);
            new CountDownTimer(Config.timeout_avod, 250) {
                @Override
                public void onTick(long millisUntilFinished) {
                    if (registerStatus > statusInit) {
                        cancel();
                        Trace.d("CDTimer is registered server echo " + millisUntilFinished);
                        if (registerStatus == statusFalse) {
                            Trace.show(getApplicationContext(), "该帐号尚未注册");
                        } else {
                            if (Config.isDebugMode)
                                forgetVerify(txtUser, txtPass);
                            else {
                                //验证验证码成功则找回成功以该帐号登录
                                smsVerify(txtProv, txtUser);
                                new CountDownTimer(Config.timeout_avod, 250) {
                                    @Override
                                    public void onTick(long millisUntilFinished) {
                                        if (smsStatus) {
                                            cancel();
                                            smsStatus = false;
                                            Trace.d("CDTimer SMSVerify server echo " + millisUntilFinished);
                                            forgetVerify(txtUser, txtPass);
                                        }
                                    }

                                    @Override
                                    public void onFinish() {
                                    }
                                }.start();
                            }
                        }
                        registerStatus = statusInit;
                    }
                }

                @Override
                public void onFinish() {
                }
            }.start();
        } else
            isEnter = false;//forgetSecret tableCheck false
    }

    @OnClick(R.id.mLoginSendProvBtn)
    public void sendProvClick() {
        String txtForget = mLoginForgetBtn.getText().toString();
        final String txtUser = mLoginUserEdt.getText().toString();
        if (txtUser.equals("") || txtUser.length() != 11) {
            Trace.show(getApplicationContext(), "请先填写11位手机号");
        } else {
            if (txtForget.equals("找回密码")) {//忘记密码
                //是否注册查询
                isRegistered(txtUser);
                new CountDownTimer(Config.timeout_avod, 250) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        if (registerStatus > statusInit) {
                            cancel();
                            Trace.d("CDTimer isRegistered server echo " + millisUntilFinished);
                            if (registerStatus == statusTrue) {
                                sendProv(false, txtUser, Config.timeout_prov);
                            } else {
                                Trace.show(getApplicationContext(), "该帐号未注册,请先注册");
                            }
                            registerStatus = statusInit;
                        }
                    }

                    @Override
                    public void onFinish() {
                    }
                }.start();
            } else {//注册
                //是否注册查询
                isRegistered(txtUser);
                new CountDownTimer(Config.timeout_avod, 250) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        if (registerStatus > statusInit) {
                            cancel();
                            Trace.d("CDTimer isRegistered server echo " + millisUntilFinished);
                            if (registerStatus == statusFalse) {
                                sendProv(true, txtUser, Config.timeout_prov);
                            } else {
                                Trace.show(getApplicationContext(), "该帐号已注册,请直接登录");
                            }
                            registerStatus = statusInit;
                        }
                    }

                    @Override
                    public void onFinish() {
                    }
                }.start();
            }
        }
    }

    //发送验证码
    @Override
    protected void sendProv(final boolean isSignUp, final String txtUser, final int validPeriod) {
        mLoginSendProvBtn.setEnabled(false);
        mLoginUserEdt.setEnabled(false);
        new CountDownTimer(validPeriod * 60 * 1000, 1000) {
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
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    LoginService.sendProv(txtUser, isSignUp, validPeriod);
                } catch (AVException e) {
                    Trace.e("发送验证码失败" + Trace.getErrorMsg(e));
                    e.printStackTrace();
                    Trace.show(LoginActivity.this, "发送验证码失败" + Trace.getErrorMsg(e));
                }
            }
        }).start();
    }

    //短信验证
    @Override
    protected void smsVerify(final String txtProv, final String txtUser) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    LoginService.smsVerify(txtProv, txtUser);
                    smsStatus = true;
                } catch (AVException e) {
                    Trace.e("验证验证码失败" + Trace.getErrorMsg(e));
                    e.printStackTrace();
                    Trace.show(LoginActivity.this, "验证码错误" + Trace.getErrorMsg(e));
                }
            }
        }).start();
    }

    //登录操作确认
    @Override
    protected void loginVerify(final String txtUser, final String txtPass) {
        SoftKeyboardUtils.hideInputMode(LoginActivity.this
                , (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE));
        mSVProgressHUD.showWithStatus("加载中...");
        isRegistered(txtUser);//是否注册查询
        new CountDownTimer(Config.timeout_avod, 250) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (registerStatus > statusInit) {
                    Trace.d("CDTimer isRegistered server echo " + millisUntilFinished);
                    if (registerStatus == statusFalse) {
                        Trace.show(getApplicationContext(), "该帐号尚未注册");
                        dismissProgress();
                    } else {
                        //密码查询
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    AVObject avObjects = LoginService.loginVerify(txtUser, txtPass);
                                    if (avObjects != null) {
                                        boolean isFrozen = avObjects.getBoolean("isFrozen");
                                        if (isFrozen) {
                                            dismissProgress();
                                            Trace.show(LoginActivity.this, "您的账号已被冻结,请联系hkq325800@163.com", Toast.LENGTH_LONG);
                                        } else {
                                            MyApplication.userDefaultFolderId = avObjects.getString("user_default_folderId");
                                            String pattern = SecretService.getPatternStr(txtUser);
                                            PatternLockUtils.setPattern(pattern, getApplicationContext());
                                            if (Config.isDebugMode)
                                                Trace.show(LoginActivity.this, pattern);
                                            goToMain();//正常登陆
                                        }
                                    } else {
                                        //密码错误重新登录
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                dismissProgress();
                                                mLoginPassEdt.setText("");
                                            }
                                        });
                                        Trace.show(LoginActivity.this, "密码错误,请重试");
                                    }
                                } catch (AVException e) {
                                    Trace.e("登陆验证失败" + Trace.getErrorMsg(e));
                                    e.printStackTrace();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            dismissProgress();
                                        }
                                    });
                                    Trace.show(LoginActivity.this, "登陆验证失败" + Trace.getErrorMsg(e));
                                }
                            }
                        }).start();
                    }
                    registerStatus = statusInit;
                    cancel();
                }
            }

            @Override
            public void onFinish() {
            }
        }.start();
    }

    //注册操作确认
    @Override
    protected void signUpVerify(final String txtUser, final String txtPass) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    MyApplication.userDefaultFolderId = LoginService.createDefaultFolder(txtUser);
                    Trace.d("signUpVerify 默认文件夹创建完成");
                } catch (AVException e) {
                    Trace.e("创建默认笔记夹失败" + Trace.getErrorMsg(e));
                    e.printStackTrace();
                    Trace.show(LoginActivity.this, "创建默认笔记夹失败" + Trace.getErrorMsg(e));
                }
                try {
                    if (!MyApplication.userDefaultFolderId.equals("")) {
                        LoginService.userSignUp(txtUser, txtPass, MyApplication.userDefaultFolderId);
                        Trace.d("signUpVerify 用户注册完成");
                        Trace.show(LoginActivity.this, txtUser + "注册成功");
                        goToMain();//注册登录
                    }
                } catch (AVException e) {
                    Trace.e("提交注册失败" + Trace.getErrorMsg(e));
                    e.printStackTrace();
                    Trace.show(LoginActivity.this, "提交注册失败" + Trace.getErrorMsg(e));
                } finally {
                    isEnter = false;//signUpVerify saveInBackground true
                }
            }
        }).start();
    }

    //忘记密码操作确认
    @Override
    protected void forgetVerify(final String txtUser, final String txtPass) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    LoginService.forgetSave(txtUser, txtPass);
                    Trace.show(LoginActivity.this, "修改成功");
                    goToMain();//找回密码登录
                } catch (AVException e) {
                    Trace.e("忘记密码操作失败" + Trace.getErrorMsg(e));
                    e.printStackTrace();
                    Trace.show(LoginActivity.this, "忘记密码操作失败" + Trace.getErrorMsg(e));
                } finally {
                    isEnter = false;//forgetVerify saveInBackground true
                }
            }
        }).start();
    }

    //是否注册验证
    @Override
    protected void isRegistered(final String txtUser) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean flag = LoginService.isRegistered(txtUser);
                    if (flag) {
                        Trace.d("验证是否已经注册 查询到" + txtUser + "已注册");
                        registerStatus = statusTrue;
                    } else
                        registerStatus = statusFalse;
                } catch (AVException e) {
                    Trace.e("验证是否注册失败" + Trace.getErrorMsg(e));
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dismissProgress();
                        }
                    });
                    Trace.show(LoginActivity.this, "验证是否注册失败" + Trace.getErrorMsg(e));
                }
            }
        }).start();
    }

    //表格信息校验
    @Override
    protected boolean tableCheck(String txtUser, String txtPass, String txtRePass, String txtProv) {
        if (Config.isDebugMode)
            return true;
        if (txtUser.equals("") || txtUser.length() != 11) {
            Trace.show(getApplicationContext(), "请输入11位手机号并接收验证码");
            return false;
        } else if (txtRePass.equals("") | txtPass.equals("")) {
            Trace.show(getApplicationContext(), "请输入密码并重复一次");
            return false;
        } else if (!txtRePass.equals(txtPass)) {
            Trace.show(getApplicationContext(), "两次输入的密码不同");
            return false;
        } else if (txtPass.length() < 6 || txtPass.length() > 13) {
            Trace.show(getApplicationContext(), "密码长度控制在6-13位");
            return false;
        } else if (txtProv.length() != 6) {
            Trace.show(getApplicationContext(), "请输入6位验证码");
            return false;
        } else {
            return true;
        }
    }

    //通过正常登陆、注册、找回密码登录 缓存状态并跳转主页面
    @Override
    protected void goToMain() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                PrimaryData.getInstance(new PrimaryData.DoAfterWithEx() {
                    @Override
                    public void justNowWithEx(Exception e) {
                        dismissProgress();
                    }
                });
            }
        }).start();
        //存入shared
        MyApplication.setUser(mLoginUserEdt.getText().toString());
        SecurePreferences.Editor editor = (SecurePreferences.Editor) MyApplication.getDefaultShared().edit();
        editor.putString(Config.KEY_USER, mLoginUserEdt.getText().toString());
        editor.putString(Config.KEY_DEFAULT_FOLDER, MyApplication.userDefaultFolderId);
        editor.putBoolean(Config.KEY_ISLOGIN, true);
        editor.putString(Config.KEY_PASS, mLoginPassEdt.getText().toString());
        editor.apply();
        handler.post(runnableForData);//goToMain
    }

    private Runnable runnableForData = new Runnable() {
        @Override
        public void run() {
            if (PrimaryData.status.isFolderReady
                    && PrimaryData.status.isItemReady
                    && PrimaryData.status.isNoteReady) {
                Trace.d("runnableForData done Login");
                MainActivity.startMe(getApplicationContext());
//                NormalUtils.goToActivity(LoginActivity.this, MainActivity.class);
                finish();
            } else {
                if (repeatCount * runnablePeriod <= runnableTimeout) {
                    Trace.d("folder:" + PrimaryData.status.isFolderReady
                            + "note:" + PrimaryData.status.isNoteReady
                            + "items:" + PrimaryData.status.isItemReady);
                    handler.postDelayed(runnableForData, runnablePeriod);
                    repeatCount++;
                } else
                    repeatCount = 0;
            }
        }
    };

    private void dismissProgress(){
        if(mSVProgressHUD.isShowing())
            mSVProgressHUD.dismiss();
    }

    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {//间隔超过2s
                Trace.show(getApplicationContext(), "再点击一次退出应用");
                mExitTime = System.currentTimeMillis();
            } else {
                Trace.i(TAG, "exit Main");
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
