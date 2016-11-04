package zj.health.fjzl.pt.global.base;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.MenuItem;

import zj.baselibrary.base.BaseActivity;
import zj.health.fjzl.pt.global.R;

/**
 * 为不需要fragment子Activity需要继承的base类
 * Created by ucmed on 2016/9/21.
 */

public abstract class MyBaseActivity extends BaseActivity {
    protected boolean isRequestDataRefresh = false;
    protected SwipeRefreshLayout mRefreshLayout;
//    protected Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (hasRefresh()) {//默认true
            setupSwipeRefresh();
        }
//        mToolbar = (Toolbar) findViewById(R.id.toolbar);
//        if (mToolbar != null) {
//            setSupportActionBar(mToolbar); //把Toolbar当做ActionBar给设置
//            if (canBack()) {//默认true
//                ActionBar actionBar = getSupportActionBar();
//                if (actionBar != null)
//                    actionBar.setDisplayHomeAsUpEnabled(true);//设置ActionBar一个返回箭头，主界面没有，次级界面有
//            }
//        }
    }

    /**
     * 判断当前 Activity 是否允许返回
     * 主界面不允许返回，次级界面允许返回
     * 不需要就重写返回false
     *
     * @return true
     */
    public boolean canBack() {
        return true;
    }

    /**
     * 判断Activity是否需要刷新功能
     * 不需要就重写返回false
     *
     * @return true
     */
    public Boolean hasRefresh(){
        return true;
    }

    /**
     * 设置UI的状态
     * 数据获取结束别忘了setRefresh(false)
     * @param requestDataRefresh
     */
    public void setRefresh(boolean requestDataRefresh) {
        if (mRefreshLayout == null) {
            return;
        }
        if (!requestDataRefresh) {
            isRequestDataRefresh = false;
            mRefreshLayout.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mRefreshLayout != null) {
                        mRefreshLayout.setRefreshing(false);
                    }
                }
            }, 1000);
        } else {
            mRefreshLayout.setRefreshing(true);
        }
    }

    /**
     * 需要Override 调用以refresh
     * 0.设置isRequestDataRefresh
     * 1.UI状态setRefresh(true);
     * 2.获取数据initData(null);
     */
    public void requestDataRefresh() {
        isRequestDataRefresh = true;
    }

    private void setupSwipeRefresh() {
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.mRefreshLayout);
        if (mRefreshLayout != null) {
            mRefreshLayout.setColorSchemeResources(R.color.refresh_progress_1,
                    R.color.refresh_progress_2, R.color.refresh_progress_3);
            mRefreshLayout.setProgressViewOffset(true, 0, (int) TypedValue
                    .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics()));
            mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    requestDataRefresh();
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 此时android.R.id.home即为返回箭头
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}
