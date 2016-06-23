package com.kerchin.yellownote.base;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Kerchin on 2016/3/6 0006.
 */
public abstract class BaseActivity extends AppCompatActivity {
//    public boolean isExisTitle = true;
    public final static String TAG = BaseActivity.class.getCanonicalName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(savedInstanceState);
        //initTitleView();
        //onTitleClick();
        initializeView(savedInstanceState);
        initializeData(savedInstanceState);
        initializeEvent(savedInstanceState);
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    protected abstract void setContentView(Bundle savedInstanceState);

    //protected abstract void onTitleClick();

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

    protected abstract void initializeView(Bundle savedInstanceState);

    protected abstract void initializeData(Bundle savedInstanceState);

    protected abstract void initializeEvent(Bundle savedInstanceState);

}
//重启
//        Intent i = getBaseContext().getPackageManager()
//                .getLaunchIntentForPackage(getBaseContext().getPackageName());
//        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        startActivity(i);

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
//        }

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            ViewGroup contentView = (ViewGroup) findViewById(android.R.id.content);
//            contentView.setBackgroundColor(getResources().getColor(R.color.lightSkyBlue));
//            contentView.setPadding(0, 30, 0 ,0);
//        }

//        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
//            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            ViewGroup contentView = (ViewGroup) findViewById(android.R.id.content);
//            contentView.setBackgroundColor(getResources().getColor(R.color.lightSkyBlue));
//            contentView.setPadding(0, NormalUtils.getStatusBarHeight(this), 0 ,0);
//        }
