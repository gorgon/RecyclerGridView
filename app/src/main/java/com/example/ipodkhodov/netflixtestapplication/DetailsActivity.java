package com.example.ipodkhodov.netflixtestapplication;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

/**
 * An activity to show movie details
 */
public class DetailsActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
    }
}
