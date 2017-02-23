package zj.remote.baselibrary.base;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.ColorRes;
import android.support.v7.app.AppCompatActivity;

import com.afollestad.materialdialogs.MaterialDialog;
import com.badoo.mobile.util.WeakHandler;

import butterknife.ButterKnife;
import zj.remote.baselibrary.util.Immerge.ImmergeUtils;

/**
 * Created by hkq325800 on 2016/9/14.
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected final static String TAG = BaseActivity.class.getCanonicalName();
    @ColorRes
    protected int immergeColor = 0;
    protected WeakHandler handler;
    public MaterialDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView之前 通常为getIntent获取上层数据及设置immergeColor handler
        doSthBeforeSetView(savedInstanceState);
        //通过provideContentViewId获取R.layout
        setContentView(provideContentViewId());
        //setContentView之后 为了不通过重写initView初始化标题栏 而设置此方法
        doSthAfterSetView();
        //ButterKnife绑定
        ButterKnife.bind(this);
        initView(savedInstanceState);
        initData(savedInstanceState);
        initEvent(savedInstanceState);
    }

    protected void doSthAfterSetView() {
        if (immergeColor != 0)
            ImmergeUtils.immerge(this, immergeColor);
    }

    protected void doSthBeforeSetView(Bundle savedInstanceState) {
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
     *
     * @param savedInstanceState
     */
    protected abstract void initView(Bundle savedInstanceState);

    /**
     * 数据初始化
     * 对proxy的调用 以及后期通过savedInstanceState对重启应用的支持
     *
     * @param savedInstanceState
     */
    protected abstract void initData(Bundle savedInstanceState);

    /**
     * 第三方控件事件初始化
     * 常用控件可用ButterKnife的@OnClick替代
     *
     * @param savedInstanceState
     */
    protected abstract void initEvent(Bundle savedInstanceState);

    protected void dismissDialog() {
        if (dialog != null && dialog.isShowing()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dialog.dismiss();
                }
            });
        }
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
