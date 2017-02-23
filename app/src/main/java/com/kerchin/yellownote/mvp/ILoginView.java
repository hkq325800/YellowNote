package com.kerchin.yellownote.mvp;

import android.os.CountDownTimer;

import com.avos.avoscloud.AVException;

/**
 * Created by hkq325800 on 2017/2/23.
 */

public interface ILoginView {
    void loginSuccess(String txtUser, String user_default_folderId, String user_icon, String user_read_pass);

    void loginFailed();

    void exception(String s, AVException e);

    void isFrozen();

    void saySth(String s);

    CountDownTimer startSendProv();

    void sendProvFailed(CountDownTimer cdt);

    void signUpSuccess(String txtUser);

    void changeEnter();
}
