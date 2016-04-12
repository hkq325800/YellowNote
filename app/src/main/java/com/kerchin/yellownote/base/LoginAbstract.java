package com.kerchin.yellownote.base;

import android.os.Build;
import android.view.View;
import android.view.WindowManager;

import com.kerchin.yellownote.utilities.SystemBarTintManager;


/**
 * Created by Kerchin on 2015/9/26 0026.
 */
public abstract class LoginAbstract extends BaseActivity {
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

    /**
     * 设置状态栏颜色
     * 也就是所谓沉浸式状态栏
     */
    public void setStatusBarColor(int color) {
        /**
         * Android4.4以上可用
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintResource(color);
            tintManager.setStatusBarTintEnabled(true);
            //Window window = getWindow();
            //window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            //window.setStatusBarColor(color);
        }
    }

//    public void immerge(int color) {
//        /**沉浸式状态栏设置部分**/
//        //Android5.0版本
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
//                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
//            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            setStatusBarColor(color);//阴影绘制
//            //设置状态栏颜色
////            getWindow().setStatusBarColor(getResources().getColor(color));
//            //设置导航栏颜色
//            getWindow().setNavigationBarColor(getResources().getColor(color));
//        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            //透明状态栏
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//            //创建状态栏的管理实例
//            SystemBarTintManager tintManager = new SystemBarTintManager(this);
//            //激活状态栏设置
//            tintManager.setStatusBarTintEnabled(true);
//            //设置状态栏颜色
//            tintManager.setTintResource(color);
//            //透明导航栏
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
//            //激活导航栏设置
//            tintManager.setNavigationBarTintEnabled(true);
//            //设置导航栏颜色
//            tintManager.setNavigationBarTintResource(color);
//        }
//    }
}
