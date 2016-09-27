package com.kerchin.yellownote.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.kerchin.yellownote.R;
import com.kerchin.yellownote.base.BaseSwipeBackActivity;
import com.kerchin.yellownote.utilities.helper.DayNightHelper;

import zj.baselibrary.util.ThreadPool.ThreadPool;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Kerchin on 2016/7/6 0006.
 */
public class ThankActivity extends BaseSwipeBackActivity {
    @BindView(R.id.mNavigationTitleEdt)
    EditText mNavigationTitleEdt;
    @BindView(R.id.mNavigationRightBtn)
    Button mNavigationRightBtn;
    @BindView(R.id.mThankLeftTxt)
    TextView mThankLeftTxt;
    @BindView(R.id.mThankTopTxt)
    TextView mThankTopTxt;
    @BindView(R.id.mThankRightTxt)
    TextView mThankRightTxt;
    String thankStrLeft, thankStrRight;
    ArrayList<String> thankList;
    ArrayList<String> linkList;
    SpannableString targetLeft, targetRight, targetTop;
    String todo;
    DayNightHelper mDayNightHelper;

    @Override
    protected void initializeEvent(Bundle savedInstanceState) {

    }

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

    @Override
    protected boolean initializeCallback(Message msg) {
        return false;
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_thank;
    }

    @Override
    protected void initializeData(Bundle savedInstanceState) {
        thankList = new ArrayList<>();
        linkList = new ArrayList<>();
        todo = getResources().getString(R.string.todo);
        final String todoUrl = getResources().getString(R.string.todoUrl);
        ThreadPool.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                //top
                String strTop = "尚待解决的问题 " + todo + "\n特此感谢以下开源库";
                targetTop = new SpannableString(strTop);
                targetTop.setSpan(new URLSpan(todoUrl), strTop.indexOf(" ") + 1, strTop.indexOf(" ") + 1 + todo.length()
                        , Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                //left right
                String thankStr;
                thankStr = getResources().getString(R.string.thank) + " ";
                thankStr = thankStr.replace("- ", "\n");
                collectThankAndLink(thankStr);
                targetLeft = new SpannableString(thankStrLeft);
                targetRight = new SpannableString(thankStrRight);
                int i = 0;
                for (String thank : thankList) {
                    if (i % 2 == 0)
                        targetLeft.setSpan(new URLSpan(linkList.get(i)), thankStrLeft.indexOf(thank)
                                , thankStrLeft.indexOf(thank) + thank.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    else
                        targetRight.setSpan(new URLSpan(linkList.get(i)), thankStrRight.indexOf(thank)
                                , thankStrRight.indexOf(thank) + thank.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    i++;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mThankTopTxt.setText(targetTop);
                        mThankTopTxt.setMovementMethod(LinkMovementMethod.getInstance());
                        mThankLeftTxt.setText(targetLeft);
                        mThankLeftTxt.setMovementMethod(LinkMovementMethod.getInstance());
                        mThankRightTxt.setText(targetRight);
                        mThankRightTxt.setMovementMethod(LinkMovementMethod.getInstance());
                    }
                });
            }

            private void collectThankAndLink(String thankStr) {
                thankStrLeft = "";
                thankStrRight = "";
                while (thankStr.contains("[")) {
                    String str = thankStr.substring(thankStr.indexOf("["), thankStr.indexOf(")") + 1);
                    String thank = str.substring(str.indexOf("[") + 1, str.indexOf("]"));
                    thankList.add(thank);
                    linkList.add(str.substring(str.indexOf("(") + 1, str.indexOf(")")));
                    if (thankList.size() % 2 == 1)
                        thankStrLeft += thank + "\n";
                    else
                        thankStrRight += thank + "\n";
                    thankStr = thankStr.replace(str, thank);
                }
            }
        });
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
        mThankTopTxt.setTextColor(mDayNightHelper.getColorRes(ThankActivity.this, DayNightHelper.COLOR_TEXT));
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
