package com.example.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyBroadcastReceiver extends BroadcastReceiver {
    public static final String ACTION = "com.example.ACTION_SOMETHING";
    @Override
    public void onReceive(Context context, Intent intent) {
        String test = intent.getStringExtra("dataToPass");
    }
}
