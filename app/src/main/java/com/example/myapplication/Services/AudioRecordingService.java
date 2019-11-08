package com.example.myapplication.Services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.example.myapplication.Activities.MainMenuActivity;
import com.example.myapplication.AudioRecorder;
import com.example.myapplication.Model.DataWriter;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


public class AudioRecordingService extends Service {

    private AudioRecorder audioRecorder;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        audioRecorder = new AudioRecorder();


        Intent notificationIntent = new Intent(this, MainMenuActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, "ForegroundServiceChannel")
                .setContentTitle("Foreground Service")
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);
        // start recording
        try {
            audioRecorder.start(DataWriter.createAudioFile());
        } catch (Exception e) {
            stopSelf();
            Log.e("Error (AudioRecordingService)", "audio recording couldn't start");
            return START_STICKY;
        }

        //sendBroadcastMessage("recording", 0, 0);
        return START_STICKY;
    }

    // Supposing that your value is an integer declared somewhere as: int myInteger;
    private void sendMessage() {
        // The string "my-integer" will be used to filer the intent
        Intent intent = new Intent("service_crash");
        // Adding some data
        intent.putExtra("message", "audio_service_destroyed");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        if(audioRecorder != null) {
            audioRecorder.stop();
        }
        super.onDestroy();
    }
}
