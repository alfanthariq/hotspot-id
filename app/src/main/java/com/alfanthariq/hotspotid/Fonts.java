package com.alfanthariq.hotspotid;

import android.content.res.AssetManager;
import android.graphics.Typeface;

/**
 * Created by Alfanthariq on 18/06/2017.
 */

public class Fonts {
    String fontPath = "fonts/Poppins-Regular.ttf";
    String fontBoldPath = "fonts/Poppins-Bold.ttf";
    String fontSemiBoldPath = "fonts/Poppins-SemiBold.ttf";
    String fontMediumPath = "fonts/Poppins-Medium.ttf";
    String fontLightPath = "fonts/Poppins-Light.ttf";

    Typeface tf, tfb, tfsb, tfm, tfl;

    public Fonts(AssetManager aset){
        tf = Typeface.createFromAsset(aset, fontPath);
        tfb = Typeface.createFromAsset(aset, fontBoldPath);
        tfsb = Typeface.createFromAsset(aset, fontSemiBoldPath);
        tfm = Typeface.createFromAsset(aset, fontMediumPath);
        tfl = Typeface.createFromAsset(aset, fontLightPath);
    }

    public Typeface getTf() {
        return tf;
    }

    public Typeface getTfb() {
        return tfb;
    }

    public Typeface getTfsb() {
        return tfsb;
    }

    public Typeface getTfm() {
        return tfm;
    }

    public Typeface getTfl() {
        return tfl;
    }
}
