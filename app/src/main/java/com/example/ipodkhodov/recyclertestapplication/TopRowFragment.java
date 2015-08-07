package com.example.ipodkhodov.recyclertestapplication;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


/**
 * Top fragment to emulate discovery
 */
public class TopRowFragment extends Fragment {
    private final String TAG = "TopFragment";

    private final int currentResId;

    public TopRowFragment(int position) {
        switch (position) {
            case 0:
                currentResId = R.drawable.top_image1;
                break;
            case 1:
                currentResId = R.drawable.top_image2;
                break;
            default:
                currentResId = R.drawable.top_image3;
                break;

        }

    }
    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_top_row, container, false);
        ImageView iv = (ImageView)fragmentView.findViewById(R.id.img);
        iv.setImageResource(currentResId);
        return fragmentView;
    }
}
