package com.example.myapplication.Model;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.JarException;

public class SettingsManager {

    private static final String DEBUG_TAG = "Gestures";
    private static final String SETTINGS_FILE_NAME = "settings.json";
    private static final String APP_FOLDER = "App_files";
    public static final String RECORDING_DURATION = "recording_duration";
    public static final String SENSORS = "sensors";
    public static final String AUDIO = "Audio";
    public static final String LOCATION = "Location";
    public static final String HEART_RATE = "Heart rate";

    public SettingsManager() {

    }

    public static void initialize(List<Integer> sensorTypes, File localDir, List<String> sensorNames) {
        File appFolder = new File (localDir, "/" + APP_FOLDER);
        if (!appFolder.exists()) {
            if(appFolder.mkdir()) {
                try {
                    File settingsFIle = new File(appFolder, "/" + SETTINGS_FILE_NAME);
                    if (!settingsFIle.createNewFile()) {
                        Log.e(DEBUG_TAG, "Error: failed to create the settings file");
                    } else {
                        createDefaultSettings(settingsFIle, sensorTypes, sensorNames);
                        Log.d(DEBUG_TAG, "Settings file created");
                    }
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public static Object loadSettings(File localDir, String type) {
        JSONObject settingsJsonObj =  readSettings(localDir);
        if(type.equals(SENSORS)) {
            return parseSensorsStatus(settingsJsonObj);
        } else if(type.equals(RECORDING_DURATION)) {
            return getRecordingDuration(settingsJsonObj);
        } else if(type.equals(AUDIO)) {
            return recordAudio(settingsJsonObj);
        } else if (type.equals(LOCATION)) {
            return isLocationOn(settingsJsonObj);
        } else if(type.equals(HEART_RATE)) {
            return parseSensorsStatus(settingsJsonObj).get(Sensor.TYPE_HEART_RATE);
        }
        return null;
    }


    @SuppressWarnings("unchecked")
    public static void saveSettings(File localDir, Object settingsObj, String type) {
        JSONObject settingsJsonObj = new JSONObject();

        if(type.equals(SENSORS)) {
            HashMap<Integer, Boolean> sensorsStatus = (HashMap<Integer, Boolean>) settingsObj;
            JSONObject sensorsStatusJsonObj = new JSONObject();
            try {
                for (Map.Entry<Integer, Boolean> entry : sensorsStatus.entrySet()) {
                    sensorsStatusJsonObj.put(entry.getKey().toString(), entry.getValue());
                }
                JSONObject currSettingsJsonObj = readSettings(localDir);

                // create the new JSON settings object
                settingsJsonObj.put(SENSORS, sensorsStatusJsonObj);
                settingsJsonObj.put(RECORDING_DURATION, getRecordingDuration(currSettingsJsonObj));
                settingsJsonObj.put(AUDIO, recordAudio(currSettingsJsonObj));
                settingsJsonObj.put(LOCATION, isLocationOn(currSettingsJsonObj));

            } catch(JSONException e) {
                e.printStackTrace();
            }
        } else if(type.equals(RECORDING_DURATION)) {
            JSONObject currSettingsJsonObj = readSettings(localDir);
            try {
                int duration = (Integer) settingsObj;
                JSONObject sensorStatusJsonObj = currSettingsJsonObj.getJSONObject(SENSORS);
                // create the new JSON settings object
                settingsJsonObj.put(SENSORS, sensorStatusJsonObj);
                settingsJsonObj.put(RECORDING_DURATION, duration);
                settingsJsonObj.put(AUDIO, recordAudio(currSettingsJsonObj));
                settingsJsonObj.put(LOCATION, isLocationOn(currSettingsJsonObj));

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if(type.equals(AUDIO)) {
            boolean recordAudio = (Boolean) settingsObj;
            JSONObject currSettingsJsonObj = readSettings(localDir);
            try {
                JSONObject sensorStatusJsonObj = currSettingsJsonObj.getJSONObject(SENSORS);
                // create the new JSON settings object
                settingsJsonObj.put(SENSORS, sensorStatusJsonObj);
                settingsJsonObj.put(RECORDING_DURATION, getRecordingDuration(currSettingsJsonObj));
                settingsJsonObj.put(AUDIO, recordAudio);
                settingsJsonObj.put(LOCATION, isLocationOn(currSettingsJsonObj));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if(type.equals(LOCATION)) {
            JSONObject currSettingsJsonObj = readSettings(localDir);
            try {
                JSONObject sensorStatusJsonObj = currSettingsJsonObj.getJSONObject(SENSORS);
                // create the new JSON settings object
                settingsJsonObj.put(SENSORS, sensorStatusJsonObj);
                settingsJsonObj.put(RECORDING_DURATION, getRecordingDuration(currSettingsJsonObj));
                settingsJsonObj.put(AUDIO, recordAudio(currSettingsJsonObj));
                boolean locationRec = (Boolean) settingsObj;
                settingsJsonObj.put(LOCATION, locationRec);


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        try {
            OutputStreamWriter outputStreamWriter =
                    new OutputStreamWriter(new FileOutputStream(
                            localDir + "/" + APP_FOLDER + "/" + SETTINGS_FILE_NAME));
            outputStreamWriter.write(settingsJsonObj.toString());
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.d(DEBUG_TAG, "failed to write the settings.json");
            e.printStackTrace();
        }
    }



    private static void createDefaultSettings(File settingsFile, List<Integer> sensorsTypes, List<String> sensorNames) {
        JSONObject settingsJsonObj = new JSONObject();
        JSONObject sensorsJsonObj = new JSONObject();
        int i = 0;
        for(Integer sensorType : sensorsTypes) {
            try {
                boolean shouldRecord = true;
                if (sensorNames.get(i).equals("AFE4405 light Sensor")) {
                    shouldRecord = false;
                }
                i += 1;
                if (sensorType == Sensor.TYPE_HEART_RATE) {shouldRecord = false;}
                sensorsJsonObj.put(sensorType.toString(), shouldRecord);
            } catch (org.json.JSONException e) {
                e.printStackTrace();
            }
            try {
                settingsJsonObj.put(SENSORS, sensorsJsonObj);
                settingsJsonObj.put(RECORDING_DURATION, 30);
                settingsJsonObj.put(AUDIO, true);
                settingsJsonObj.put(LOCATION, true);
            } catch(org.json.JSONException e) {
                e.printStackTrace();
            }
        }
        try {
            OutputStreamWriter outputStreamWriter =
                    new OutputStreamWriter(new FileOutputStream(settingsFile));
            outputStreamWriter.write(settingsJsonObj.toString());
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.d(DEBUG_TAG, "failed to write the settings.json");
            e.printStackTrace();
        }
    }

    private static JSONObject readSettings(File localDir) {
        JSONObject settingsJsonObj = new JSONObject();
        try {
            InputStream inputStream = new FileInputStream(new File( localDir + "/" + APP_FOLDER + "/" + SETTINGS_FILE_NAME));
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            String settingsJsonString = new String(buffer, "UTF-8");
            settingsJsonObj = new JSONObject(settingsJsonString);
        } catch (org.json.JSONException e) {
            e.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return settingsJsonObj;
    }

    private static int getRecordingDuration(JSONObject settingsObj) {
        try {
            return settingsObj.getInt(RECORDING_DURATION);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static boolean isLocationOn(JSONObject settingsObj) {
        try {
            return settingsObj.getBoolean(LOCATION);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean recordAudio(JSONObject settingsObj) {
        try {
            return settingsObj.getBoolean(AUDIO);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static HashMap<Integer, Boolean> parseSensorsStatus(JSONObject settingsObj) {

        HashMap<Integer, Boolean> sensorsStatus = new HashMap<>();  // sensor type --> status
        try {
            JSONObject sensorsJsonObj = settingsObj.getJSONObject(SENSORS);
            Iterator<String> keyIterator = sensorsJsonObj.keys();

            while(keyIterator.hasNext()) {
                String sensorType = keyIterator.next();
                sensorsStatus.put(Integer.parseInt(sensorType),sensorsJsonObj.getBoolean(sensorType));
            }

        } catch (org.json.JSONException e) {
            e.printStackTrace();
        }

        return sensorsStatus;
    }
}
