package com.kerchin.yellownote.mvp;

import com.avos.avoscloud.AVException;

/**
 * Created by hkq325800 on 2017/2/23.
 */

public interface ILoginView extends UiCallback {
    void loginSuccess(String txtUser, String user_default_folderId, String user_icon, String user_read_pass);

    void exception(String s, AVException e);

    void saySth(String s);

//    CountDownTimer startSendProv();

//    void sendProvFailed(CountDownTimer cdt);

    void signUpSuccess(String txtUser);

    void forgetSuccess();

    void changeEnter();
}
