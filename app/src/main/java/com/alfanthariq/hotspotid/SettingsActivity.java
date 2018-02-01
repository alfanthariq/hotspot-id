package com.alfanthariq.hotspotid;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.SwitchPreference;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatCheckedTextView;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import android.widget.Toast;

import com.rey.material.app.Dialog;
import com.rey.material.app.DialogFragment;
import java.util.Calendar;

import biz.kasual.materialnumberpicker.MaterialNumberPicker;

/**
 * Created by alfanthariq on 30/10/2017.
 */

public class SettingsActivity extends PreferenceActivity {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    SwitchPreference isSchedulerOn, isScheduleOp1, isScheduleOp2, isScheduleOp3, isScheduleOp4, isScheduleOp5;
    Dialog.Builder builder = null;

    public static DialogFragment newInstance(){
        DialogFragment fragment = new DialogFragment();
        return fragment;
    }

    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new UnCaughtException(SettingsActivity.this));

        getPrefs();

        LinearLayout root = (LinearLayout)findViewById(android.R.id.list).getParent().getParent().getParent();
        Toolbar bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.toolbar2, root, false);
        root.addView(bar, 0); // insert at top
        bar.setTitle("Settings");
        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        setupSimplePreferencesScreen();

        isSchedulerOn = (SwitchPreference)getPreferenceManager().findPreference("isSchedulerOn");
        isScheduleOp1 = (SwitchPreference)getPreferenceManager().findPreference("isScheduleOp1");
        isScheduleOp2 = (SwitchPreference)getPreferenceManager().findPreference("isScheduleOp2");
        isScheduleOp3 = (SwitchPreference)getPreferenceManager().findPreference("isScheduleOp3");
        isScheduleOp4 = (SwitchPreference)getPreferenceManager().findPreference("isScheduleOp4");
        isScheduleOp5 = (SwitchPreference)getPreferenceManager().findPreference("isScheduleOp5");
        Preference.OnPreferenceClickListener listener = new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                String key = preference.getKey();

                switch (key){
                    case "isSchedulerOn":
                        if (isSchedulerOn.isChecked()) {
                            Toast.makeText(SettingsActivity.this, "Scheduler On", Toast.LENGTH_SHORT).show();
                            isScheduleOp1.setEnabled(true);
                            isScheduleOp2.setEnabled(true);
                            isScheduleOp3.setEnabled(true);
                            isScheduleOp4.setEnabled(true);
                            isScheduleOp5.setEnabled(true);
                            startService(new Intent(SettingsActivity.this, SchedulerService.class));
                        } else {
                            Toast.makeText(SettingsActivity.this, "Scheduler Off", Toast.LENGTH_SHORT).show();
                            isScheduleOp1.setEnabled(false);
                            isScheduleOp2.setEnabled(false);
                            isScheduleOp3.setEnabled(false);
                            isScheduleOp4.setEnabled(false);
                            isScheduleOp5.setEnabled(false);
                            stopService(new Intent(SettingsActivity.this, SchedulerService.class));
                        }
                        editor = pref.edit();
                        editor.putBoolean("isSchedulerOn", isSchedulerOn.isChecked());
                        editor.apply();
                        break;
                    case "isScheduleOp1":
                        if (isScheduleOp1.isChecked()) {
                            Calendar mcurrentTime = Calendar.getInstance();
                            int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                            int minute = mcurrentTime.get(Calendar.MINUTE);
                            TimePickerDialog mTimePicker;
                            mTimePicker = new TimePickerDialog(SettingsActivity.this, new TimePickerDialog.OnTimeSetListener() {
                                @Override
                                public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                                    String time = String.format("%02d:%02d", selectedHour, selectedMinute);
                                    editor = pref.edit();
                                    editor.putString("turnOnTime", time);
                                    editor.apply();
                                    isScheduleOp1.setTitle("Turn on hotspot every day at "+pref.getString("turnOnTime", "hh:mm"));
                                }
                            }, hour, minute, true);//Yes 24 hour time
                            mTimePicker.setCancelable(false);
                            mTimePicker.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    if (which == DialogInterface.BUTTON_NEGATIVE) {
                                        isScheduleOp1.setChecked(false);
                                    }
                                }
                            });
                            mTimePicker.setTitle("Select Time");
                            mTimePicker.show();
                        }
                        editor = pref.edit();
                        editor.putBoolean("isScheduleOp1", isScheduleOp1.isChecked());
                        editor.apply();
                        break;
                    case "isScheduleOp2":
                        if (isScheduleOp2.isChecked()) {
                            Calendar mcurrentTime = Calendar.getInstance();
                            int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                            int minute = mcurrentTime.get(Calendar.MINUTE);
                            TimePickerDialog mTimePicker;
                            mTimePicker = new TimePickerDialog(SettingsActivity.this, new TimePickerDialog.OnTimeSetListener() {
                                @Override
                                public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                                    String time = String.format("%02d:%02d", selectedHour, selectedMinute);
                                    editor = pref.edit();
                                    editor.putString("turnOffTime", time);
                                    editor.apply();
                                    isScheduleOp2.setTitle("Turn off hotspot every day at "+pref.getString("turnOffTime", "hh:mm"));
                                }
                            }, hour, minute, true);//Yes 24 hour time
                            mTimePicker.setCancelable(false);
                            mTimePicker.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    if (which == DialogInterface.BUTTON_NEGATIVE) {
                                        isScheduleOp2.setChecked(false);
                                    }
                                }
                            });
                            mTimePicker.setTitle("Select Time");
                            mTimePicker.show();
                        }
                        editor = pref.edit();
                        editor.putBoolean("isScheduleOp2", isScheduleOp2.isChecked());
                        editor.apply();
                        break;
                    case "isScheduleOp3":
                        if (isScheduleOp3.isChecked()) {
                            final MaterialNumberPicker numberPicker = new MaterialNumberPicker.Builder(SettingsActivity.this)
                                    .minValue(5)
                                    .maxValue(720)
                                    .defaultValue(pref.getInt("timeLimit", 30))
                                    .backgroundColor(Color.WHITE)
                                    .separatorColor(Color.TRANSPARENT)
                                    .textColor(Color.BLACK)
                                    .textSize(20)
                                    .enableFocusability(false)
                                    .wrapSelectorWheel(true)
                                    .build();

                            new AlertDialog.Builder(SettingsActivity.this)
                                    .setTitle("Set Minutes")
                                    .setView(numberPicker)
                                    .setPositiveButton(getString(R.string.set), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            editor = pref.edit();
                                            editor.putInt("timeLimit", numberPicker.getValue());
                                            editor.apply();
                                            isScheduleOp3.setTitle("Turn off hotspot in "+pref.getInt("timeLimit", 30)+" minute");
                                        }
                                    })
                                    .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            isScheduleOp3.setChecked(false);
                                        }
                                    })
                                    .setCancelable(false)
                                    .show();
                        }
                        editor = pref.edit();
                        editor.putBoolean("isScheduleOp3", isScheduleOp3.isChecked());
                        editor.apply();
                        break;
                    case "isScheduleOp4":
                        if (isScheduleOp4.isChecked()) {
                            final MaterialNumberPicker numberPicker = new MaterialNumberPicker.Builder(SettingsActivity.this)
                                    .minValue(1)
                                    .maxValue(100)
                                    .defaultValue(pref.getInt("batteryLevel", 10))
                                    .backgroundColor(Color.WHITE)
                                    .separatorColor(Color.TRANSPARENT)
                                    .textColor(Color.BLACK)
                                    .textSize(20)
                                    .enableFocusability(false)
                                    .wrapSelectorWheel(true)
                                    .build();

                            new AlertDialog.Builder(SettingsActivity.this)
                                    .setTitle("Set Level %")
                                    .setView(numberPicker)
                                    .setPositiveButton(getString(R.string.set), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            editor = pref.edit();
                                            editor.putInt("batteryLevel", numberPicker.getValue());
                                            editor.apply();
                                            isScheduleOp4.setTitle("Turn off hotspot when battery at "+pref.getInt("batteryLevel", 10)+"%");
                                        }
                                    })
                                    .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            isScheduleOp4.setChecked(false);
                                        }
                                    })
                                    .setCancelable(false)
                                    .show();
                        }
                        editor = pref.edit();
                        editor.putBoolean("isScheduleOp4", isScheduleOp4.isChecked());
                        editor.apply();
                        break;
                    case "isScheduleOp5":
                        editor = pref.edit();
                        editor.putBoolean("isScheduleOp5", isScheduleOp5.isChecked());
                        editor.apply();
                        break;
                }

                return false;
            }
        };
        isSchedulerOn.setOnPreferenceClickListener(listener);
        isScheduleOp1.setOnPreferenceClickListener(listener);
        isScheduleOp2.setOnPreferenceClickListener(listener);
        isScheduleOp3.setOnPreferenceClickListener(listener);
        isScheduleOp4.setOnPreferenceClickListener(listener);
        isScheduleOp5.setOnPreferenceClickListener(listener);
        isSchedulerOn.setChecked(pref.getBoolean("isSchedulerOn", false));

        if (isSchedulerOn.isChecked()) {
            //Toast.makeText(SettingsActivity.this, "Scheduler On", Toast.LENGTH_SHORT).show();
            isScheduleOp1.setEnabled(true);
            isScheduleOp2.setEnabled(true);
            isScheduleOp3.setEnabled(true);
            isScheduleOp4.setEnabled(true);
            isScheduleOp5.setEnabled(true);
            startService(new Intent(SettingsActivity.this, SchedulerService.class));
        } else {
            //Toast.makeText(SettingsActivity.this, "Scheduler Off", Toast.LENGTH_SHORT).show();
            isScheduleOp1.setEnabled(false);
            isScheduleOp2.setEnabled(false);
            isScheduleOp3.setEnabled(false);
            isScheduleOp4.setEnabled(false);
            isScheduleOp5.setEnabled(false);
            stopService(new Intent(SettingsActivity.this, SchedulerService.class));
        }

        setPrefTitle();
    }

    private void getPrefs() {
        pref = getApplicationContext().getSharedPreferences("AppPref", MODE_PRIVATE);
    }

    private void setPrefTitle(){
        isScheduleOp1.setTitle("Turn on hotspot every day at "+pref.getString("turnOnTime", "hh:mm"));
        isScheduleOp2.setTitle("Turn off hotspot every day at "+pref.getString("turnOffTime", "hh:mm"));
        isScheduleOp3.setTitle("Turn off hotspot in "+pref.getInt("timeLimit", 30)+" minute");
        isScheduleOp4.setTitle("Turn off hotspot when battery at "+pref.getInt("batteryLevel", 10)+"%");
    }

    @SuppressWarnings("deprecation")
    private void setupSimplePreferencesScreen() {
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        // Allow super to try and create a view first
        final View result = super.onCreateView(name, context, attrs);
        if (result != null) {
            return result;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            // If we're running pre-L, we need to 'inject' our tint aware Views in place of the
            // standard framework versions
            switch (name) {
                case "EditText":
                    return new AppCompatEditText(this, attrs);
                case "Spinner":
                    return new AppCompatSpinner(this, attrs);
                case "CheckBox":
                    return new AppCompatCheckBox(this, attrs);
                case "RadioButton":
                    return new AppCompatRadioButton(this, attrs);
                case "CheckedTextView":
                    return new AppCompatCheckedTextView(this, attrs);
            }
        }

        return null;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {

    }
}
