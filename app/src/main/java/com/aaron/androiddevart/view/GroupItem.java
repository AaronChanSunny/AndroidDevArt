package com.aaron.androiddevart.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * Created by Git on 2016/3/19.
 */
public class GroupItem extends TextView {
    private static final String TAG = GroupItem.class.getSimpleName();

    public GroupItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GroupItem(Context context) {
        super(context);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        Log.d(TAG, "dispatchTouchEvent");
        return super.dispatchTouchEvent(event);
    }

    /*@Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        Log.d(TAG, "onTouchEvent, action is " + action);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                return true;
            default:
                return false;
        }
    }*/
}
