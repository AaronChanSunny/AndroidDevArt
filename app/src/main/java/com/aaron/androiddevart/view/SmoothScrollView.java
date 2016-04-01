package com.aaron.androiddevart.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Scroller;
import android.widget.TextView;

/**
 * Created by aaronchan on 16/4/1.
 */
public class SmoothScrollView extends TextView {
    private static final String TAG = SmoothScrollView.class.getSimpleName();

    private Scroller mScroller;

    public SmoothScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mScroller = new Scroller(context);
    }

    public void smoothScrollTo(int destX, int desY) {
        int scrollX = getScrollX();
        int deltaX = destX - scrollX;

        mScroller.startScroll(scrollX, 0, deltaX, 0, 1000);

        invalidate();
    }

    public void smoothScrollTo1(final int destX, int destY) {
        ValueAnimator animator = ValueAnimator.ofFloat(0, 1)
                .setDuration(1000);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float fraction = animation.getAnimatedFraction();
                Log.d(TAG, "onAnimationUpdate, fraction is " + fraction);

                scrollTo((int) (fraction * destX), 0);
            }
        });

        animator.start();
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
    }
}
