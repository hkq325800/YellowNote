package com.kerchin.yellownote.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.kerchin.yellownote.R;
import com.kerchin.yellownote.base.BaseHasSwipeActivity;
import com.kerchin.yellownote.utilities.NormalUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Kerchin on 2016/7/6 0006.
 */
public class ThankActivity extends BaseHasSwipeActivity {
    @BindView(R.id.mNavigationTitleEdt)
    EditText mNavigationTitleEdt;
    @BindView(R.id.mNavigationRightBtn)
    Button mNavigationRightBtn;
    @BindView(R.id.mThankTxt)
    TextView mThankTxt;
    String thankStr;
    ArrayList<String> thankList;
    ArrayList<String> linkList;
    SpannableString target;

    @Override
    protected void setContentView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_thank);
        NormalUtils.immerge(this, R.color.lightSkyBlue);
    }

    @Override
    protected void initializeEvent(Bundle savedInstanceState) {

    }

    @Override
    protected void initializeData(Bundle savedInstanceState) {
        thankList = new ArrayList<>();
        linkList = new ArrayList<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                thankStr = getResources().getString(R.string.thank);
                thankStr = thankStr.replace("- ", "\n");
                while (thankStr.contains("[")) {
                    String str = thankStr.substring(thankStr.indexOf("["), thankStr.indexOf(")") + 1);
                    String thank = str.substring(str.indexOf("[") + 1, str.indexOf("]"));
                    thankList.add(thank);
                    linkList.add(str.substring(str.indexOf("(") + 1, str.indexOf(")")));
                    thankStr = thankStr.replace(str, thank);
                }
                target = new SpannableString(thankStr);
                int i = 0;
                for (String thank : thankList) {
//                    SpannableString ss = new SpannableString(thank);
                    target.setSpan(new URLSpan(linkList.get(i)), thankStr.indexOf(thank)
                            , thankStr.indexOf(thank) + thank.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    i++;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mThankTxt.setText(target);
                        mThankTxt.setMovementMethod(LinkMovementMethod.getInstance());
                    }
                });
            }
        }).start();
    }

    @Override
    protected void initializeView(Bundle savedInstanceState) {
        ButterKnife.bind(this);
        mNavigationRightBtn.setVisibility(View.INVISIBLE);
        mNavigationTitleEdt.setEnabled(false);
        mNavigationTitleEdt.setHint("");
        mNavigationTitleEdt.setFocusable(false);
        mNavigationTitleEdt.clearFocus();
        mNavigationTitleEdt.setFocusableInTouchMode(false);
    }

    public static void startMe(Context context) {
        Intent intent = new Intent(context, ThankActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @OnClick(R.id.mNavigationLeftBtn)
    public void back() {
        finish();
    }
}
