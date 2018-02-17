package com.alfanthariq.hotspotid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alfanthariq.hotspotid.wifihotspotutils.WifiApManager;
import com.facebook.network.connectionclass.ConnectionClassManager;
import com.facebook.network.connectionclass.ConnectionQuality;
import com.facebook.network.connectionclass.DeviceBandwidthSampler;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.itemanimators.AlphaCrossFadeAnimator;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mingle.sweetpick.CustomDelegate;
import com.mingle.sweetpick.NoneEffect;
import com.mingle.sweetpick.SweetSheet;
import com.skyfishjy.library.RippleBackground;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import hotchemi.android.rate.AppRate;
import hotchemi.android.rate.OnClickButtonListener;

public class MainActivity extends AppCompatActivity {
    private SweetSheet mSweetSheet;
    private RelativeLayout rl;
    private Boolean isCreate = false;
    private Toolbar myToolbar = null;
    private Drawer mDrawer = null;
    private WifiApManager wifiApManager;
    private WifiConfiguration wifiConfig, wifiConfigNew;
    private RippleBackground rippleBackground;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Fonts font;
    private View viewPager;
    private TextView txtConnDev, txtMinutes, txtSeconds, txtNetSpeed;
    private Date startTime;
    private Timer updateTimer, speedTimer;
    private ConnectionQuality mConnectionClass = ConnectionQuality.UNKNOWN;
    private ConnectionClassManager mConnectionClassManager;
    private DeviceBandwidthSampler mDeviceBandwidthSampler;
    private String mURL = "http://alfanthariq.com/file/dummy.txt";
    private int mTries = 0;
    private double graphLastXValue = 5d;
    private LineGraphSeries<DataPoint> mSeries;
    private GraphView graph;
    private Intent intent;

    int versionCode = BuildConfig.VERSION_CODE;
    String versionName = BuildConfig.VERSION_NAME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Thread.setDefaultUncaughtExceptionHandler(new UnCaughtException(MainActivity.this));

        mConnectionClassManager = ConnectionClassManager.getInstance();
        mDeviceBandwidthSampler = DeviceBandwidthSampler.getInstance();

        viewPager = LayoutInflater.from(this).inflate(R.layout.layout_custom_view, null, false);
        font = new Fonts(this.getAssets());
        pref = getApplicationContext().getSharedPreferences("AppPref", MODE_PRIVATE);
        isCreate = true;
        wifiApManager = new WifiApManager(this);
        wifiConfig = wifiApManager.getWifiApConfiguration();
        rippleBackground=(RippleBackground)findViewById(R.id.contentRipple);
        rl = (RelativeLayout) findViewById(R.id.contentSweetSheet);
        txtConnDev = (TextView) viewPager.findViewById(R.id.txtConnectedDev);
        txtConnDev.setTypeface(font.getTf());
        txtConnDev.setText(Integer.toString(0));
        txtMinutes = (TextView) viewPager.findViewById(R.id.txtMinuteTime);
        txtMinutes.setTypeface(font.getTf());
        txtMinutes.setText(Integer.toString(0));
        txtSeconds = (TextView) viewPager.findViewById(R.id.txtSecondTime);
        txtSeconds.setTypeface(font.getTf());
        txtSeconds.setText(Integer.toString(0));

        txtNetSpeed = (TextView) viewPager.findViewById(R.id.txtNetSpeed);
        txtNetSpeed.setTypeface(font.getTf());
        txtNetSpeed.setText(mConnectionClassManager.getCurrentBandwidthQuality().toString());

        AppRate.with(this)
                .setInstallDays(7) // default 10, 0 means install day.
                .setLaunchTimes(10) // default 10
                .setRemindInterval(5) // default 1
                .setShowLaterButton(true) // default true
                .setDebug(false) // default false
                .setOnClickButtonListener(new OnClickButtonListener() { // callback listener.
                    @Override
                    public void onClickButton(int which) {}
                })
                .monitor();

        // Show a dialog if meets conditions
        AppRate.showRateDialogIfMeetsConditions(this);

        if (pref.getBoolean("isSchedulerOn", false)) {
            startService(new Intent(MainActivity.this, SchedulerService.class));
        }

        ImageView imageView=(ImageView)findViewById(R.id.centerImage);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!rippleBackground.isRippleAnimationRunning()) {
                    if (pref.getBoolean("isAPChange", false)) {
                        wifiConfigNew = new WifiConfiguration();
                        wifiConfigNew.SSID = pref.getString("SSID", "");
                        wifiConfigNew.preSharedKey = pref.getString("preSharedKey", "");
                        wifiConfigNew.status = WifiConfiguration.Status.ENABLED;
                        wifiConfigNew.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                        wifiConfigNew.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                        wifiConfigNew.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                        wifiConfigNew.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                        wifiConfigNew.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                        wifiConfigNew.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                        wifiConfigNew.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                        wifiConfigNew.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                        wifiConfigNew.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                        if (wifiApManager.setWifiApEnabled(wifiConfigNew, true)) {
                            rippleBackground.startRippleAnimation();
                            //Toast.makeText(MainActivity.this, "Enable hotspot", Toast.LENGTH_SHORT).show();
                            startService(new Intent(MainActivity.this, MainService.class));
                            Calendar calendar = Calendar.getInstance(Locale.getDefault());
                            startTime = calendar.getTime();
                            startTimer();
                            startTimerSpeed();
                        }
                    } else {
                        if (wifiApManager.setWifiApEnabled(wifiConfig, true)) {
                            rippleBackground.startRippleAnimation();
                            //Toast.makeText(MainActivity.this, "Enable hotspot", Toast.LENGTH_SHORT).show();
                            startService(new Intent(MainActivity.this, MainService.class));
                            Calendar calendar = Calendar.getInstance(Locale.getDefault());
                            startTime = calendar.getTime();
                            startTimer();
                            startTimerSpeed();
                        }
                    }
                } else {
                    if (wifiApManager.setWifiApEnabled(wifiConfig, false)) {
                        rippleBackground.stopRippleAnimation();
                        //Toast.makeText(MainActivity.this, "Disable hotspot", Toast.LENGTH_SHORT).show();
                        stopService(new Intent(MainActivity.this, MainService.class));
                        txtConnDev.setText(Integer.toString(0));
                        txtMinutes.setText(Integer.toString(0));
                        txtSeconds.setText(Integer.toString(0));
                        stopTimer();
                        stopTimerSpeed();
                    }
                }
            }
        });

        if (wifiApManager.isWifiApEnabled()) {
            rippleBackground.startRippleAnimation();
        } else {
            rippleBackground.stopRippleAnimation();
        }

        getPrefs();
        setupToolbar();
        setupNavDrawer(savedInstanceState);
        setupViewpager();
        setupBroadcastListener();
        graph = (GraphView) findViewById(R.id.graph);
        initGraph(graph);
        if (isNetworkAvailable()) {
            txtNetSpeed.setText("0 KB/s");
        } else {
            txtNetSpeed.setText("No internet access");
        }
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (wifiApManager.isWifiApEnabled()) {
                rippleBackground.startRippleAnimation();
                //Toast.makeText(MainActivity.this, "Enable hotspot", Toast.LENGTH_SHORT).show();
                startService(new Intent(MainActivity.this, MainService.class));
                Calendar calendar = Calendar.getInstance(Locale.getDefault());
                startTime = calendar.getTime();
                startTimer();
                startTimerSpeed();
            } else {
                rippleBackground.stopRippleAnimation();
                //Toast.makeText(MainActivity.this, "Disable hotspot", Toast.LENGTH_SHORT).show();
                stopService(new Intent(MainActivity.this, MainService.class));
                txtConnDev.setText(Integer.toString(0));
                txtMinutes.setText(Integer.toString(0));
                txtSeconds.setText(Integer.toString(0));
                stopTimer();
                stopTimerSpeed();
            }
        }
    };

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private class DownloadImage extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            mDeviceBandwidthSampler.startSampling();
        }

        @Override
        protected Void doInBackground(String... url) {
            String imageURL = url[0];
            try {
                // Open a stream to download the image from our URL.
                URLConnection connection = new URL(imageURL).openConnection();
                connection.setUseCaches(false);
                connection.connect();
                InputStream input = connection.getInputStream();
                try {
                    byte[] buffer = new byte[1024];

                    // Do some busy waiting while the stream is open.
                    while (input.read(buffer) != -1) {
                    }
                } finally {
                    input.close();
                }
            } catch (IOException e) {
                //Log.e("", "Error while downloading image.");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            mDeviceBandwidthSampler.stopSampling();
            // Retry for up to 10 times until we find a ConnectionClass.
            if (mConnectionClass == ConnectionQuality.UNKNOWN && mTries < 10) {
                mTries++;
                new DownloadImage().execute(mURL);
            }
        }
    }

    public void startTimerSpeed(){
        final Handler handler = new Handler();

        TimerTask timertask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            if (isNetworkAvailable()) {
                                double netSpeed=0;
                                new DownloadImage().execute(mURL);
                                if (mConnectionClassManager.getDownloadKBitsPerSecond()==-1) {
                                    netSpeed = 0;
                                } else {
                                    netSpeed = mConnectionClassManager.getDownloadKBitsPerSecond();
                                }
                                txtNetSpeed.setText(String.format("%.0f KB/s", netSpeed));

                                graphLastXValue += 0.25d;
                                mSeries.appendData(new DataPoint(graphLastXValue, netSpeed), true, 22);
                            } else {
                                txtNetSpeed.setText("No internet access");
                            }
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                        }
                    }
                });
            }
        };
        speedTimer = new Timer();
        speedTimer.schedule(timertask, 0, 5000); // execute in every 15sec
    }

    public void stopTimerSpeed(){
        speedTimer.cancel();
        speedTimer.purge();
    }

    public void startTimer() {
        final Handler handler = new Handler();

        TimerTask timertask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            Calendar calendar = Calendar.getInstance(Locale.getDefault());
                            long mills = calendar.getTimeInMillis() - startTime.getTime();
                            int hours = (int) (mills/(1000 * 60 * 60));
                            int mins = (int) (mills/(1000*60)) % 60;
                            int secs = (int) (mills / 1000) % 60;
                            txtMinutes.setText(Integer.toString(mins));
                            txtSeconds.setText(Integer.toString(secs));
                            editor = pref.edit();
                            editor.putInt("minuteRunning", mins);
                            editor.apply();
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                        }
                    }
                });
            }
        };
        updateTimer = new Timer();
        updateTimer.schedule(timertask, 0, 1000); // execute in every 15sec
    }

    private void stopTimer(){
        updateTimer.cancel();
        updateTimer.purge();
    }

    private void setupBroadcastListener(){
        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        int connDev = intent.getIntExtra("connectedDevice", 0);
                        TextView txtConnDev = (TextView) viewPager.findViewById(R.id.txtConnectedDev);
                        txtConnDev.setText(Integer.toString(connDev));
                    }
                }, new IntentFilter(MainService.ACTION_CLIENT_LIST_BROADCAST)
        );
    }

    private void setupNavDrawer(Bundle savedInstanceState){
        mDrawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(myToolbar)
                .withHasStableIds(true)
                .withItemAnimator(new AlphaCrossFadeAnimator())
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(R.string.drawer_item_manager).
                                withIcon(GoogleMaterial.Icon.gmd_wifi_tethering).withIdentifier(1).withSelectable(false),
                        new PrimaryDrawerItem().withName(R.string.drawer_item_settings).
                                withIcon(GoogleMaterial.Icon.gmd_settings).withIdentifier(2).withSelectable(false),
                        new SectionDrawerItem().withName("Give me some star"),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_contact).
                                withIcon(GoogleMaterial.Icon.gmd_mail).withIdentifier(4).withSelectable(false),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_share).
                                withIcon(GoogleMaterial.Icon.gmd_share).withIdentifier(5).withSelectable(false),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_rate).
                                withIcon(GoogleMaterial.Icon.gmd_thumb_up).withIdentifier(6).withSelectable(false)
                ) // add the items we want to use with our Drawer
                .addStickyDrawerItems(new PrimaryDrawerItem().withName("version "+ versionName +" © 2017").withIdentifier(7))
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        //check if the drawerItem is set.
                        //there are different reasons for the drawerItem to be null
                        //--> click on the header
                        //--> click on the footer
                        //those items don't contain a drawerItem

                        if (drawerItem != null) {
                            Intent intent = null;
                            if (drawerItem.getIdentifier() == 1) {
                                intent = new Intent(MainActivity.this, HotspotManager.class);
                            } else
                            if (drawerItem.getIdentifier() == 2) {
                                intent = new Intent(MainActivity.this, SettingsActivity.class);
                            } else
                            if (drawerItem.getIdentifier() == 3) {  //About

                            } else
                            if (drawerItem.getIdentifier() == 4) {  //Contact us
                                final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

                                /* Fill it with Data */
                                emailIntent.setType("message/rfc822");
                                emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"contact@alfanthariq.com"});
                                emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Contact from apps");
                                emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "");

                                /* Send it off to the Activity-Chooser */
                                MainActivity.this.startActivity(Intent.createChooser(emailIntent, "Choose mail apps"));
                            } else
                            if (drawerItem.getIdentifier() == 5) {  //Share
                                try {
                                    Intent i = new Intent(Intent.ACTION_SEND);
                                    i.setType("text/plain");
                                    i.putExtra(Intent.EXTRA_SUBJECT, "HotspotID");
                                    String sAux = "\nLet me recommend you this application\n\n";
                                    sAux = sAux + "https://play.google.com/store/apps/details?id=com.alfanthariq.hotspotid \n\n";
                                    i.putExtra(Intent.EXTRA_TEXT, sAux);
                                    startActivity(Intent.createChooser(i, "Share with"));
                                } catch(Exception e) {
                                    //e.toString();
                                }
                            } else
                            if (drawerItem.getIdentifier() == 6) {  //Rate us
                                AppRate.with(MainActivity.this).clearAgreeShowDialog();
                                AppRate.with(MainActivity.this).showRateDialog(MainActivity.this);
                            }
                            if (intent != null) {
                                MainActivity.this.startActivity(intent);
                            }
                        }

                        return false;
                    }
                })
                .withSavedInstance(savedInstanceState)
                .withShowDrawerOnFirstLaunch(true)
                .withShowDrawerUntilDraggedOpened(true)
                .build();

        mDrawer.setSelection(7);
        mDrawer.closeDrawer();
    }

    private void setupViewpager() {
        if (mSweetSheet != null) {
            mSweetSheet.dismiss();
            mSweetSheet = null;
        }
        mSweetSheet = new SweetSheet(rl);

        //从menu 中设置数据源
        //mSweetSheet.setMenuList(R.menu.menu_sweet);
        CustomDelegate customDelegate = new CustomDelegate(true,
                CustomDelegate.AnimationType.DuangAnimation);

        if(viewPager.getParent()!=null)
            ((ViewGroup)viewPager.getParent()).removeView(viewPager);
        customDelegate.setCustomView(viewPager);
        mSweetSheet.setDelegate(customDelegate);
        mSweetSheet.setBackgroundEffect(new NoneEffect());
        mSweetSheet.setBackgroundClickEnable(false);
        mSweetSheet.toggle();
    }

    protected void setupToolbar() {
        myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initGraph(GraphView graph) {
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(4);

        graph.getGridLabelRenderer().setLabelVerticalWidth(100);

        // first mSeries is a line
        mSeries = new LineGraphSeries<>();
        mSeries.setDrawDataPoints(true);
        mSeries.setDrawBackground(true);
        graph.addSeries(mSeries);
    }

    @Override
    public void onBackPressed() {
        if (mDrawer != null && mDrawer.isDrawerOpen()) {
            mDrawer.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    private void getPrefs() {
        pref = getApplicationContext().getSharedPreferences("AppPref", MODE_PRIVATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (speedTimer!=null) {
            stopTimerSpeed();
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();

        registerReceiver(broadcastReceiver, new IntentFilter(
                SchedulerService.BROADCAST_ACTION));

        if (wifiApManager.isWifiApEnabled()) {
            rippleBackground.startRippleAnimation();
            startTimerSpeed();
        } else {
            rippleBackground.stopRippleAnimation();
        }

        if (isCreate == false) {
            setupViewpager();
        }

        if (isNetworkAvailable()) {
            txtNetSpeed.setText("0 KB/s");
        } else {
            txtNetSpeed.setText("No internet access");
        }
        isCreate = false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //add the values which need to be saved from the drawer to the bundle
        outState = mDrawer.saveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }
}
