package com.kerchin.yellownote.mvp;

import android.content.Context;
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
    private UiCallback mainCallback;

    public LoginModel(UiCallback mainCallback) {
        this.mainCallback = mainCallback;
    }

    public void isAbleToSignUp(final Callback callback) {
        ThreadPool.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final boolean isAbleToSignIn = LoginService.isAbleToSignIn();
                    doThisOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!isAbleToSignIn) {
                                callback.isFalse();
                            } else {
                                callback.isTrue();
                            }
                        }
                    });
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
                    doThisOnUiThread(new Runnable() {
                        @Override
                        public void run() {
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
                        }
                    });
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
                    doThisOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (isReg) {
                                callback.isTrue();
                            } else
                                callback.isFalse();
                        }
                    });
                } catch (AVException e) {
                    callback.isException(e);
                }
            }
        });
    }

    public boolean tableCheck(Context context, String txtUser, String txtPass, String txtRePass, String txtProv) {
        if (txtUser.equals("") || txtUser.length() != 11) {
            Trace.show(context, "请输入11位手机号");
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
        } else if (txtProv.length() != 4) {
            Trace.show(context, "请输入4位验证码");
            return false;
        } else {
            return true;
        }
    }

//    public void smsVerify(final String txtProv, final String txtUser, final NonCallback callback) {
//        ThreadPool.getInstance().execute(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    LoginService.smsVerify(txtProv, txtUser);
//                    doThisOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            callback.isTrue();
//                        }
//                    });
//                } catch (AVException e) {
////                    callback.isFalse();
//                    callback.isException(e);
//                }
//            }
//        });
//    }

    public void forget(final String txtUser, final String txtPass, final NonCallback callback) {
        ThreadPool.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    LoginService.forgetSave(txtUser, txtPass);
                    doThisOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.isTrue();
                        }
                    });
                } catch (AVException e) {
//                    callback.isFalse();
                    callback.isException(e);
                }
            }
        });
    }

//    /**
//     * 未调用callback.isTrue()
//     *
//     * @param txtUser
//     * @param isSignUp
//     * @param validPeriod
//     * @param callback
//     */
//    public void sendProv(final String txtUser, final boolean isSignUp, final int validPeriod, final Callback callback) {
//        ThreadPool.getInstance().execute(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    LoginService.sendProv(txtUser, isSignUp, validPeriod);
//                } catch (AVException e) {
//                    doThisOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            callback.isFalse();
//                        }
//                    });
//                    callback.isException(e);
//                }
//            }
//        });
//    }

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
                    doThisOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.isTrue();
                        }
                    });
                } catch (AVException e) {
//                    callback.isFalse();
                    callback.isException(e);
                }
            }
        });
    }

    private void doThisOnUiThread(Runnable runnable) {
        if (mainCallback == null) return;
        mainCallback.doThisOnUiThread(runnable);
    }

    public interface LoginCallback {
        void isFrozen();

        void isException(AVException e);

        void loginSuccess(String txtUser, String user_default_folderId, String user_icon, String user_read_pass);

        void loginFailed();
    }

    public interface NonCallback {
        void isTrue();

        void isException(AVException e);
    }

    public interface Callback {
        void isTrue();

        void isFalse();

        void isException(AVException e);
    }

    public interface CreateCallback {
        void isTrue(String folderName);

        void isFalse();

        void isException(AVException e);
    }
}
