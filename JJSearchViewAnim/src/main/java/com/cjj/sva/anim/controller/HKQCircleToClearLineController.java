package com.cjj.sva.anim.controller;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import com.cjj.sva.anim.JJBaseController;

/**
 * Created by Kerchin on 2016/4/25 0025.
 * 稍微修改了一下JJCircleToLineAlphaController
 * 使得搜索突变变成了叉叉
 */
public class HKQCircleToClearLineController extends JJBaseController {
    private String mColor = "#673AB7";
    private int cx, cy, cr;
    private RectF mRectF, mRectFLarge;
    private float tran = 200;
    private static float mGap = 2.5f;
    private int widthRatio = 2;//用于高度固定 长度match_parent的场景

    public HKQCircleToClearLineController() {
        mRectF = new RectF();
        mRectFLarge = new RectF();
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
//        canvas.drawColor(Color.parseColor(mColor));
        switch (mState) {
            case STATE_ANIM_NONE:
                drawNormalView(paint, canvas);
                break;
            case STATE_ANIM_START:
                drawStartAnimView(paint, canvas);
                break;
            case STATE_ANIM_STOP:
                drawStopAnimView(paint, canvas);
                break;
        }
    }

    private void drawStopAnimView(Paint paint, Canvas canvas) {
        canvas.save();
        if (mPro > 0.2) {
            paint.setAlpha((int) (mPro * 255));
            drawNormalView(paint, canvas);
        }
        canvas.restore();
    }

    private void drawStartAnimView(Paint paint, Canvas canvas) {
        canvas.save();//只讲旋转应用在线上
        canvas.rotate(45, mRectF.centerX(), mRectF.centerY());//旋转
        //先画横线先把横线缩短
        canvas.drawLine(mRectF.centerX() + cr
                , mRectF.centerY()
                , mRectF.centerX() + cr + (cr * (1 - mPro))
                , mRectF.centerY(), paint);
        //交叉中的横线
        if (mPro >= 0.5f)
            //再画交叉中的竖线后0.5mPro才开始画
            if (mPro >= 0.5f && mPro < 1.0f) {
                canvas.drawLine(mRectF.centerX()
                        , mRectF.centerY() - (cr * 2 * (mPro - 0.5f))
                        , mRectF.centerX()
                        , mRectF.centerY() + (cr * 2 * (mPro - 0.5f)), paint);
                canvas.drawLine(mRectF.centerX() - cr * 2 * (mPro - 0.5f)
                        , mRectF.centerY()
                        , mRectF.centerX() + (cr * 2 * (mPro - 0.5f))
                        , mRectF.centerY(), paint);
            } else if (mPro == 1.0f) {
                canvas.drawLine(mRectF.centerX()
                        , mRectF.centerY() - cr
                        , mRectF.centerX()
                        , mRectF.centerY() + cr, paint);
                canvas.drawLine(mRectF.centerX() - cr
                        , mRectF.centerY()
                        , mRectF.centerX() + cr
                        , mRectF.centerY(), paint);
            }
        if (mPro != 1.0f)
            canvas.drawArc(mRectF, 0, (360) * (1 - mPro), false, paint);//圆
        canvas.restore();
//        RectF mRectFCircle = new RectF(mRectF);
//        mRectFCircle.top -= 10;
//        mRectFCircle.left -= 10;
//        mRectFCircle.right += 10;
//        mRectFCircle.bottom += 10;
        canvas.drawArc(mRectFLarge, 90, -360 * (1 - mPro), false, paint);//圆外的线随时间逐渐缩短
        if (mPro >= 0f && mPro < 1.0f) {
            canvas.drawLine(mRectFLarge.left * (1 - mPro) + mRectF.width(), mRectFLarge.bottom,
                    (mRectFLarge.right - mGap * cr), mRectFLarge.bottom, paint);
        }
        if (mPro == 1.0f) {
            canvas.drawLine(mRectFLarge.left * (1 - mPro) + mRectF.width(), mRectFLarge.bottom,
                    mRectF.left, mRectFLarge.bottom, paint);
        }
        mRectF.left = cx - cr + tran * mPro;
        mRectF.right = cx + cr + tran * mPro;
        mRectFLarge.left = cx - mGap * cr + tran * mPro;
        mRectFLarge.right = cx + mGap * cr + tran * mPro;
    }

    private void drawNormalView(Paint paint, Canvas canvas) {
        cr = getWidth() / 30;
        cx = getWidth() / widthRatio;//居中
        cy = getHeight() / 2;//居中
        mRectF.left = cx - cr;
        mRectF.right = cx + cr;
        mRectF.top = cy - cr;
        mRectF.bottom = cy + cr;

        mRectFLarge.left = cx - mGap * cr;
        mRectFLarge.right = cx + mGap * cr;
        mRectFLarge.top = cy - mGap * cr;
        mRectFLarge.bottom = cy + mGap * cr;

        canvas.save();
        paint.reset();
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(4);
        paint.setStyle(Paint.Style.STROKE);

        canvas.rotate(45, cx, cy);
        canvas.drawLine(cx + cr, cy, cx + cr * 2, cy, paint);
        canvas.drawArc(mRectF, 0, 360, false, paint);
        canvas.drawArc(mRectFLarge, 0, 360, false, paint);
        canvas.restore();
    }

    @Override
    public void startAnim() {
        if (mState == STATE_ANIM_START) return;
        mState = STATE_ANIM_START;
        startSearchViewAnim();
        tran = getWidth() / widthRatio * (widthRatio - 1) - mRectF.width() * 2;
    }

    @Override
    public void resetAnim() {
        if (mState == STATE_ANIM_STOP) return;
        mState = STATE_ANIM_STOP;
        startSearchViewAnim();
    }
}
