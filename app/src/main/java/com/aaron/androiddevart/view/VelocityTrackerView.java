package com.aaron.androiddevart.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.widget.TextView;

/**
 * Created by Aaron on 2016/3/17.
 */
public class VelocityTrackerView extends TextView {
    private static final String TAG = VelocityTrackerView.class.getSimpleName();

    public VelocityTrackerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                return true;
            case MotionEvent.ACTION_MOVE:
                traceMoveVelocity(event);
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    private void traceMoveVelocity(MotionEvent event) {
        VelocityTracker velocityTracker = VelocityTracker.obtain();
        velocityTracker.addMovement(event);

        velocityTracker.computeCurrentVelocity(1000);
        float xV = velocityTracker.getXVelocity();
        float yV = velocityTracker.getYVelocity();
        Log.d(TAG, "trace velocity x is " + xV + ", y is " + yV);

        velocityTracker.recycle();
    }
}
