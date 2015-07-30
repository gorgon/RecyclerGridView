package com.example.ipodkhodov.netflixtestapplication;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
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
import android.view.animation.LinearInterpolator;
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
    private Bitmap inputBitmap;
    private Bitmap backgroundBitmap;
    private Bitmap blurredBackgroundBitmap;
    private boolean alphaAnimationStarted;
    private ObjectAnimator alphaShowAnimation;
    private ObjectAnimator alphaFadeAnimation;


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

                    // If we are now going out from the fragment we need to hide background image (e.g. when it is still in process of fading out)
                    if (heroTransionFinished) {
                        alphaFadeAnimation = ObjectAnimator.ofFloat(boxArt, "alpha", 1.0f, 0.0f);
                        alphaFadeAnimation.setDuration(300);
                        alphaFadeAnimation.start();
                        background.setVisibility(View.GONE);
                    }
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
        inputBitmap = (Bitmap)getActivity().getIntent().getParcelableExtra(BOX_ART_URL_EXTRA);
        String desc = getActivity().getIntent().getStringExtra(DESCRIPTION_EXTRA);
        initViews(desc);
        return fragmentView;
    }

    private void findViews(View fragmentView) {
        background = (ImageView) fragmentView.findViewById(R.id.background);
        boxArt = (ImageView) fragmentView.findViewById(R.id.boxart);
        description = (TextView)fragmentView.findViewById(R.id.description);
    }

    private void initViews(String desc) {
        boxArt.setImageBitmap(inputBitmap);

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

                Bitmap image = NetworkUtils.loadImage(initialUrl);

                if (getActivity() == null || getActivity().isFinishing()) {
                    return null;
                }

                backgroundBitmap = image;
                blurredBackgroundBitmap = blurRenderScript(image);
                return image;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);

                if (getActivity() == null || getActivity().isFinishing()) {
                    return;
                }

                tryUpdateBackground();
            }
        };
        asyncTask.execute(url);
    }

    private void tryUpdateBackground() {
        if (!alphaAnimationStarted && backgroundBitmap != null && getActivity() != null && !getActivity().isFinishing()) {
            background.setImageBitmap(backgroundBitmap);
            alphaShowAnimation = ObjectAnimator.ofFloat(background, "alpha", 0.0f, 1.0f);
            alphaShowAnimation.setDuration(1000);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                alphaShowAnimation.setAutoCancel(true);
            }
            alphaShowAnimation.setInterpolator(new LinearInterpolator());
            alphaShowAnimation.start();
            alphaFadeAnimation = ObjectAnimator.ofFloat(boxArt, "alpha", 1.0f, 0.0f);
            alphaFadeAnimation.setDuration(1000);
            alphaFadeAnimation.setInterpolator(new LinearInterpolator());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                alphaFadeAnimation.setAutoCancel(true);
            }
            alphaFadeAnimation.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    // Swap bitmaps to perform out hero animation when hiding fragment back
                    Drawable[] layers = new Drawable[2];
                    layers[0] = new BitmapDrawable(getActivity().getResources(), backgroundBitmap);
                    layers[1] = new BitmapDrawable(getActivity().getResources(), blurredBackgroundBitmap);
                    // Converting into Blur Animation - use blur with cross-fading (TransitionDrawable). Details: http://stackoverflow.com/a/24863507/1390874
                    TransitionDrawable td = new TransitionDrawable(layers);
                    td.startTransition(1000);
                    boxArt.setImageDrawable(td);
                    boxArt.setAlpha(1.0f);
                    background.setVisibility(View.GONE);
                    backgroundBitmap = null;
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            alphaFadeAnimation.start();
            alphaAnimationStarted = true;
        }
    }

    private Bitmap blurRenderScript(Bitmap smallBitmap) {
        Log.i(TAG, "Creating blur bitmap started..");
        long startTime = System.currentTimeMillis();

        RenderScript rsScript = getRenderScript(getActivity());

        Bitmap output = smallBitmap;
        if (rsScript == null) {
            return smallBitmap;
        }
        try {
            // If the bitmap's internal config is in one of the public formats,
            // getConfig() will return that config, otherwise it will return null.
            Bitmap.Config config = smallBitmap.getConfig();
            int usage = Allocation.USAGE_SCRIPT;
            if (config == null) {
                // If the config is null, we use the recommended ARGB 8888 config
                config = Bitmap.Config.ARGB_8888;
            } else {
                // if the config IS in one of the public formats, then we can use
                // Allocation.USAGE_SHARED causing the bitmap copy to be just a
                // synchronization rather than a full copy.
                usage = usage | Allocation.USAGE_SHARED;
            }

            // This block takes more then 20% of time - does it really worth it (seems that everything works ok without it)...
            if (smallBitmap.getWidth() % 4 != 0) {
                // Blurring images that aren't a mulitple of 4 pixels wide results in artefacts
                // https://plus.google.com/+RomanNurik/posts/TLkVQC3M6jW
                Bitmap cropped = Bitmap.createBitmap(smallBitmap, 0, 0, smallBitmap.getWidth() - smallBitmap.getWidth() % 4, smallBitmap.getHeight());
                //smallBitmap.recycle();
                smallBitmap = cropped;
            }

            output = smallBitmap.copy(config, true);
            Allocation alloc = Allocation.createFromBitmap(rsScript, smallBitmap, Allocation.MipmapControl.MIPMAP_NONE, usage);

            Allocation outAlloc = Allocation.createTyped(rsScript, alloc.getType());
            // ScriptIntrinsicBlur is the one that does all the magic
            ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(rsScript, Element.U8_4(rsScript));

            // The radius needs to be between 1 and 25
            blur.setRadius(25);
            blur.setInput(alloc);
            blur.forEach(outAlloc);
            outAlloc.copyTo(output);

            // We need to do some cleanup before we exit to avoid memory issues
            rsScript.destroy();
            //smallBitmap.recycle();
        } catch (Exception ex) {
            Log.e(TAG, "Got exception!");
        }

        long timeSpent = System.currentTimeMillis() - startTime;
        Log.i(TAG, "Finished creating blur bitmap. Time spend: " + timeSpent + "ms");
        return output;
    }

    private static RenderScript getRenderScript(Context context) {
        RenderScript rsScript = null;
        try {
            // This step can fail on some phones, so instead of crashing we need to just
            // avoid using RenderScript
            rsScript = RenderScript.create(context);
        } catch (Exception e) {
            // Do logging
            Log.e(TAG, "Got exception inside getRenderScript: " + e);
        }
        return rsScript;
    }
}
