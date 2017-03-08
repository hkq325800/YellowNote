package com.kerchin.yellownote.ui.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
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
import com.alibaba.android.arouter.facade.annotation.Route;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.kerchin.global.Config;
import com.kerchin.yellownote.R;
import com.kerchin.yellownote.base.BaseSwipeBackActivity;
import com.kerchin.yellownote.data.proxy.ShareSuggestService;
import com.kerchin.yellownote.data.service.DownloadService;
import com.kerchin.yellownote.global.MyApplication;
import com.kerchin.yellownote.utilities.helper.DayNightHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import zj.remote.baselibrary.util.NormalUtils;
import zj.remote.baselibrary.util.PreferenceUtils;
import zj.remote.baselibrary.util.SystemUtils;
import zj.remote.baselibrary.util.ThreadPool.ThreadPool;
import zj.remote.baselibrary.util.Trace;

/**
 * Created by Kerchin on 2016/3/5 0005.
 */
@Route(path = "/yellow/share_suggest")
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
    int quickSuggestTimes;
    final static int deadLine = 30000;
    String appVersionNow;
    String versionCode;
    boolean isLatest = false;
    DayNightHelper mDayNightHelper;

    @Override
    protected void doSthBeforeSetView(Bundle savedInstanceState) {
        super.doSthBeforeSetView(savedInstanceState);
        mDayNightHelper = DayNightHelper.getInstance(this);
        quickSuggestTimes = PreferenceUtils.getInt("quickSuggestTimes", 0, ShareSuggestActivity.this);
        if (mDayNightHelper.isDay()) {
            setTheme(R.style.TransparentThemeDay);
        } else {
            setTheme(R.style.TransparentThemeNight);
        }
    }

    String SHARED_FILE_NAME;

    /**
     * 初始化分享的图片
     */
    private File initImagePath() {
        try {//判断SD卡中是否存在此文件夹
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                    && Environment.getExternalStorageDirectory().exists()) {
                SHARED_FILE_NAME = Environment.getExternalStorageDirectory().getAbsolutePath() + "/YellowNote" + "/ic_code.png";
            } else {
                SHARED_FILE_NAME = getApplication().getFilesDir().getAbsolutePath() + "/YellowNote" + "/ic_code.png";
            }
            File file = new File(SHARED_FILE_NAME);
            //判断图片是否存此文件夹中
            if (!file.exists()) {
                file.createNewFile();
                Bitmap pic = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_code);
                FileOutputStream fos = new FileOutputStream(file);
                pic.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();
                fos.close();
            }
            return file;
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    @OnClick(R.id.mShareSuggestCodeImg)
    public void share() {
        initImagePath();
        Trace.show(this, "已保存至本地" + SHARED_FILE_NAME);
//        File file = initImagePath();
//        Uri uri = Uri.parse(SHARED_FILE_NAME);//.fromFile(file);
//        ShareUtil.friendsShare(this, uri);
    }

    public void download() {
        NormalUtils.downloadByWeb(ShareSuggestActivity.this, versionCode, DownloadService.class, getString(R.string.uri_download)
                , getResources().getString(R.string.app_name) + versionCode + ".apk");
        Trace.show(ShareSuggestActivity.this, "后台下载中...");
//        if (!isLatest) {
//            NormalUtils.downloadByWeb(ShareSuggestActivity.this, versionCode, ShareSuggestService.class, getString(R.string.uri_download), getResources().getString(R.string.app_name) + versionCode + ".apk");
//        } else {
//            NormalUtils.downloadByUri(ShareSuggestActivity.this, getString(R.string.uri_download));
////            Trace.show(ShareSuggestActivity.this, "后台下载中...");
//        }
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
                            ShareSuggestService.pushSuggest(PreferenceUtils.getString(Config.KEY_USER, "", MyApplication.context)
                                    , msg, mShareSuggestTouchEdt.getText().toString());
                            long thisSuggestTime = new Date().getTime();
                            long lastSuggestTime = PreferenceUtils.getLong("lastSuggestTime", thisSuggestTime, ShareSuggestActivity.this);
                            PreferenceUtils.putLong("lastSuggestTime", thisSuggestTime, ShareSuggestActivity.this);
                            //距离上一次提交意见30秒内再次提交记一次违规 3次违规禁止提交 重装恢复
                            PreferenceUtils.putInt("quickSuggestTimes",
                                    thisSuggestTime - lastSuggestTime < deadLine ? quickSuggestTimes + 1 : 1, ShareSuggestActivity.this);
                            Trace.d("quickSuggestTimes " + quickSuggestTimes + " boolean " + (thisSuggestTime - lastSuggestTime < 30000));
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
                ShareSuggestService.setUnableToSuggest(PreferenceUtils.getString(Config.KEY_USER, "", MyApplication.context));
            }
        }
    }

    @OnClick(R.id.mNavigationLeftBtn)
    public void back() {
        finish();
    }

    @Override
    protected void initEvent(Bundle savedInstanceState) {
        super.initEvent(savedInstanceState);
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
    protected boolean initCallback(Message msg) {
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
    protected void initData(Bundle savedInstanceState) {
        appVersionNow = SystemUtils.getAppVersion(getApplicationContext());
        ThreadPool.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean flag = ShareSuggestService.isAbleToSuggest(PreferenceUtils.getString(Config.KEY_USER, "", MyApplication.context));
                    Trace.d("查询isAbleToSuggest 查询到" + PreferenceUtils.getString(Config.KEY_USER, "", MyApplication.context) + " 的记录为" + flag);
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
                                                .backgroundColor(mDayNightHelper.getColorRes(ShareSuggestActivity.this, DayNightHelper.COLOR_BACKGROUND))
                                                .titleColor(mDayNightHelper.getColorRes(ShareSuggestActivity.this, DayNightHelper.COLOR_TEXT))
                                                .contentColor(mDayNightHelper.getColorRes(ShareSuggestActivity.this, DayNightHelper.COLOR_TEXT))
                                                .positiveText("下载")
                                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                    @Override
                                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                        download();
                                                    }
                                                }).show();
                                    }
                                });
                                isLatest = false;
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
                                                .backgroundColor(mDayNightHelper.getColorRes(ShareSuggestActivity.this, DayNightHelper.COLOR_BACKGROUND))
                                                .titleColor(mDayNightHelper.getColorRes(ShareSuggestActivity.this, DayNightHelper.COLOR_TEXT))
                                                .contentColor(mDayNightHelper.getColorRes(ShareSuggestActivity.this, DayNightHelper.COLOR_TEXT))
                                                .content(version.getString("version_content")).show();
                                    }
                                });
                                isLatest = true;
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
    protected void initView(Bundle savedInstanceState) {
        ButterKnife.bind(this);
//        setSlidingModeRight();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        NormalUtils.clearTextLineCache();
    }
}
