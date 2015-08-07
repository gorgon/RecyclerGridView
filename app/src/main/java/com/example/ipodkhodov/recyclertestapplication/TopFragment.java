package com.example.ipodkhodov.recyclertestapplication;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.viewpagerindicator.CirclePageIndicator;


/**
 * Top fragment to emulate discovery
 */
public class TopFragment extends Fragment {
    private final String TAG = "TopFragment";

    private static final int NUM_PAGES = 3;
    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_top, container, false);

        // Set the pager with an adapter
        ViewPager pager = (ViewPager)fragmentView.findViewById(R.id.pager);
        pager.setAdapter(new ScreenSlidePagerAdapter(getActivity().getSupportFragmentManager()));

        // Bind the title indicator to the adapter
        CirclePageIndicator titleIndicator = (CirclePageIndicator)fragmentView.findViewById(R.id.titles);
        titleIndicator.setViewPager(pager);

        return fragmentView;
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return new TopRowFragment(position);
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }
}
