package com.aaron.androiddevart.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Aaron on 2016/3/17.
 */
public class GestureDetectorView extends View {
    private GestureDetector mGestureDetector;

    public GestureDetectorView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mGestureDetector = new GestureDetector(getContext(), new MyOnDoubleTapListener());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return acquireGestureDetector(event);
    }

    private boolean acquireGestureDetector(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    static class MyOnDoubleTapListener extends GestureDetector.SimpleOnGestureListener {
        private static final String TAG = MyOnDoubleTapListener.class.getSimpleName();

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Log.d(TAG, "onDoubleTap");
            return super.onDoubleTap(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            Log.d(TAG, "onLongPress");
            super.onLongPress(e);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Log.d(TAG, "onFling, velocityX is " + velocityX + ", velocityY is " + velocityY);
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }
}
