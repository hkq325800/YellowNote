package com.kerchin.yellownote.activity;

import android.app.Activity;
import android.app.LauncherActivity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.kerchin.yellownote.R;
import com.kerchin.yellownote.base.BaseActivity;
import com.kerchin.yellownote.global.Config;
import com.kerchin.yellownote.global.MyApplication;
import com.kerchin.yellownote.utilities.NormalUtils;
import com.kerchin.yellownote.utilities.SystemBarTintManager;
import com.kerchin.yellownote.utilities.Trace;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2016/4/3 0003.
 */
public class LaunchActivity extends BaseActivity {
    View view;
    @Bind(R.id.mLoginRetryLinear)
    LinearLayout mLoginRetryLinear;

    @Override
    protected void setContentView(Bundle savedInstanceState) {
        view = LayoutInflater.from(this).inflate(R.layout.fragment_welcome,
                null);
        setContentView(view);
        immerge(R.color.minionYellow);
    }

    @Override
    protected void initializeClick(Bundle savedInstanceState) {

    }

    @Override
    protected void initializeData(Bundle savedInstanceState) {

    }

    @Override
    protected void initializeView(Bundle savedInstanceState) {
        ButterKnife.bind(this);
        final String pass = MyApplication.getDefaultShared().getString(Config.KEY_PASS, "");
        mLoginRetryLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNeedToRefresh) {
                    isNeedToRefresh = false;
                    loginVerify(MyApplication.user, pass);
                }
            }
        });
        loginVerify(MyApplication.user, pass);
        //guidePage
//        if (PrefsAccessor.getInstance().getBoolean(SysConfig.GUIDE_FLAG, false)) {
        // 2秒的动画
//            AlphaAnimation animation = new AlphaAnimation(0.3f, 1.0f);
//            animation.setDuration(1500);
//            animation.setAnimationListener(new Animation.AnimationListener() {
//
//                @Override
//                public void onAnimationStart(Animation animation) {
//                    String pass = MyApplication.getDefaultShared().getString(Config.KEY_PASS, "");
//                    loginVerify(MyApplication.user, pass);
//                }
//
//                @Override
//                public void onAnimationRepeat(Animation animation) {
//
//                }
//
//                @Override
//                public void onAnimationEnd(Animation animation) {
//                    // 跳转界面
//
//                    if (IUEApplication.isLogin) {
//                        if (IUEApplication.getDoctorSetting()
//                                .getRegisterState() == RegisterState.unPass
//                                || IUEApplication.getDoctorSetting()
//                                .getRegisterState() == RegisterState.unRegister) {
//                            startActivity(new Intent(LauncherActivity.this,
//                                    ApplyDoctorActivity.class));
//                        } else {
//                            startActivity(new Intent(LauncherActivity.this,
//                                    MainActivity.class));
//                        }
//                    } else {
//
//                        startActivity(new Intent(LauncherActivity.this,
//                                LoginActivity.class));
//
//                    }
//                    finish();
//                }
//            });
//            view.setAnimation(animation);
//        } else {
//            startActivity(new Intent(this, GuideActivity.class));
//            finish();
//        }
    }

    private void immerge(int color) {
        /**沉浸式状态栏设置部分**/
        //Android5.0版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//
            // Translucent status bar
//            Window window = getWindow();
//            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
//                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            setStatusBarColor(color);//阴影绘制
            //设置状态栏颜色
//            getWindow().setStatusBarColor(getResources().getColor(color));
            //设置导航栏颜色
            getWindow().setNavigationBarColor(getResources().getColor(color));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //创建状态栏的管理实例
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            //激活状态栏设置
            tintManager.setStatusBarTintEnabled(true);
            //设置状态栏颜色
            tintManager.setTintResource(color);
            //激活导航栏会变黑
            //透明导航栏
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
//            //激活导航栏设置
//            tintManager.setNavigationBarTintEnabled(true);
//            //设置导航栏颜色
//            tintManager.setNavigationBarTintResource(color);
        }
    }//登录操作确认

    boolean isNeedToRefresh = false;
    private static final byte wel = 0;
    private static final byte next = 1;
    private static final byte reLog = 2;
    private static final byte withoutNet = 3;
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case wel:
                    NormalUtils.goToActivity(LaunchActivity.this, LoginActivity.class);
                    finish();
                    break;
                case reLog:
                    NormalUtils.goToActivity(LaunchActivity.this, LoginActivity.class);
                    finish();
//                    mLoginUserEdt.setText(MyApplication.user);
                    break;
                case next:
                    NormalUtils.goToActivity(LaunchActivity.this, MainActivity.class);
                    finish();
                    break;
                case withoutNet:
                    Trace.show(LaunchActivity.this, "请检查网络后单击图标重试", Toast.LENGTH_LONG);
                    break;
                default:
                    break;
            }
        }
    };


    protected void loginVerify(final String txtUser, final String txtPass) {
        //缓存查询流程
        if (!txtUser.equals("") && !txtPass.equals("")) {
            //密码查询
            AVQuery<AVObject> query = new AVQuery<>("mUser");
            query.whereEqualTo("user_tel", txtUser);
            query.whereEqualTo("user_pass", MyApplication.Secret(txtPass));
            query.findInBackground(new FindCallback<AVObject>() {
                public void done(List<AVObject> avObjects, AVException e) {
                    if (e == null) {
                        Trace.d("查询缓存 查询到" + avObjects.size() + " 条符合条件的数据");
                        if (avObjects.size() > 0) {
                            boolean isFrozen = avObjects.get(0).getBoolean("isFrozen");
                            Trace.d("isFrozen " + isFrozen);
                            if (isFrozen) {
                                Trace.show(LaunchActivity.this, "您的账号已被冻结,请联系hkq325800@163.com");
                                Message message = Message.obtain();//更新UI
                                message.what = reLog;
                                handler.sendMessageDelayed(message, 1000);
                            } else {
                                MyApplication.userDefaultFolderId = avObjects.get(0).getString("user_default_folderId");
                                //缓存正确跳转
                                Message message = Message.obtain();//更新UI
                                message.what = next;
                                handler.sendMessageDelayed(message, 1000);
                            }
                        } else {
                            //缓存错误重新登录
                            Trace.show(LaunchActivity.this, "你的密码已被修改,请重新登录");
                            Message message = Message.obtain();//更新UI
                            message.what = reLog;
                            handler.sendMessageDelayed(message, 1000);
                        }
                    } else {
                        e.printStackTrace();
                        isNeedToRefresh = true;
                        Message message = Message.obtain();//更新UI
                        message.what = withoutNet;
                        handler.sendMessageDelayed(message, 1000);
                    }
                }
            });
        } else {//无缓存显示界面
            //注销时logoutFlag为true不显示welcome
            Message message = Message.obtain();//更新UI
            message.what = wel;
            //TODO delay时间随加载快慢变化
            handler.sendMessageDelayed(message, 1500);
        }
    }
}
