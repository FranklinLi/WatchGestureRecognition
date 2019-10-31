package com.example.myapplication.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.KeyEvent;

import com.example.myapplication.Model.DataWriter;
import com.example.myapplication.R;
import com.example.myapplication.Adapters.RecordingsListAdapter;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.util.HashMap;
import java.util.List;

import androidx.wear.widget.WearableLinearLayoutManager;
import androidx.wear.widget.WearableRecyclerView;

public class RecordingsListActivity extends WearableActivity {

    private WearableRecyclerView recyclerView;
    private HashMap<String, Integer> recordingsList;
    private List<Integer> recordingsSize;
    private RecordingsListAdapter recordingsListAdapter;
    FirebaseStorage storage;
    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recordings_list);

        // setup the recycler view
        recyclerView = findViewById(R.id.recordings_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setEdgeItemsCenteringEnabled(false);
        recyclerView.setCircularScrollingGestureEnabled(false);
        recyclerView.setLayoutManager(new WearableLinearLayoutManager(this));

        //get the list of recordings
        recordingsList = new HashMap<>();
        recordingsList = DataWriter.getRecordingsDir(this);


        recordingsListAdapter = new RecordingsListAdapter(this, recordingsList,
                recordingsSize, new RecordingsListAdapter.AdapterCallback() {
                    @Override
                    public void onItemClicked(Integer position) {

                    }
                });

        recyclerView.setAdapter(recordingsListAdapter);

        // setup cloud assets
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();


        // Enables Always-on
        setAmbientEnabled();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        startActivity(new Intent(this, MainMenuActivity.class));
        return super.onKeyDown(keyCode, event);
    }
}
