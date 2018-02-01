package com.alfanthariq.hotspotid;

import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.alfanthariq.hotspotid.wifihotspotutils.WifiApManager;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.rey.material.widget.Button;
import com.rey.material.widget.CheckBox;
import com.rey.material.widget.EditText;

public class HotspotManager extends AppCompatActivity {
    //FloatLabeledEditText flEdSSID, flEdPassword;
    CheckBox cbShowPwd;
    EditText edSSID, edPwd;
    Button btnSave;
    WifiApManager wifiApManager;
    WifiConfiguration wifiConfig;
    Fonts fs;
    TextView tx;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    private Toolbar myToolbar = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotspot_manager);
        Thread.setDefaultUncaughtExceptionHandler(new UnCaughtException(HotspotManager.this));

        pref = getApplicationContext().getSharedPreferences("AppPref", MODE_PRIVATE);

        fs = new Fonts(this.getAssets());
        tx = (TextView) findViewById(R.id.title);
        tx.setTypeface(fs.getTfb());

        //flEdSSID = (FloatLabeledEditText) findViewById(R.id.flSSID);
        //flEdPassword = (FloatLabeledEditText) findViewById(R.id.flPassword);
        edSSID = (EditText) findViewById(R.id.edSSID);
        edPwd = (EditText) findViewById(R.id.edPassword);

        cbShowPwd = (CheckBox) findViewById(R.id.cbShowPwd);
        btnSave = (Button) findViewById(R.id.btnSave);

        wifiApManager = new WifiApManager(this);
        wifiConfig = wifiApManager.getWifiApConfiguration();

        edSSID.setText(wifiConfig.SSID);
        edPwd.setText(wifiConfig.preSharedKey);

        final MaterialSpinner spinner = (MaterialSpinner) findViewById(R.id.spPwd);
        spinner.setItems("No Password", "WPA2 PSK");
        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {
            @Override public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                switch (position) {
                    case 0:
                        edPwd.setVisibility(View.INVISIBLE);
                        cbShowPwd.setVisibility(View.INVISIBLE);
                        break;
                    case 1:
                        edPwd.setVisibility(View.VISIBLE);
                        cbShowPwd.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });

        cbShowPwd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b==true) {
                    edPwd.setTransformationMethod(null);
                } else {
                    edPwd.setTransformationMethod(new PasswordTransformationMethod());
                }
            }
        });

        if (!edPwd.getText().toString().equals("")) {
            spinner.setSelectedIndex(1);
        } else {
            spinner.setSelectedIndex(0);
        }


        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editor = pref.edit();
                editor.putString("SSID", edSSID.getText().toString());
                if (spinner.getSelectedIndex()==1) {
                    editor.putString("preSharedKey", edPwd.getText().toString());
                } else {
                    editor.putString("preSharedKey", "");
                }
                editor.putBoolean("isAPChange", true);
                editor.apply();

                if (wifiApManager.isWifiApEnabled()) {
                    Toast.makeText(HotspotManager.this, "Hotspot configuration has been save, please restart your hotspot", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(HotspotManager.this, "Hotspot configuration has been save", Toast.LENGTH_SHORT).show();
                }
            }
        });

        myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
    }
}
