package com.aaron.androiddevart.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * Created by Git on 2016/3/18.
 */
public class ScrollEnabledView1 extends TextView {
    private int mLastX;
    private int mLastY;

    public ScrollEnabledView1(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getRawX();
        int y = (int) event.getRawY();

        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                return true;
            case MotionEvent.ACTION_MOVE:
                int deltaX = x - mLastX;
                int deltaY = y - mLastY;

                animate().translationXBy(deltaX).translationYBy(deltaY).start();
                return true;
            default:
                break;
        }

        mLastX = x;
        mLastY = y;
        return super.onTouchEvent(event);
    }
}
