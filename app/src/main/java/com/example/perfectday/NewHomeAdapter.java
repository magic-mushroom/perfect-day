package com.example.perfectday;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class NewHomeAdapter extends RecyclerView.Adapter<NewHomeAdapter.ViewHolder> {

    private ArrayList<HabitHome> myHabits;
    private static ClickListener clickListener;

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public TextView time;
        public TextView title;

        public ViewHolder (View v) {
            super(v);

            time = (TextView) v.findViewById(R.id.time_home_row);
            title = (TextView) v.findViewById(R.id.title_home_row);

            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            clickListener.onItemClick(getAdapterPosition(), v);
        }
    }

    // public Constructor
    public NewHomeAdapter (ArrayList<HabitHome> mHabits) {
        myHabits = mHabits;
    }

    // Creating new views
    public NewHomeAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_home, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(NewHomeAdapter.ViewHolder holder, int position) {

        holder.title.setText (myHabits.get(position).getHomeName());

        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa");

        Calendar alarmTime = myHabits.get(position).getHomeAlarmTime();
        holder.time.setText(sdf.format(alarmTime.getTime()));
    }

    @Override
    public int getItemCount() {
        return myHabits.size();
    }

    public void setOnItemClickListener(ClickListener clickListener){
        this.clickListener = clickListener;
    }

    public interface ClickListener{
        void onItemClick(int position, View v);
    }

}
