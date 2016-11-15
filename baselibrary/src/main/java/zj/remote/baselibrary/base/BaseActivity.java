package zj.remote.baselibrary.base;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.ColorRes;
import android.support.v7.app.AppCompatActivity;

import com.afollestad.materialdialogs.MaterialDialog;
import com.badoo.mobile.util.WeakHandler;

import butterknife.ButterKnife;
import zj.remote.baselibrary.R;
import zj.remote.baselibrary.util.Immerge.ImmergeUtils;

/**
 * Created by ucmed on 2016/9/14.
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected final static String TAG = BaseActivity.class.getCanonicalName();
    @ColorRes
    protected int immergeColor = R.color.colorPrimary;
    protected WeakHandler handler;
    public MaterialDialog dialog;
    protected boolean isImmerge = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        doSthBeforeSetView();
        setContentView(provideContentViewId());
        if (isImmerge)
            ImmergeUtils.immerge(this, immergeColor);
        ButterKnife.bind(this);
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

    protected void dismissDialog(){
        if(dialog!=null && dialog.isShowing())
            dialog.dismiss();
    }

//    public static class MyHandler extends Handler {
//        private final WeakReference<Activity> mActivity;
//        private InitializeCallback callback;
//
//        void setCallback(InitializeCallback callback) {
//            this.callback = callback;
//        }
//
//        MyHandler(Activity activity) {
//            mActivity = new WeakReference<Activity>(activity);
//        }
//
//        @Override
//        public void handleMessage(Message msg) {
//            Activity activity = mActivity.get();
//            if (activity != null) {
//                callback.handleMessage(msg);
//            }
//        }
//    }
//
//    public interface InitializeCallback{
//        void handleMessage(Message msg);
//    }

}
