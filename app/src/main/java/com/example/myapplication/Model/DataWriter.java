package com.example.myapplication.Model;

import android.content.Context;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class DataWriter {
    public static String SUBFOLDER_NAME = "";
    public static String FOLDER_NAME = "SensorData";

    public static HashMap<String, FileOutputStream> createSensorFiles(List<String> sensorsList) throws Exception {

        if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Log.e("Error (DataWriter)", "Couldn't find the sd card");
            return null;
        }

        // create the recording folder
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(System.currentTimeMillis());
        if (SUBFOLDER_NAME.equals("")) {
            SUBFOLDER_NAME = DateFormat.format("dd-MM-yy-HH-mm-ss", cal).toString();
        }

        // check if the directory exist
        File storageDir = new File(Environment.getExternalStorageDirectory(),
                FOLDER_NAME + "/" + SUBFOLDER_NAME);

        // create the files
        if (!storageDir.exists()) {
            if(!storageDir.mkdirs()) {
                Log.e("Error (DataWriter)", "Couldn't create the sensor files");
                return null;
            }
        }

        HashMap<String, FileOutputStream> outputStreamsMap = new HashMap<>();

        // create the files
        for (String sensorName : sensorsList) {
            File file = new File(storageDir, sensorName + ".csv");
            outputStreamsMap.put(sensorName, new FileOutputStream(file));

        }
        return outputStreamsMap;
    }

    public static File createAudioFile() throws NullPointerException {

        // create the recording folder
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(System.currentTimeMillis());
        if (SUBFOLDER_NAME.equals("")) {
            SUBFOLDER_NAME = DateFormat.format("dd-MM-yyyy-HH-mm-ss", cal).toString();
        }

        // check if the directory exist
        File storageDir = new File(Environment.getExternalStorageDirectory(),
                FOLDER_NAME + "/" + SUBFOLDER_NAME);

        // create the files
        if (!storageDir.exists()) {
            if(!storageDir.mkdirs()) {
                Log.e("Error (DataWriter)", "Couldn't create the audio file");
                return null;
            }
        }

        return  new File(storageDir, System.currentTimeMillis() + "_audio.wav");
    }

    public static File createLocationLogFile() throws NullPointerException{
        // create the recording folder
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(System.currentTimeMillis());
        if (SUBFOLDER_NAME.equals("")) {
            SUBFOLDER_NAME = DateFormat.format("dd-MM-yyyy-HH-mm-ss", cal).toString();
        }

        // check if the directory exist
        File storageDir = new File(Environment.getExternalStorageDirectory(),
                FOLDER_NAME + "/" + SUBFOLDER_NAME);

        // create the files
        if (!storageDir.exists()) {
            if(!storageDir.mkdirs()) {
                Log.e("Error (DataWriter)", "Couldn't create the location file");
                return null;
            }
        }

        return  new File(storageDir, "location.csv");

    }

    public static File createWifiLogFile() throws NullPointerException{
        // create the recording folder
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(System.currentTimeMillis());
        if (SUBFOLDER_NAME.equals("")) {
            SUBFOLDER_NAME = DateFormat.format("dd-MM-yyyy-HH-mm-ss", cal).toString();
        }

        // check if the directory exist
        File storageDir = new File(Environment.getExternalStorageDirectory(),
                FOLDER_NAME + "/" + SUBFOLDER_NAME);

        // create the files
        if (!storageDir.exists()) {
            if(!storageDir.mkdirs()) {
                Log.e("Error (DataWriter)", "Couldn't create the location file");
                return null;
            }
        }

        return  new File(storageDir, "wifi.csv");
    }



    public static File createFolder() {
        if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Log.e("Error (LabelManager)", "Couldn't find the sd card");
            return null;
        }

        // create the recording folder
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(System.currentTimeMillis());
        if (SUBFOLDER_NAME.equals("")) {
            SUBFOLDER_NAME = DateFormat.format("dd-MM-yyyy-HH-mm-ss", cal).toString();
        }

        // check if the directory exist
        File storageDir = new File(Environment.getExternalStorageDirectory(),
                FOLDER_NAME + "/" + SUBFOLDER_NAME);

        // create the directory
        if (!storageDir.exists()) {
            if(!storageDir.mkdirs()) {
                Log.e("Error (LabelManager)", "Couldn't create the sensor files");
                return null;
            }
        }
        return new File(storageDir.toString());

    }

    public static HashMap<String, Integer> getRecordingsDir(Context context) {
        HashMap<String, Integer> recordingsSize = new HashMap<>();

        // sd card?
        if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(context, "Couldn't find the sd card", Toast.LENGTH_SHORT).show();
            return null;
        }

        // check if there is any recordings
        File recordingsFolder = new File(Environment.getExternalStorageDirectory(), FOLDER_NAME);
        if (!recordingsFolder.exists()) {
            Toast.makeText(context, "No recordings", Toast.LENGTH_SHORT).show();
            return null;
        }

        String[] directories = recordingsFolder.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });

        File[] folders = recordingsFolder.listFiles();
        for(int i = 0; i < folders.length; i++) {
            File[] recordingFiles = folders[i].listFiles();
            int size = 0;
            for (int j = 0; j < recordingFiles.length; j ++) {
                size += recordingFiles[j].length();
            }
            recordingsSize.put(folders[i].getName(), (int) (size/1000));
        }

        return recordingsSize;
    }
}
