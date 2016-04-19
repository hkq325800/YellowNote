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

    /**
     * 判断用户的密码是否正确
     *
     * @param txtUser 用户名
     * @param txtPass 用户密码
     * @return AVObject
     * @throws AVException
     */
    public static AVObject loginVerify(String txtUser, String txtPass) throws AVException {
        AVQuery<AVObject> query = new AVQuery<>("mUser");
        query.whereEqualTo("user_tel", txtUser);
        query.whereEqualTo("user_pass", MyApplication.Secret(txtPass));
        return query.getFirst();
    }

    /**
     * 忘记密码的保存
     *
     * @param txtUser 用户名
     * @param txtPass 用户密码
     * @throws AVException
     */
    public static void forgetVerify(String txtUser, String txtPass) throws AVException {
        AVQuery<AVObject> query = new AVQuery<>("mUser");
        query.whereEqualTo("user_tel", txtUser);
        AVObject user = query.getFirst();//query.get(objectId);
        user.put("user_pass", MyApplication.Secret(txtPass));
        user.save();
    }

    /**
     * 判断是否注册
     *
     * @param txtUser 用户名
     * @return AVObject 为了获取objectId
     * @throws AVException
     */
    public static boolean isRegistered(String txtUser) throws AVException {
        AVQuery<AVObject> query = new AVQuery<>("mUser");
        query.whereEqualTo("user_tel", txtUser);
        return query.getFirst() != null;
    }

    /**
     * 注册创建默认笔记夹
     *
     * @param txtUser 用户名
     * @return String 默认笔记夹唯一Id
     * @throws AVException
     */
    public static String createDefaultFolder(final String txtUser) throws AVException {
        final AVObject folder = new AVObject("Folder");
        folder.put("user_tel", txtUser);
        folder.setFetchWhenSave(true);
        folder.save();
        return folder.getObjectId();
    }

    /**
     * 用户注册
     *
     * @param txtUser         用户名
     * @param txtPass         用户密码
     * @param defaultFolderId 用户的默认笔记夹Id
     * @throws AVException
     */
    public static void userSignUp(String txtUser, String txtPass, String defaultFolderId) throws AVException {
        AVObject user = new AVObject("mUser");
        user.put("user_tel", txtUser);
        user.put("user_default_folderId", defaultFolderId);
        user.put("user_pass", MyApplication.Secret(txtPass));
        user.save();
    }

    /**
     * 发送验证码
     *
     * @param txtUser     用户名
     * @param isSignUp    是否注册，决定了模板
     * @param validPeriod 发送验证码的过期时间
     * @throws AVException
     */
    public static void sendProv(String txtUser, boolean isSignUp, int validPeriod) throws AVException {
        AVOSCloud.requestSMSCode(txtUser, "小黄云笔记", isSignUp ? "注册" : "找回密码", validPeriod);
    }

    /**
     * 验证验证码的正确性
     *
     * @param txtProv 验证码
     * @param txtUser 用户名
     * @throws AVException
     */
    public static void smsVerify(String txtProv, String txtUser) throws AVException {
        AVOSCloud.verifySMSCode(txtProv, txtUser);
    }

    //获取文件
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
