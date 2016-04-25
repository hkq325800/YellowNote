package com.kerchin.yellownote.widget;

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
    private static final long listViewShowDuration = 1000;
    private ImageView /*mLoadingImg,*/ mNoDataImg;
    private TextView mNoDataTxt;
    private LoadingAnimView mLoadingAnim;
    private Button mNoDataBtn;
    Activity context;
    private static final long showDuration = 400;
    private static final long hideDuration = 200;
    private static final long avoidBlockDelay = 450;
    private static final int openloadimg = 0;
    private static final int dismiss = 1;

    private SystemHandler defaultHandler = new SystemHandler(this) {

        @Override
        public void handlerMessage(Message msg) {
            switch (msg.what) {
                case openloadimg:
                    context.runOnUiThread(new Runnable() {
                        public void run() {
                            Trace.d("runOnUiThread mLoadingAnim.getAlpha():" + mLoadingAnim.getAlpha());
                            if (mLoadingAnim.getAlpha() != 1) {
                                Trace.d("showLoadingAnim");
                                mLoadingAnim.animate().alpha(1)
                                        .setDuration(showDuration).start();
                                // mLoadingAnim.setVisibility(VISIBLE);
                            }
                            mLoadingAnim.startRotateAnimation();
//                            mLoadingImg.setVisibility(View.GONE);// VISIBLE
                            mNoDataTxt.setVisibility(View.GONE);
                            mNoDataImg.setVisibility(View.GONE);
                            mNoDataBtn.setVisibility(View.GONE);
                        }
                    });
                    break;
                case dismiss:
                    break;
                default:
                    break;
            }
        }
    };

    public ProgressLayout(Context context, AttributeSet attrs,
                          int defStyle) {
        super(context, attrs, defStyle);
        initializeView(context);
        this.context = (Activity) context;
    }

    public ProgressLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeView(context);
        this.context = (Activity) context;
    }

    public ProgressLayout(Context context) {
        super(context);
        initializeView(context);
        this.context = (Activity) context;
    }

    private void initializeView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.widget_loading, this);
        mLoadingAnim = (LoadingAnimView) findViewById(R.id.mLoadingAnim);
//        mLoadingImg = (ImageView) findViewById(R.id.mLoadingImg);
        mNoDataImg = (ImageView) findViewById(R.id.mNoDataImg);
        mNoDataTxt = (TextView) findViewById(R.id.mNoDataTxt);
        mNoDataBtn = (Button) findViewById(R.id.mNoDataBtn);
//        AnimationDrawable animationDrawable = (AnimationDrawable) mLoadingImg
//                .getBackground();
//        animationDrawable.start();
    }

    public void closeloadimg() {
        hideAlphaView(mLoadingAnim);
//        mLoadingImg.setVisibility(View.GONE);

    }

    public void openloadimg() {
        Trace.d("ProgressRelativeLayout openloadimg");
        defaultHandler.sendEmptyMessageDelayed(openloadimg, avoidBlockDelay);
    }

    public void showNoData(String text) {
//        mLoadingImg.setVisibility(View.GONE);
        mNoDataBtn.setVisibility(View.GONE);
        mNoDataTxt.setText(text);
        mNoDataTxt.setVisibility(View.VISIBLE);
    }

    public void dismiss() {
        hideAlphaView(mLoadingAnim);
        mNoDataTxt.setVisibility(View.GONE);
        mNoDataImg.setVisibility(View.GONE);
        mNoDataBtn.setVisibility(View.GONE);
    }

    public void dismissProg() {
        closeloadimg();
        dismiss();
    }

    public void startProg() {
        openloadimg();
    }

    public void showNoDataImg() {
        showAlphaView(mNoDataImg);
    }

    public void setNoDataImgRes(int id, String text) {
        mNoDataImg.setImageResource(id);
        mNoDataTxt.setText(text);
        showAlphaView(mNoDataTxt);
    }

    public void showRefresh(String text, String err,
                            OnClickListener noDataInterface) {
        Trace.d(err);
        mNoDataImg.setImageResource(R.mipmap.ic_no_network);
        mNoDataTxt.setText(text);
        showAlphaView(mNoDataTxt, mNoDataImg, mNoDataBtn);
        mNoDataBtn.setOnClickListener(noDataInterface);
    }

    // 对view进行显示动画
    public synchronized void showAlphaView(View... views) {
        for (View view : views) {
            view.setAlpha(0);
            view.setVisibility(View.VISIBLE);
            view.animate().alpha(1).setDuration(showDuration).start();
        }
    }

    // 对view进行隐藏动画
    public void hideAlphaView(View... views) {
        for (View view : views) {
            view.animate().alpha(0).setDuration(hideDuration).start();
        }
    }

    public void showListView(View... views) {
        for (View view : views) {
            view.setVisibility(View.VISIBLE);
            view.animate().alpha(1).setDuration(listViewShowDuration).start();
        }
    }
}
