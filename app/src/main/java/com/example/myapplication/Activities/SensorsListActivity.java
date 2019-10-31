package com.example.myapplication.Activities;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.KeyEvent;

import com.example.myapplication.Model.SensorItem;
import com.example.myapplication.R;
import com.example.myapplication.Model.SensorNames;
import com.example.myapplication.Adapters.SensorsListAdapter;
import com.example.myapplication.Model.SettingsManager;

import androidx.wear.widget.WearableLinearLayoutManager;
import androidx.wear.widget.WearableRecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SensorsListActivity extends WearableActivity {

    private static final String DEBUG_TAG = "Gestures";
    private WearableRecyclerView recyclerView;
    private ArrayList<SensorItem> sensorItemList;
    private SensorsListAdapter sensorsListAdapter;
    private final static int TYPE_AUDIO = -313;
    private final static int TYPE_LOCATION = -314;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensors_list);

        // setup the recycler view
        recyclerView = findViewById(R.id.recycler_view_container);
        recyclerView.setHasFixedSize(true);
        recyclerView.setEdgeItemsCenteringEnabled(false);
        recyclerView.setCircularScrollingGestureEnabled(false);
        recyclerView.setLayoutManager(new WearableLinearLayoutManager(this));

        // load settings (sensors status)
        HashMap<Integer, Boolean> sensorsStatus =
                (HashMap<Integer, Boolean>) SettingsManager
                        .loadSettings(this.getFilesDir(), SettingsManager.SENSORS);

        // load settings (audio)
        boolean recordAudio = (Boolean) SettingsManager.loadSettings(this.getFilesDir(), SettingsManager.AUDIO);
        sensorsStatus.put(TYPE_AUDIO, recordAudio);

        // load settings (location)
        boolean isLocationOn = (Boolean) SettingsManager.loadSettings(this.getFilesDir(), SettingsManager.LOCATION);
        sensorsStatus.put(TYPE_LOCATION, isLocationOn);

        // create a list of sensor items (visual element)
        sensorItemList = createSensorItemList(sensorsStatus);

        sensorsListAdapter = new SensorsListAdapter(this, sensorItemList, new SensorsListAdapter.AdapterCallback() {
            @Override
            public void onItemClicked(final Integer sensorIndex) {
                SensorItem sItem = sensorItemList.get(sensorIndex);
                sItem.toggle();
                sensorItemList.set(sensorIndex, sItem);
                sensorsListAdapter.update(sensorItemList);
            }
        }, new SensorsListAdapter.AdapterCallback2() {
            @Override
            public void onItemLongClicked(Integer sensorIndex) {
            }
        });
        recyclerView.setAdapter(sensorsListAdapter);


        // Enables Always-on
        setAmbientEnabled();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        HashMap<Integer, Boolean> sensorsStatus = getSensorsStatusMap(sensorItemList);
        boolean recordAudio = getFeatureStatus(sensorItemList, TYPE_AUDIO);
        boolean recordLocation = getFeatureStatus(sensorItemList, TYPE_LOCATION);

        SettingsManager.saveSettings(this.getFilesDir(), sensorsStatus, SettingsManager.SENSORS);
        SettingsManager.saveSettings(this.getFilesDir(), recordAudio, SettingsManager.AUDIO);
        SettingsManager.saveSettings(this.getFilesDir(), recordLocation, SettingsManager.LOCATION);
        startActivity(new Intent(this, MainMenuActivity.class));
        return super.onKeyDown(keyCode, event);
    }

    private HashMap<Integer, Boolean> getSensorsStatusMap(List<SensorItem> sensorItemList) {
        HashMap<Integer, Boolean> sensorsStatus = new HashMap<>();
        for(SensorItem sensorItem: sensorItemList) {
            if(sensorItem.getType() == TYPE_AUDIO || sensorItem.getType() == TYPE_LOCATION) {continue;}
            sensorsStatus.put(sensorItem.getType(), sensorItem.isChecked());
        }
        return sensorsStatus;
    }

    private boolean getFeatureStatus(List<SensorItem> sensorItemList, int type) {
        boolean status = false;
        for(SensorItem sensorItem: sensorItemList) {
            if(sensorItem.getType() == type) {
                status = sensorItem.isChecked();
            }
        }
        return status;
    }

    private ArrayList<SensorItem> createSensorItemList(HashMap<Integer, Boolean> sensorsStatus){
        ArrayList<SensorItem> sensorItemList = new ArrayList<>();
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        for(HashMap.Entry<Integer, Boolean> entry: sensorsStatus.entrySet()){
            // audio
            if(entry.getKey() == TYPE_AUDIO) {
                SensorItem sensorItem = new SensorItem(SettingsManager.AUDIO, TYPE_AUDIO, entry.getValue(), null);
                sensorItemList.add(0, sensorItem);
                continue;
            }
            // location
            if(entry.getKey() == TYPE_LOCATION) {
                SensorItem sensorItem = new SensorItem(SettingsManager.LOCATION, TYPE_LOCATION, entry.getValue(), null);
                sensorItemList.add(0, sensorItem);
                continue;
            }
            // device sensors
            Sensor sensor = sensorManager.getDefaultSensor(entry.getKey());
            SensorNames sensorNames = new SensorNames();
            if (sensor != null) {
                String sensorName = sensorNames.get(sensor.getType());
                if(sensorName == null) {
                    sensorName = sensor.getName();
                }
                SensorItem sensorItem = new SensorItem(sensorName, sensor.getType(), entry.getValue(), sensor);
                sensorItemList.add(sensorItem);
            }
        }

        return sensorItemList;
    }
}
