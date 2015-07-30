package com.example.ipodkhodov.netflixtestapplication;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Main fragment with grid recycler view
 */
public class MainActivityFragment extends Fragment {
    private final String TAG = "MainActivityFragment";

    private RecyclerView recyclerView;
    private View loadingView;
    private MyAdapter adapter;

    private int currentPage = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_main, container, false);
        findViews(fragmentView);
        initRecyclerView();
        return fragmentView;
    }

    private void findViews(View fragmentView) {
        recyclerView = (RecyclerView) fragmentView.findViewById(R.id.recycler_view);
        loadingView = fragmentView.findViewById(R.id.loading_view);
    }

    private void initRecyclerView() {
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        int totalRows = (int)(dm.widthPixels / getActivity().getResources().getDimension(R.dimen.row_width));
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), totalRows));
        initAdapter(null);

        // Load the initial page
        loadUrls();
    }

    private void initAdapter(ArrayList<String> urls) {
        adapter = new MyAdapter();
        recyclerView.setAdapter(adapter);
    }

    private void loadUrls() {
        Log.i(TAG, "Loading page: " + currentPage);

        UrlsLoader.load(currentPage, new UrlsLoader.UrlsLoaderListener() {
            @Override
            public void onLoaded(List<UrlsLoader.MovieData> data) {
                loadingView.setVisibility(View.GONE);
                adapter.addData(data);
            }
        });
    }

    private void loadNextPage() {
        currentPage++;
        loadUrls();
    }

    /**
     * Class that takes care of data and its representation inside RecyclerView
     */
    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private final String TAG = "MyAdapter";

        private List<UrlsLoader.MovieData> dataset = new ArrayList<>();
        private Map<String, Bitmap> bitmapCache = new HashMap<>();

        public void addData(List<UrlsLoader.MovieData> newDataset) {
            dataset.addAll(newDataset);
            notifyDataSetChanged();
        }

        public void onViewHolderClick(View view, int position) {
            String description = dataset.get(position).description;
            Bitmap bmp = bitmapCache.get(dataset.get(position).url);
            Intent intent = DetailsFragment.createStartIntent(getActivity(), bmp, description);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // Exclusing status and navigation bars from animation
                View statusBar = getActivity().findViewById(android.R.id.statusBarBackground);
                View navigationBar = getActivity().findViewById(android.R.id.navigationBarBackground);

                List<Pair<View, String>> pairs = new ArrayList<>();
                pairs.add(Pair.create(statusBar, Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME));
                pairs.add(Pair.create(navigationBar, Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME));
                pairs.add(Pair.create(view, "boxArtTransition"));
                // Lollipop hero transition
                Bundle options = ActivityOptions.makeSceneTransitionAnimation(getActivity(), pairs.toArray(new Pair[pairs.size()])).toBundle();
                getActivity().startActivity(intent, options);
            } else {
                getActivity().startActivity(intent);
            }
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public ImageView image;
            public String url; // Keep track of current url (not to set obsolete image into new position)

            public ViewHolder(View v) {
                super(v);
                image = (ImageView) v.findViewById(R.id.thumbnail);;
                image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int position = ViewHolder.this.getAdapterPosition();
                        onViewHolderClick(view, position);
                    }
                });
            }
        }

        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
            // create a new view - got from https://developer.android.com/training/material/lists-cards.html
            ViewGroup v = (ViewGroup)LayoutInflater.from(parent.getContext()).inflate(R.layout.thumbnail_view, parent, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            String url = dataset.get(position).url;

            // Update url not to set ImageView into already re-used ViewHolder
            holder.url = url;
            // If we find bitmap in cache - load it
            if (bitmapCache.containsKey(url)) {
                holder.image.setImageBitmap(bitmapCache.get(url));
            } else {
                // Otherwise show default icon and schedule fetch request
                holder.image.setImageResource(R.drawable.loading);
                scheduleDownload(holder);
            }

            // Scheduling to load the next page
            if (position == dataset.size() - 1) {
                loadNextPage();
            }
        }

        @Override
        public int getItemCount() {
            return dataset.size();
        }

        private void scheduleDownload(ViewHolder holder) {
            AsyncTask<ViewHolder, ViewHolder, Bitmap> asyncTask = new AsyncTask<ViewHolder, ViewHolder, Bitmap>() {
                private String initialUrl;
                private ViewHolder viewHolder;

                @Override
                protected Bitmap doInBackground(ViewHolder... holder) {
                    viewHolder = holder[0];
                    initialUrl = viewHolder.url;

                    return NetworkUtils.loadImage(initialUrl);
                }

                @Override
                protected void onPostExecute(Bitmap bitmap) {
                    super.onPostExecute(bitmap);

                    // Always update cache with downloaded bitmap (could be for future needs)
                    bitmapCache.put(initialUrl, bitmap);

                    // If ViewHolder was not re-used - update the ImageView (we are already in the main thread)
                    if(viewHolder.url.equalsIgnoreCase(initialUrl)) {
                        viewHolder.image.setImageBitmap(bitmap);
                    } else {
                        Log.i(TAG, "ViewHolder was already re-used. New url: " + viewHolder.url + "; old one: " + initialUrl);
                    }
                }
            };
            asyncTask.execute(holder);
        }
    }
}
