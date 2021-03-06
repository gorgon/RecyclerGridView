package com.example.ipodkhodov.recyclertestapplication;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that takes care of loading pages of urls
 */
public class UrlsLoader {
    private static final String TAG = "UrlsLoader";

    public static final int MAX_ELEMENTS_PER_PAGE = 30;
    private static final String baseUrl = "http://api.rottentomatoes.com/api/public/v1.0/movies.json?apikey=9htuhtcb4ymusd73d4z6jxcj&q=the&page_limit=%d&page=%d";
    private UrlsLoaderListener listener;

    public static class MovieData {
        public String url;
        public String description;
    }

    public interface UrlsLoaderListener {
        public void onLoaded(List<MovieData> urls);
    }

    public static void load(int pageNumber, UrlsLoaderListener listener) {
        String currentUrl = String.format(baseUrl, MAX_ELEMENTS_PER_PAGE, pageNumber);
        startDownload(currentUrl, listener);
    }

    private static void startDownload(String url, final UrlsLoaderListener listener) {
        AsyncTask<String, String, JSONObject> asyncTask = new AsyncTask<String, String, JSONObject>() {
            private String url;

            @Override
            protected JSONObject doInBackground(String... urls) {
                this.url = urls[0];
                return loadUrl(this.url);
            }

            @Override
            protected void onPostExecute(JSONObject json) {
                super.onPostExecute(json);

                List<MovieData> arrayList = new ArrayList<>();

                // Parse json
                try {
                    if (json == null) {
                        Log.e(TAG, "Got null JSON!");
                        return;
                    }

                    JSONArray array = json.getJSONArray("movies");
                    for (int i = 0; i < array.length(); ++i) {
                        JSONObject movie = array.getJSONObject(i);
                        JSONObject poster = movie.getJSONObject("posters");
                        MovieData data = new MovieData();
                        data.url = poster.getString("thumbnail");
                        data.description = String.format("%s - %s (%d), Rating: %s\nRuntime: %s", movie.getString("id"), movie.getString("title"), movie.getInt("year"), movie.getString("mpaa_rating"), movie.getString("runtime"));
                        arrayList.add(data);
                    }
                } catch (JSONException ex) {
                    Log.e(TAG, "Got exception trying to parse JSON: " + ex);
                }

                listener.onLoaded(arrayList);
            }

            private JSONObject loadUrl(String url) {
                JSONObject json = null;
                InputStream in = null;
                try {
                    in = NetworkUtils.OpenHttpConnection(url);
                    BufferedInputStream bis = new BufferedInputStream(in);
                    InputStreamReader reader = new InputStreamReader(bis);
                    BufferedReader bufferedReader = new BufferedReader(reader);
                    StringBuilder builder = new StringBuilder();
                    String line = bufferedReader.readLine();
                    while (line != null) {
                        builder.append(line);
                        line = bufferedReader.readLine();
                    }
                    json = new JSONObject(builder.toString());
                    in.close();
                    bis.close();
                } catch (IOException ex) {
                    Log.i(TAG, "loadUrl got an exception: " + ex);

                } catch (JSONException ex2) {
                    Log.i(TAG, "loadUrl got a JSON exception: " + ex2);
                }
                return json;
            }
        };

        asyncTask.execute(url);
    }
}
