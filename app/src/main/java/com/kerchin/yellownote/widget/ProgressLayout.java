package com.kerchin.yellownote.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kerchin.yellownote.R;
import com.kerchin.yellownote.utilities.SystemHandler;
import com.kerchin.yellownote.utilities.Trace;
import com.kerchin.yellownote.widget.waterdrop.WaterDropListView;

/**
 * Activity
 * 1.setContentView:isprogcess = true;
 * 2.initialView:initProcess(mListView);
 * 3.handler:dismissProg();+showListView(mListView);
 * Fragment
 * 1.initProcess(view,mListView);
 * 2.handler:dismissProg();+showListView(mListView);
 *
 * @author HKQ
 */
public class ProgressLayout extends RelativeLayout {
    private static final long listViewShowDuration = 1000;//listView显示的延迟
    private ImageView mNoDataImg;
    private TextView mNoDataTxt;
    private LoadingAnimView mLoadingAnim;
    private Button mNoDataBtn;
    private long timeStart;
    private long timeDismiss;
    private Context context;
    private volatile byte status;
    private static final byte statusNoneAnim = -1;//没有过任何调用的初始态
    private static final byte statusStart = 0;//界面调用了dismiss
    private static final byte statusStillAnim = 1;//数据还没加载好 继续动画
    private static final byte statusEndAnim = 2;//数据加载好了且快于avoidBlockDelay 保持最少1000ms的动画
    private static final byte statusDismiss = 3;//界面调用了dismiss
    private static final byte statusShowList = 4;//界面调用了显示列表
    private static final byte statusShowNoData = 5;
    private static final byte statusShowRefresh = 6;

    private static final long showDuration = 400;//内部控件显示的延迟
    private static final long hideDuration = 200;//
    private static final long avoidBlockDelay = 450;//界面需要准备时间否则动画在一开始会卡顿
    private View[] views;//init

    public ProgressLayout(Context context, AttributeSet attrs,
                          int defStyle) {
        super(context, attrs, defStyle);
        initializeView(context);
        this.context = context;
    }

    public ProgressLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeView(context);
        this.context = context;
    }

    public ProgressLayout(Context context) {
        super(context);
        initializeView(context);
        this.context = context;
    }

    private void initializeView(Context context) {
        status = statusNoneAnim;
        LayoutInflater.from(context).inflate(R.layout.widget_loading, this);
        mLoadingAnim = (LoadingAnimView) findViewById(R.id.mLoadingAnim);
        mNoDataImg = (ImageView) findViewById(R.id.mNoDataImg);
        mNoDataTxt = (TextView) findViewById(R.id.mNoDataTxt);
        mNoDataBtn = (Button) findViewById(R.id.mNoDataBtn);
    }

    /**
     * 初始化将来需要隐藏的View
     *
     * @param views
     */
    public void initListView(View... views) {
        this.views = views;
        for (View view : views) {
            view.setVisibility(INVISIBLE);
        }
    }

    /**
     * 开启过场
     */
    public void startProgress() {
        status = statusStart;
        timeStart = System.currentTimeMillis();
        Trace.d("ProgressRelativeLayout startProgress" + timeStart);
        mLoadingAnim.postDelayed(new Runnable() {
            @Override
            public void run() {
                long ex = timeDismiss - timeStart;
                Trace.d("showLoadingAnim postDelayed" + System.currentTimeMillis() + " ex" + ex);
                if (ex < 0) {//数据还没加载好
                    status = statusStillAnim;
                    Trace.d("stillAnim");
                    showSpecificView(mLoadingAnim, showDuration);
                    mLoadingAnim.startRotateAnimation();
                } else {//数据加载好了且快于avoidBlockDelay 保持最少1200ms的动画
                    status = statusEndAnim;
                    Trace.d("endAnim");
                    showSpecificView(mLoadingAnim, showDuration);
                    mLoadingAnim.startRotateAnimation();
                    mLoadingAnim.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            dismiss();
                            showListView(views);
                        }
                    }, 1200);
                }
            }
        }, avoidBlockDelay);
    }

    /**
     * 隐藏过场
     */
    public void dismiss() {
        if (timeDismiss == 0L) {
            timeDismiss = System.currentTimeMillis();
            Trace.d("dismiss" + timeDismiss);
        }
        if (status > statusStart) {//已经运行到delay之后 ex<0
            status = statusDismiss;
            hideAlphaView(mLoadingAnim);
//            dismissNoData();
        }
    }

    /**
     * 可见设置、alpha动画设置
     *
     * @param views
     */
    public void showListView(View... views) {
        Trace.d("showListView");
        if (status >= statusDismiss) {
            status = statusShowList;
            for (View view : views) {
                showSpecificView(view, listViewShowDuration);
            }
        }
    }

    /**
     * 只显示没有数据的文字
     *
     * @param text 无数据时的文字
     */
    public void showNoData(String text, OnClickListener noDataInterface) {
        Trace.d("showNoData");
        if (status >= statusDismiss) {
            status = statusShowNoData;
            mNoDataBtn.setVisibility(View.GONE);
            mNoDataTxt.setText(text);
            mNoDataTxt.setVisibility(View.VISIBLE);
            mNoDataTxt.setOnClickListener(noDataInterface);
        }
    }

    /**
     * 出现异常情况下显示刷新
     *
     * @param text
     * @param err
     * @param noDataInterface
     */
    public void showRefresh(String text, String err,
                            OnClickListener noDataInterface) {
        Trace.d("showRefresh");
        if (status >= statusDismiss) {
            status = statusShowRefresh;
            Trace.d(err);
            mNoDataImg.setImageResource(R.mipmap.ic_no_network);
            mNoDataTxt.setText(text);
            showAlphaView(mNoDataTxt, mNoDataImg, mNoDataBtn);
            mNoDataBtn.setOnClickListener(noDataInterface);
        }
    }

    /**
     * 隐藏无数据时的提示
     */
    public void dismissNoData() {
        mNoDataTxt.setVisibility(View.GONE);
        mNoDataImg.setVisibility(View.GONE);
        mNoDataBtn.setVisibility(View.GONE);
    }

    /**
     * 对特定的View进行显示动画
     *
     * @param view
     * @param duration
     */
    private void showSpecificView(final View view, long duration) {
        view.setAlpha(0);
        view.setVisibility(View.VISIBLE);
//        if (duration > 1000)
//            view.animate().alpha(1).setDuration(duration)
//                    .setListener(new AnimatorListenerAdapter() {
//                        @Override
//                        public void onAnimationEnd(Animator animation) {
//                            view.setVisibility(GONE);
//                        }
//                    }).start();
//        else
        view.animate().alpha(1).setDuration(duration).start();
    }

    /**
     * 对view进行显示动画
     *
     * @param views
     */
    private void showAlphaView(View... views) {
        for (View view : views) {
            showSpecificView(view, showDuration);
        }
    }

    // 对view进行隐藏动画
    private void hideAlphaView(View... views) {
        for (final View view : views) {
            view.animate().alpha(0).setDuration(hideDuration).start();
        }
    }

    public void showNoDataImg() {
        showAlphaView(mNoDataImg);
    }

    public void setNoDataImgRes(int id, String text) {
        mNoDataImg.setImageResource(id);
        mNoDataTxt.setText(text);
        showAlphaView(mNoDataTxt);
    }
}
