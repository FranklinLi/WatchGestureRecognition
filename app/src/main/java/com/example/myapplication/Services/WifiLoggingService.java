package com.example.myapplication.Services;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.example.myapplication.Activities.MainMenuActivity;
import com.example.myapplication.Model.DataWriter;

import java.io.FileOutputStream;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import androidx.core.app.NotificationCompat;

public class WifiLoggingService extends Service {

    private WifiManager wifiManager;
    private List<ScanResult> scanResults;
    private WiFiBroadcastReceiver wiFiBroadcastReceiver;
    private Handler wifiScanHandler;
    private FileOutputStream outputStream = null;
    private boolean stopScanning = true;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Intent notificationIntent = new Intent(this, MainMenuActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, "ForegroundServiceChannel")
                .setContentTitle("Foreground Service2")
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

        stopScanning = false;

        // create the file
        try {
            outputStream = new FileOutputStream(DataWriter.createWifiLogFile());
        } catch (Exception e) {
            stopSelf();
            Toast.makeText(this, "Error in creating location log file",
                    Toast.LENGTH_LONG).show();
            return START_STICKY;
        }

        // setup wifi signal logging
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);

        wiFiBroadcastReceiver = new WiFiBroadcastReceiver();
        registerReceiver(wiFiBroadcastReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        wifiScanHandler = new Handler();
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    while(!stopScanning) {
                        System.out.println("wifi thread: " + Thread.currentThread());
                        wifiManager.startScan();
                        sleep(2*1000);
                        //wifiScanHandler.post(this);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        thread.start();

        return START_STICKY;
    }

    class WiFiBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            scanResults = wifiManager.getScanResults();
            for (ScanResult scanResult : scanResults) {
                if (outputStream != null) {
                    try {
                        outputStream.write((System.currentTimeMillis() + ", ").getBytes());
                        outputStream.write((scanResult.SSID + ", ").getBytes());
                        outputStream.write((scanResult.BSSID + ",").getBytes());
                        outputStream.write((scanResult.level + "\n").getBytes());
                    } catch (Exception e) {
                        stopSelf();
                        Log.e("Error (LocationLogService)", "writing into the file failure");
                        e.printStackTrace();
                    }
                }
            }
        }

    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        stopScanning = true;
        unregisterReceiver(wiFiBroadcastReceiver);
        // TODO: it is not getting unregistered
        wifiScanHandler.removeCallbacksAndMessages(null);
        System.out.println("wifi logger onDestroy");
    }
}
