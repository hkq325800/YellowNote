package com.kerchin.yellownote.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.kerchin.yellownote.R;
import com.kerchin.yellownote.base.MyOrmLiteBaseActivity;
import com.kerchin.yellownote.data.bean.PrimaryData;
import com.kerchin.yellownote.global.Config;
import com.kerchin.yellownote.global.SampleApplicationLike;
import com.kerchin.yellownote.utilities.helper.sql.OrmLiteHelper;
import com.kerchin.yellownote.data.proxy.LoginService;
import com.kerchin.yellownote.utilities.NormalUtils;
import com.kerchin.yellownote.utilities.PatternLockUtils;
import zj.baselibrary.util.ThreadPool.ThreadPool;
import com.kerchin.yellownote.utilities.Trace;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Kerchin on 2016/4/3 0003.
 */
public class LaunchActivity extends MyOrmLiteBaseActivity<OrmLiteHelper> {
    private final static int delayTime = 1500;
    private final static int delayTimeToMain = 1200;
    private static final byte wel = 0;
    private static final byte next = 1;
    private static final byte reLog = 2;
    private static final byte reLogForFrozen = 4;
    @BindView(R.id.mWelcomeRetryReL)
    RelativeLayout mWelcomeRetryReL;
    @BindView(R.id.mWelcomeTxt)
    TextView mWelcomeTxt;
    private boolean isNeedToRefresh = false;
    private int repeatCount = 0;
    private long getDataStart;//用于配置launch页时间
    private Message cycleTarget;
//    View view;

    @Override
    protected void doSthBeforeSetView() {
        super.doSthBeforeSetView();
        closeSliding();
        immergeColor = R.color.minionYellow;
    }

    @Override
    protected void initializeView(Bundle savedInstanceState) {
        ButterKnife.bind(this);
    }

    @Override
    protected void initializeEvent(Bundle savedInstanceState) {
        mWelcomeRetryReL.setOnClickListener(new View.OnClickListener() {
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
    protected boolean initializeCallback(Message msg) {
        return false;
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_launch;
    }

    @Override
    protected void initializeData(Bundle savedInstanceState) {
        //test crash
//        String str = null;
//        str.toCharArray();
        //只为有缓存登录的用户初始化数据
        if (SampleApplicationLike.isLogin()) {
            SampleApplicationLike.userDefaultFolderId = SampleApplicationLike.getDefaultShared().getString(Config.KEY_DEFAULT_FOLDER, "");
            ThreadPool.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    Trace.d("PrimaryData" + Thread.currentThread().getId());
                    getDataStart = System.currentTimeMillis();
                    //一般而言登陆过的用户都有数据本地缓存
                    PrimaryData.getInstance(getHelper(), new PrimaryData.DoAfterWithEx() {
                        @Override
                        public void justNowWithEx(Exception e) {
                            if (!SampleApplicationLike.getDefaultShared().getBoolean(Config.KEY_CAN_OFFLINE, true)) {
                                isNeedToRefresh = true;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mWelcomeTxt.setText("请检查网络后点击屏幕重试");
                                    }
                                });
                                Trace.show(LaunchActivity.this, "请检查网络后点击屏幕重试" + Trace.getErrorMsg(e), Toast.LENGTH_LONG);
                            }
                        }
                    });//isNeedToRefresh
                }
            });
        }
        loginVerify(SampleApplicationLike.user);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.gc();//用于icon的回收
    }

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case wel://首次登陆
                    NormalUtils.goToActivity(LaunchActivity.this, LoginActivity.class);
                    finish();
                    break;
                case reLog://由于密码错误重新登陆
                    Trace.show(LaunchActivity.this, "你的密码已被修改,请重新登录", Toast.LENGTH_LONG);
                    NormalUtils.goToActivity(LaunchActivity.this, LoginActivity.class);
                    finish();
                    break;
                case reLogForFrozen://账户冻结
                    Trace.show(LaunchActivity.this, "您的账号已被冻结,请联系 hkq325800@163.com", Toast.LENGTH_LONG);
                    NormalUtils.goToActivity(LaunchActivity.this, LoginActivity.class);
                    finish();
                    break;
                case next://缓存正确 直接进入
                    if(PrimaryData.getInstance().isOffline)
                        Trace.show(LaunchActivity.this, "当前数据处于离线状态");
                    if (PatternLockUtils.hasPattern(getApplicationContext())) {
                        Intent intent = new Intent(LaunchActivity.this, ConfirmPatternActivity.class);
                        intent.putExtra("isFromLaunch", true);
                        startActivity(intent);
                    } else
                        MainActivity.startMe(getApplicationContext());
//                    NormalUtils.goToActivity(LaunchActivity.this, MainActivity.class);
                    finish();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void finish(){
        super.finish();
        overridePendingTransition(R.anim.push_left_in,
                R.anim.push_left_out);
    }

    private Runnable runnableForData = new Runnable() {
        @Override
        public void run() {
            Trace.d("runnableForData" + Thread.currentThread().getId());
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            if (PrimaryData.status == null) {
                handler.postDelayed(runnableForData, Config.period_runnable);
                repeatCount++;
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String str = PrimaryData.status.toString();
                    if (!str.equals(""))
                        mWelcomeTxt.setText(str);
                }
            });
            if (PrimaryData.status.isFolderReady
                    && PrimaryData.status.isNoteReady
                    && PrimaryData.status.isItemReady
                    && PrimaryData.status.isHeaderReady
                    && cycleTarget != null) {
                Trace.d("runnableForData done");
                //保证标志图的最低显示时间为delayTimeToMain
                if (System.currentTimeMillis() - getDataStart <= delayTimeToMain) {
                    handler.sendMessageDelayed(cycleTarget, delayTimeToMain - System.currentTimeMillis() + getDataStart);
                } else
                    handler.sendMessage(cycleTarget);
            } else {
                if (repeatCount * Config.period_runnable <= Config.timeout_runnable) {
                    Trace.d("folder:" + PrimaryData.status.isFolderReady
                            + "note:" + PrimaryData.status.isNoteReady
                            + "items:" + PrimaryData.status.isItemReady);
                    handler.postDelayed(runnableForData, Config.period_runnable);
                    repeatCount++;
                } else {
                    repeatCount = 0;
                    Trace.show(LaunchActivity.this, "网络状况不佳 稍后再试吧");
                }
            }
        }
    };

    //登录操作确认
    protected void loginVerify(final String txtUser) {
        //缓存查询流程
        if (!txtUser.equals("") && !SampleApplicationLike.getDefaultShared().getString(Config.KEY_PASS, "").equals("")) {
            //密码查询
            mWelcomeTxt.setText("查询到缓存 正在进行验证...");
            ThreadPool.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    Trace.d("loginVerify" + Thread.currentThread().getId());
                    android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                    try {
                        AVObject user = LoginService.loginVerify(txtUser, SampleApplicationLike.getDefaultShared().getString(Config.KEY_PASS, ""));
//                        if (PatternLockUtils.isDateTooLong(getApplicationContext())) {//超过一天重新获取一次阅读密码的设置
//                        }
                        if (user != null) {
                            Trace.d("查询缓存 用户" + user.get("user_tel") + "登陆成功");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mWelcomeTxt.setText("登录成功 正在获取数据...");
                                }
                            });
                            boolean isFrozen = user.getBoolean("isFrozen");
                            PatternLockUtils.setPattern(user.getString("user_read_pass"), getApplicationContext());
//                            if (Config.isDebugMode)
//                                Trace.show(LaunchActivity.this, SecretService.getPatternStr(txtUser));
                            if (isFrozen) {
                                //由于账户冻结重新登陆
                                Message message = Message.obtain();
                                message.what = reLogForFrozen;
                                handler.sendMessageDelayed(message, delayTime);
                            } else {
                                //默认笔记本id统一在login获取 因为不轻易改变 保存在本地
                                //userIcon每次获取
                                SampleApplicationLike.setUserIcon(user.getString("user_icon"));
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
                        if (SampleApplicationLike.isLogin()) {
                            cycleTarget = Message.obtain();//直接进入
                            cycleTarget.what = next;
                            handler.post(runnableForData);//无网络时
                        }
                    }
                }
            });
        } else {//无缓存显示界面
            Message message = Message.obtain();//首次登陆
            message.what = wel;
            handler.sendMessageDelayed(message, delayTime);
        }
    }
}
