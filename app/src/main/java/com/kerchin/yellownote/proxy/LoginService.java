package com.kerchin.yellownote.proxy;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.kerchin.yellownote.global.MyApplication;

/**
 * Created by Kerchin on 2016/4/5 0005.
 */
public class LoginService {

    public static AVObject loginVerify(String txtUser, String txtPass) throws AVException {
        AVQuery<AVObject> query = new AVQuery<>("mUser");
        query.whereEqualTo("user_tel", txtUser);
        query.whereEqualTo("user_pass", MyApplication.Secret(txtPass));
        return query.getFirst();
    }

    public static void forgetVerify(String objectId, String txtPass) throws AVException {
        AVQuery<AVObject> query = new AVQuery<>("mUser");
        AVObject user = query.get(objectId);
        user.put("user_pass", MyApplication.Secret(txtPass));
        user.save();
    }

    public static AVObject isRegistered(String txtUser) throws AVException {
        AVQuery<AVObject> query = new AVQuery<>("mUser");
        query.whereEqualTo("user_tel", txtUser);
        return query.getFirst();
    }

    public static AVObject signUpVerify(final String txtUser) throws AVException {
        final AVObject folder = new AVObject("Folder");
        folder.put("user_tel", txtUser);
        folder.setFetchWhenSave(true);
        folder.save();
        return folder;
    }

    public static void userSignUp(String txtUser, String txtPass, String objectId) throws AVException {
        AVObject user = new AVObject("mUser");
        user.put("user_tel", txtUser);
        user.put("user_default_folderId", objectId);
        user.put("user_pass", MyApplication.Secret(txtPass));
        user.save();
    }

    public static void sendProv(String txtUser,boolean isSignUp, int count) throws AVException {
        AVOSCloud.requestSMSCode(txtUser, "小黄云笔记", isSignUp ? "注册" : "找回密码", count);
    }

    public static void smsVerify(String txtProv, String txtUser) throws AVException {
        AVOSCloud.verifySMSCode(txtProv, txtUser);
    }

//        AVFile.withObjectIdInBackground("560ba05f60b2ce30b2d4727e", new GetFileCallback<AVFile>() {
//            @Override
//            public void done(AVFile avFile, AVException e) {
//                if (e == null) {
//                    AVObject Folder = new AVObject("Folder");
//                    Folder.put("user_tel", txtUser);
//                    Folder.put("folder_cover", avFile);
//                    Folder.saveInBackground(new SaveCallback() {
//                        @Override
//                        public void done(AVException e) {
//                            if (e == null) {
//                                Log.d("signupVerify", "默认文件夹创建完成");
//                                goToMain();
//                                Toast.makeText(LoginActivity.this, txtUser + "注册成功", Toast.LENGTH_SHORT).show();
//                                isEnter = false;
//                            } else {
//                                e.printStackTrace();
//                            }
//                        }
//                    });
//                } else {
//                    e.printStackTrace();
//                }
//            }
//        });
}
