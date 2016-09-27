package zj.baselibrary.base;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;
import com.badoo.mobile.util.WeakHandler;

import butterknife.ButterKnife;

/**
 * Created by ucmed on 2016/9/19.
 */
public abstract class BaseFragment extends Fragment {
    protected final static String TAG = BaseFragment.class.getCanonicalName();
    public MaterialDialog dialog;
    protected WeakHandler handler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new WeakHandler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                return initializeCallback(msg);
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(provideContentViewId(), container, false);
        ButterKnife.bind(this, rootView);
        initializeView(rootView);
        initializeData(savedInstanceState);
        initializeEvent(savedInstanceState);
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    protected abstract int provideContentViewId();

    /**
     * 控件初始化
     * setContentView ButterKnife.bind(this);
     *
     * @param rootView
     */
    protected abstract void initializeView(View rootView);

    /**
     * 数据初始化
     * 对proxy的调用
     *
     * @param savedInstanceState
     */
    protected abstract void initializeData(Bundle savedInstanceState);

    /**
     * 事件初始化
     * 或可用ButterKnife的@OnClick替代
     *
     * @param savedInstanceState
     */
    protected abstract void initializeEvent(Bundle savedInstanceState);

    /**
     * 回调初始化
     * 自带handler 初始化Message时记得用Message.obtain()
     *
     * @param msg
     * @return True if no further handling is desired
     */
    protected abstract boolean initializeCallback(Message msg);
}
