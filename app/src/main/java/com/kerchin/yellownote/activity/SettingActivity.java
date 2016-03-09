package com.kerchin.yellownote.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.SaveCallback;
import com.kerchin.yellownote.R;
import com.kerchin.yellownote.global.MyApplication;
import com.kerchin.yellownote.utilities.Trace;
import com.securepreferences.SecurePreferences;

import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2016/3/5 0005.
 */
public class SettingActivity extends BaseHasSwipActivity {
    @Bind(R.id.mNavigationRightBtn)
    Button mNavigationRightBtn;
    @Bind(R.id.mNavigationLeftBtn)
    Button mNavigationLeftBtn;
    @Bind(R.id.mSettingCodeImg)
    ImageView mSettingCodeImg;
    @Bind(R.id.mNavigationTitleEdt)
    EditText mNavigationTitleEdt;
    @Bind(R.id.mSettingTouchTextInput)
    android.support.design.widget.TextInputLayout mSettingTouchTextInput;
    @Bind(R.id.mSettingContentTextInput)
    android.support.design.widget.TextInputLayout mSettingContentTextInput;
    //防止多次提交数据
    private boolean isPosting = false;
    int quickSuggestTimes = MyApplication.getDefaultShared().getInt("quickSuggestTimes", 0);

    @Override
    protected void setContentView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);
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
        mNavigationRightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = mSettingContentTextInput.getEditText().getText().toString();
                if (msg.equals("")) {
                    Trace.show(SettingActivity.this, "请输入具体的意见");
                } else {
                    AVObject suggest = new AVObject("Suggest");
                    suggest.put("user_tel", MyApplication.user);
                    suggest.put("suggest_msg", msg);
                    suggest.put("suggest_contact", mSettingTouchTextInput.getEditText().getText().toString());
                    if (!isPosting) {
                        isPosting = true;
                        suggest.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(AVException e) {
                                if (e == null) {
                                    isPosting = false;
                                    long thisSuggestTime = new Date().getTime();

                                    long lastSuggestTime = MyApplication.getDefaultShared().getLong("lastSuggestTime", thisSuggestTime);
                                    SecurePreferences.Editor editor = (SecurePreferences.Editor) MyApplication.getDefaultShared().edit();
                                    editor.putLong("lastSuggestTime", thisSuggestTime);
                                    editor.putInt("quickSuggestTimes",
                                            thisSuggestTime - lastSuggestTime < 30000 ? ++quickSuggestTimes : 1);
                                    editor.apply();
                                    Trace.show(SettingActivity.this, "非常感谢您的建议，我会做得更好");
                                    finish();
                                } else {
                                    isPosting = false;
                                    Trace.show(SettingActivity.this, "提交失败，请重试");
                                }
                            }
                        });
                    } else {
                        Trace.show(SettingActivity.this, "您宝贵的意见正在提交中···");
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
        if (quickSuggestTimes >= 5)
            mNavigationRightBtn.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void initializeData(Bundle savedInstanceState) {

    }

    @Override
    protected void initializeView(Bundle savedInstanceState) {

    }

    public static void startMe(Context context) {
        // 指定下拉列表的显示数据
        Intent intent = new Intent(context, SettingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
