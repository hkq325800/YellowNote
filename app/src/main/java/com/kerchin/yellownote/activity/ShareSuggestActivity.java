package com.kerchin.yellownote.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.kerchin.yellownote.R;
import com.kerchin.yellownote.base.BaseHasSwipeActivity;
import com.kerchin.yellownote.global.MyApplication;
import com.kerchin.yellownote.proxy.ShareSuggetService;
import com.kerchin.yellownote.utilities.SystemHandler;
import com.kerchin.yellownote.utilities.Trace;
import com.securepreferences.SecurePreferences;

import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Kerchin on 2016/3/5 0005.
 */
public class ShareSuggestActivity extends BaseHasSwipeActivity {
    private final static int hideSaveBtn = 0;
    @Bind(R.id.mNavigationRightBtn)
    Button mNavigationRightBtn;
    @Bind(R.id.mNavigationTitleEdt)
    EditText mNavigationTitleEdt;
    @Bind(R.id.mShareSuggetCodeImg)
    ImageView mShareSuggetCodeImg;
    @Bind(R.id.mShareSuggetContentEdt)
    EditText mShareSuggetContentEdt;
    @Bind(R.id.mShareSuggetTouchEdt)
    EditText mShareSuggetTouchEdt;
    @Bind(R.id.mShareSuggetScV)
    ScrollView mShareSuggetScV;
    //防止多次提交数据
    private boolean isPosting = false;
    int quickSuggestTimes = MyApplication.getDefaultShared().getInt("quickSuggestTimes", 0);
    final static int deadLine = 30000;

    private SystemHandler handler = new SystemHandler(this) {

        @Override
        public void handlerMessage(Message msg) {
            switch (msg.what) {
                case hideSaveBtn:
                    mNavigationRightBtn.setVisibility(View.INVISIBLE);
                    break;
            }
        }
    };

    @Override
    protected void setContentView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_share_suggest);
    }

    @OnClick(R.id.mShareSuggetCodeImg)
    public void download(){
        Uri uri = Uri.parse(getString(R.string.uri_download));//指定网址
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);//指定Action
        intent.setData(uri);//设置Uri
        startActivity(intent);//启动Activity
    }

    @OnClick(R.id.mNavigationRightBtn)
    public void submit(){
        final String msg = mShareSuggetContentEdt.getText().toString();
        if (msg.equals("")) {
            Trace.show(ShareSuggestActivity.this, "请输入具体的意见");
        } else {
            if (!isPosting) {
                isPosting = true;
                mNavigationRightBtn.setEnabled(false);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ShareSuggetService.pushSuggest(MyApplication.user, msg, mShareSuggetTouchEdt.getText().toString());
                            long thisSuggestTime = new Date().getTime();
                            long lastSuggestTime = MyApplication.getDefaultShared().getLong("lastSuggestTime", thisSuggestTime);
                            SecurePreferences.Editor editor = (SecurePreferences.Editor) MyApplication.getDefaultShared().edit();
                            editor.putLong("lastSuggestTime", thisSuggestTime);
                            //距离上一次提交意见30秒内再次提交记一次违规 5次违规禁止提交 重装恢复
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
                }).start();
            } else {
                Trace.show(ShareSuggestActivity.this, "您宝贵的意见正在提交中···");
            }
            if (quickSuggestTimes >= 2) {
                ShareSuggetService.setUnableToSuggest(MyApplication.user);
            }
        }
    }

    @OnClick(R.id.mNavigationLeftBtn)
    public void back(){
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
        mShareSuggetCodeImg.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mShareSuggetScV.smoothScrollTo(0, mShareSuggetScV.getHeight());
                    }
                }, 400);
            }
        });
    }

    @Override
    protected void initializeData(Bundle savedInstanceState) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    AVObject a = ShareSuggetService.isAbleToSuggest(MyApplication.user);
                    boolean flag = a.getBoolean("isAbleToSuggest");
                    Trace.d("查询isAbleToSuggest 查询到" + a.get("user_tel") + " 的记录为" + flag);
                    if (!flag) {
                        handler.sendEmptyMessage(hideSaveBtn);
                    }
                } catch (AVException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    protected void initializeView(Bundle savedInstanceState) {
        ButterKnife.bind(this);
    }

    public static void startMe(Context context) {
        Intent intent = new Intent(context, ShareSuggestActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        System.gc();//TODO 泄漏
    }
}
