package com.alfanthariq.hotspotid;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.FloatRange;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import agency.tango.materialintroscreen.MaterialIntroActivity;
import agency.tango.materialintroscreen.MessageButtonBehaviour;
import agency.tango.materialintroscreen.SlideFragmentBuilder;
import agency.tango.materialintroscreen.animations.IViewTranslation;

public class IntroActivity extends MaterialIntroActivity {

    SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pref = getApplicationContext().getSharedPreferences("AppPref", MODE_PRIVATE);

        if(pref.contains("configured")){
            Intent intent = new Intent(IntroActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        enableLastSlideAlphaExitTransition(true);

        getBackButtonTranslationWrapper()
                .setEnterTranslation(new IViewTranslation() {
                    @Override
                    public void translate(View view, @FloatRange(from = 0, to = 1.0) float percentage) {
                        view.setAlpha(percentage);
                    }
                });

        addSlide(new IntroSlide());

        addSlide(new PermissionSlide(),
                new MessageButtonBehaviour(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                                    Uri.parse("package:"+getPackageName()));
                            startActivityForResult(intent, 200);
                        }
                    }
                }, "Grant Permission")
        );

        addSlide(new FinishSlide());
    }

    @Override
    public void onFinish() {
        super.onFinish();
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("configured", true);
        editor.putBoolean("isSchedulerOn", false);
        editor.putBoolean("isScheduleOp1", false);
        editor.putString("turnOnTime", "00:00");
        editor.putBoolean("isScheduleOp2", false);
        editor.putString("turnOffTime", "00:00");
        editor.putBoolean("isScheduleOp3", false);
        editor.putInt("timeLimit", 30);
        editor.putBoolean("isScheduleOp4", false);
        editor.putInt("batteryLevel", 10);
        editor.putBoolean("isScheduleOp5", false);
        editor.commit();

        Intent intent = new Intent(IntroActivity.this, MainActivity.class);
        startActivity(intent);
    }
}
