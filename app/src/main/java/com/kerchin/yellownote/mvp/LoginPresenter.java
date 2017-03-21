package com.kerchin.yellownote.mvp;

import android.content.Context;
import android.os.CountDownTimer;

import com.avos.avoscloud.AVException;

/**
 * Created by hkq325800 on 2017/2/23.
 */

public class LoginPresenter extends BasePresenter<LoginModel, ILoginView> {
    public LoginPresenter(LoginActivity view) {
        setVM(new LoginModel(view), view);
    }

    public void login(final String txtUser, final String txtPass) {
        mModel.isReg(txtUser, new LoginModel.Callback() {
            @Override
            public void isTrue() {
                mModel.login(txtUser, txtPass, new LoginModel.LoginCallback() {
                    @Override
                    public void isFrozen() {
                        mView.saySth("您的账号已被冻结，请联系hkq325800@163.com");
                    }

                    @Override
                    public void isException(AVException e) {
                        mView.exception("login", e);
                    }

                    @Override
                    public void loginSuccess(String txtUser, String user_default_folderId, String user_icon, String user_read_pass) {
                        mView.loginSuccess(txtUser, user_default_folderId, user_icon, user_read_pass);
                    }

                    @Override
                    public void loginFailed() {
                        mView.saySth("密码错误,请重试");
                    }
                });
            }

            @Override
            public void isFalse() {
                mView.saySth("该帐号尚未注册");
            }

            @Override
            public void isException(AVException e) {
                mView.exception("isReg", e);
            }
        });
    }

    public void signUp(final String txtUser, final String txtPass, final String txtProv) {
        mModel.smsVerify(txtProv, txtUser, new LoginModel.NonCallback() {

            @Override
            public void isTrue() {
                mModel.createDefaultFolder(txtUser, new LoginModel.CreateCallback() {
                    @Override
                    public void isTrue(String folderName) {
                        mModel.signUp(folderName, txtUser, txtPass, new LoginModel.NonCallback() {

                            @Override
                            public void isTrue() {
                                mView.signUpSuccess(txtUser);
                                mView.changeEnter();
                            }

                            @Override
                            public void isException(AVException e) {
                                mView.exception("signUp", e);
                                mView.changeEnter();
                            }
                        });
                    }

                    @Override
                    public void isFalse() {
                        mView.saySth("注册失败");
                        mView.changeEnter();
                    }

                    @Override
                    public void isException(AVException e) {
                        mView.exception("signUp", e);
                        mView.changeEnter();
                    }
                });

            }

            @Override
            public void isException(AVException e) {
                mView.exception("sms", e);
            }
        });
    }

    public void forget(final String txtUser, final String txtPass, final String txtProv) {
        mModel.smsVerify(txtProv, txtUser, new LoginModel.NonCallback() {

            @Override
            public void isTrue() {
                mModel.forget(txtUser, txtPass, new LoginModel.NonCallback() {

                    @Override
                    public void isTrue() {
                        mView.saySth("修改成功，请重新登陆");
                        mView.changeEnter();
                    }

                    @Override
                    public void isException(AVException e) {
                        mView.exception("forget", e);
                        mView.changeEnter();
                    }
                });
            }

            @Override
            public void isException(AVException e) {
                mView.exception("sms", e);
            }
        });
    }

    public boolean tableCheck(Context context, String txtUser, String txtPass, String txtRePass, String txtProv) {
        return mModel.tableCheck(context, txtUser, txtPass, txtRePass, txtProv);
    }

    public void forgetSendProv(final String txtUser, final boolean isSignUp, final int validPeriod) {
        mModel.isReg(txtUser, new LoginModel.Callback() {

            @Override
            public void isTrue() {
                final CountDownTimer cdt = mView.startSendProv();
                mModel.sendProv(txtUser, isSignUp, validPeriod, new LoginModel.Callback() {
                    @Override
                    public void isTrue() {
                        //doNothing
                    }

                    @Override
                    public void isFalse() {
                        mView.sendProvFailed(cdt);
                    }

                    @Override
                    public void isException(AVException e) {
                        mView.sendProvFailed(cdt);
                        mView.exception("sendProv", e);
                    }
                });
            }

            @Override
            public void isFalse() {
                mView.saySth("该帐号未注册，请先注册");
            }

            @Override
            public void isException(AVException e) {
                mView.exception("isReg", e);
            }
        });
    }

    public void signUpSendProv(final String txtUser, final boolean isSignUp, final int validPeriod) {
        mModel.isAbleToSignUp(new LoginModel.Callback() {

            @Override
            public void isTrue() {
                sendProv(txtUser, isSignUp, validPeriod);
            }

            @Override
            public void isFalse() {
                mView.saySth("非常抱歉，目前不开放注册");
            }

            @Override
            public void isException(AVException e) {
                mView.exception("ableToSignUp", e);
            }
        });
    }

    private void sendProv(final String txtUser, final boolean isSignUp, final int validPeriod) {
        mModel.isReg(txtUser, new LoginModel.Callback() {

            @Override
            public void isTrue() {
                mView.saySth("该帐号已注册，请登录");
            }

            @Override
            public void isFalse() {
                final CountDownTimer cdt = mView.startSendProv();
                mModel.sendProv(txtUser, isSignUp, validPeriod, new LoginModel.Callback() {
                    @Override
                    public void isTrue() {
                        //doNothing
                    }

                    @Override
                    public void isFalse() {
                        mView.sendProvFailed(cdt);
                    }

                    @Override
                    public void isException(AVException e) {
                        mView.sendProvFailed(cdt);
                        mView.exception("sendProv", e);
                    }
                });
            }

            @Override
            public void isException(AVException e) {
                mView.exception("isReg", e);
            }
        });
    }
}