package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.myapplication.Model.DataWriter;
import com.example.myapplication.Model.DesiredSensorsList;
import com.example.myapplication.Services.SensorDataCollectionService;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SensorRecordingRunnable implements Runnable {

    private HashMap<Integer, Boolean> sensorsList;
    private Context context;
    private int ii = 0;
    private final TriggerEventListener triggerEventListener = new MyTriggerEventListener();
    private MySensorEventListener sensorEventListener = new MySensorEventListener();


    public SensorRecordingRunnable(HashMap<Integer, Boolean> sensorsList, Context context) {
        this.sensorsList = sensorsList;
        this.context = context;
    }

    private HashMap<String, FileOutputStream> outputStreams = new HashMap<>();

    @Override
    public void run() {
        // create files
        try {
            outputStreams = DataWriter.createSensorFiles(_getSelectedSensors(sensorsList));
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(outputStreams == null) {
            Log.e("Error: (DataCollectionService)", "sensor files creation failed");
            System.out.println("ERRORRRRRRRRR");
        }

        registerSensors(sensorsList);
    }

    // Supposing that your value is an integer declared somewhere as: int myInteger;
    private void sendMessage(int i) {
        // The string "my-integer" will be used to filer the intent
        Intent intent = new Intent("running_flag");
        // Adding some data
        intent.putExtra("message", i);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }



    private void registerSensors(HashMap<Integer, Boolean> sensorsList) {

        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
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
                sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
            }

        }
    }

    private void unregisterSensors() {
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        Log.e("Sensor service", "unregistered");
        sensorManager.unregisterListener(sensorEventListener);
    }

    //---------private classes--------------//
    private class MySensorEventListener implements SensorEventListener {
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

            } catch (IOException e) {
                Log.e("Error (DataCollectionService)", "failure in writing into the file");
                e.printStackTrace();
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }


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

    private List<String> _getSelectedSensors(HashMap<Integer, Boolean> sensorsList) {
        List<String> sensorNames = new ArrayList<>();

        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
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
}
