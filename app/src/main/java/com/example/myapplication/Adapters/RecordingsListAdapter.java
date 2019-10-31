package com.example.myapplication.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class RecordingsListAdapter extends RecyclerView.Adapter<RecordingsListAdapter.RecyclerViewHolder>   {


    private HashMap<String, Integer> recordingsList = new HashMap<>();
    public interface AdapterCallback{
        void onItemClicked(Integer menuPosition);
    }

    private AdapterCallback clickCallback;
    private Context context;


    public RecordingsListAdapter(Context context, HashMap<String, Integer> recordingsList, List<Integer> recordingsSize, RecordingsListAdapter.AdapterCallback clickCallback){
        this.context = context;
        this.recordingsList = recordingsList;
        this.clickCallback = clickCallback;
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recording_item,parent,false);

        RecyclerViewHolder recyclerViewHolder = new RecyclerViewHolder(view);

        return recyclerViewHolder;
    }

    public static class RecyclerViewHolder extends RecyclerView.ViewHolder
    {
        LinearLayout recordingItemContainer;
        TextView recordingText;
        TextView sizeTextView;

        public RecyclerViewHolder(View view) {
            super(view);
            recordingItemContainer = view.findViewById(R.id.recording_item_container);
            recordingText = view.findViewById(R.id.recording_item_text);
            sizeTextView = view.findViewById(R.id.folder_size);
        }
    }


    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, final int position) {
        List<String> s = new ArrayList<>(recordingsList.keySet());
        List<Integer> val = new ArrayList<>(recordingsList.values());
        final String recordingItem = s.get(position);

        holder.recordingText.setText(recordingItem);
        holder.sizeTextView.setText(String.format("%d KB", val.get(position)));
        holder.recordingItemContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(clickCallback != null) {
                    clickCallback.onItemClicked(position);
                    notifyItemChanged(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (recordingsList != null) {
            return recordingsList.size();
        }
        return 0;
    }
}
