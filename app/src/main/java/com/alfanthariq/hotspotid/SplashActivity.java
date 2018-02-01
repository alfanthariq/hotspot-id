package com.alfanthariq.hotspotid;

import android.app.TaskStackBuilder;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Start home activity
        startActivity(new Intent(SplashActivity.this, IntroActivity.class));
        // close splash activity
        finish();
    }
}
