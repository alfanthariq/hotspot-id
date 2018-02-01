package com.alfanthariq.hotspotid;

import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import agency.tango.materialintroscreen.SlideFragment;


public class PermissionSlide extends SlideFragment {
    Boolean canWrite;
    Fonts fs;
    TextView tx, loc, locSub;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_permission_slide, container, false);

        fs = new Fonts(getActivity().getAssets());
        tx = (TextView) view.findViewById(R.id.title);
        loc = (TextView) view.findViewById(R.id.location);
        locSub = (TextView) view.findViewById(R.id.locationSub);
        tx.setTypeface(fs.getTf());
        loc.setTypeface(fs.getTfb());
        locSub.setTypeface(fs.getTf());
        return view;
    }

    @Override
    public int backgroundColor() {
        return R.color.third_slide_background;
    }

    @Override
    public int buttonsColor() {
        return R.color.third_slide_buttons;
    }

    @Override
    public boolean canMoveFurther() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            canWrite = Settings.System.canWrite(getActivity());
        }
        return canWrite;
    }

    @Override
    public String cantMoveFurtherErrorMessage() {
        return "Please grant permission first";
    }
}
