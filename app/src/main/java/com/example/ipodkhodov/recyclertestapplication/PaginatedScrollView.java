package com.example.ipodkhodov.recyclertestapplication;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.OverScroller;
import android.widget.ScrollView;

import java.lang.reflect.Field;

/**
 * Class to emulate vertical ViewPager with limited functionality
 */
public class PaginatedScrollView extends ScrollView {
    private static final String TAG = "PaginatedScrollView";
    
    private final static int TOP_PAGE = 0;
    private final static int BOTTOM_PAGE = 1;
    private int currentPage = TOP_PAGE;
    
    private int pageHeight;
    private OverScroller mScroller;
    private boolean fling;

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
        //mScroller = new OverScroller(getContext()); // <- it's an another scroller:(

        Field f = null;
        try {
            f = super.getClass().getSuperclass().getDeclaredField("mScroller");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            Log.e(TAG, "ex: " + e);
        }
        f.setAccessible(true);
        try {
            mScroller = (OverScroller) f.get(this); //IllegalAccessException
            Log.i(TAG, "mScroller: " + mScroller);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Log.e(TAG, "ex2: " + e);
        }

        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(dm);
        pageHeight = dm.heightPixels;
    }
    
    @Override
    public void fling (int velocityY)
    {
        fling = true;

        Log.i(TAG, "velocityY: " + velocityY + "; currentPage: " + currentPage + "; getScrollY() " + getScrollY());

        // Skip small speed to avoid "scrolling" instead of flinging that can lead to not switching the page
        if (Math.abs(velocityY) < 300) {
            return;
        }

        if (velocityY > 0) {
            // Scrolling bottom
            if (currentPage == TOP_PAGE) {
                scrollToBottom();
            } else {
                super.fling(velocityY);
            }
        } else if (velocityY < 0) {
            // Scrolling top
            if (currentPage == BOTTOM_PAGE && getScrollY() < pageHeight) {
                scrollToTop();
            } else {
                int height = getHeight();
                int bottom = getChildAt(0).getHeight();
                mScroller.fling(getScrollX(), getScrollY(), 0, velocityY, 0, 0, pageHeight, Math.max(0, bottom - height), 0, height / 5);
                postInvalidateOnAnimation();
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        boolean res = super.onTouchEvent(ev);
        final int actionMasked = ev.getActionMasked();

        switch (actionMasked) {
            case MotionEvent.ACTION_DOWN: {
                Log.i(TAG, "ACTION_DOWN");
                fling = false;
                Log.i(TAG, "fling == false");
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                Log.i(TAG, "ACTION_MOVE");
                fling = false;
                Log.i(TAG, "fling == false");
                break;
            }

            case MotionEvent.ACTION_UP: {
                Log.i(TAG, "ACTION_UP: " + mScroller.isFinished());

                if (!fling && mScroller.isFinished()) {
                    Log.i(TAG, "scrollY: " + getScrollY() + "; pageHeight: " + pageHeight);

                    if(getScrollY() < pageHeight / 2) {
                        scrollToTop();
                    } else {
                        scrollToBottom();
                    }
                }
                break;
            }
        }
        return res;
    }

    private void scrollToTop() {
        this.smoothScrollTo(0, 0);
        currentPage = TOP_PAGE;
    }

    private void scrollToBottom() {
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity)getContext()).getWindowManager().getDefaultDisplay().getMetrics(dm);
        this.smoothScrollTo(0, dm.heightPixels);
        currentPage = BOTTOM_PAGE;
    }
}
