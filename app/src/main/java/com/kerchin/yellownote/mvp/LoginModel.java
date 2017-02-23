package com.kerchin.yellownote.mvp;

import android.content.Context;
import android.text.TextUtils;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.kerchin.yellownote.data.proxy.LoginService;

import zj.remote.baselibrary.util.ThreadPool.ThreadPool;
import zj.remote.baselibrary.util.Trace;

/**
 * Created by hkq325800 on 2017/2/23.
 */

public class LoginModel {

    public void isAbleToSignUp(final BooleanCallback booleanCallback) {
        ThreadPool.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                boolean isAbleToSignIn;
                try {
                    isAbleToSignIn = LoginService.isAbleToSignIn();
                    if (!isAbleToSignIn) {
                        booleanCallback.isFalse();
                    } else {
                        booleanCallback.isTrue();
                    }
                } catch (AVException e) {
                    e.printStackTrace();
                    booleanCallback.isFalse();
                }
            }
        });
    }

    public void login(final String txtUser, final String txtPass, final LoginCallback callback) {
        ThreadPool.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    AVObject user = LoginService.loginVerify(txtUser, txtPass);
                    if (user != null) {
                        if (user.getBoolean("isFrozen")) {
                            callback.isFrozen();//账号冻结无法登陆
                        } else {
                            callback.loginSuccess(txtUser, user.getString("user_default_folderId"), user.getString("user_icon"), user.getString("user_read_pass"));//正常登陆
                        }
                    } else {
                        //密码错误重新登录
                        callback.loginFailed();
                    }
                } catch (AVException e) {
                    callback.isException(e);
                }
            }
        });
    }

    public void isReg(final String txtUser, final RegCallback callback) {
        ThreadPool.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (LoginService.isRegistered(txtUser)) {
                        callback.isTrue();
                    } else
                        callback.isFalse();
                } catch (AVException e) {
                    callback.isException(e);
                }
            }
        });
    }

    public boolean tableCheck(Context context, String txtUser, String txtPass, String txtRePass, String txtProv) {
        if (txtUser.equals("") || txtUser.length() != 11) {
            Trace.show(context, "请输入11位手机号并接收验证码");
            return false;
        } else if (txtRePass.equals("") | txtPass.equals("")) {
            Trace.show(context, "请输入密码并重复一次");
            return false;
        } else if (!txtRePass.equals(txtPass)) {
            Trace.show(context, "两次输入的密码不同");
            return false;
        } else if (txtPass.length() < 6) {
            Trace.show(context, "密码长度需大于6位");
            return false;
        } else if (txtProv.length() != 6) {
            Trace.show(context, "请输入6位验证码");
            return false;
        } else {
            return true;
        }
    }

    public void smsVerify(final String txtProv, final String txtUser, final RegCallback regCallback) {
        ThreadPool.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    LoginService.smsVerify(txtProv, txtUser);
                    regCallback.isTrue();
                } catch (AVException e) {
                    regCallback.isException(e);
                }
            }
        });
    }

    public void forget(final String txtUser, final String txtPass, final RegCallback regCallback) {
        ThreadPool.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    LoginService.forgetSave(txtUser, txtPass);
                    regCallback.isTrue();
                } catch (AVException e) {
                    regCallback.isFalse();
                    regCallback.isException(e);
                }
            }
        });
    }

    public void sendProv(final String txtUser, final boolean isSignUp, final int validPeriod, final RegCallback callback) {
        ThreadPool.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    LoginService.sendProv(txtUser, isSignUp, validPeriod);
                    callback.isTrue();
                } catch (AVException e) {
                    callback.isFalse();
                    callback.isException(e);
                }
            }
        });
    }

    public void signUp(final String txtUser, final String txtPass, final RegCallback regCallback) {
        ThreadPool.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String userDefaultFolderId = createDefaultFolder(txtUser);
                    if (!TextUtils.isEmpty(userDefaultFolderId)) {
                        LoginService.userSignUp(txtUser, txtPass, userDefaultFolderId);
                        regCallback.isTrue();
                    } else {
                        regCallback.isFalse();
                    }
                } catch (AVException e) {
                    regCallback.isException(e);
                }
            }
        });
    }

    public String createDefaultFolder(String txtUser) {
        try {
            return LoginService.createDefaultFolder(txtUser);
        } catch (AVException e) {
            e.printStackTrace();
            return "";
        }
    }

    public interface LoginCallback {
        void isFrozen();

        void isException(AVException e);

        void loginSuccess(String txtUser, String user_default_folderId, String user_icon, String user_read_pass);

        void loginFailed();
    }

    public interface ReCallback {
        void isTrue();

        void isException(AVException e);
    }

    public interface RegCallback {
        void isTrue();

        void isFalse();

        void isException(AVException e);
    }

    public interface BooleanCallback {
        void isTrue();

        void isFalse();
    }

    public interface Callback {
        void success();

        void failed(String error);
    }
}
