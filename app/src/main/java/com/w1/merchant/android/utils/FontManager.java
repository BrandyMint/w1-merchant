package com.w1.merchant.android.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;

public class FontManager {

    private static volatile FontManager sFontManager;

    private final Typeface mW1RoubleFont;

    private FontManager(Context context) {
        AssetManager assetManager =  context.getApplicationContext().getAssets();
        mW1RoubleFont = Typeface.createFromAsset(assetManager,
                "fonts/W1Rouble-Regular.otf");
    }

    public static FontManager getInstance() {
        return sFontManager;
    }

    public static void onAppInit(Context appContext) {
        sFontManager = new FontManager(appContext);
    }

    public Typeface getRoubleFont() {
        return mW1RoubleFont;
    }

}
