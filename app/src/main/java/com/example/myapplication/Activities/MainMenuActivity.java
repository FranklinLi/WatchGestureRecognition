package com.example.myapplication.Activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.widget.Toast;

import com.example.myapplication.Adapters.ActionsListAdapter;
import com.example.myapplication.Services.AudioRecordingService;
import com.example.myapplication.Services.LocationLogService;
import com.example.myapplication.R;
import com.example.myapplication.Services.SensorDataCollectionService;
import com.example.myapplication.Model.SettingsManager;
import com.example.myapplication.Services.WifiLoggingService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.wear.widget.WearableLinearLayoutManager;
import androidx.wear.widget.WearableRecyclerView;


public class MainMenuActivity extends WearableActivity {


    private WearableRecyclerView recyclerView;
    private ActionsListAdapter actionsListAdapter;
    private HashMap<String, Boolean> resourcePermissions = new HashMap<>();
    private boolean recordAudio = false;
    private boolean recordLocation = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        Log.w("before","Logcat save");
        // File logFile = new File( + "/log.txt" );
        try {
            Process process = Runtime.getRuntime().exec("logcat -d");
            process = Runtime.getRuntime().exec( "logcat -f " + "/storage/emulated/0/"+"Logging.txt");
        }catch(Exception e)
        {
            e.printStackTrace();
        }

        ArrayList<String> actions = new ArrayList<>();
        actions.add("Start");
        actions.add("Sensors");
        actions.add("Timing");
        actions.add("Recordings");

        // setup the recycler view
        recyclerView = findViewById(R.id.actions_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setEdgeItemsCenteringEnabled(true);
        recyclerView.setCircularScrollingGestureEnabled(false);
        recyclerView.setLayoutManager(new WearableLinearLayoutManager(this));


        actionsListAdapter = new ActionsListAdapter(this, actions, new ActionsListAdapter.AdapterCallback() {

            @Override
            public void onItemClicked(final Integer position) {
                if (position == 0) {
                    startRecording(recordAudio, recordLocation);
                } else if (position == 1) {
                    startActivity(new Intent(getApplicationContext(), SensorsListActivity.class));
                } else if (position == 2) {
                    startActivity(new Intent(getApplicationContext(), TimeSettingsActivity.class));
                } else if (position == 3) {
                    startActivity(new Intent(getApplicationContext(), RecordingsListActivity.class));
                }
            }
        });

        recyclerView.setAdapter(actionsListAdapter);

        // get sensors
        SensorManager sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        List<Integer> sensorsList = new ArrayList<>();
        List<String> sensorNames = new ArrayList<>();
        for(Sensor sensor: sensors) {
            if(sensor.getType() == Sensor.TYPE_ACCELEROMETER || sensor.getType() == Sensor.TYPE_GRAVITY
                    || sensor.getType() == Sensor.TYPE_GYROSCOPE
                    || sensor.getType() == Sensor.TYPE_LIGHT || sensor.getType() == Sensor.TYPE_ROTATION_VECTOR
                    || sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION
                    || sensor.getType() == Sensor.TYPE_PRESSURE || sensor.getType() == Sensor.TYPE_STEP_DETECTOR
                    || sensor.getType() == Sensor.TYPE_STEP_COUNTER
                    || sensor.getType() == Sensor.TYPE_PROXIMITY) {
                sensorsList.add(sensor.getType());
                sensorNames.add(sensor.getName());
            }
        }

        // initialize/load list-of-sensors and the duration recording
        SettingsManager.initialize(sensorsList, this.getFilesDir(), sensorNames);

        boolean heartRate = false;

        // check/ask for permissions
        checkResourcePermissions(heartRate);

        // should record audio/location?
        recordAudio = (Boolean) SettingsManager
                .loadSettings(this.getFilesDir(), SettingsManager.AUDIO);
        recordLocation = (Boolean) SettingsManager
                .loadSettings(this.getFilesDir(), SettingsManager.LOCATION);


        // Enables Always-on
        setAmbientEnabled();
    }

    private void startRecording(boolean recordAudio, boolean recordLocation) {
        boolean anyGrantedPermission = false;
        if(!resourcePermissions.get(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(getApplicationContext(),
                    "Permission to write in sd card is required", Toast.LENGTH_LONG).show();
            return;
        }

        // disable heart-rate if permission is not granted

        // sensors service
        anyGrantedPermission = true;
        startService(new Intent(MainMenuActivity.this.getApplicationContext(), SensorDataCollectionService.class));

        // wifi logging service
        startService(new Intent(MainMenuActivity.this.getApplicationContext(), WifiLoggingService.class));

        // audio service
        if (recordAudio && resourcePermissions.get(Manifest.permission.RECORD_AUDIO)) {
            startService(new Intent(MainMenuActivity.this.getApplicationContext(), AudioRecordingService.class));
            anyGrantedPermission = true;
        }
        // location service
        if (recordLocation
                && (resourcePermissions.get(Manifest.permission.ACCESS_COARSE_LOCATION)
                || resourcePermissions.get(Manifest.permission.ACCESS_FINE_LOCATION))) {
            startService(new Intent(MainMenuActivity.this.getApplicationContext(), LocationLogService.class));
            anyGrantedPermission = true;
        }

        if (anyGrantedPermission) {
            startActivity(new Intent(MainMenuActivity.this.getApplicationContext(),
                    RecordingActivity.class));
        }
    }

    private void checkResourcePermissions(boolean heartRate) {

        List<String> manifestPermissions = new ArrayList<>();
        manifestPermissions.add(Manifest.permission.RECORD_AUDIO);
        manifestPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        manifestPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        manifestPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        manifestPermissions.add(Manifest.permission.ACCESS_WIFI_STATE);
        manifestPermissions.add(Manifest.permission.CHANGE_WIFI_STATE);
        if (heartRate) {
            manifestPermissions.add(Manifest.permission.BODY_SENSORS);
        }

        List<String> listOfNeededPermissions = new ArrayList<>();
        for (String permission : manifestPermissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                listOfNeededPermissions.add(permission);
            } else {
                resourcePermissions.put(permission, true);
            }
        }

        if(!listOfNeededPermissions.isEmpty()) {
            // request for for non-granted permissions
            ActivityCompat.requestPermissions(this,
                    listOfNeededPermissions.toArray(new String[listOfNeededPermissions.size()]),
                    117);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        if(requestCode == 117) {
            for (int i = 0; i < grantResults.length; i++) {
                // If request is cancelled, the result arrays are empty.
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    resourcePermissions.put(permissions[i], true);
                } else {
                    resourcePermissions.put(permissions[i], false);
                }
            }
        }
    }
}
