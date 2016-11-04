package zj.health.fjzl.pt.global.base;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import zj.baselibrary.base.BaseFragment;
import zj.health.fjzl.pt.global.R;

/**
 * Created by ucmed on 2016/9/21.
 */

public abstract class MyBaseFragment extends BaseFragment {
    protected boolean isRequestDataRefresh = false;
    protected SwipeRefreshLayout mRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        if (hasRefresh()) {
            setupSwipeRefresh(rootView);
        }
        return rootView;
    }

    /**
     * 判断是否需要刷新功能
     * 不需要就重写返回false
     *
     * @return true
     */
    public Boolean hasRefresh() {
        return true;
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

    /**
     * 需要Override 调用以refresh
     * 0.设置isRequestDataRefresh
     * 1.UI状态setRefresh(true);
     * 2.获取数据initData(null);
     */
    public void requestDataRefresh() {
        isRequestDataRefresh = true;
    }

    private void setupSwipeRefresh(View view) {
        mRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.mRefreshLayout);
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
