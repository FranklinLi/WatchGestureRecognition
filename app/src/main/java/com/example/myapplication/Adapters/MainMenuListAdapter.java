package com.example.myapplication.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myapplication.R;

import java.util.ArrayList;

public class MainMenuListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<String> items;

    public MainMenuListAdapter(Context context, ArrayList<String> items) {
        this.context = context;
        this.items = items;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View itemView, ViewGroup container) {

        View view = View.inflate(context, R.layout.main_menu_item, null);
        TextView actionText = view.findViewById(R.id.menu_item_text);
        ImageView actionIcon = view.findViewById(R.id.menu_item_icon);

        actionText.setText(items.get(position));

        if (position == 0) {
            actionIcon.setImageResource(R.drawable.action_record);
        } else if (position == 1) {
            actionIcon.setImageResource(R.drawable.action_select_sensors);
        } else if (position == 2) {
            actionIcon.setImageResource(R.drawable.action_timing);
        } else if (position == 3) {
            actionIcon.setImageResource(R.drawable.action_recordings);
        }

        return view;
    }

    @Override
    public int getCount() {
        return items.size();
    }


    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
