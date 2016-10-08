package com.kerchin.yellownote.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.kerchin.yellownote.R;
import com.kerchin.yellownote.base.BaseSwipeBackActivity;
import com.kerchin.yellownote.global.SampleApplicationLike;
import com.kerchin.yellownote.utilities.helper.DayNightHelper;
import com.kerchin.yellownote.data.proxy.ShareSuggestService;
import com.kerchin.yellownote.utilities.NormalUtils;

import zj.baselibrary.util.ThreadPool.ThreadPool;

import com.kerchin.yellownote.utilities.Trace;
import com.securepreferences.SecurePreferences;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import zj.baselibrary.util.SystemUtils;

/**
 * Created by Kerchin on 2016/3/5 0005.
 */
public class ShareSuggestActivity extends BaseSwipeBackActivity {
    private final static int hideSaveBtn = 0;
    @BindView(R.id.mShareSuggestVersionTxt)
    TextView mShareSuggestVersionTxt;
    @BindView(R.id.mShareSuggestTipsTxt)
    TextView mShareSuggestTipsTxt;
    @BindView(R.id.mNavigationRightBtn)
    Button mNavigationRightBtn;
    @BindView(R.id.mNavigationTitleEdt)
    EditText mNavigationTitleEdt;
    @BindView(R.id.mShareSuggestCodeImg)
    ImageView mShareSuggestCodeImg;
    @BindView(R.id.mShareSuggestContentEdt)
    EditText mShareSuggestContentEdt;
    @BindView(R.id.mShareSuggestTouchEdt)
    EditText mShareSuggestTouchEdt;
    @BindView(R.id.mShareSuggestScV)
    ScrollView mShareSuggestScV;
    //防止多次提交数据
    private boolean isPosting = false;
    int quickSuggestTimes = SampleApplicationLike.getDefaultShared().getInt("quickSuggestTimes", 0);
    final static int deadLine = 30000;
    String appVersionNow;
    String versionCode;
    boolean isLatest = false;
    DayNightHelper mDayNightHelper;

    @Override
    protected void doSthBeforeSetView() {
        super.doSthBeforeSetView();
        mDayNightHelper = new DayNightHelper(this);
        if (mDayNightHelper.isDay()) {
            setTheme(R.style.TransparentThemeDay);
        } else {
            setTheme(R.style.TransparentThemeNight);
        }
    }

    @OnClick(R.id.mShareSuggestCodeImg)
    public void download() {
        mShareSuggestCodeImg.setOnClickListener(null);
        if (isLatest) {
            NormalUtils.downloadByWeb(ShareSuggestActivity.this, versionCode);
        } else {
            NormalUtils.downloadByUri(ShareSuggestActivity.this, getString(R.string.uri_download));
            Trace.show(ShareSuggestActivity.this, "后台下载中...");
        }
    }

    @OnClick(R.id.mNavigationRightBtn)
    public void submit() {
        final String msg = mShareSuggestContentEdt.getText().toString();
        if (msg.equals("")) {
            Trace.show(getApplicationContext(), "请输入具体的意见");
        } else {
            if (!isPosting) {
                isPosting = true;
                mNavigationRightBtn.setEnabled(false);
                ThreadPool.getInstance().execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ShareSuggestService.pushSuggest(SampleApplicationLike.user, msg, mShareSuggestTouchEdt.getText().toString());
                            long thisSuggestTime = new Date().getTime();
                            long lastSuggestTime = SampleApplicationLike.getDefaultShared().getLong("lastSuggestTime", thisSuggestTime);
                            SecurePreferences.Editor editor = (SecurePreferences.Editor) SampleApplicationLike.getDefaultShared().edit();
                            editor.putLong("lastSuggestTime", thisSuggestTime);
                            //距离上一次提交意见30秒内再次提交记一次违规 3次违规禁止提交 重装恢复
                            editor.putInt("quickSuggestTimes",
                                    thisSuggestTime - lastSuggestTime < deadLine ? quickSuggestTimes + 1 : 1);
                            Trace.d("quickSuggestTimes " + quickSuggestTimes + " boolean " + (thisSuggestTime - lastSuggestTime < 30000));
                            editor.apply();
                            Trace.show(ShareSuggestActivity.this, "非常感谢您的建议，我会做得更好");
                            finish();
                        } catch (AVException e) {
                            e.printStackTrace();
                            Trace.show(ShareSuggestActivity.this, "提交失败" + Trace.getErrorMsg(e));
                        } finally {
                            isPosting = false;
                            mNavigationRightBtn.setEnabled(false);
                        }
                    }
                });
            } else {
                Trace.show(getApplicationContext(), "您宝贵的意见正在提交中···");
            }
            if (quickSuggestTimes >= 2) {
                ShareSuggestService.setUnableToSuggest(SampleApplicationLike.user);
            }
        }
    }

    @OnClick(R.id.mNavigationLeftBtn)
    public void back() {
        finish();
    }

    @Override
    protected void initializeEvent(Bundle savedInstanceState) {
        mNavigationTitleEdt.setText("分享与反馈");
        mNavigationRightBtn.setText("提交");
        mNavigationRightBtn.setVisibility(View.VISIBLE);
        mNavigationTitleEdt.setEnabled(false);
        mNavigationTitleEdt.setFocusable(false);
        mNavigationTitleEdt.setFocusableInTouchMode(false);
        mShareSuggestCodeImg.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mShareSuggestScV.smoothScrollTo(0, mShareSuggestScV.getHeight());
                    }
                }, 400);
            }
        });
    }

    @Override
    protected boolean initializeCallback(Message msg) {
        switch (msg.what) {
            case hideSaveBtn:
                mNavigationRightBtn.setVisibility(View.INVISIBLE);
                break;
        }
        return false;
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_share_suggest;
    }

    @Override
    protected void initializeData(Bundle savedInstanceState) {
        appVersionNow = SystemUtils.getAppVersion(getApplicationContext());
        ThreadPool.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean flag = ShareSuggestService.isAbleToSuggest(SampleApplicationLike.user);
                    Trace.d("查询isAbleToSuggest 查询到" + SampleApplicationLike.user + " 的记录为" + flag);
                    if (!flag) {
                        handler.sendEmptyMessage(hideSaveBtn);
                    }
                } catch (AVException e) {
                    e.printStackTrace();
                }
            }
        });
        ThreadPool.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final AVObject version = ShareSuggestService.getVersionInfo();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            versionCode = version.getString("version_name");
                            if (versionCode.compareTo(appVersionNow) > 0) {
                                String str = "最新版本:" + versionCode + "[查看内容]";
                                mShareSuggestVersionTxt.setText(str);
                                mShareSuggestVersionTxt.setTextColor(getResources().getColor(R.color.crimson));
                                mShareSuggestVersionTxt.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialog = new MaterialDialog.Builder(ShareSuggestActivity.this)
                                                .title("升级版本:" + versionCode)
                                                .content(version.getString("version_content"))
                                                .backgroundColorRes(mDayNightHelper.getColorResId(ShareSuggestActivity.this, DayNightHelper.COLOR_BACKGROUND))
                                                .titleColor(mDayNightHelper.getColorRes(ShareSuggestActivity.this, DayNightHelper.COLOR_TEXT))
                                                .contentColor(mDayNightHelper.getColorRes(ShareSuggestActivity.this, DayNightHelper.COLOR_TEXT))
                                                .positiveText("下载")
                                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                    @Override
                                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                        download();
                                                    }
                                                }).show();
//                                        AlertDialog alertDialog = new AlertDialog.Builder(ShareSuggestActivity.this)
//                                                .setTitle("版本:" + versionCode)
//                                                .setMessage(version.getString("version_content"))
//                                                .setPositiveButton("下载", new DialogInterface.OnClickListener() {
//                                                    @Override
//                                                    public void onClick(DialogInterface dialog, int which) {
//                                                        download();
//                                                    }
//                                                })
//                                                .create();
//                                        alertDialog.show();
                                    }
                                });
                                isLatest = true;
//                                Trace.show(getApplicationContext(), version.getString("version_content"));
                            } else {
                                String str = "当前版本：" + appVersionNow + " 已是最新";
                                mShareSuggestVersionTxt.setText(str);
                                mShareSuggestTipsTxt.setText("分享给你的朋友们吧！");
                                mShareSuggestVersionTxt.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialog = new MaterialDialog.Builder(ShareSuggestActivity.this)
                                                .title("当前版本:" + versionCode)
                                                .backgroundColorRes(mDayNightHelper.getColorResId(ShareSuggestActivity.this, DayNightHelper.COLOR_BACKGROUND))
                                                .titleColor(mDayNightHelper.getColorRes(ShareSuggestActivity.this, DayNightHelper.COLOR_TEXT))
                                                .contentColor(mDayNightHelper.getColorRes(ShareSuggestActivity.this, DayNightHelper.COLOR_TEXT))
                                                .content(version.getString("version_content")).show();
//                                        AlertDialog alertDialog = new AlertDialog.Builder(ShareSuggestActivity.this)
//                                                .setTitle("版本:" + versionCode)
//                                                .setMessage(version.getString("version_content"))
//                                                .create();
//                                        alertDialog.show();
                                    }
                                });
                                isLatest = false;
                            }
                        }
                    });
                } catch (AVException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void initializeView(Bundle savedInstanceState) {
        ButterKnife.bind(this);
//        setSlidingModeRight();
    }

    public static void startMe(Context context) {
        Intent intent = new Intent(context, ShareSuggestActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        NormalUtils.clearTextLineCache();
    }
}
