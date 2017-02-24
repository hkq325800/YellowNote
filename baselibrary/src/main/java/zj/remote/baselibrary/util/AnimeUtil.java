package zj.remote.baselibrary.util;

import android.animation.ValueAnimator;
import android.content.Context;
import android.view.View;

/**
 * Created by hkq325800 on 2017/2/24.
 */

public class AnimeUtil{

        /**
         * @param isExpand
         * @param view
         * @param height 130
         * @param duration 200
         */
        public static void runAnimator(final boolean isExpand, final View view, final int height, final int duration) {
            ValueAnimator valueAnimator;
            if (isExpand) {
                valueAnimator = ValueAnimator.ofFloat(0, height);
                view.setVisibility(View.VISIBLE);
            } else {
                valueAnimator = ValueAnimator.ofFloat(height, 0);
                view.setVisibility(View.INVISIBLE);
            }
            valueAnimator.setDuration(duration);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    view.getLayoutParams().height =
                            ((Float) animation.getAnimatedValue()).intValue();
                    view.requestLayout();
                }
            });
            valueAnimator.start();
        }

    /**
     * @param context
     * @param isExpand
     * @param view
     * @param height 130
     * @param duration 200
     */
    public static void runAnimator(Context context, final boolean isExpand, final View view, final int height, final int duration) {
        ValueAnimator valueAnimator;
        if (isExpand) {
            valueAnimator = ValueAnimator.ofFloat(0, DisplayUtil.dp2px(context, height));
            view.setVisibility(View.VISIBLE);
        } else {
            valueAnimator = ValueAnimator.ofFloat(DisplayUtil.dp2px(context, height), 0);
            view.setVisibility(View.INVISIBLE);
        }
        valueAnimator.setDuration(duration);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                view.getLayoutParams().height =
                        ((Float) animation.getAnimatedValue()).intValue();
                view.requestLayout();
            }
        });
        valueAnimator.start();
    }
}
