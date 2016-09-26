package com.kerchin.yellownote.base;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingActivityHelper;

import static com.kerchin.yellownote.R.drawable.slide_shadow;

/**
 * Created by hailonghan on 15/6/11.
 *
 *
 */
public abstract class BaseSwipeBackActivity extends AppCompatActivity
        implements SlidingMenu.OnOpenedListener {

    private SlidingActivityHelper mHelper;
    //SlidingMenu
    private SlidingMenu mSlidingMenu;

    /**
     * 禁止滑动退出
     */
    public void closeSliding(){
        mSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
    }

    public void openSliding(){
        mSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mHelper = new SlidingActivityHelper(this);
        mHelper.onCreate(savedInstanceState);

        setSlidingActionBarEnabled(false);
        //这里借用了SlidingMenu的setBehindContentView方法来设置一个透明菜单
        View behindView = new View(this);
        behindView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        behindView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        setBehindContentView(behindView);
        mSlidingMenu = getSlidingMenu();
        //设置阴影宽度为10个px
        mSlidingMenu.setShadowWidth(10);
        //设置阴影
        mSlidingMenu.setShadowDrawable(slide_shadow);
        //设置下面的布局，也就是我们上面定义的透明菜单离右边屏幕边缘的距离为0，也就是滑动开以后菜单会全屏幕显示
        mSlidingMenu.setBehindOffset(0);
        mSlidingMenu.setFadeDegree(0.35f);
        //菜单打开监听，因为菜单打开后我们要finish掉当前的Activity
        mSlidingMenu.setOnOpenedListener(this);
        //设置手势滑动方向，因为我们要实现微信那种右滑动的效果，这里设置成SlidingMenu.LEFT模式
        mSlidingMenu.setMode(SlidingMenu.LEFT);
        //因为微信是只有边缘滑动，我们设置成TOUCHMODE_MARGIN模式，如果你想要全屏幕滑动，只需要把这个改成TOUCHMODE_FULLSCREEN就OK了
        mSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        setSlidingMargin(40f);
        super.onCreate(savedInstanceState);
    }

    /**
     * 设置左滑动
     */
    public void setSlidingModeRight(){
        mSlidingMenu.setMode(SlidingMenu.RIGHT);
        mSlidingMenu.setShadowWidth(-100);
        setSlidingMargin(48f);
    }

    /**
     * 设置滑动边距
     * @param dp 边距
     */
    public void setSlidingMargin(float dp){
        mSlidingMenu.setTouchmodeMarginThreshold((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dp, getResources().getDisplayMetrics()));//默认48dp
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mHelper.onPostCreate(savedInstanceState);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return true;
    }

    @Override
    public View findViewById(int id) {
        View v = super.findViewById(id);
        if (v != null)
            return v;
        return mHelper.findViewById(id);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mHelper.onSaveInstanceState(outState);
    }

    @Override
    public void setContentView(int id) {
        setContentView(getLayoutInflater().inflate(id, null));
    }

    @Override
    public void setContentView(View v) {
        setContentView(v, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    @Override
    public void setContentView(View v, ViewGroup.LayoutParams params) {
        super.setContentView(v, params);
        mHelper.registerAboveContentView(v, params);
    }

    public void setBehindContentView(int id) {
        setBehindContentView(getLayoutInflater().inflate(id, null));
    }

    public void setBehindContentView(View v) {
        setBehindContentView(v, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public void setBehindContentView(View v, ViewGroup.LayoutParams params) {
        mHelper.setBehindContentView(v, params);
    }

    public SlidingMenu getSlidingMenu() {
        return mHelper.getSlidingMenu();
    }

    public void toggle() {
        mHelper.toggle();
    }

    public void showContent() {
        mHelper.showContent();
    }

    public void showMenu() {
        mHelper.showMenu();
    }

    public void showSecondaryMenu() {
        mHelper.showSecondaryMenu();
    }

    public void setSlidingActionBarEnabled(boolean b) {
        mHelper.setSlidingActionBarEnabled(b);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        boolean b = mHelper.onKeyUp(keyCode, event);
        return b || super.onKeyUp(keyCode, event);
    }

    //滑动完全打开菜单后结束掉当前的Activity
    @Override
    public void onOpened() {
        this.finish();
    }
}
