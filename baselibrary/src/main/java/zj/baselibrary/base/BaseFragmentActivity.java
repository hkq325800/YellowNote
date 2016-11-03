package zj.baselibrary.base;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.ColorRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.badoo.mobile.util.WeakHandler;

import java.util.ArrayList;

import butterknife.ButterKnife;
import zj.baselibrary.R;
import zj.baselibrary.util.Immerge.ImmergeUtils;

/**
 * Created by ucmed on 2016/9/19.
 * 没有toolbar canBack
 */
public abstract class BaseFragmentActivity extends AppCompatActivity {

    public final static String TAG = BaseFragmentActivity.class.getCanonicalName();
    @ColorRes
    protected int immergeColor = R.color.colorPrimary;
    protected ArrayList<Fragment> fragments = new ArrayList<>();
    protected WeakHandler handler;
    protected int lastTabIndex;
    protected int nextTabIndex;

    /**
     * Fragment切换
     *
     * @param resId
     */
    protected void switchFragment(int resId) {

        FragmentTransaction trx = getSupportFragmentManager().beginTransaction();
        trx.hide(fragments.get(lastTabIndex));
        if (!fragments.get(nextTabIndex).isAdded()) {
            trx.add(resId, fragments.get(nextTabIndex));
        }
        trx.show(fragments.get(nextTabIndex)).commit();
        lastTabIndex = nextTabIndex;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        doSthBeforeSetView();
        setContentView(provideContentViewId());
        ImmergeUtils.immerge(this, immergeColor);
        ButterKnife.bind(this);
        initFragments(savedInstanceState);
        initView(savedInstanceState);
        initData(savedInstanceState);
        initEvent(savedInstanceState);
    }

    protected void doSthBeforeSetView() {
        handler = new WeakHandler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                return initCallback(msg);
            }
        });
    }

    /**
     * 回调初始化
     * 自带handler 初始化Message时记得用Message.obtain()
     *
     * @param msg
     * @return True if no further handling is desired
     */
    protected abstract boolean initCallback(Message msg);

    protected abstract int provideContentViewId();//用于引入布局文件

    /**
     * 初始化Fragments
     * 1.获取fragment的newInstance
     * 2.添加到fragments
     * 3.getSupportFragmentManager()
     * .beginTransaction()
     * .add(R.id.container, mHomePageFragment)
     * .show(mHomePageFragment).commit();
     */
    protected abstract void initFragments(Bundle savedInstanceState);

    /**
     * 控件初始化
     * setContentView ButterKnife.bind(this);
     *
     * @param savedInstanceState
     */
    protected abstract void initView(Bundle savedInstanceState);

    /**
     * 数据初始化
     * 对proxy的调用
     *
     * @param savedInstanceState
     */
    protected abstract void initData(Bundle savedInstanceState);

    /**
     * 事件初始化
     * 或可用ButterKnife的@OnClick替代
     *
     * @param savedInstanceState
     */
    protected abstract void initEvent(Bundle savedInstanceState);
}
