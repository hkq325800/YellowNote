package com.cjj.sva.anim.controller;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import com.cjj.sva.anim.JJBaseController;

/**
 * 这是一个神奇的类，searchview释放外面的圈圈，呼吸新鲜空气！
 *
 * Created by androidcjj on 2016/4/2.
 */
public class JJCircleToLineAlphaController extends JJBaseController {
    private String mColor = "#673AB7";
    private int cx, cy, cr;
    private RectF mRectF, mRectFLarge;
    private float tran = 200;
    private static int mGap = 3;

    public JJCircleToLineAlphaController() {
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
        canvas.save();
        canvas.rotate(45, mRectF.centerX(), mRectF.centerY());
        canvas.drawLine(mRectF.centerX() + cr, mRectF.centerY(), mRectF.centerX() + (cr * 2), mRectF.centerY(), paint);
        canvas.restore();
        canvas.drawArc(mRectF, 0, 360, false, paint);
        canvas.drawArc(mRectFLarge, 90, -360 * (1 - mPro), false, paint);
        if (mPro >= 0.4f && mPro < 1.0f) {
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
        cx = getWidth() / 2;
        cy = getHeight() / 2;
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
        tran = getWidth() / 2 - mRectF.width() * 2;
    }

    @Override
    public void resetAnim() {
        if (mState == STATE_ANIM_STOP) return;
        mState = STATE_ANIM_STOP;
        startSearchViewAnim();
    }
}