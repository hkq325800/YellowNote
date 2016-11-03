package zj.health.fjzl.pt.global.base;

import android.os.Bundle;
import android.support.v4.view.ViewPager;

import butterknife.BindView;
import zj.health.fjzl.pt.base.base.MyFragmentPagerAdapter;

import zj.health.fjzl.pt.global.R;

/**
 * 只要是有ViewPager的Activity就可以用
 * 但ViewPager的id必须为mViewPager
 * Created by ucmed on 2016/9/21.
 */

public abstract class MyBaseFragmentActivityVP extends MyBaseFragmentActivity {
    public MyFragmentPagerAdapter adapter;
    public ViewPager mViewPager;

    @Override
    protected void initView(Bundle savedInstanceState) {
        mViewPager = (ViewPager) findViewById(R.id.mViewPager);
        mViewPager.setOffscreenPageLimit(fragments.size());
        adapter = new MyFragmentPagerAdapter(
                getSupportFragmentManager(), fragments);
        mViewPager.setAdapter(adapter);
//        mViewPager.setPageTransformer(true, new DepthPageTransformer());
    }

    /**
     * 如果需要替换viewPager或者tabLayout的监听 单个的建议继承超类然后重新设置两个都需要替换则不必继承超类
     *
     * @param savedInstanceState
     */
    @Override
    protected void initEvent(Bundle savedInstanceState) {
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                lastTabIndex = nextTabIndex;
                nextTabIndex = position;
                onViewPagerSelected(position);
//                mTabLayout.setCurrentTab(position);
////                mMainNavigation.selectTab(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    protected abstract void onViewPagerSelected(int position);
}
