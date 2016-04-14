package com.kerchin.yellownote.proxy;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.kerchin.yellownote.global.MyApplication;

/**
 * Created by Kerchin on 2016/4/9 0009.
 * More Code on hkq325800@163.com
 */
public class SecretService {

    public static void alterSecret(String txtUser, String txtPass) throws AVException {
        AVQuery<AVObject> query = new AVQuery<>("mUser");
        query.whereEqualTo("user_tel", txtUser);
        AVObject user = query.getFirst();
        user.put("user_pass", MyApplication.Secret(txtPass));
        user.save();
    }
}
