package com.alfanthariq.hotspotid;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.alfanthariq.hotspotid.wifihotspotutils.ClientScanResult;
import com.alfanthariq.hotspotid.wifihotspotutils.FinishScanListener;
import com.alfanthariq.hotspotid.wifihotspotutils.WifiApManager;

import java.util.ArrayList;

public class MainService extends Service {
    private Looper mServiceLooper;
    private MainService.ServiceHandler mServiceHandler;
    boolean loop;
    int connectedDevice;
    SharedPreferences pref;
    WifiApManager wifiApManager;
    WifiConfiguration wifiConfig;
    public static final String ACTION_CLIENT_LIST_BROADCAST = "com.alfanthariq.hotspotid.MainActivity";

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

        wifiApManager = new WifiApManager(this);
        wifiConfig = wifiApManager.getWifiApConfiguration();
        wifiApManager.getClientList(true, 300, new FinishScanListener() {
            @Override
            public void onFinishScan(ArrayList<ClientScanResult> clients) {
                connectedDevice = clients.size();
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // call a new service handler. The service ID can be used to identify the service
        Message message = mServiceHandler.obtainMessage();
        message.arg1 = startId;
        mServiceHandler.sendMessage(message);
        loop = true;

        return START_STICKY;
    }

    private void getPrefs() {
        pref = getApplicationContext().getSharedPreferences("AppPref", MODE_PRIVATE);

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
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
                wifiApManager.getClientList(false, new FinishScanListener() {
                    @Override
                    public void onFinishScan(ArrayList<ClientScanResult> clients) {
                        int clientCount = clients.size();
                        //int clientCount = 0;
                        if (connectedDevice!=clientCount) {
                            connectedDevice = clientCount;
                            sendBroadcastMessage();
                        }
                    }
                });
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            // the msg.arg1 is the startId used in the onStartCommand,
            // so we can track the running sevice here.

            // Add your cpu-blocking activity here
        }
    }

    private void sendBroadcastMessage() {
        Intent intent = new Intent(ACTION_CLIENT_LIST_BROADCAST);
        intent.putExtra("connectedDevice", connectedDevice);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onDestroy(){
        loop = false;
        super.onDestroy();
    }
}
