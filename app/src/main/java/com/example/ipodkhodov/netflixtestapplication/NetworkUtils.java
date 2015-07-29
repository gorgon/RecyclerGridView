package com.example.ipodkhodov.netflixtestapplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * Class that has Network utilities
 */
public class NetworkUtils {
    private static final String TAG = "NetworkUtils";

    public static InputStream OpenHttpConnection(String strURL) throws IOException {
        InputStream inputStream = null;
        URL url = new URL(strURL);
        URLConnection conn = url.openConnection();

        try {
            HttpURLConnection httpConn = (HttpURLConnection) conn;
            httpConn.setRequestMethod("GET");
            httpConn.connect();

            if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                inputStream = httpConn.getInputStream();
            }
        } catch (Exception ex) {
            Log.e(TAG, "Got exception opening http connection: " + ex);
        }
        return inputStream;
    }

    public static Bitmap loadImage(String url) {
        Bitmap bitmap = null;
        InputStream in = null;
        try {
            in = NetworkUtils.OpenHttpConnection(url);
            bitmap = BitmapFactory.decodeStream(in);
            in.close();
        } catch (IOException ex) {
            Log.i(TAG, "loadImage got an exception: " + ex);
        }
        return bitmap;
    }
}
