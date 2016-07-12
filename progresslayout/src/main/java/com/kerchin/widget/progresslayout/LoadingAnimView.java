package com.kerchin.widget.progresslayout;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * Created by Administrator on 2016/4/25 0025.
 * More Code on hkq325800@163.com
 */
public class LoadingAnimView extends View {
    public static final byte typeLoading = 0;
    public static final byte typeGuide = 1;
    static final double trans = 180 / Math.PI;// value()/trans = PI/2、PI、3PI/2
    Context context;
    Point[] point;
    ValueAnimator[] valueAnimator;
    byte type = typeLoading;// view的类型
    int size = 3;// 小点的数量
    int distance = 80;// 每两点间的距离
    int radius = 18;// 小点半径
    int limitTimes = 30;// 最多重复次数
    Paint paint;// loading画笔，聚焦画笔
    boolean isInit = false;// 用于初始化centreX和centreY
    double centreX, centreY;// 控件中心距离

    long duration = 800;// 完成一次波浪的时间 用于loading
    int repeatTimes;// 重复次数 用于loading
    boolean isStart = false;// 防止多次启动动画 用于loading
    int baseOffset = 80;// 基础上升高度 总的一半 用于loading

    Paint paintUnFocus;// 未聚焦画笔 用于guide
    int focusPosition = 0;// 聚焦位置用于guide

    public LoadingAnimView(Context context, AttributeSet attrs,
                           int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initPaint();
    }

    public LoadingAnimView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initPaint();
    }

    public LoadingAnimView(Context context) {
        super(context);
        this.context = context;
        initPaint();
    }

    private void initPaint() {
        valueAnimator = new ValueAnimator[size];
        // 未聚焦画笔初始化
        paintUnFocus = new Paint();
        paintUnFocus.setColor(getResources().getColor(R.color.color_unfocus)); // 设置画笔的颜色
        paintUnFocus.setStyle(Paint.Style.FILL); // 设置空心
        paintUnFocus.setAntiAlias(true); // 消除锯齿
        // 聚焦画笔初始化
        paint = new Paint();
        paint.setColor(context.getResources().getColor(R.color.color_focus)); // 设置画笔的颜色
        paint.setStyle(Paint.Style.FILL); // 设置空心
        paint.setAntiAlias(true); // 消除锯齿
    }

    public void init() {
        repeatTimes = 1;
        point = new Point[size];
        // 初始化点的半径
        for (int i = 0; i < size; i++) {
            point[i] = new Point();
            point[i].r = radius;
        }
        centreX = getWidth() / 2;
        centreY = getHeight() / 2;
        initPoint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!isInit && getHeight() != 0) {
            init();
            paint.setStrokeWidth(2);
            isInit = true;
        }
        if (type == 0)
            for (int i = 0; i < size; i++) {
                // if (i == 0)
                // Log.d("point[0].y", point[0].y + "");
                canvas.drawCircle(Float.parseFloat(point[i].x + ""),
                        Float.parseFloat(point[i].y + ""), point[i].r, paint);
            }
        else if (type == 1) {
            for (int i = 0; i < size; i++) {
                canvas.drawCircle(Float.parseFloat(point[i].x + ""),
                        Float.parseFloat(point[i].y + ""), point[i].r,
                        i == focusPosition ? paint : paintUnFocus);
            }
        }
        super.onDraw(canvas);
    }

    private void initPoint() {
        for (int i = 0; i < size; i++) {
            point[i].y = centreY;
        }
        if (size % 2 == 0) {
            final int centerA = size / 2;// 中心后面那个数
            final int centerB = size / 2 - 1;// 中心前面那个数
            for (int i = 0; i < size; i++) {
                if (i == centerA) {
                    point[i].x = centreX + distance / 2;
                } else if (i == centerB) {
                    point[i].x = centreX - distance / 2;
                } else {
                    int juli = i - centerA;
                    point[i].x = centreX + distance / 2 + distance * juli;
                }
            }
        } else {
            final int center = (size - 1) / 2;
            for (int i = 0; i < size; i++) {
                if (i != center) {
                    int juli = i - center;
                    point[i].x = centreX + juli * distance;
                } else {
                    point[center].x = centreX;
                }
            }
        }
        for (int i = 0; i < size; i++) {
            point[i].offset = 0;
        }
    }

    /**
     * 停止loading动画
     */
    public void stopRotateAnimation() {
        for (int i = 0; i < size; i++) {
            if (valueAnimator[i] != null)
                valueAnimator[i].removeAllUpdateListeners();//.cancel();//.end();
        }
        init();
        isStart = false;
    }

    /**
     * 开始loading动画
     */
    public void startRotateAnimation() {
        // 补齐不足的数量
        if (size > valueAnimator.length) {
            valueAnimator = new ValueAnimator[size];
        }
        repeatTimes = 1;
        if (!isStart) {
            isStart = true;
            for (int i = 0; i < size; i++) {
                final int j = i;
                valueAnimator[i] = ValueAnimator.ofInt(0, 360);
                valueAnimator[i].setInterpolator(new LinearInterpolator());
                // valueAnimator[i].setRepeatMode(ValueAnimator.RESTART);
                // valueAnimator[i].setRepeatCount(50);
                valueAnimator[i].setDuration(duration);
//                ValueAnimator.setFrameDelay(50);
                valueAnimator[i]
                        .addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(
                                    ValueAnimator animation) {
                                if (isInit) {
                                    int value = (Integer) animation
                                            .getAnimatedValue();
                                    double piValue = value / trans;
                                    point[j].offset = baseOffset
                                            * Math.sin(piValue);
                                    point[j].y = centreY - point[j].offset;
                                    invalidate();
                                }
                            }
                        });
            }
            for (int i = 1; i < size; i++) {
                valueAnimator[i].setStartDelay(125 * i);
            }
            for (int i = 0; i < size; i++) {
                valueAnimator[i].start();
            }
        }
    }

    /**
     * guide类型入口
     *
     * @param focusPosition 焦点位置
     * @param focusColor    焦点颜色
     * @param unFocusColor  非焦点颜色
     */
    public void focusPoint(int focusPosition, int focusColor, int unFocusColor) {
        paintUnFocus.setColor(getResources().getColor(unFocusColor));
        paint.setColor(getResources().getColor(focusColor));
        this.focusPosition = focusPosition;
        invalidate();
    }

    /**
     * 设置点的个数
     *
     * @param size 点的个数
     */
    public void setSize(int size) {
        if (size < 1) {
            this.size = 3;
        } else {
            this.size = size;
        }
    }

    /**
     * 设置每两点间的距离
     *
     * @param distance xxdp
     */
    public void setDistance(int distance) {
        this.distance = dip2px(context, distance);
    }

    /**
     * 设置半径
     *
     * @param radius xxdp
     */
    public void setRadius(int radius) {
        this.radius = dip2px(context, radius);
        // for (int i = 0; i < size; i++) {
        // point[i].r = radius;
        // }
    }

    /**
     * 设置类型
     *
     * @param type typeLoading typeGuide
     */
    public void setType(byte type) {
        this.type = type;
    }

    class Point {
        double x;
        int r;
        double y;
        /**
         * 点距离水平线的偏移量
         */
        double offset;

    }


    /**
     * 根据手机的分辨率从 dp(像素) 的单位 转成为 px
     */
    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) ((dipValue - 0.5f) * scale);
    }
}
