package com.kerchin.yellownote.activity;


import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.kerchin.yellownote.R;

public abstract class BaseFragmentActivity extends FragmentActivity {

    public ImageView mTitleBack;
    public TextView mRightTxt, mTitle;
    ;
    public ImageView mBackImg;
    // public boolean isBack = false;
    public boolean isExisTitle = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView();
        initTitleView();
        onTitleClick();
        initializeView();
        initializeData();
    }

    protected abstract void onTitleClick();

    private void initTitleView() {
        if (isExisTitle) {
            /*mRightTxt = (TextView) findViewById(R.id.mRightTxt);
			mTitleBack = (ImageView) findViewById(R.id.mTitleBack);
			mTitle = (TextView) findViewById(R.id.mTitle);
			mTitleBack.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					finish();
				}
			});*/
        }
    }

    protected abstract void setContentView();

    protected abstract void initializeView();

    protected abstract void initializeData();

    @Override
    protected void onResume() {
        super.onResume();
        // TODO umeng
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
