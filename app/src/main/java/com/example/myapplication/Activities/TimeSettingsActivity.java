package com.example.myapplication.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.activity.WearableActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.Model.SettingsManager;

public class TimeSettingsActivity extends WearableActivity {

    private static final String DEBUG_TAG = "Gestures";
    private TextView timeTextView;
    private ImageButton increaseButton, decreaseButton;
    private Integer duration = 30;
    private boolean firstTouch = true;
    float firstTouchY, currentY;
    private int rate = 1;  // per-second
    private boolean buttonReleased = true;
    private static final String MSG_KEY = "112";
    private Thread timeChangeThread;


    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            timeTextView.setText(String.format("%d min", duration));
        }
    };


    @SuppressLint({"DefaultLocale", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_settings);

        firstTouch = true;
        increaseButton = findViewById(R.id.plus_btn);
        decreaseButton = findViewById(R.id.minus_btn);
        timeTextView = findViewById(R.id.time_text);

        // load the saved time duration
        duration = (Integer) SettingsManager.loadSettings(this.getFilesDir(),
                SettingsManager.RECORDING_DURATION);
        timeTextView.setText(String.format("%d min", duration));

        // set onclick listeners for the control button
        increaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                duration += 1;
                timeTextView.setText(String.format("%d min", duration));
            }
        });

        decreaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(duration > 1) {
                    duration -= 1;
                    timeTextView.setText(String.format("%d min", duration));
                }
            }
        });

        // Enables Always-on
        setAmbientEnabled();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        SettingsManager.saveSettings(getFilesDir(), duration, SettingsManager.RECORDING_DURATION);
        startActivity(new Intent(this, MainMenuActivity.class));
        return super.onKeyDown(keyCode, event);
    }

//    private int computeRate(float diff, float moveSign) {
//        if (moveSign == 0) {
//            return 1;
//        }
//        if(diff < 20) {
//            return (int) (10*moveSign);
//        } else if(diff < 60) {
//            return (int) (20*moveSign);
//        } else if(diff < 100) {
//            return (int) (40*moveSign);
//        } else {
//            return (int) (80*moveSign);
//        }
//    }

//    private int getScreenWidth() {
//        Display display = getWindowManager().getDefaultDisplay();
//        Point size = new Point();
//        display.getSize(size);
//        return size.x;
//    }
//
//    private int getScreenHeight() {
//        Display display = getWindowManager().getDefaultDisplay();
//        Point size = new Point();
//        display.getSize(size);
//        return size.y;
//    }
}
