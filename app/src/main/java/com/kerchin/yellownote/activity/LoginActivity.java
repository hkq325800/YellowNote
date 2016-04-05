package com.kerchin.yellownote.activity;

import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.InputFilter;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.kerchin.yellownote.R;
import com.kerchin.yellownote.global.Config;
import com.kerchin.yellownote.global.MyApplication;
import com.kerchin.yellownote.base.User;
import com.kerchin.yellownote.proxy.LoginService;
import com.kerchin.yellownote.utilities.NormalUtils;
import com.kerchin.yellownote.utilities.SystemBarTintManager;
import com.kerchin.yellownote.utilities.Trace;
import com.securepreferences.SecurePreferences;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Kerchin on 2015/8/1 0005.
 */
public class LoginActivity extends User {
    private static final String LOG_TAG = "YellowNote-" + LoginActivity.class.getSimpleName();
    private static Long mExitTime = (long) 0;//退出时间
    private final byte statusInit = -1;//未查询或查询中
    private final byte statusFalse = 0;//查询结果为假
    private final byte statusTrue = 1;//查询结果为真
    boolean isEnter = false;
    private String objectId;
    private byte registerStatus = -1;
    private boolean smsStatus = false;
    @Bind(R.id.mLoginScV)
    ScrollView mLoginScV;
    @Bind(R.id.mLoginUserEdt)
    EditText mLoginUserEdt;
    @Bind(R.id.mLoginPassEdt)
    EditText mLoginPassEdt;
    @Bind(R.id.mLoginProveEdt)
    EditText mLoginProveEdt;
    @Bind(R.id.mLoginRePassEdt)
    EditText mLoginRePassEdt;
    @Bind(R.id.mLoginSendProvBtn)
    Button mLoginSendProvBtn;
    @Bind(R.id.mLoginBtn)
    Button mLoginBtn;
    @Bind(R.id.mLoginSignUpBtn)
    Button mLoginSignUpBtn;
    @Bind(R.id.mLoginForgetBtn)
    Button mLoginForgetBtn;
    @Bind(R.id.mSignUpRelative)
    RelativeLayout mSignUpRelative;
    @Bind(R.id.mLoginFunLiL)
    LinearLayout mLoginFunLiL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        immerge(R.color.lightSkyBlue);
        init();
    }

    private void immerge(int color) {
        /**沉浸式状态栏设置部分**/
        //Android5.0版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            setStatusBarColor(color);//阴影绘制
            //设置状态栏颜色
//            getWindow().setStatusBarColor(getResources().getColor(color));
            //设置导航栏颜色
            getWindow().setNavigationBarColor(getResources().getColor(color));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //创建状态栏的管理实例
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            //激活状态栏设置
            tintManager.setStatusBarTintEnabled(true);
            //设置状态栏颜色
            tintManager.setTintResource(color);
            //激活导航栏会变黑
            //透明导航栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            //激活导航栏设置
            tintManager.setNavigationBarTintEnabled(true);
            //设置导航栏颜色
            tintManager.setNavigationBarTintResource(color);
        }
    }

    private void init() {
        ButterKnife.bind(this);
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
        mLoginSignUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        });
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        });
        mLoginForgetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLoginForgetBtn.getText().toString().equals("忘记密码")) {
                    mLoginBtn.setText("返回登录");
                    mLoginForgetBtn.setText("找回密码");
                    mSignUpRelative.setVisibility(View.VISIBLE);
                    //Toast.makeText(LoginActivity.this, "请输入曾注册的手机号", Toast.LENGTH_SHORT).show();
                } else {
                    forgetSecret();
                }
            }
        });
        mLoginSendProvBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txtForget = mLoginForgetBtn.getText().toString();
                final String txtUser = mLoginUserEdt.getText().toString();
                final int count = Config.timeout_prov;
                if (txtUser.equals("") || txtUser.length() != 11) {
                    Trace.show(LoginActivity.this, "请先填写11位手机号");
                } else {
                    if (txtForget.equals("找回密码")) {//忘记密码
                        //是否注册查询
                        isRegistered(txtUser);
                        new CountDownTimer(Config.timeout_avod, 500) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                                if (registerStatus > statusInit) {
                                    cancel();
                                    Trace.d("CDTimer isRegistered server echo " + millisUntilFinished);
                                    if (registerStatus == statusTrue) {
                                        sendProv(false, txtUser, count);
                                    } else {
                                        Trace.show(LoginActivity.this, "该帐号未注册,请先注册");
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
                        new CountDownTimer(Config.timeout_avod, 500) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                                if (registerStatus > statusInit) {
                                    cancel();
                                    Trace.d("CDTimer isRegistered server echo " + millisUntilFinished);
                                    if (registerStatus == statusFalse) {
                                        sendProv(true, txtUser, count);
                                    } else {
                                        Trace.show(LoginActivity.this, "该帐号已注册,请直接登录");
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
        });
    }

    //登录流程起点
    @Override
    protected void login() {
        String txtUser = mLoginUserEdt.getText().toString();
        String txtPass = mLoginPassEdt.getText().toString();
        if (txtUser.equals("")) {
            Trace.show(LoginActivity.this, "请输入手机号");
        } else if (txtPass.equals("")) {
            Trace.show(LoginActivity.this, "请输入密码");
        } else {
            //登录验证
            loginVerify(txtUser, txtPass);
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
                new CountDownTimer(Config.timeout_avod, 500) {
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
            new CountDownTimer(Config.timeout_avod, 500) {
                @Override
                public void onTick(long millisUntilFinished) {
                    if (registerStatus > statusInit) {
                        cancel();
                        Trace.d("CDTimer is registered server echo " + millisUntilFinished);
                        if (registerStatus == statusFalse) {
                            Trace.show(LoginActivity.this, "该帐号尚未注册");
                        } else {
                            if (Config.isDebugMode)
                                forgetVerify(txtUser, txtPass);
                            else {
                                //验证验证码成功则找回成功以该帐号登录
                                smsVerify(txtProv, txtUser);
                                new CountDownTimer(Config.timeout_avod, 500) {
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

    //发送验证码
    @Override
    protected void sendProv(final boolean isSignUp, final String txtUser, final int count) {
        mLoginSendProvBtn.setEnabled(false);
        mLoginUserEdt.setEnabled(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    new CountDownTimer(count * 60 * 1000, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            mLoginSendProvBtn.setText("发送成功" + ((millisUntilFinished) / 1000));
                        }

                        @Override
                        public void onFinish() {
                            mLoginSendProvBtn.setEnabled(true);
                            mLoginUserEdt.setEnabled(true);
                            mLoginSendProvBtn.setText("发送验证码");
                        }
                    }.start();
                    LoginService.sendProv(txtUser, isSignUp, count);
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
        isRegistered(txtUser);//是否注册查询
        new CountDownTimer(Config.timeout_avod, 500) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (registerStatus > statusInit) {
                    Trace.d("CDTimer isRegistered server echo " + millisUntilFinished);
                    if (registerStatus == statusFalse) {
                        Trace.show(LoginActivity.this, "该帐号尚未注册");
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
                                            Trace.show(LoginActivity.this, "您的账号已被冻结,请联系hkq325800@163.com", Toast.LENGTH_LONG);
                                        } else {
                                            MyApplication.userDefaultFolderId = avObjects.getString("user_default_folderId");
                                            goToMain();
                                        }
                                    } else {
                                        //密码错误重新登录
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                mLoginPassEdt.setText("");
                                            }
                                        });
                                        Trace.show(LoginActivity.this, "密码错误,请重试");
                                    }
                                } catch (AVException e) {
                                    Trace.e("登陆验证失败" + Trace.getErrorMsg(e));
                                    e.printStackTrace();
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
                AVObject folder = null;
                try {
                    folder = LoginService.signUpVerify(txtUser);
                    Trace.d("signUpVerify 默认文件夹创建完成");
                    MyApplication.userDefaultFolderId = folder.getObjectId();
                } catch (AVException e) {
                    Trace.e("创建默认笔记夹失败" + Trace.getErrorMsg(e));
                    e.printStackTrace();
                    Trace.show(LoginActivity.this, "创建默认笔记夹失败" + Trace.getErrorMsg(e));
                }
                try {
                    if (folder != null) {
                        LoginService.userSignUp(txtUser, txtPass, MyApplication.userDefaultFolderId);
                        Trace.d("signUpVerify 用户注册完成");
                        Trace.show(LoginActivity.this, txtUser + "注册成功");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                goToMain();
                            }
                        });
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
                    LoginService.forgetVerify(objectId, txtPass);
                    Trace.show(LoginActivity.this, "修改成功");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            goToMain();
                        }
                    });
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
                    AVObject a = LoginService.isRegistered(txtUser);
                    if (a != null) {
                        Trace.d("验证是否已经注册 查询到" + a.get("user_tel") + " 已注册");
                        objectId = a.getObjectId();
                        registerStatus = statusTrue;
                    } else
                        registerStatus = statusFalse;
                } catch (AVException e) {
                    Trace.e("验证是否注册失败" + Trace.getErrorMsg(e));
                    e.printStackTrace();
                    Trace.show(LoginActivity.this, "验证是否注册失败" + Trace.getErrorMsg(e));
                }
            }
        }).start();
    }

    //表格信息校验
    @Override
    protected boolean tableCheck(String txtUser, String txtPass, String txtRepass, String txtProv) {
        if (Config.isDebugMode)
            return true;
        if (txtUser.equals("") || txtUser.length() != 11) {
            Trace.show(LoginActivity.this, "请输入11位手机号并接收验证码");
            return false;
        } else if (txtRepass.equals("") | txtPass.equals("")) {
            Trace.show(LoginActivity.this, "请输入密码并重复一次");
            return false;
        } else if (!txtRepass.equals(txtPass)) {
            Trace.show(LoginActivity.this, "两次输入的密码不同");
            return false;
        } else if (txtPass.length() < 6 || txtPass.length() > 13) {
            Trace.show(LoginActivity.this, "密码长度控制在6-13位");
            return false;
        } else if (txtProv.length() != 6) {
            Trace.show(LoginActivity.this, "请输入6位验证码");
            return false;
        } else {
            return true;
        }
    }

    //缓存登录状态跳转主页面
    protected void goToMain() {
        //存入shared
        SecurePreferences.Editor editor = (SecurePreferences.Editor) MyApplication.getDefaultShared().edit();
        editor.putString(Config.KEY_User, mLoginUserEdt.getText().toString());
        editor.putString(Config.KEY_PASS, mLoginPassEdt.getText().toString());
        editor.apply();
        MyApplication.setUser(mLoginUserEdt.getText().toString());
        NormalUtils.goToActivity(LoginActivity.this, MainActivity.class);
        finish();
    }

    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {//间隔超过2s
                Trace.show(LoginActivity.this, "再点击一次退出应用");
                mExitTime = System.currentTimeMillis();
            } else {
                Trace.i(LOG_TAG, "exit Main");
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
