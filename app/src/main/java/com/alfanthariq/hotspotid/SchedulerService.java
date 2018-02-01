package com.alfanthariq.hotspotid;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration;
import android.os.*;
import android.os.Process;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.alfanthariq.hotspotid.wifihotspotutils.WifiApManager;

import java.util.Calendar;
import java.util.Locale;

public class SchedulerService extends Service {
    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    boolean loop;
    NotificationCompat.Builder mBuilder;
    Notification notification;
    NotificationManager mNotificationManager;
    int notifyID = 1203;
    SharedPreferences pref;
    Boolean isScheduleOp1, isScheduleOp2,
            isScheduleOp3, isScheduleOp4, isScheduleOp5;
    String turnOnTime, turnOffTime;
    int timeLimit, batteryLevel, currBattLevel, minuteRunning;
    WifiApManager wifiApManager;
    WifiConfiguration wifiConfig;

    @Override
    public void onCreate() {
        // To avoid cpu-blocking, we create a background handler to run our service
        HandlerThread thread = new HandlerThread("TutorialService",
                Process.THREAD_PRIORITY_BACKGROUND);
        // start the new handler thread
        thread.start();

        mServiceLooper = thread.getLooper();
        // start the service using the background handler
        mServiceHandler = new ServiceHandler(mServiceLooper);

        mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(getNotificationIcon())
                .setContentTitle("HotspotID")
                .setOngoing(true)
                .setColor(getResources().getColor(R.color.colorPrimaryDark))
                .setContentText("Your scheduler was running");
        notification = mBuilder.build();

        wifiApManager = new WifiApManager(this);
        wifiConfig = wifiApManager.getWifiApConfiguration();
        this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    private int getNotificationIcon() {
        boolean useWhiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.drawable.ic_launcher_white : R.drawable.ic_launcher;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Toast.makeText(this, "onStartCommand", Toast.LENGTH_SHORT).show();

        // call a new service handler. The service ID can be used to identify the service
        Message message = mServiceHandler.obtainMessage();
        message.arg1 = startId;
        mServiceHandler.sendMessage(message);
        loop = true;

        mNotificationManager.notify(notifyID, notification);
        return START_STICKY;
    }

    protected void showToast(final String msg){
        //gets the main thread
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                // run this code in the main thread
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getPrefs() {
        pref = getApplicationContext().getSharedPreferences("AppPref", MODE_PRIVATE);

        isScheduleOp1 = pref.getBoolean("isScheduleOp1", false);
        isScheduleOp2 = pref.getBoolean("isScheduleOp2", false);
        isScheduleOp3 = pref.getBoolean("isScheduleOp3", false);
        isScheduleOp4 = pref.getBoolean("isScheduleOp4", false);
        isScheduleOp5 = pref.getBoolean("isScheduleOp5", false);

        turnOnTime = pref.getString("turnOnTime", "00:00");
        turnOffTime = pref.getString("turnOffTime", "00:00");

        timeLimit = pref.getInt("timeLimit", 30);
        batteryLevel = pref.getInt("batteryLevel", 10);
        minuteRunning = pref.getInt("minuteRunning", 0);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context ctxt, Intent intent) {
            currBattLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            //showToast(currBattLevel+"%");
        }
    };

    // Object responsible for
    private final class ServiceHandler extends Handler {

        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            // Well calling mServiceHandler.sendMessage(message); from onStartCommand,
            // this method will be called.
            while (loop) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                getPrefs();
                Calendar calendar = Calendar.getInstance(Locale.getDefault());
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                if (isScheduleOp1) {
                    String[] time = turnOnTime.split ( ":" );
                    int scHour = Integer.parseInt ( time[0].trim() );
                    int scMin = Integer.parseInt ( time[1].trim() );

                    if (scHour==hour && scMin==minute) {
                        wifiApManager.setWifiApEnabled(wifiConfig, true);
                    }
                }

                if (isScheduleOp2) {
                    String[] time = turnOffTime.split ( ":" );
                    int scHour = Integer.parseInt ( time[0].trim() );
                    int scMin = Integer.parseInt ( time[1].trim() );

                    if (scHour==hour && scMin==minute) {
                        wifiApManager.setWifiApEnabled(wifiConfig, false);
                    }
                }

                if (isScheduleOp3) {
                    if (timeLimit==minuteRunning) {
                        wifiApManager.setWifiApEnabled(wifiConfig, false);
                    }
                }

                if (isScheduleOp4) {
                    if (batteryLevel==currBattLevel) {
                        wifiApManager.setWifiApEnabled(wifiConfig, false);
                    }
                }
            }
            // the msg.arg1 is the startId used in the onStartCommand,
            // so we can track the running sevice here.

            // Add your cpu-blocking activity here
        }
    }

    @Override
    public void onDestroy(){
        loop = false;
        mNotificationManager.cancel(notifyID);
        super.onDestroy();
    }
}
