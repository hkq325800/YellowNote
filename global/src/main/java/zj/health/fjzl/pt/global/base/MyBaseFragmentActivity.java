package zj.health.fjzl.pt.global.base;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.TypedValue;

import zj.baselibrary.base.BaseFragmentActivity;
import zj.health.fjzl.pt.global.R;

/**
 * Created by ucmed on 2016/9/29.
 */

public abstract class MyBaseFragmentActivity extends BaseFragmentActivity {
    protected boolean isRequestDataRefresh = false;
    protected SwipeRefreshLayout mRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (hasRefresh()) {//默认true
            setupSwipeRefresh();
        }
    }

    /**
     * 判断Activity是否需要刷新功能
     * 不需要就重写返回false
     *
     * @return true
     */
    public Boolean hasRefresh() {
        return true;
    }

    /**
     * 需要重写
     */
    public void requestDataRefresh() {
        isRequestDataRefresh = true;
    }

    /**
     * 调用以refresh
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
}
