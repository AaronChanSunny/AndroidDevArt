package com.aaron.androiddevart.view;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import com.aaron.androiddevart.R;

/**
 * Created by Git on 2016/3/19.
 */
public class ViewEventDispatcher1 extends RelativeLayout {
    private static final String TAG = ViewEventDispatcher1.class.getSimpleName();

    public ViewEventDispatcher1(Context context, AttributeSet attrs) {
        super(context, attrs);

        GroupItem textView = new GroupItem(getContext());

        textView.setText(R.string.view_event_dispatcher1_text);
        textView.setPadding(20, 20, 20, 20);
        textView.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.holo_blue_light));

        addView(textView);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.d(TAG, "dispatchTouchEvent");
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

        return super.onTouchEvent(event);
        /*switch (action) {
            case MotionEvent.ACTION_DOWN:
                return true;
            default:
                return super.onTouchEvent(event);
        }*/
    }
}
