package com.example.perfectday;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class HomeAdapter extends BaseAdapter {

    private ArrayList<HabitHome> myHabits;
    private Context ctxt;

    public HomeAdapter(Context ctxt, ArrayList<HabitHome> myHabits) {

        this.ctxt = ctxt;
        this.myHabits = myHabits;
    }

    @Override
    public int getCount() {
        return myHabits.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null){
            LayoutInflater inflater = (LayoutInflater)ctxt.getSystemService(Context
                    .LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.row_home, parent, false);
        }

        TextView time = (TextView) convertView.findViewById(R.id.time_home_row);
        TextView title = (TextView) convertView.findViewById(R.id.title_home_row);

        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa");

        Calendar alarmTime = myHabits.get(position).getHomeAlarmTime();
        time.setText(sdf.format(alarmTime.getTime()));

        title.setText(myHabits.get(position).getHomeName());

        return convertView;

    }

    @Override
    public Object getItem(int position) {
        return myHabits.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

}
