package com.example.myapplication.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.Model.SensorItem;

import java.util.ArrayList;

import androidx.recyclerview.widget.RecyclerView;

public class SensorsListAdapter extends RecyclerView.Adapter<SensorsListAdapter.RecyclerViewHolder>  {

    private ArrayList<SensorItem> sensorItemsList;
    public interface AdapterCallback{
        void onItemClicked(Integer menuPosition);
    }
    public interface AdapterCallback2{
        void onItemLongClicked(Integer menuPosition);
    }
    private AdapterCallback clickCallback;
    private AdapterCallback2 longClickCallback;
    private Context context;



    public SensorsListAdapter(Context context, ArrayList<SensorItem> sensorItemsList, AdapterCallback clickCallback, AdapterCallback2 longClickCallback){
        this.context = context;
        this.sensorItemsList = sensorItemsList;
        this.clickCallback = clickCallback;
        this.longClickCallback = longClickCallback;
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view =LayoutInflater.from(parent.getContext()).inflate(R.layout.sensor_item,parent,false);

        RecyclerViewHolder recyclerViewHolder = new RecyclerViewHolder(view);

        return recyclerViewHolder;
    }

    public static class RecyclerViewHolder extends RecyclerView.ViewHolder
    {
        LinearLayout sensorItemContainer;
        TextView sensorName;

        public RecyclerViewHolder(View view) {
            super(view);
            sensorItemContainer = view.findViewById(R.id.sensor_item_container);
            sensorName = view.findViewById(R.id.sensor_item_name);
        }

        public void setColor(boolean isChecked) {
            if (isChecked) {
                sensorName.setTextColor(Color.parseColor("#77F9FF"));
            } else {
                sensorName.setTextColor(Color.WHITE);
            }
        }

    }

    public void update(ArrayList<SensorItem> updatedSensorItemsList) {
        sensorItemsList = updatedSensorItemsList;
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, final int position) {
        final SensorItem sensorItem = sensorItemsList.get(position);

        holder.sensorName.setText(sensorItem.getSensorName());
        holder.setColor(sensorItem.isChecked());
        holder.sensorName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(clickCallback != null) {
                    clickCallback.onItemClicked(position);
                    notifyItemChanged(position);
                }
            }
        });
        holder.sensorName.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(longClickCallback != null) {
                    longClickCallback.onItemLongClicked(position);
                    notifyItemChanged(position);
                }
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return sensorItemsList.size();
    }
}
