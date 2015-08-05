package com.example.ipodkhodov.recyclertestapplication;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * Top fragment to emulate discovery
 */
public class TopFragment extends Fragment {
    private final String TAG = "TopFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_top, container, false);
        return fragmentView;
    }
}
