package com.kerchin.yellownote.activity;

import android.os.*;
import android.os.Process;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.kerchin.yellownote.R;
import com.kerchin.yellownote.base.BaseActivity;
import com.kerchin.yellownote.bean.PrimaryData;
import com.kerchin.yellownote.global.Config;
import com.kerchin.yellownote.global.MyApplication;
import com.kerchin.yellownote.proxy.LoginService;
import com.kerchin.yellownote.utilities.NormalUtils;
import com.kerchin.yellownote.utilities.SystemBarTintManager;
import com.kerchin.yellownote.utilities.Trace;

/**
 * Created by Kerchin on 2016/4/3 0003.
 */
public class LaunchActivity extends BaseActivity {
    final static int delayTime = 1500;
    final static int delayTimeToMain = 1200;
//    View view;

    @Override
    protected void setContentView(Bundle savedInstanceState) {
//        view = LayoutInflater.from(this).inflate(R.layout.fragment_welcome,
//                null);
        setContentView(R.layout.fragment_welcome);
        immerge(R.color.minionYellow);
    }

    @Override
    protected void initializeEvent(Bundle savedInstanceState) {
    }

    @Override
    protected void initializeView(Bundle savedInstanceState) {
//        ButterKnife.bind(this);
//        mLoginRetryLinear.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (isNeedToRefresh) {
//                    isNeedToRefresh = false;
//                    loginVerify(MyApplication.user);
//                }
//            }
//        });
        //guidePage
        if (MyApplication.getDefaultShared().getBoolean("isGuide", false)) {
            loginVerify(MyApplication.user);

//            startActivity(new Intent(this, GuideActivity.class));
//            finish();
        } else {
            loginVerify(MyApplication.user);
        }
    }

    @Override
    protected void initializeData(Bundle savedInstanceState) {
        //只为有缓存登录的用户初始化数据
        if (MyApplication.isLogin()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                    PrimaryData.getInstance();
                    getDataStart = System.currentTimeMillis();
                }
            }).start();
        }
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
            //透明导航栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            //激活导航栏设置
            tintManager.setNavigationBarTintEnabled(true);
            //设置导航栏颜色
            tintManager.setNavigationBarTintResource(color);
        }
    }//登录操作确认

    @Override
    public void onDestroy(){
        super.onDestroy();
        System.gc();
    }

    //    boolean isNeedToRefresh = false;
    private static final byte wel = 0;
    private static final byte next = 1;
    private static final byte reLog = 2;
    private static final byte reLogForFrozen = 4;
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case wel://首次登陆
                    NormalUtils.goToActivity(LaunchActivity.this, LoginActivity.class);
                    finish();
                    overridePendingTransition(R.anim.push_left_in,
                            R.anim.push_left_out);
                    break;
                case reLog://由于密码错误重新登陆
                    Trace.show(LaunchActivity.this, "你的密码已被修改,请重新登录", Toast.LENGTH_LONG);
                    NormalUtils.goToActivity(LaunchActivity.this, LoginActivity.class);
                    finish();
                    overridePendingTransition(R.anim.push_left_in,
                            R.anim.push_left_out);
                    break;
                case reLogForFrozen://账户冻结
                    Trace.show(LaunchActivity.this, "您的账号已被冻结,请联系 hkq325800@163.com", Toast.LENGTH_LONG);
                    NormalUtils.goToActivity(LaunchActivity.this, LoginActivity.class);
                    finish();
                    overridePendingTransition(R.anim.push_left_in,
                            R.anim.push_left_out);
                    break;
                case next://缓存正确 直接进入
                    NormalUtils.goToActivity(LaunchActivity.this, MainActivity.class);
                    finish();
                    overridePendingTransition(R.anim.push_left_in,
                            R.anim.push_left_out);
                    break;
                default:
                    break;
            }
        }
    };

    int repeatCount = 0;
    Message cycleTarget;
    long getDataStart;
    private Runnable runnableForData = new Runnable() {
        @Override
        public void run() {
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            if (PrimaryData.status.isFolderReady
                    && PrimaryData.status.isItemReady
                    && PrimaryData.status.isNoteReady
                    && cycleTarget != null) {
                Trace.d("runnableForData done");
                if (System.currentTimeMillis() - getDataStart <= delayTimeToMain) {
                    handler.sendMessageDelayed(cycleTarget, delayTimeToMain - System.currentTimeMillis() + getDataStart);
                } else
                    handler.sendMessage(cycleTarget);
            } else {
                Trace.d("folder:" + PrimaryData.status.isFolderReady
                        + "note:" + PrimaryData.status.isNoteReady
                        + "items:" + PrimaryData.status.isItemReady);
                handler.postDelayed(runnableForData, 200);
                repeatCount++;
            }
        }
    };

    protected void loginVerify(final String txtUser) {
        //缓存查询流程
        if (!txtUser.equals("") && !MyApplication.getDefaultShared().getString(Config.KEY_PASS, "").equals("")) {
            //密码查询
            new Thread(new Runnable() {
                @Override
                public void run() {
                    android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                    try {
                        AVObject avObjects = LoginService.loginVerify(txtUser, MyApplication.getDefaultShared().getString(Config.KEY_PASS, ""));
                        if (avObjects != null) {
                            Trace.d("查询缓存 用户" + avObjects.get("user_tel") + "登陆成功");
                            boolean isFrozen = avObjects.getBoolean("isFrozen");
                            if (isFrozen) {
                                //由于账户冻结重新登陆
                                Message message = Message.obtain();
                                message.what = reLogForFrozen;
                                handler.sendMessageDelayed(message, delayTime);
                            } else {
                                //保存默认笔记夹id
                                MyApplication.userDefaultFolderId = avObjects.getString("user_default_folderId");
                                //缓存正确跳转
                                cycleTarget = Message.obtain();//直接进入
                                cycleTarget.what = next;
                                handler.post(runnableForData);
                            }
                        } else {
                            //缓存错误重新登录
                            Message message = Message.obtain();//由于密码错误重新登陆
                            message.what = reLog;
                            handler.sendMessageDelayed(message, delayTime);
                        }
                    } catch (AVException e) {
                        //无网络时如果已经有缓存登录，还是允许进入查看离线消息
                        e.printStackTrace();
//                        isNeedToRefresh = true;
                        cycleTarget = Message.obtain();//直接进入
                        cycleTarget.what = next;
                        handler.post(runnableForData);
//                        Trace.show(LaunchActivity.this, "请检查网络后单击图标重试" + Trace.getErrorMsg(e), Toast.LENGTH_LONG);
                    }
                }
            }).start();
        } else {//无缓存显示界面
            Message message = Message.obtain();//首次登陆
            message.what = wel;
            handler.sendMessageDelayed(message, delayTime);
        }
    }
}
