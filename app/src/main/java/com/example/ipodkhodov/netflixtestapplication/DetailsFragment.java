package com.example.ipodkhodov.netflixtestapplication;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;
import android.transition.Transition;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Class that shows movie details
 */
public class DetailsFragment extends Fragment {
    private final static String TAG = "DetailsFragment";

    private ImageView boxArt;
    private ImageView background;
    private TextView description;
    private boolean heroTransionFinished;
    private Bitmap backgroundBitmap;
    private boolean alphaAnimationStarted;


    private static final String BOX_ART_URL_EXTRA = "boxart_url";
    private static final String DESCRIPTION_EXTRA = "description";

    public static Intent createStartIntent(Activity act, Bitmap bmp, String descr) {
        Intent intent = new Intent(act, DetailsActivity.class);
        intent.putExtra(BOX_ART_URL_EXTRA, bmp);
        intent.putExtra(DESCRIPTION_EXTRA, descr);
        return intent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Scheduling an animation listener to avoid running alpha animation before hero animation finished
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActivity().getWindow().getSharedElementReturnTransition().addListener(new Transition.TransitionListener() {
                @Override
                public void onTransitionStart(Transition transition) {
                    Log.d(TAG, "ActivityB.onTransitionStart");
                }

                @Override
                public void onTransitionEnd(Transition transition) {
                    Log.d(TAG, "ActivityB.onTransitionEnd");
                    heroTransionFinished = true;
                    tryUpdateBackground();
                }

                @Override
                public void onTransitionCancel(Transition transition) {
                }

                @Override
                public void onTransitionPause(Transition transition) {
                }

                @Override
                public void onTransitionResume(Transition transition) {
                }
            });
        } else {
            heroTransionFinished = true;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_details, container, false);
        findViews(fragmentView);
        Bitmap bmp = (Bitmap)getActivity().getIntent().getParcelableExtra(BOX_ART_URL_EXTRA);
        String desc = getActivity().getIntent().getStringExtra(DESCRIPTION_EXTRA);
        initViews(bmp, desc);
        return fragmentView;
    }

    private void findViews(View fragmentView) {
        background = (ImageView) fragmentView.findViewById(R.id.background);
        boxArt = (ImageView) fragmentView.findViewById(R.id.boxart);
        description = (TextView)fragmentView.findViewById(R.id.description);
    }

    private void initViews(Bitmap bitmap, String desc) {
        boxArt.setImageBitmap(bitmap);

        // An example of downloading a large image during hero transition animation
        String url = "http://www.freedvdcover.com/wp-content/uploads/The_Notebook_R4-front-www.GetCovers.net_.jpg"; // 3206x2135 px
        url = "http://40.media.tumblr.com/7627bdc47917703ed127e021745b3cdd/tumblr_nf0cyhF8Ik1tr5s6io1_1280.jpg"; // 1014x1500 px
        scheduleImageDownload(url);
        description.setText(desc);
    }

    private void scheduleImageDownload(String url) {
        AsyncTask<String, String, Bitmap> asyncTask = new AsyncTask<String, String, Bitmap>() {
            private String initialUrl;

            @Override
            protected Bitmap doInBackground(String... urls) {
                initialUrl = urls[0];

                if (getActivity() == null || getActivity().isFinishing()) {
                    return null;
                }

                return blurRenderScript(NetworkUtils.loadImage(initialUrl));
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);
                backgroundBitmap = bitmap;
                tryUpdateBackground();
            }
        };
        asyncTask.execute(url);
    }

    private void tryUpdateBackground() {
        if (!alphaAnimationStarted && backgroundBitmap != null && heroTransionFinished && getActivity() != null && !getActivity().isFinishing()) {
            background.setImageBitmap(backgroundBitmap);
            AlphaAnimation alphaShowAnimation = new AlphaAnimation(0.0f, 1.0f);
            alphaShowAnimation.setDuration(1000);
            background.startAnimation(alphaShowAnimation);
            AlphaAnimation alphaFadeAnimation = new AlphaAnimation(1.0f, 0.0f);
            alphaFadeAnimation.setDuration(1000);
            alphaFadeAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    // Swap bitmaps to perform out hero animation when hiding fragment back
                    boxArt.setImageBitmap(backgroundBitmap);
                    background.setVisibility(View.GONE);
                    backgroundBitmap = null;
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            boxArt.startAnimation(alphaFadeAnimation);
            alphaAnimationStarted = true;
        }
    }

    private Bitmap blurRenderScript(Bitmap smallBitmap) {
        Log.i(TAG, "Creating blur bitmap started..");
        long startTime = System.currentTimeMillis();
        Bitmap output = Bitmap.createBitmap(smallBitmap.getWidth(), smallBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        RenderScript rs = RenderScript.create(getActivity());
        ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        Allocation inAlloc = Allocation.createFromBitmap(rs, smallBitmap, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_GRAPHICS_TEXTURE);
        Allocation outAlloc = Allocation.createFromBitmap(rs, output);
        script.setRadius(25); // Maximum blur radius
        script.setInput(inAlloc);
        script.forEach(outAlloc);
        outAlloc.copyTo(output);
        rs.destroy();
        smallBitmap.recycle();

        long timeSpent = System.currentTimeMillis() - startTime;
        Log.i(TAG, "Finished creating blur bitmap. Time spend: " + timeSpent + "ms");
        return output;
    }
}
