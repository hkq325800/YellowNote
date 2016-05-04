package com.kerchin.yellownote.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kerchin.yellownote.R;
import com.kerchin.yellownote.utilities.Trace;

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
    private byte reachDelayStatus;
    private static final byte statusNoneReach = -1;//没有过任何调用的初始态
    private static final byte statusReachStill = 0;//没有过任何调用的初始态
    private static final byte statusReachEnd = 1;//没有过任何调用的初始态

    private volatile byte status;
    private static final byte statusNoneAnim = -1;//没有过任何调用的初始态
    private static final byte statusStart = 0;//界面调用了dismiss
    //    private static final byte statusStillAnim = 1;//数据还没加载好 继续动画
//    private static final byte statusEndAnim = 2;//数据加载好了且快于avoidBlockDelay 保持最少1000ms的动画
//    private static final byte statusDismiss = 3;//界面调用了dismiss
    private static final byte statusShowList = 4;//界面调用了显示列表
    private static final byte statusShowNoData = 5;//界面调用了没有数据
    private static final byte statusShowRefresh = 6;//界面调用了需要刷新

    private static final long showDuration = 400;//内部控件显示的延迟
    private static final long hideDuration = 200;//
    private static final long avoidBlockDelay = 450;//界面需要准备时间否则动画在一开始会卡顿
    private View[] views;//init

    public ProgressLayout(Context context, AttributeSet attrs,
                          int defStyle) {
        super(context, attrs, defStyle);
        initializeView(context);
    }

    public ProgressLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeView(context);
    }

    public ProgressLayout(Context context) {
        super(context);
        initializeView(context);
    }

    private void initializeView(Context context) {
        status = statusNoneAnim;
        reachDelayStatus = statusNoneReach;
        LayoutInflater.from(context).inflate(R.layout.widget_loading, this);
        mLoadingAnim = (LoadingAnimView) findViewById(R.id.mLoadingAnim);
        mNoDataImg = (ImageView) findViewById(R.id.mNoDataImg);
        mNoDataTxt = (TextView) findViewById(R.id.mNoDataTxt);
        mNoDataBtn = (Button) findViewById(R.id.mNoDataBtn);
    }

    /**
     * 初始化将来需要隐藏的View
     * first
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
     * second
     */
    public void startProgress(boolean isFirst) {
//        if (status >= statusStart)
//            return;
        dismissNoData();
        status = statusStart;
        timeStart = System.currentTimeMillis();
        Trace.d("ProgressRelativeLayout startProgress" + timeStart);
        mLoadingAnim.postDelayed(new Runnable() {
            @Override
            public void run() {
                long ex = timeDismiss - timeStart;
                Trace.d("showLoadingAnim postDelayed" + System.currentTimeMillis() + " ex" + ex);
                if (ex < 0) {//数据还没加载好
                    reachDelayStatus = statusReachStill;
                    status = statusNoneAnim;
                    timeDismiss = 0;
                    Trace.d("stillAnim" + reachDelayStatus);
                } else {//数据加载好了且快于avoidBlockDelay 保持最少1200ms的动画
//                    status = statusEndAnim;
                    reachDelayStatus = statusReachEnd;
                    Trace.d("endAnim" + reachDelayStatus);
                    mLoadingAnim.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (status == statusShowList)
                                showListView(views);
                            else if (status == statusShowNoData)
                                showNoData(noDataText, noDataInterface);
                            timeDismiss = 0;
                            status = statusNoneAnim;
                        }
                    }, 1200);
                }
                showSpecificView(mLoadingAnim, showDuration);
                mLoadingAnim.startRotateAnimation();
            }
        }, isFirst ? avoidBlockDelay : 300);
    }

    /**
     * 隐藏过场
     */
    public void dismiss() {
        Trace.d("dismiss" + timeDismiss);
        hideAlphaView(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoadingAnim.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mLoadingAnim.stopRotateAnimation();
                    }
                }, hideDuration * 5);
            }
        }, mLoadingAnim);
        reachDelayStatus = statusNoneReach;
//            dismissNoData();
    }

    /**
     * 可见设置、alpha动画设置
     * fourth
     *
     * @param views
     */
    public void showListView(View... views) {
        Trace.d("showListView" + reachDelayStatus);
        status = statusShowList;
        if (reachDelayStatus > statusNoneReach) {
            dismiss();
            for (View view : views) {
                showSpecificView(view, listViewShowDuration);
            }
        } else
            timeDismiss = System.currentTimeMillis();
        dismissNoData();
    }

    String noDataText;
    OnClickListener noDataInterface;

    /**
     * 只显示没有数据的文字
     * fourth
     *
     * @param text 无数据时的文字
     */
    public void showNoData(String text, OnClickListener noDataInterface) {
        noDataText = text;
        this.noDataInterface = noDataInterface;
        Trace.d("showNoData" + reachDelayStatus);
        if (reachDelayStatus > statusNoneReach || status == statusNoneAnim) {
            dismiss();
            mNoDataBtn.setVisibility(View.GONE);
            mNoDataTxt.setText(noDataText);
            mNoDataTxt.setVisibility(View.VISIBLE);
            mNoDataTxt.setOnClickListener(this.noDataInterface);
        } else
            timeDismiss = System.currentTimeMillis();
        status = statusShowNoData;
    }

    public void setNoDataImgRes(int id, String text) {
        mNoDataImg.setImageResource(id);
        mNoDataTxt.setText(text);
        showAlphaView(mNoDataTxt);
    }

    /**
     * 出现异常情况下显示刷新
     * fourth
     *
     * @param text
     * @param err
     * @param noDataInterface
     */
    public void showRefresh(String text, String err,
                            OnClickListener noDataInterface) {
        Trace.d("showRefresh");
        status = statusShowRefresh;
        if (reachDelayStatus > statusNoneReach) {
            dismiss();
            Trace.d(err);
            mNoDataImg.setImageResource(R.mipmap.ic_no_network);
            mNoDataTxt.setText(text);
            showAlphaView(mNoDataTxt, mNoDataImg, mNoDataBtn);
            mNoDataBtn.setOnClickListener(noDataInterface);
        } else
            timeDismiss = System.currentTimeMillis();
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
    private void hideAlphaView(AnimatorListenerAdapter listener, View... views) {
        for (final View view : views) {
            view.animate().alpha(0).setDuration(hideDuration)
                    .setListener(listener).start();
        }
    }

    public void showNoDataImg() {
        showAlphaView(mNoDataImg);
    }
}
