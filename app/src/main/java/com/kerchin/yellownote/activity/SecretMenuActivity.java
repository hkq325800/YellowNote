package com.kerchin.yellownote.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ToggleButton;

import com.kerchin.yellownote.R;
import com.kerchin.yellownote.base.BaseHasSwipeActivity;
import com.kerchin.yellownote.utilities.NormalUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Kerchin on 2016/6/6 0006.
 */
public class SecretMenuActivity extends BaseHasSwipeActivity {
    @BindView(R.id.mNavigationTitleEdt)
    EditText mNavigationTitleEdt;
    @BindView(R.id.mNavigationRightBtn)
    Button mNavigationRightBtn;
    @BindView(R.id.mSecretMenuPatternToggle)
    ToggleButton mSecretMenuPatternToggle;

    @Override
    protected void setContentView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_secret_menu);
        NormalUtils.immerge(this, R.color.lightSkyBlue);
    }

    @Override
    protected void initializeEvent(Bundle savedInstanceState) {

    }

    @Override
    protected void initializeData(Bundle savedInstanceState) {
    }

    @Override
    protected void initializeView(Bundle savedInstanceState) {
        ButterKnife.bind(this);
        mNavigationTitleEdt.setText("密码相关");
        mNavigationRightBtn.setText("提交");
        mNavigationRightBtn.setVisibility(View.VISIBLE);
        mNavigationTitleEdt.setEnabled(false);
        mNavigationTitleEdt.setFocusable(false);
        mNavigationTitleEdt.setFocusableInTouchMode(false);
        mNavigationRightBtn.setFocusable(true);
        mNavigationRightBtn.setFocusableInTouchMode(true);
    }

    public static void startMe(Context context) {
        Intent intent = new Intent(context, SecretMenuActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @OnClick(R.id.mSecretMenuPatternToggle)
    public void toggleClick() {
        SetPatternActivity.startMe(SecretMenuActivity.this, 0);
        overridePendingTransition(R.anim.push_right_in,
                R.anim.push_right_out);
    }

    @OnClick(R.id.mSecretMenuLoginLiL)
    public void gotoSecret() {
        SecretActivity.startMe(getApplicationContext());
        overridePendingTransition(R.anim.push_right_in,
                R.anim.push_right_out);
    }

    @OnClick(R.id.mSecretMenuPatternToggleLiL)
    public void gotoSecretSet() {
        mSecretMenuPatternToggle.setChecked(!mSecretMenuPatternToggle.isChecked());
//        SetPatternActivity.startMe(SecretMenuActivity.this, 1);
        Intent intent = new Intent(SecretMenuActivity.this, SetPatternActivity.class);

        startActivityForResult(intent, 1);
        overridePendingTransition(R.anim.push_right_in,
                R.anim.push_right_out);
    }

    @OnClick(R.id.mSecretMenuPatternLiL)
    public void gotoSecretModify() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {

        } else if(requestCode == 0 && resultCode == RESULT_CANCELED){
            mSecretMenuPatternToggle.setChecked(!mSecretMenuPatternToggle.isChecked());
        }
    }
}
