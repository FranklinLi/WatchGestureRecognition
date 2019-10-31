package com.example.myapplication.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class ActionsListAdapter extends RecyclerView.Adapter<ActionsListAdapter.RecyclerViewHolder>  {

    private List<String> actions = new ArrayList<>();
    private Context context;
    public interface AdapterCallback{
        void onItemClicked(Integer menuPosition);
    }
    private AdapterCallback callback;


    public ActionsListAdapter(Context context, ArrayList<String> actions, ActionsListAdapter.AdapterCallback clickCallback) {
        this.context = context;
        this.actions = actions;
        this.callback = clickCallback;
    }


    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_menu_item,parent,false);

        return new RecyclerViewHolder(view);
    }

    public static class RecyclerViewHolder extends RecyclerView.ViewHolder
    {
        LinearLayout actionItemContainer;
        ImageView actionIcon;
        TextView actionText;

        public RecyclerViewHolder(View view) {
            super(view);
            actionItemContainer = view.findViewById(R.id.action_item_container);
            actionText = view.findViewById(R.id.menu_item_text);
            actionIcon = view.findViewById(R.id.menu_item_icon);
        }
    }

    @Override
    public void onBindViewHolder(ActionsListAdapter.RecyclerViewHolder holder, final int position) {
        final String action = actions.get(position);

        holder.actionText.setText(action);

        int iconId = 0;
        if (position == 0) {
            iconId = R.drawable.action_record;
        } else if (position == 1) {
            iconId = R.drawable.action_select_sensors;
        } else if (position == 2) {
            iconId = R.drawable.action_timing;
        } else if (position == 3) {
            iconId = R.drawable.action_recordings;
        }

        holder.actionIcon.setImageResource(iconId);
        holder.actionItemContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onItemClicked(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return actions.size();
    }

}
