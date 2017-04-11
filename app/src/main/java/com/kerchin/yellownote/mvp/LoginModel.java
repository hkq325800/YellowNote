package com.kerchin.yellownote.mvp;

import android.content.Context;
import android.support.annotation.UiThread;
import android.text.TextUtils;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.kerchin.yellownote.data.proxy.LoginService;

import zj.remote.baselibrary.util.ThreadPool.ThreadPool;
import zj.remote.baselibrary.util.Trace;

/**
 * Model为具体实现，连接网络的service，方法一般运行在工作线程
 * Created by hkq325800 on 2017/2/23.
 */

public class LoginModel {

    public void isAbleToSignUp(final Callback callback) {
        ThreadPool.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final boolean isAbleToSignIn = LoginService.isAbleToSignIn();
                    if (!isAbleToSignIn) {
                        callback.isFalse();
                    } else {
                        callback.isTrue();
                    }
                } catch (AVException e) {
                    callback.isException(e);
                }
            }
        });
    }

    public void login(final String txtUser, final String txtPass, final LoginCallback callback) {
        ThreadPool.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final AVObject user = LoginService.loginVerify(txtUser, txtPass);
                    if (user != null) {
                        if (user.getBoolean("isFrozen")) {
                            callback.isFrozen();//账号冻结无法登陆
                        } else {
                            callback.loginSuccess(txtUser, user.getString("user_default_folderId"), user.getString("user_icon")
                                    , user.getString("user_read_pass"));//正常登陆
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

    public void isReg(final String txtUser, final Callback callback) {
        ThreadPool.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final boolean isReg = LoginService.isRegistered(txtUser);
                    if (isReg) {
                        callback.isTrue();
                    } else {
                        callback.isFalse();
                    }
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

    public void smsVerify(final String txtProv, final String txtUser, final NonCallback callback) {
        ThreadPool.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    LoginService.smsVerify(txtProv, txtUser);
                    callback.isTrue();
                } catch (AVException e) {
//                    callback.isFalse();
                    callback.isException(e);
                }
            }
        });
    }

    public void forget(final String txtUser, final String txtPass, final NonCallback callback) {
        ThreadPool.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    LoginService.forgetSave(txtUser, txtPass);
                    callback.isTrue();
                } catch (AVException e) {
//                    callback.isFalse();
                    callback.isException(e);
                }
            }
        });
    }

    /**
     * 未调用callback.isTrue()
     *
     * @param txtUser
     * @param isSignUp
     * @param validPeriod
     * @param callback
     */
    public void sendProv(final String txtUser, final boolean isSignUp, final int validPeriod, final Callback callback) {
        ThreadPool.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    LoginService.sendProv(txtUser, isSignUp, validPeriod);
                } catch (AVException e) {
                    callback.isFalse();
//                    callback.isException(e);
                }
            }
        });
    }

    public void createDefaultFolder(final String txtUser, final CreateCallback callback) {
        ThreadPool.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String userDefaultFolderId = LoginService.createDefaultFolder(txtUser);
                    if (!TextUtils.isEmpty(userDefaultFolderId)) {
                        callback.isTrue(userDefaultFolderId);
                    } else {
                        callback.isFalse();
                    }
                } catch (AVException e) {
                    callback.isException(e);
                }
            }
        });
    }

    public void signUp(final String userDefaultFolderId, final String txtUser, final String txtPass, final NonCallback callback) {
        ThreadPool.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    LoginService.userSignUp(txtUser, txtPass, userDefaultFolderId);
                    callback.isTrue();
                } catch (AVException e) {
//                    callback.isFalse();
                    callback.isException(e);
                }
            }
        });
    }

//    private void doThisOnUiThread(Runnable runnable) {
//        if (mainCallback == null) return;
//        mainCallback.doThisOnUiThread(runnable);
//    }

    public interface LoginCallback {
        @UiThread
        void isFrozen();

        @UiThread
        void isException(AVException e);

        @UiThread
        void loginSuccess(String txtUser, String user_default_folderId, String user_icon, String user_read_pass);

        @UiThread
        void loginFailed();
    }

    public interface NonCallback {
        @UiThread
        void isTrue();

        @UiThread
        void isException(AVException e);
    }

    public interface Callback {
        @UiThread
        void isTrue();

        @UiThread
        void isFalse();

        @UiThread
        void isException(AVException e);
    }

    public interface CreateCallback {
        @UiThread
        void isTrue(String folderName);

        @UiThread
        void isFalse();

        @UiThread
        void isException(AVException e);
    }
}
