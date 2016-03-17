package com.aaron.androiddevart.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by Aaron on 2016/3/17.
 */
public class ScrollEnabledView extends TextView {
    public ScrollEnabledView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void scrollXByStep(int step) {
        scrollTo(getScrollX() + step, getScrollY());
    }
}
