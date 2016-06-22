package com.kerchin.yellownote.base;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.kerchin.yellownote.helper.sql.OrmLiteHelper;

/**
 * Created by Kerchin on 2015/9/26 0026.
 */
public abstract class LoginAbstract extends OrmLiteBaseActivity<OrmLiteHelper> {
    /**
     * 点击登录：表格检查-loginVerify
     */
    protected abstract void login();

    /**
     * 点击注册：表格检查-短信验证-signUpVerify(是否注册查询在sendProv时检查)
     */
    protected abstract void signUp();

    /**
     * 点击找回密码：表格检查-是否注册查询-短信验证-forgetVerify
     */
    protected abstract void forgetSecret();

    /**
     * 发送验证码(请求)
     * CountDownTimer显示倒计时
     *
     * @param isSignUp 判断是注册还是找回密码
     * @param txtUser  用户名
     * @param count    可用时限
     */
    protected abstract void sendProv(boolean isSignUp, String txtUser, int count);

    /**
     * 短信验证(验证)
     * 以手机号和验证码检查验证码的正确性
     * 由于多为子线程查询 查询的状态 smsStatus 为全局
     *
     * @param txtProv 验证码
     * @param txtUser 用户名
     */
    protected abstract void smsVerify(String txtProv, String txtUser);

    /**
     * 登录操作确认(查询)
     * 是否有缓存(isCache)?(密码查询?跳转主页面:重新登录):(是否注册?(密码查询?跳转主页面:失败):提示)
     *
     * @param txtUser 用户名
     * @param txtPass 密码
     */
    protected abstract void loginVerify(String txtUser, String txtPass);

    /**
     * 注册操作确认(插入)
     * 提交注册-跳转主界面
     *
     * @param txtUser 用户
     * @param txtPass 密码
     */
    protected abstract void signUpVerify(String txtUser, String txtPass);

    /**
     * 忘记密码操作确认(更新)
     * 取出对象更新-跳转主界面
     *
     * @param txtUser 用户
     * @param txtPass 密码
     */
    protected abstract void forgetVerify(String txtUser, String txtPass);

    /**
     * 是否注册验证(查询)
     * 由于多为子线程查询 查询的状态 registerStatus 为全局
     *
     * @param txtUser 用户名
     */
    protected abstract void isRegistered(String txtUser);

    /**
     * 表格信息校验
     *
     * @param txtUser   判断用户名是否符合规范
     * @param txtPass   判断用户密码是否符合规范
     * @param txtRePass 判断重复密码是否正确
     * @param txtProv   判断验证码格式是否正确
     * @return boolean
     */
    protected abstract boolean tableCheck(String txtUser, String txtPass, String txtRePass, String txtProv);

    /**
     * 缓存登录状态跳转主页面
     */
    protected abstract void goToMain();

}
