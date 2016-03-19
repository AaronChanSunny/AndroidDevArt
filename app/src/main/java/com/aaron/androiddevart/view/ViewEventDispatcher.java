package com.aaron.androiddevart.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;

/**
 * Created by Git on 2016/3/19.
 */
public class ViewEventDispatcher extends ViewGroup {
    private static final String TAG = ViewEventDispatcher.class.getSimpleName();

    public ViewEventDispatcher(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        Log.d(TAG, "onInterceptTouchEvent");
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        Log.d(TAG, "onTouchEvent, action is " + action);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }
}
