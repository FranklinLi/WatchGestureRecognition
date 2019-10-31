package com.example.myapplication.Model;

import android.hardware.Sensor;

public class SensorItem {
    private String sensorName;
    private int type;
    private boolean isChecked;
    private Sensor sensor;

    public SensorItem(String name, Integer type, boolean isChecked, Sensor sensor) {
        this.sensorName = name;
        this.type = type;
        this.isChecked = isChecked;
        this.sensor = sensor;
    }

    public String getSensorName() {
        return sensorName;
    }

    public int getType() {
        return type;
    }

    public boolean isChecked() {
        return isChecked;
    }


    public void toggle() {
        isChecked = !isChecked;
    }

    public Sensor getSensor() {
        return sensor;
    }


}