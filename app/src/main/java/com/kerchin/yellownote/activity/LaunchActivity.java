package com.kerchin.yellownote.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.view.View;
import android.widget.LinearLayout;
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
import com.kerchin.yellownote.utilities.Trace;

/**
 * Created by Kerchin on 2016/4/3 0003.
 */
public class LaunchActivity extends BaseActivity {
    private final static int delayTime = 1500;
    private final static int delayTimeToMain = 1200;
    private final static long runnableTimeout = 8000;
    private final static long runnablePeriod = 200;
    private static final byte wel = 0;
    private static final byte next = 1;
    private static final byte reLog = 2;
    private static final byte reLogForFrozen = 4;
    private LinearLayout mLoginRetryLinear;
    private boolean isNeedToRefresh = false;
    private int repeatCount = 0;
    private long getDataStart;
    private Message cycleTarget;
//    View view;

    @Override
    protected void setContentView(Bundle savedInstanceState) {
//        view = LayoutInflater.from(this).inflate(R.layout.fragment_welcome,
//                null);
        setContentView(R.layout.fragment_welcome);
        NormalUtils.immerge(LaunchActivity.this, R.color.minionYellow);
    }

    @Override
    protected void initializeView(Bundle savedInstanceState) {
        mLoginRetryLinear = (LinearLayout) findViewById(R.id.mLoginRetryLinear);
//        ButterKnife.bind(this);
    }

    @Override
    protected void initializeEvent(Bundle savedInstanceState) {
        mLoginRetryLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNeedToRefresh) {
                    isNeedToRefresh = false;
                    PrimaryData.getInstance().clearData();
                    initializeData(null);
                }
            }
        });
    }

    @Override
    protected void initializeData(Bundle savedInstanceState) {
        //只为有缓存登录的用户初始化数据
        if (MyApplication.isLogin()) {
            MyApplication.userDefaultFolderId = MyApplication.getDefaultShared().getString(Config.KEY_DEFAULT_FOLDER, "");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    getDataStart = System.currentTimeMillis();
                    try {
                        //一般而言登陆过的用户都有数据本地缓存
                        Looper.prepare();
                        PrimaryData.getInstance().initData();
                        Looper.loop();
                    } catch (AVException e) {
                        e.printStackTrace();
                        isNeedToRefresh = true;
                        //TODO 在完善了本地存储后可以取消 因为那时必定有数据
                        Trace.show(LaunchActivity.this, "请检查网络后单击图标重试" + Trace.getErrorMsg(e), Toast.LENGTH_LONG);
                    }
                }
            }).start();
        }
        loginVerify(MyApplication.user);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.gc();
    }

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

    private Runnable runnableForData = new Runnable() {
        @Override
        public void run() {
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            if (PrimaryData.status == null) {
                handler.postDelayed(runnableForData, runnablePeriod);
                repeatCount++;
                return;
            }
            if (PrimaryData.status.isFolderReady
                    && PrimaryData.status.isNoteReady
                    && cycleTarget != null) {
                Trace.d("runnableForData done");
                //保证标志图的最低显示时间为delayTimeToMain
                if (System.currentTimeMillis() - getDataStart <= delayTimeToMain) {
                    handler.sendMessageDelayed(cycleTarget, delayTimeToMain - System.currentTimeMillis() + getDataStart);
                } else
                    handler.sendMessage(cycleTarget);
            } else {
                if (repeatCount * runnablePeriod <= runnableTimeout) {
                    Trace.d("folder:" + PrimaryData.status.isFolderReady
                            + "note:" + PrimaryData.status.isNoteReady
                            + "items:" + PrimaryData.status.isItemReady);
                    handler.postDelayed(runnableForData, runnablePeriod);
                    repeatCount++;
                } else
                    repeatCount = 0;
            }
        }
    };

    //登录操作确认
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
                                cycleTarget = Message.obtain();//直接进入
                                cycleTarget.what = next;
                                handler.post(runnableForData);//缓存正确跳转
                            }
                        } else {
                            //缓存错误重新登录
                            Message message = Message.obtain();//由于密码错误重新登陆
                            message.what = reLog;
                            handler.sendMessageDelayed(message, delayTime);
                        }
                    } catch (AVException e) {
                        e.printStackTrace();
                        //无网络时如果已经有缓存登录，还是允许进入查看离线消息
                        //TODO 在完善本地化后取消注释
//                        if (MyApplication.isLogin()) {
//                            cycleTarget = Message.obtain();//直接进入
//                            cycleTarget.what = next;
//                            handler.post(runnableForData);//无网络时
//                        }
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
