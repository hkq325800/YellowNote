package com.kerchin.yellownote.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.GetCallback;
import com.avos.avoscloud.SaveCallback;
import com.kerchin.yellownote.R;
import com.kerchin.yellownote.base.BaseHasSwipeActivity;
import com.kerchin.yellownote.global.MyApplication;
import com.kerchin.yellownote.utilities.SystemHandler;
import com.kerchin.yellownote.utilities.Trace;
import com.securepreferences.SecurePreferences;

import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2016/3/5 0005.
 */
public class SettingActivity extends BaseHasSwipeActivity {
    @Bind(R.id.mNavigationRightBtn)
    Button mNavigationRightBtn;
    @Bind(R.id.mNavigationLeftBtn)
    Button mNavigationLeftBtn;
    @Bind(R.id.mSettingCodeImg)
    ImageView mSettingCodeImg;
    @Bind(R.id.mNavigationTitleEdt)
    EditText mNavigationTitleEdt;
    @Bind(R.id.mSettingContentEdt)
    EditText mSettingContentEdt;
    @Bind(R.id.mSettingTouchEdt)
    EditText mSettingTouchEdt;
    //    @Bind(R.id.mSettingTouchTextInput)
//    android.support.design.widget.TextInputLayout mSettingTouchTextInput;
//    @Bind(R.id.mSettingContentTextInput)
//    android.support.design.widget.TextInputLayout mSettingContentTextInput;
    //防止多次提交数据
    private boolean isPosting = false;
    int quickSuggestTimes = MyApplication.getDefaultShared().getInt("quickSuggestTimes", 0);

    @Override
    protected void setContentView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_setting);
    }

    @Override
    protected void initializeClick(Bundle savedInstanceState) {
        mSettingCodeImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse(getString(R.string.uri_download));//指定网址
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);//指定Action
                intent.setData(uri);//设置Uri
                startActivity(intent);//启动Activity
            }
        });
        mNavigationTitleEdt.setText("设置");
        mNavigationRightBtn.setText("提交");
        mNavigationRightBtn.setVisibility(View.VISIBLE);
        mNavigationTitleEdt.setEnabled(false);
        mNavigationTitleEdt.setFocusable(false);
        mNavigationTitleEdt.setFocusableInTouchMode(false);
        mNavigationRightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = mSettingContentEdt.getText().toString();
                if (msg.equals("")) {
                    Trace.show(SettingActivity.this, "请输入具体的意见");
                } else {
                    AVObject suggest = new AVObject("Suggest");
                    suggest.put("user_tel", MyApplication.user);
                    suggest.put("suggest_msg", msg);
                    suggest.put("suggest_contact", mSettingTouchEdt.getText().toString());
                    if (!isPosting) {
                        isPosting = true;
                        mNavigationRightBtn.setEnabled(false);
                        suggest.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(AVException e) {
                                isPosting = false;
                                mNavigationRightBtn.setEnabled(false);
                                if (e == null) {
                                    long thisSuggestTime = new Date().getTime();
                                    long lastSuggestTime = MyApplication.getDefaultShared().getLong("lastSuggestTime", thisSuggestTime);
                                    SecurePreferences.Editor editor = (SecurePreferences.Editor) MyApplication.getDefaultShared().edit();
                                    editor.putLong("lastSuggestTime", thisSuggestTime);
                                    //距离上一次提交意见30秒内再次提交记一次违规 5次违规禁止提交 重装恢复
                                    editor.putInt("quickSuggestTimes",
                                            thisSuggestTime - lastSuggestTime < 30000 ? quickSuggestTimes + 1 : 1);
                                    Trace.d("quickSuggestTimes " + quickSuggestTimes + " boolean " + (thisSuggestTime - lastSuggestTime < 30000));
                                    editor.apply();
                                    Trace.show(SettingActivity.this, "非常感谢您的建议，我会做得更好");
                                    finish();
                                } else {
                                    Trace.show(SettingActivity.this, "提交失败" + Trace.getErrorMsg(e));
                                    e.printStackTrace();
                                }
                            }
                        });
                    } else {
                        Trace.show(SettingActivity.this, "您宝贵的意见正在提交中···");
                    }
                    if (quickSuggestTimes >= 2) {
                        AVQuery<AVObject> query = new AVQuery<>("mUser");
                        query.whereEqualTo("user_tel", MyApplication.user);
                        query.getFirstInBackground(new GetCallback<AVObject>() {
                            @Override
                            public void done(AVObject avObject, AVException e) {
                                if (e == null) {
                                    avObject.put("isAbleToSuggest", false);
                                    avObject.saveInBackground();
                                } else {
                                    Trace.d("设置isAbleToSuggest false失败");
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }
            }
        });
        mNavigationLeftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mNavigationRightBtn.requestFocus();
    }

    @Override
    protected void initializeData(Bundle savedInstanceState) {
        AVQuery<AVObject> query = new AVQuery<>("mUser");
        query.whereEqualTo("user_tel", MyApplication.user);
        query.findInBackground(new FindCallback<AVObject>() {
            public void done(List<AVObject> avObjects, AVException e) {
                if (e == null) {
                    Trace.d("查询isAbleToSuggest 查询到" + avObjects.size() + " 条符合条件的数据");
                    if (avObjects.size() > 0) {
                        boolean flag = avObjects.get(0).getBoolean("isAbleToSuggest");
                        if (!flag) {
                            handler.sendEmptyMessage(hideSaveBtn);
                        }
                    }
                }
                handler.sendEmptyMessageDelayed(edittextFocusTrue, 1000);
            }
        });
    }

    private final int hideSaveBtn = 0;
    private final int edittextFocusTrue = 1;
    private SystemHandler handler = new SystemHandler(this) {

        @Override
        public void handlerMessage(Message msg) {
            switch (msg.what) {
                case hideSaveBtn:
                    mNavigationRightBtn.setVisibility(View.INVISIBLE);
                    break;
                case edittextFocusTrue:
                    mSettingTouchEdt.setFocusable(true);
                    mSettingContentEdt.setFocusable(true);
                    mSettingTouchEdt.setFocusableInTouchMode(true);
                    mSettingContentEdt.setFocusableInTouchMode(true);
                    break;
            }
        }
    };

    @Override
    protected void initializeView(Bundle savedInstanceState) {
        ButterKnife.bind(this);
    }

    public static void startMe(Context context) {
        // 指定下拉列表的显示数据
        Intent intent = new Intent(context, SettingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
