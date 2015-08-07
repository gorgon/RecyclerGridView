package com.example.ipodkhodov.recyclertestapplication;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ScrollView;

/**
 * Class to emulate vertical ViewPager with limited functionality
 */
public class PaginatedScrollView extends ScrollView {
    private static final String TAG = "PaginatedScrollView";
    private static final int SCROLL_DELAY_TIMEOUT = 100;

    private int pageHeight;
    private Handler handler = new Handler();
    private Runnable scrollRunnable = new Runnable() {
        @Override
        public void run() {
            if (getScrollY() > 0 && getScrollY() < pageHeight / 2) {
                scrollToTop();
            } else if (getScrollY() >= pageHeight / 2 && getScrollY() < pageHeight) {
                scrollToBottom();
            }
        }
    };

    public PaginatedScrollView(Context context) {
        super(context);
        init();
    }

    public PaginatedScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PaginatedScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public PaginatedScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(dm);
        pageHeight = dm.heightPixels;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        Log.i(TAG, "onScrollChanged: " + l + "; t: " + t + "; oldl " + oldl + "; oldt: " + oldt);

        handler.removeCallbacks(scrollRunnable);
        handler.postDelayed(scrollRunnable, SCROLL_DELAY_TIMEOUT);
    }

    private void scrollToTop() {
        this.smoothScrollTo(0, 0);
    }

    private void scrollToBottom() {
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity)getContext()).getWindowManager().getDefaultDisplay().getMetrics(dm);
        this.smoothScrollTo(0, dm.heightPixels - 100);
    }
}
