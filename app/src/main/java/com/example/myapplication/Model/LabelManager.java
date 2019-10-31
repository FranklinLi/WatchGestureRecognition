package com.example.myapplication.Model;

import android.os.Environment;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class LabelManager {

    private final static String FOLDER_NAME = "SensorData";
    private String label1 = "";
    private String label2 = "";

    private long time1_1 = 0;
    private long time1_2 = 0;
    private long time2_1 = 0;
    private long time2_2 = 0;
    private File directory;
    private JSONArray labelsJsonObj = new JSONArray();

    public LabelManager() {
        // create the folder
        directory = DataWriter.createFolder();
    }

    public void setLabel(String label, int i) {
        if (i == 12) {
            label1 = label;
            time1_2 = System.currentTimeMillis();
        } else if (i == 22) {
            label2 = label;
            time2_2 = System.currentTimeMillis();
        } else if (i == 11) {
            time1_1 = System.currentTimeMillis();
        } else if (i == 21) {
            time2_1 = System.currentTimeMillis();
        }
    }

    public void saveLabel() {
        if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Log.e("Error (LabelManager)", "Couldn't find the sd card");
            return;
        }

        try {
            // create the JSON object
            JSONObject labelJsonObj = new JSONObject();
            labelJsonObj.put("label1_2", label1)
                        .put("label2_2", label2)
                        .put("time1_1", time1_1)
                        .put("time1_2", time1_2)
                        .put("time2_1", time2_1)
                        .put("time2_2", time2_2);

            labelsJsonObj.put(labelJsonObj);

            // reset attributes
            reset();


            // write the label
            OutputStreamWriter outputStreamWriter =
                    new OutputStreamWriter(new FileOutputStream(directory + "/labels.json"));
            outputStreamWriter.write(labelsJsonObj.toString());
            outputStreamWriter.close();


        } catch (JSONException e) {
            Log.e("Error (LabelManager)", "failed to create the json object");
            e.printStackTrace();

        } catch (IOException e) {
            Log.e("Error (LabelManager)", "failed to create the json file");
            e.printStackTrace();
        }
    }

    private void reset() {
        label1 = "";
        label2 = "";
        time1_1 = 0;
        time1_2 = 0;
        time2_1 = 0;
        time2_2 = 0;
    }

}
