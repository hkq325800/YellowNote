package com.kerchin.yellownote.widget;


import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

import com.kerchin.yellownote.utilities.Trace;

/**
 * Created by Kerchin on 2016/3/14 0014.
 */
public class MyScrollView extends ScrollView {
    private OnScrollListener onScrollListener;
    /**
     * 主要是用在用户手指离开MyScrollView，MyScrollView还在继续滑动，我们用来保存Y的距离，然后做比较
     */
    private int lastScrollY;

    public MyScrollView(Context context) {
        this(context, null);
    }

    public MyScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * 设置滚动接口
     *
     * @param onScrollListener
     */
    public void setOnScrollListener(OnScrollListener onScrollListener) {
        this.onScrollListener = onScrollListener;
    }


    /**
     * 用于用户手指离开MyScrollView的时候获取MyScrollView滚动的Y距离，然后回调给onScroll方法中
     */
//    private Handler handler = new Handler() {
//
//        public void handleMessage(android.os.Message msg) {
//            int scrollY = MyScrollView.this.getScrollY();
//            //此时的距离和记录下的距离不相等，在隔5毫秒给handler发送消息
//            if (lastScrollY != scrollY) {
//                lastScrollY = scrollY;
//                handler.sendMessageDelayed(handler.obtainMessage(), 5);
//            }
//            if (onScrollListener != null) {
//                onScrollListener.onScroll(scrollY);
//            }
//        }
//    };

    @Override
    protected void onScrollChanged(int mScrollX, int mScrollY, int oldX, int oldY) {
        super.onScrollChanged(mScrollX, mScrollY, oldX, oldY);
//        Trace.d("mScrollY" + mScrollY + "oldY" + oldY);
        if (onScrollListener != null) {
            onScrollListener.onScroll(mScrollY, oldY);
        }
    }

    /**
     * 重写onTouchEvent， 当用户的手在MyScrollView上面的时候，
     * 直接将MyScrollView滑动的Y方向距离回调给onScroll方法中，当用户抬起手的时候，
     * MyScrollView可能还在滑动，所以当用户抬起手我们隔5毫秒给handler发送消息，在handler处理
     * MyScrollView滑动的距离
     */
    int downY, upY;
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
//        if (onScrollListener != null) {
//            onScrollListener.onScroll(lastScrollY = this.getScrollY());
//        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_UP:
//                handler.sendMessageDelayed(handler.obtainMessage(), 5);
                upY = getScrollY();
                Trace.d("upY"+upY+"downY"+downY);
                if(upY==0&&onScrollListener != null){
                    onScrollListener.onScrollTop();
                }
                break;
            case MotionEvent.ACTION_DOWN:
                downY = getScrollY();
                break;
        }
        return super.onTouchEvent(ev);
    }


    /**
     * 滚动的回调接口
     *
     * @author xiaanming
     */
    public interface OnScrollListener {
        /**
         * 回调方法， 返回MyScrollView滑动的Y方向距离
         *
         * @param mScrollY 当前
         * @param oldY 过去
         */
        public void onScroll(int mScrollY, int oldY);

        public void onScrollTop();
    }


}
