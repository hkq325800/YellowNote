package com.kerchin.yellownote.base;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import com.kerchin.yellownote.utilities.SystemBarTintManager;
import com.kerchin.yellownote.utilities.Trace;

/**
 * Created by Administrator on 2016/3/6 0006.
 */
public abstract class BaseActivity extends AppCompatActivity /*implements SlidingPaneLayout.PanelSlideListener*/ {
//    public boolean isExisTitle = true;
    //是否支持滑动返回
//    public boolean isSupportSwipeBack = true;
    public final static String TAG = BaseActivity.class.getCanonicalName();

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Trace.d("onSaveInstanceState");
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        setContentView(savedInstanceState);
        //initTitleView();
        //onTitleClick();
        initializeView(savedInstanceState);
        initializeData(savedInstanceState);
        initializeEvent(savedInstanceState);
//        initSwipeBackFinish();
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

    /**
     * 初始化滑动返回
     */
//    private void initSwipeBackFinish() {
//        if (isSupportSwipeBack) {
//            SlidingPaneLayout slidingPaneLayout = new SlidingPaneLayout(this);
//            //通过反射改变mOverhangSize的值为0，这个mOverhangSize值为菜单到右边屏幕的最短距离，默认
//            //是32dp，现在给它改成0
//            try {
//                //属性
//                Field f_overHang = SlidingPaneLayout.class.getDeclaredField("mOverhangSize");
//                f_overHang.setAccessible(true);
//                f_overHang.set(slidingPaneLayout, 0);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//            slidingPaneLayout.setPanelSlideListener(BaseActivity.this);
//            slidingPaneLayout.setSliderFadeColor(getResources().getColor(android.R.color.transparent));
//            //slidingPaneLayout.setLayoutParams(new SlidingPaneLayout.LayoutParams(100,SlidingPaneLayout.LayoutParams.MATCH_PARENT));
//            View leftView = new View(this);
//            leftView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//            slidingPaneLayout.addView(leftView, 0);
//
//            ViewGroup decor = (ViewGroup) getWindow().getDecorView();
//            ViewGroup decorChild = (ViewGroup) decor.getChildAt(0);
//            //decorChild.setBackgroundColor(getResources().getColor(android.R.color.white));
//            decor.removeView(decorChild);
//            decor.addView(slidingPaneLayout);
//            slidingPaneLayout.addView(decorChild, 1);
//        }
//    }

//    @Override
//    public void onPanelClosed(View view) {
////        Log.d("onPanelClosed", "getX:" + view.getX());
//    }
//
//    @Override
//    public void onPanelOpened(View view) {
//        Log.d("onPanelOpened","getX:"+view.getX());
//        finish();
//        this.overridePendingTransition(0, R.anim.slide_out_right);
//    }
//
//    @Override
//    public void onPanelSlide(View view, float slideOffset) {
//        Log.d("onPanelSlide","x:"+view.getX()+" slideOffset:"+slideOffset);
//    }

    /**
     * 设置状态栏颜色
     * 也就是所谓沉浸式状态栏
     */
    public void setStatusBarColor(int color) {
        /**
         * Android4.4以上可用
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintResource(color);
            tintManager.setStatusBarTintEnabled(true);
            //Window window = getWindow();
            //window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            //window.setStatusBarColor(color);
        }
    }
}
