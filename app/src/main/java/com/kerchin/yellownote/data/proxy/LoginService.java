package com.kerchin.yellownote.data.proxy;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.kerchin.global.Config;
import com.kerchin.yellownote.global.MyApplication;

import java.io.FileNotFoundException;

import zj.remote.baselibrary.util.PreferenceUtils;

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
    public static void forgetSave(String txtUser, String txtPass) throws AVException {
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
     * 注册创建默认笔记本
     *
     * @param txtUser 用户名
     * @return String 默认笔记本唯一Id
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
     * @param defaultFolderId 用户的默认笔记本Id
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

    /**
     * 获取文件
     *
     * @param userIcon
     * @return
     * @throws AVException
     * @throws FileNotFoundException
     */
    public static AVFile getUserIcon(final String userIcon) throws AVException, FileNotFoundException {
        return AVFile.withObjectId(userIcon);
    }

    /**
     * 保存文件
     *
     * @param path
     * @return
     * @throws AVException
     * @throws FileNotFoundException
     */
    public static String saveUserIcon(String path) throws AVException, FileNotFoundException {
        String mUser = PreferenceUtils.getString(Config.KEY_USER, "", MyApplication.context);
        AVFile file = AVFile.withAbsoluteLocalPath(mUser + ".jpg", path);
        file.save();
        AVQuery<AVObject> query = new AVQuery<>("mUser");
        query.whereEqualTo("user_tel", mUser);
        AVObject user = query.getFirst();
        user.put("user_icon", file.getObjectId());
        user.save();
        return file.getObjectId();
    }

    /**
     * 通过id保存用户icon
     *
     * @param path
     * @return
     * @throws AVException
     * @throws FileNotFoundException
     */
    public static String saveUserIconById(String path) throws AVException, FileNotFoundException {
        String mUser = PreferenceUtils.getString(Config.KEY_USER, "", MyApplication.context);
        AVFile file = AVFile.withObjectId(PreferenceUtils.getString(Config.KEY_USERICON, "", MyApplication.context));
        file.delete();
        AVFile newFile = AVFile.withAbsoluteLocalPath(mUser + ".jpg", path);
        newFile.save();
        AVQuery<AVObject> query = new AVQuery<>("mUser");
        query.whereEqualTo("user_tel", mUser);
        AVObject user = query.getFirst();
        user.put("user_icon", newFile.getObjectId());
        user.save();
        return newFile.getObjectId();
    }

    /**
     * 应用是否允许注册
     *
     * @return
     * @throws AVException
     */
    public static boolean isAbleToSignIn() throws AVException {
        AVQuery<AVObject> query = new AVQuery<>("App");
        return query.getFirst().getBoolean("app_ableToSignIn");
    }
}
