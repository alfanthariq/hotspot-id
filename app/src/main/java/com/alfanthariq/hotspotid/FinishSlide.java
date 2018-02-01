package com.alfanthariq.hotspotid;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import agency.tango.materialintroscreen.SlideFragment;


public class FinishSlide extends SlideFragment {
    Fonts fs;
    TextView tx, loc, locSub;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_finish_slide, container, false);

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
        return R.color.fourth_slide_background;
    }

    @Override
    public int buttonsColor() {
        return R.color.fourth_slide_buttons;
    }

    @Override
    public boolean canMoveFurther() {
        return true;
    }

    @Override
    public String cantMoveFurtherErrorMessage() {
        return "";
    }
}
