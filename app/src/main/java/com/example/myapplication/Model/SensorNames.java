package com.example.myapplication.Model;

import android.hardware.Sensor;

import java.util.HashMap;

public class SensorNames {

    private HashMap<Integer, String> sensorNames = new HashMap<>();

    public SensorNames() {
        sensorNames.put(Sensor.TYPE_ACCELEROMETER, "Accelerometer");
        sensorNames.put(Sensor.TYPE_HEART_RATE, "Heart rate");
        sensorNames.put(Sensor.TYPE_ACCELEROMETER_UNCALIBRATED, "Accelerometer (uncalibrated)");
        sensorNames.put(Sensor.TYPE_AMBIENT_TEMPERATURE, "Ambient temperature");
        sensorNames.put(Sensor.TYPE_DEVICE_PRIVATE_BASE, "Device private base");
        sensorNames.put(Sensor.TYPE_GAME_ROTATION_VECTOR, "Game rotation vector");
        sensorNames.put(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR, "Geometric rotation vector");
        sensorNames.put(Sensor.TYPE_GRAVITY, "Gravity");
        sensorNames.put(Sensor.TYPE_GYROSCOPE, "Gyroscope");
        sensorNames.put(Sensor.TYPE_GYROSCOPE_UNCALIBRATED, "Gyroscope (uncalibrated)");
        sensorNames.put(Sensor.TYPE_HEART_BEAT, "Heart beat");
        sensorNames.put(Sensor.TYPE_LIGHT, "Light");
        sensorNames.put(Sensor.TYPE_LINEAR_ACCELERATION, "Linear acceleration");
        sensorNames.put(Sensor.TYPE_LOW_LATENCY_OFFBODY_DETECT, "Low latency off body detect");
        sensorNames.put(Sensor.TYPE_MAGNETIC_FIELD, "Magnetic field");
        sensorNames.put(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED, "Magnetic field (uncalibrated)");
        sensorNames.put(Sensor.TYPE_MOTION_DETECT, "Motion detect");
        sensorNames.put(Sensor.TYPE_POSE_6DOF, "Pose 6DOF");
        sensorNames.put(Sensor.TYPE_PRESSURE, "Pressure");
        sensorNames.put(Sensor.TYPE_PROXIMITY, "Proximity");
        sensorNames.put(Sensor.TYPE_RELATIVE_HUMIDITY, "Relative humidity");
        sensorNames.put(Sensor.TYPE_ROTATION_VECTOR, "Rotation vector");
        sensorNames.put(Sensor.TYPE_SIGNIFICANT_MOTION, "Significant motion");
        sensorNames.put(Sensor.TYPE_STATIONARY_DETECT, "Stationary detect");
        sensorNames.put(Sensor.TYPE_STEP_COUNTER, "Step detector");
        sensorNames.put(Sensor.TYPE_STEP_DETECTOR, "Step counter");
    }

    public String get(int type) {
        if (sensorNames.containsKey(type)) {
            return sensorNames.get(type);
        }
        return null;
    }

}
