package com.example.ipodkhodov.recyclertestapplication;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.transition.Slide;
import android.transition.Transition;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;

/**
 * An activity to show movie details
 */
public class DetailsActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        final View decor = getWindow().getDecorView();
        decor.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                decor.getViewTreeObserver().removeOnPreDrawListener(this);
                return true;
            }
        });

        // Excluding status and navigation bars from animation
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Transition top = new Slide(Gravity.TOP);
            top.excludeTarget(android.R.id.statusBarBackground, true);
            top.excludeTarget(android.R.id.navigationBarBackground, true);
            getWindow().setExitTransition(top);
            Transition bottom = new Slide(Gravity.BOTTOM);
            bottom.excludeTarget(android.R.id.statusBarBackground, true);
            bottom.excludeTarget(android.R.id.navigationBarBackground, true);
            getWindow().setEnterTransition(bottom);
        }
    }
}
