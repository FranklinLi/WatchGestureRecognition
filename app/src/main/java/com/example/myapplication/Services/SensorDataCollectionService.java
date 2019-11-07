package com.example.myapplication.Services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

import com.example.myapplication.Activities.MainMenuActivity;
import com.example.myapplication.Model.DataWriter;
import com.example.myapplication.Model.DesiredSensorsList;
import com.example.myapplication.Model.SettingsManager;
import com.example.myapplication.SensorRecordingRunnable;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class SensorDataCollectionService extends Service implements SensorEventListener {

    private final TriggerEventListener triggerEventListener = new MyTriggerEventListener();
    private HashMap<String, FileOutputStream> outputStreams = new HashMap<>();
    private int ii = 0;
    private HandlerThread mSensorThread;
    private Handler mSensorHandler;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("Sensor service", "onStartCommand");


        Intent notificationIntent = new Intent(this, MainMenuActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, "ForegroundServiceChannel")
                .setContentTitle("Foreground Service2")
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);


        // read settings (sensorsList, duration)
        HashMap<Integer, Boolean> sensorsList = (HashMap<Integer, Boolean>)
                SettingsManager.loadSettings(getFilesDir(), SettingsManager.SENSORS);

        int duration = (int) SettingsManager
                .loadSettings(getFilesDir(), SettingsManager.RECORDING_DURATION);

//        SensorRecordingRunnable myRunnable = new SensorRecordingRunnable(sensorsList, getApplicationContext());
//        Thread recordingThread = new Thread(myRunnable);
//        recordingThread.start();

        if(sensorsList == null) {
            Log.e("Error: (DataCollectionService)", "No sensor list available");
            return START_STICKY;
        }

        // create files
        try {
            outputStreams = DataWriter.createSensorFiles(_getSelectedSensors(sensorsList));
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(outputStreams == null) {
            Log.e("Error: (DataCollectionService)", "sensor files creation failed");
            return START_STICKY;
        }

        registerSensors(sensorsList);

        return START_STICKY;
    }

    private void registerSensors(HashMap<Integer, Boolean> sensorsList) {
        // start a new thread
        mSensorThread = new HandlerThread("Sensor thread", Thread.MAX_PRIORITY);
        mSensorThread.start();
        mSensorHandler = new Handler(mSensorThread.getLooper());

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        for(HashMap.Entry<Integer, Boolean> entry: sensorsList.entrySet()) {
            if(!entry.getValue()) {
                continue;
            }
            Sensor sensor = sensorManager.getDefaultSensor(entry.getKey());
            if (sensor == null) {continue;}
            if(sensor.getReportingMode() == Sensor.REPORTING_MODE_ONE_SHOT) {
                sensorManager.requestTriggerSensor(triggerEventListener, sensor);
                continue;
            }
            if(DesiredSensorsList.shouldRecord(sensor.getType())) {
                sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL, mSensorHandler);
            }

        }
    }

    private void unregisterSensors() {
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Log.e("Sensor service", "unregistered");
        mSensorThread.quitSafely();
        sensorManager.unregisterListener(this);
    }


    // sensor event listener methods
    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        try {
            if (ii % 1000 == 0) {
                sendMessage(ii);
                System.out.println(Thread.currentThread());
            }
            ii += 1;

            // Normal write
            // write timestamp
            outputStreams.get(event.sensor.getName())
                    .write(((event.timestamp / 1000000) + ",").getBytes());
            outputStreams.get(event.sensor.getName())
                    .write((System.currentTimeMillis() + ",").getBytes());
            // write sensor values
            float[] values = event.values;
            for (int i = 0; i < values.length; i++) {
                outputStreams.get(sensor.getName()).write(Float.toString(values[i]).getBytes());
                if (i != values.length - 1) {
                    outputStreams.get(event.sensor.getName()).write(", ".getBytes());
                } else {
                    outputStreams.get(event.sensor.getName()).write("\n".getBytes());
                }
            }

            // AsyncTask write


        } catch (IOException e) {
            Log.e("Error (DataCollectionService)", "failure in writing into the file");
            e.printStackTrace();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private List<String> _getSelectedSensors(HashMap<Integer, Boolean> sensorsList) {
        List<String> sensorNames = new ArrayList<>();

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        for(Map.Entry<Integer, Boolean> entry : sensorsList.entrySet()) {
            if(entry.getValue()) {
                Sensor sensor = sensorManager.getDefaultSensor(entry.getKey());
                if (sensor != null) {
                    sensorNames.add(sensor.getName());
                }
            }
        }
        return sensorNames;
    }




    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("Sensor service", "destroyed");
        //Toast.makeText(getApplicationContext(), "sensor", Toast.LENGTH_LONG).show();
        unregisterSensors();

    }

    // Supposing that your value is an integer declared somewhere as: int myInteger;
    private void sendMessage(int i) {
        // The string "my-integer" will be used to filer the intent
        Intent intent = new Intent("running_flag");
        // Adding some data
        intent.putExtra("message", i);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    //---------private classes--------------//
    private class MyTriggerEventListener extends TriggerEventListener {
        @Override
        public void onTrigger(TriggerEvent event) {
            Sensor sensor = event.sensor;
            try {
                // write timestamp
                outputStreams.get(sensor.getName())
                        .write(((event.timestamp / 1000000) + ",").getBytes());
                outputStreams.get(sensor.getName())
                        .write((System.currentTimeMillis() + ",").getBytes());
                // write sensor values
                float[] values = event.values;
                for (int i = 0; i < values.length; i++) {
                    outputStreams.get(sensor.getName()).write(Float.toString(values[i]).getBytes());
                    if (i != values.length - 1) {
                        outputStreams.get(sensor.getName()).write(", ".getBytes());
                    } else {
                        outputStreams.get(sensor.getName()).write("\n".getBytes());
                    }
                }
            } catch (IOException e) {
                Log.e("Error (DataCollectionService)",
                        "failure in writing into the file (onTrigger)");
                e.printStackTrace();
            }
        }
    }

    private class SensorEventLoggerTask extends AsyncTask<SensorEvent, Void, Void> {

        @Override
        protected Void doInBackground(SensorEvent... events) {
            SensorEvent event = events[0];
            Sensor sensor = event.sensor;
            try {
                if (ii % 500 == 0) {
                    System.out.println(ii);
                }
                ii += 1;
                // write timestamp
                outputStreams.get(sensor.getName())
                        .write(((event.timestamp / 1000000) + ",").getBytes());
                outputStreams.get(sensor.getName())
                        .write((System.currentTimeMillis() + ",").getBytes());
                // write sensor values
                float[] values = event.values;
                for (int i = 0; i < values.length; i++) {
                    outputStreams.get(sensor.getName()).write(Float.toString(values[i]).getBytes());
                    if (i != values.length - 1) {
                        outputStreams.get(sensor.getName()).write(", ".getBytes());
                    } else {
                        outputStreams.get(sensor.getName()).write("\n".getBytes());
                    }
                }

            } catch (IOException e) {
                Log.e("Error (DataCollectionService)", "failure in writing into the file");
                e.printStackTrace();
            }
            return null;
        }
    }
}
