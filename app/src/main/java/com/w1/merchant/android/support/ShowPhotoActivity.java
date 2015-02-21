package com.w1.merchant.android.support;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;
import com.w1.merchant.android.R;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

public class ShowPhotoActivity extends Activity {
    public static final String ARG_IMAGE_URL = "com.w1.merchant.android.support.ShowPhotoActivity.ARG_IMAGE_URL";
    public static final String ARG_IMAGE_WIDTH = "com.w1.merchant.android.support.ShowPhotoActivity.ARG_IMAGE_WIDTH";

    private static final int HIDE_ACTION_BAR_DELAY = 5000;

    private PhotoView mPhotoView;
    private PhotoViewAttacher mPhotoViewAttacher;

    private boolean isNavigationHidden = false;
    private final Handler mHideActionBarHandler = new Handler();
    private volatile boolean userForcedToChangeOverlayMode = false;

    public static void startShowPhotoActivity(Context context, String url, Integer imageWidth, View animateFrom) {
        Intent intent = new Intent(context, ShowPhotoActivity.class);
        intent.putExtra(ARG_IMAGE_URL, url);
        if (imageWidth != null) intent.putExtra(ARG_IMAGE_WIDTH, imageWidth);

        if (animateFrom != null) {
            ActivityOptionsCompat options = ActivityOptionsCompat.makeScaleUpAnimation(
                    animateFrom, 0, 0, animateFrom.getWidth(), animateFrom.getHeight());
            if (context instanceof Activity) {
                ActivityCompat.startActivity((Activity) context, intent, options.toBundle());
            } else {
                context.startActivity(intent);
            }
        } else {
            context.startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_photo);

        String photoUrl = getIntent().getStringExtra(ARG_IMAGE_URL);
        if (photoUrl == null) throw new IllegalStateException("no photo url");

        mPhotoView = (PhotoView) findViewById(R.id.picturePhotoView);
        Picasso picasso = Picasso.with(this);

        mPhotoViewAttacher = new PhotoViewAttacher(mPhotoView);
        mPhotoViewAttacher.setScaleType(ImageView.ScaleType.FIT_CENTER);
        mPhotoViewAttacher.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
            @Override
            public void onPhotoTap(View view, float v, float v2) {
                userForcedToChangeOverlayMode = true;
                toggleShowOrHideHideyBarMode();
            }
        });

        RequestCreator rq = picasso
                .load(photoUrl)
                .memoryPolicy(MemoryPolicy.NO_STORE);
        int width = getIntent().getIntExtra(ARG_IMAGE_WIDTH, 0);
        if (width > 0) rq.resize(width, 0);

        rq.into(mPicassoTarget);

    }

    @Override
    protected void onResume() {
        super.onResume();
        runHideBarTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHideActionBarHandler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPhotoViewAttacher != null) {
            mPhotoViewAttacher.cleanup();
        }

    }

    private void runHideBarTimer() {
        mHideActionBarHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!userForcedToChangeOverlayMode && !isNavigationHidden) {
                    toggleShowOrHideHideyBarMode();
                }
            }
        }, HIDE_ACTION_BAR_DELAY);
    }

    /**
     * Detects and toggles actionbarOverlay mode (also known as "hidey bar" mode).
     */
    @SuppressLint("InlinedApi")
    public void toggleShowOrHideHideyBarMode() {

        int newUiOptions;

        if (Build.VERSION.SDK_INT >= 19) {
            newUiOptions = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        } else {
            newUiOptions = getWindow().getDecorView().getSystemUiVisibility();
        }

        if (!isNavigationHidden) {
            if (Build.VERSION.SDK_INT >= 19) {
                newUiOptions
                        |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE;
            } else {
                newUiOptions = View.SYSTEM_UI_FLAG_LOW_PROFILE;
            }
            isNavigationHidden = true;
        } else {
            if (Build.VERSION.SDK_INT < 19) {
                newUiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
            }
            isNavigationHidden = false;
            userForcedToChangeOverlayMode = false;
            runHideBarTimer();
        }
        getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
    }


    private final Target mPicassoTarget = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            findViewById(R.id.progress).setVisibility(View.GONE);
            int maxTextureSize = 2048;
            if (bitmap.getHeight() >= maxTextureSize || bitmap.getWidth() > maxTextureSize) {
                mPhotoView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            }
            mPhotoView.setImageDrawable(new BitmapDrawable(getResources(), bitmap));
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            findViewById(R.id.progress).setVisibility(View.GONE);
            Toast.makeText(ShowPhotoActivity.this, R.string.network_error, Toast.LENGTH_SHORT).show();
            ShowPhotoActivity.this.finish();
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
            findViewById(R.id.progress).setVisibility(View.VISIBLE);
        }
    };


}
