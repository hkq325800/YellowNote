package com.kerchin.yellownote.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.kerchin.yellownote.R;
import com.kerchin.yellownote.base.BaseHasSwipeActivity;
import com.kerchin.yellownote.global.Config;
import com.kerchin.yellownote.global.MyApplication;
import com.kerchin.yellownote.proxy.LoginService;
import com.kerchin.yellownote.proxy.SecretService;
import com.kerchin.yellownote.utilities.NormalUtils;
import com.kerchin.yellownote.utilities.Trace;
import com.securepreferences.SecurePreferences;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Kerchin on 2016/4/9 0009.
 * More Code on hkq325800@163.com
 */
public class SecretActivity extends BaseHasSwipeActivity {
    @Bind(R.id.mNavigationTitleEdt)
    EditText mNavigationTitleEdt;
    @Bind(R.id.mNavigationRightBtn)
    Button mNavigationRightBtn;
    @Bind(R.id.mSecretPassEdt)
    EditText mSecretPassEdt;
    @Bind(R.id.mSecretNewPassEdt)
    EditText mSecretNewPassEdt;
    @Bind(R.id.mSecretNewPassAgainEdt)
    EditText mSecretNewPassAgainEdt;

    @Override
    protected void setContentView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_secret);
        NormalUtils.immerge(this, R.color.lightSkyBlue);
    }

    @Override
    protected void initializeEvent(Bundle savedInstanceState) {
        mNavigationTitleEdt.setText("密码相关");
        mNavigationRightBtn.setText("提交");
        mNavigationRightBtn.setVisibility(View.VISIBLE);
        mNavigationTitleEdt.setEnabled(false);
        mNavigationTitleEdt.setFocusable(false);
        mNavigationTitleEdt.setFocusableInTouchMode(false);
        mNavigationRightBtn.setFocusable(true);
        mNavigationRightBtn.setFocusableInTouchMode(true);
    }

    @Override
    protected void initializeData(Bundle savedInstanceState) {

    }

    @Override
    protected void initializeView(Bundle savedInstanceState) {
        ButterKnife.bind(this);
        setSlidingModeRight();
        mSecretPassEdt.setFilters(new InputFilter[]{new InputFilter.LengthFilter(13)});
        mSecretNewPassEdt.setFilters(new InputFilter[]{new InputFilter.LengthFilter(13)});
        mSecretNewPassAgainEdt.setFilters(new InputFilter[]{new InputFilter.LengthFilter(13)});
    }

    public static void startMe(Context context) {
        Intent intent = new Intent(context, SecretActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @OnClick(R.id.mNavigationLeftBtn)
    public void back() {
        finish();
    }

    @OnClick(R.id.mNavigationRightBtn)
    public void submit() {
        if (tableCheck()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        AVObject avObjects = LoginService.loginVerify(MyApplication.user, mSecretPassEdt.getText().toString());
                        if (avObjects != null) {
                            Trace.d("查询 用户" + avObjects.get("user_tel") + "旧密码正确");
                            boolean isFrozen = avObjects.getBoolean("isFrozen");
                            if (isFrozen) {//冻结修改不成功
                                Message message = Message.obtain();//由于账户冻结重新登陆
                                message.what = reLogForFrozen;
                                handler.sendMessage(message);
                            } else {
                                SecretService.alterSecret(MyApplication.user, mSecretNewPassEdt.getText().toString());
                                //存入shared
                                SecurePreferences.Editor editor = (SecurePreferences.Editor) MyApplication.getDefaultShared().edit();
                                editor.putString(Config.KEY_USER, MyApplication.user);
                                editor.putString(Config.KEY_PASS, mSecretNewPassEdt.getText().toString());
                                editor.apply();
                                //密码正确进行修改
                                Message message = Message.obtain();//直接进入
                                message.what = trueForSecret;
                                handler.sendMessage(message);
                            }
                        } else {
                            //缓存错误重新登录
                            Message message = Message.obtain();//由于密码错误重新登陆
                            message.what = secretError;
                            handler.sendMessage(message);
                        }
                    } catch (AVException e) {
                        e.printStackTrace();
                        Trace.show(SecretActivity.this, "请检查网络后单击图标重试" + Trace.getErrorMsg(e), Toast.LENGTH_LONG);
                    }
                }
            }).start();
        }
    }

    private final static byte secretError = 0;
    private final static byte reLogForFrozen = 1;
    private final static byte trueForSecret = 2;
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case secretError:
                    Trace.show(SecretActivity.this, "你输入的旧密码出错啦");
                    break;
                case reLogForFrozen:
                    Trace.show(SecretActivity.this, "您操作的账号已被冻结,请联系 hkq325800@163.com", Toast.LENGTH_LONG);
                    break;
                case trueForSecret:
                    Trace.show(SecretActivity.this, "密码修改成功");
                    mSecretNewPassEdt.setText("");
                    mSecretPassEdt.setText("");
                    mSecretNewPassAgainEdt.setText("");
                    mSecretPassEdt.requestFocus();
                    break;
            }
        }
    };

    private boolean tableCheck() {
        if (mSecretPassEdt.getText().toString().equals("")
                || mSecretNewPassEdt.getText().toString().equals("")
                || mSecretNewPassAgainEdt.getText().toString().equals("")) {
            Trace.show(SecretActivity.this, "请将信息填写完整");
            return false;
        } else if (!mSecretNewPassEdt.getText().toString().equals(mSecretNewPassAgainEdt.getText().toString())) {
            Trace.show(SecretActivity.this, "两次填写的新密码不一致");
            return false;
        } else if (mSecretNewPassEdt.getText().toString().length() < 6 || mSecretNewPassEdt.getText().toString().length() > 13) {
            Trace.show(SecretActivity.this, "密码长度控制在6-13位");
            return false;
        }
        return true;
    }
}
