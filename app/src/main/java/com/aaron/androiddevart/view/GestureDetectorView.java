package com.aaron.androiddevart.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.lang.ref.WeakReference;

/**
 * Created by Aaron on 2016/3/17.
 */
public class GestureDetectorView extends View {
    private GestureDetector mGestureDetector;

    public GestureDetectorView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mGestureDetector = new GestureDetector(getContext(), new MyOnDoubleTapListener(getContext()));
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

        private WeakReference<Context> mContextWeakReference;

        public MyOnDoubleTapListener(Context context) {
            mContextWeakReference = new WeakReference<Context>(context);
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Log.d(TAG, "onDoubleTap");
            showToast("onDoubleTap");
            return super.onDoubleTap(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            Log.d(TAG, "onLongPress");
            showToast("onLongPress");
            super.onLongPress(e);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Log.d(TAG, "onFling, velocityX is " + velocityX + ", velocityY is " + velocityY);
            showToast("onFling");
            return super.onFling(e1, e2, velocityX, velocityY);
        }

        private void showToast(String msg) {
            if (mContextWeakReference != null) {
                Context context = mContextWeakReference.get();
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
