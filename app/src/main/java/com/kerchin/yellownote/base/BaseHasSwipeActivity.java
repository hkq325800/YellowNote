package com.kerchin.yellownote.base;

import android.os.Bundle;

import com.afollestad.materialdialogs.MaterialDialog;
import com.kerchin.yellownote.R;
import com.kerchin.yellownote.utilities.Trace;

/**
 * Created by hailonghan on 15/6/9.
 *
 * MyOrmLiteBaseActivity的父类 也可以作为Activity直接父类
 */
public abstract class BaseHasSwipeActivity extends BaseSwipeBackActivity {

    public final static String TAG = BaseHasSwipeActivity.class.getCanonicalName();
    public MaterialDialog dialog;

//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        Trace.d("onSaveInstanceState");
//        super.onSaveInstanceState(outState);
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(savedInstanceState);

//        initTitleView();
//        onTitleClick();
        initializeView(savedInstanceState);
        initializeData(savedInstanceState);
        initializeEvent(savedInstanceState);
    }

    protected abstract void setContentView(Bundle savedInstanceState);

    //protected abstract void onTitleClick();

    protected abstract void initializeEvent(Bundle savedInstanceState);

//    private void initTitleView() {
//        if (isExisTitle) {
//          mRightTxt = (TextView) findViewById(R.id.mRightTxt);
//          mTitleBack = (ImageView) findViewById(R.id.mTitleBack);
//			mTitle = (TextView) findViewById(R.id.mTitle);
//			mTitleBack.setOnClickListener(new OnClickListener() {
//
//				@Override
//				public void onClick(View v) {
//					finish();
//				}
//			});
//        }
//    }

    protected abstract void initializeData(Bundle savedInstanceState);

    protected abstract void initializeView(Bundle savedInstanceState);

    @Override
    public void finish() {
        super.finish();
        this.overridePendingTransition(0, R.anim.slide_out_right);
    }
}
