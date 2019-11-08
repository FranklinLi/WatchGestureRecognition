package com.example.myapplication.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.wearable.activity.WearableActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import com.example.myapplication.Services.AudioRecordingService;
import com.example.myapplication.Model.DataWriter;
import com.example.myapplication.Model.LabelManager;
import com.example.myapplication.Services.LocationLogService;
import com.example.myapplication.R;
import com.example.myapplication.Services.SensorDataCollectionService;
import com.example.myapplication.Services.WifiLoggingService;

import java.util.HashMap;
import java.util.List;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class RecordingActivity extends WearableActivity {

    private TextView timeTextView;
    private ImageButton stopButton;
    private CountDownTimer timer;
    private int numLabels = 0;
    private LabelManager labelManager = new LabelManager();
    private boolean isRecording = true;
    private Dialog debugDialog;
    private TextView debug_text;
    private static final int SPEECH_REQUEST_CODE = 0;
    private EditText labelText;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording);


        timeTextView = findViewById(R.id.elapsed_time_text_view);
        stopButton = findViewById(R.id.stop_btn);

        debugDialog = new Dialog(RecordingActivity.this);
        debugDialog.setContentView(R.layout.debug_dialog);

        debug_text = findViewById(R.id.debug_textt);



        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRecording = false;
                stopService(new Intent(getApplicationContext(),
                        SensorDataCollectionService.class));
                stopService(new Intent(getApplicationContext(), AudioRecordingService.class));
                stopService(new Intent (getApplicationContext(), LocationLogService.class));
                stopService(new Intent (getApplicationContext(), WifiLoggingService.class));
                startActivity(new Intent(getApplicationContext(), MainMenuActivity.class));
            }
        });


        // create the timer
        timer = new CountDownTimer(900000000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeTextView.setText(formatTime((900000000 - millisUntilFinished) / 1000));
            }

            @Override
            public void onFinish() {

            }
        };

        Bundle bundle = this.getIntent().getExtras();
        HashMap<String, Boolean> resourcePermissions = new HashMap<>();
        if (bundle != null) {
            resourcePermissions = (HashMap<String, Boolean>) bundle.getSerializable("permissions");

        }



        // wifi logging service
        startService(new Intent(RecordingActivity.this.getApplicationContext(), WifiLoggingService.class));

        // audio service
        if (true && resourcePermissions.get(Manifest.permission.RECORD_AUDIO)) {
            startService(new Intent(RecordingActivity.this.getApplicationContext(), AudioRecordingService.class));
        }
        // location service
        if (true
                && (resourcePermissions.get(Manifest.permission.ACCESS_COARSE_LOCATION)
                || resourcePermissions.get(Manifest.permission.ACCESS_FINE_LOCATION))) {
            startService(new Intent(RecordingActivity.this.getApplicationContext(), LocationLogService.class));
        }

        // sensors service
        startService(new Intent(RecordingActivity.this.getApplicationContext(), SensorDataCollectionService.class));


        timer.start();

        // Enables Always-on
        setAmbientEnabled();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        final Dialog dialog = new Dialog(this);
        ImageButton okButton, cancelButton;

        if (numLabels % 2 == 0) {
            dialog.setContentView(R.layout.action_label_dialog);
            okButton = dialog.findViewById(R.id.ok_btn);
            cancelButton = dialog.findViewById(R.id.cancel_btn);
            labelText = dialog.findViewById(R.id.label_edit_text);
            labelManager.setLabel("", 11);
        } else {
            dialog.setContentView(R.layout.action_dialog_end2);
            okButton = dialog.findViewById(R.id.ok_btn2);
            cancelButton = dialog.findViewById(R.id.cancel_btn2);
            labelText = dialog.findViewById(R.id.label_edit_text2);
            labelManager.setLabel("", 21);
        }

        okButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String label = labelText.getText().toString();

                if (numLabels % 2 == 0) {
                    if (label.equals("")) {
                        Toast.makeText(getApplicationContext(), "No label is provided", Toast.LENGTH_SHORT).show();
                    } else {
                        // take the first label
                        labelManager.setLabel(label, 12);
                        numLabels += 1;
                        dialog.dismiss();
                    }
                } else {
                    // take the second label
                    labelManager.setLabel(label, 22);
                    labelManager.saveLabel();
                    numLabels += 1;
                    dialog.dismiss();
                }
            }

        });


        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displaySpeechRecognizer();
            }
        });


        dialog.show();

        return super.onKeyDown(keyCode, event);
    }

    private void displaySpeechRecognizer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        // Start the activity, the intent will be populated with the speech text
        startActivityForResult(intent, SPEECH_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            labelText.setText(spokenText);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @SuppressLint("DefaultLocale")
    private String formatTime(long time) {
        // time -> second
        int hour = (int) Math.floor(time / 3600);
        int minute = (int) Math.floor((time - hour*3600) / 60.0);
        long second = time - minute * 60 - hour * 3600;
        return String.format("%02d:%02d:%02d", hour, minute, second);
    }

    // receiver for getting messages from services
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Integer msgValue = intent.getIntExtra("message", -1);
            if (isRecording) {
                debug_text.setText(String.valueOf(msgValue));

            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        // receive messages
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mMessageReceiver,
                        new IntentFilter("running_flag"));
    }

    @Override
    protected void onPause() {
        // Unregister since the activity is not visible
        DataWriter.SUBFOLDER_NAME = "";
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(mMessageReceiver);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        DataWriter.SUBFOLDER_NAME = "";
        super.onDestroy();
    }
}
