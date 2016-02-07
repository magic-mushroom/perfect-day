package com.example.perfectday;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity {

    Button btn;
    ArrayList<HabitHome> myHabits = new ArrayList<>();
    String nameHome, categoryHome, dayOfWeekHome, alarmHomeString;
    Calendar alarmHome;
    int idHome, dayOfWeek, dayOfWeekBinary, nextDayOfWeekBinary, scheduleHome;
    Context context;

    // Trying out RecyclerView
    RecyclerView homeHabits;
    NewHomeAdapter myAdapter;
    RecyclerView.LayoutManager myLayoutManager;

    RadioGroup snoozeRadioGroup;
    EditText snoozeHours;
    String snoozeHoursText;

    // Sharedpreferences to save day start-end time
    SharedPreferences sharedPref;
    Calendar dayEndTime;
    int dayEndTimeInt;

    // To get the day's field
    String doWStatusString;
    int doWInt, doWInt1;
    Calendar doWStatus;
    Boolean isDay;

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;


        // Showing toasts passed as intent extras
        String toastie = getIntent().getStringExtra("toast");
        if (toastie!=null){
            Toast toast = Toast.makeText(this, toastie, Toast.LENGTH_SHORT);
            toast.show();
        }


        // Setting sharedpreferences for first time
        sharedPref = this.getSharedPreferences("settings", Context.MODE_PRIVATE);
        if (!sharedPref.contains("notif_yes")) {

            SharedPreferences.Editor editor = sharedPref.edit();

            editor.putBoolean("notif_yes", true);

            Calendar now = Calendar.getInstance();
            now.set(Calendar.HOUR_OF_DAY, 3);
            now.set(Calendar.MINUTE, 0);
            editor.putString("end_time", sdf.format(now.getTime()));

            editor.commit();

        }

        // Reading end_time
        String dayEndTimeStr = sharedPref.getString("end_time", "null");
        dayEndTime = Calendar.getInstance();
        try {
            dayEndTime.setTime(sdf.parse(dayEndTimeStr));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        dayEndTimeInt = dayEndTime.get(Calendar.HOUR_OF_DAY)*60 + dayEndTime.get(Calendar.MINUTE);

        Log.d("debug_dayendtime", String.valueOf(dayEndTimeInt));


        // Button for adding new task
        btn = (Button) findViewById(R.id.sample_button);

//       snoozeRadioGroup = (RadioGroup) findViewById(R.id.radio_group);
//        snoozeHours = (EditText) findViewById(R.id.radio_edittext);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent iForAddActivity = new Intent(MainActivity.this, AddHabit.class);
                startActivity(iForAddActivity);
            }
        });


        // Showing the day's habits
        homeHabits = (RecyclerView) findViewById(R.id.home_habits);


        // Setting adapter for RecyclerView
        myLayoutManager = new LinearLayoutManager(context);
        homeHabits.setLayoutManager(myLayoutManager);

        myAdapter = new NewHomeAdapter(myHabits);
        homeHabits.setAdapter(myAdapter);

        // Getting today's date
        Calendar now = Calendar.getInstance();
        dayOfWeek = now.get(Calendar.DAY_OF_WEEK);

        //M (=8), T, W, TH, F, SA, SU

        doWInt = dayOfWeek+6;


        // If before dayEndTime, get the day and previous day's status
        if ((now.get(Calendar.HOUR_OF_DAY)*60 + now.get(Calendar.MINUTE))<dayEndTimeInt) {

            doWInt1 = doWInt - 1;
            isDay = false;
        }
        else {

            // get day and next day's status
            doWInt1 = doWInt + 1;
            isDay = true;

        }

        // Adjusting to the correct range of 8-14
        if (doWInt < 8) {
            doWInt += 7;
        }
        else if (doWInt > 14) {
            doWInt -= 7;
        }

        if (doWInt1 < 8) {
            doWInt1 += 7;
        }
        else if (doWInt1 > 14) {
            doWInt1 -= 7;
        }



        Log.d("debug_dow", String.valueOf(doWInt));
        Log.d("debug_dow1", String.valueOf(doWInt1));


        // Clearing the Habit list and pulling data from DB
        myHabits.clear();
        new GetHabits().execute();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private class GetHabits extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            HabitDatabase dbSingleton = HabitDatabase.getInstance(getApplicationContext());
            SQLiteDatabase db = dbSingleton.getReadableDatabase();

            // Active habits have non-negative "streak"
            Cursor cursor = db.rawQuery("select * from HABITS where STREAK<>-1",
                    null);
            cursor.moveToFirst();

            if (cursor.moveToFirst()) {
                do {

                    boolean toAdd;
                    HabitHome row;
                    idHome = cursor.getInt(0);
                    nameHome = cursor.getString(1);
                    categoryHome = cursor.getString(2);
                    scheduleHome = cursor.getInt(3);
                    alarmHomeString = cursor.getString(4);

                    alarmHome = Calendar.getInstance();
                    try {
                        alarmHome.setTime(sdf.parse(alarmHomeString));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    doWStatusString = cursor.getString(doWInt);

                    switch(doWStatusString){

                        case "Done":
                            toAdd = false;
                            break;

                        case "Skipped":
                            toAdd = false;
                            break;

                        case "N":
                            toAdd = false;
                            break;

                        default:

                            // Get activity time
                            doWStatus = Calendar.getInstance();
                            try {
                                doWStatus.setTime(sdf.parse(doWStatusString));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            // See if it is for today
                            if ((!isDay) && ((doWStatus.get(Calendar.HOUR_OF_DAY)*60 +
                                    doWStatus.get(Calendar
                                            .MINUTE)) < dayEndTimeInt))  {

                                toAdd = true;

                            }
                            else if ((isDay) && ((doWStatus.get(Calendar.HOUR_OF_DAY)*60 +
                                    doWStatus.get(Calendar
                                            .MINUTE)) >= dayEndTimeInt)) {

                                toAdd = true;

                            }
                            else {

                                toAdd = false;
                            }

                            break;

                    }

                    // If did not get habit for today, try for tomorrow/yesterday
                    if (!toAdd) {

                        doWStatusString = cursor.getString(doWInt1);

                        switch(doWStatusString){

                            case "Done":
                                toAdd = false;
                                break;

                            case "Skipped":
                                toAdd = false;
                                break;

                            case "N":
                                toAdd = false;
                                break;

                            default:

                                // Get activity time
                                doWStatus = Calendar.getInstance();
                                try {
                                    doWStatus.setTime(sdf.parse(doWStatusString));
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                                // See if it is for today
                                if ((!isDay) && ((doWStatus.get(Calendar.HOUR_OF_DAY)*60 +
                                        doWStatus.get(Calendar
                                                .MINUTE)) >= dayEndTimeInt))  {

                                    toAdd = true;

                                }
                                else if ((isDay) && ((doWStatus.get(Calendar.HOUR_OF_DAY)*60 +
                                        doWStatus.get(Calendar
                                                .MINUTE)) < dayEndTimeInt)) {

                                    toAdd = true;

                                }
                                else {

                                    toAdd = false;
                                }

                                break;
                        }
                    }

                    // Only add if toAdd is TRUE
                    if (toAdd) {
                        row = new HabitHome(idHome, nameHome, categoryHome, scheduleHome,
                                alarmHome, doWStatus);
                        myHabits.add(row);

                        Log.d("debug_added", nameHome + " , " + String.valueOf(doWStatus.get(Calendar.HOUR_OF_DAY)*60 +
                                doWStatus.get(Calendar.MINUTE))  +
                                "," + String.valueOf(alarmHome.get(Calendar.HOUR_OF_DAY)*60 +
                                alarmHome.get(Calendar.MINUTE)));

                    }


                } while (cursor.moveToNext());
            }

            cursor.close();
            db.close();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (myHabits.isEmpty()) {
                Log.d("db_habits", "No Goals?");
            } else {


                // Showing only today's habits
//                Calendar now = Calendar.getInstance();
//                dayOfWeek = now.get(Calendar.DAY_OF_WEEK);
//
//                dayOfWeekBinary = 0;
//
//                switch (dayOfWeek) {
//                    case 1:
//                        // Sunday
//                        dayOfWeekBinary = 1;
//                        break;
//
//                    case 2:
//                        // Monday
//                        dayOfWeekBinary = 64;
//                        break;
//
//                    case 3:
//                        // Tuesday
//                        dayOfWeekBinary = 32;
//                        break;
//
//                    case 4:
//                        // Wednesday
//                        dayOfWeekBinary = 16;
//                        break;
//
//                    case 5:
//                        // Thursday
//                        dayOfWeekBinary = 8;
//                        break;
//
//                    case 6:
//                        // Friday
//                        dayOfWeekBinary = 4;
//                        break;
//
//                    case 7:
//                        // Saturday
//                        dayOfWeekBinary = 2;
//                        break;
//                }
//
//                nextDayOfWeekBinary = dayOfWeekBinary >> 1;
//
//                // Remove habits which are not today (after end_time) and tomorrow (before end_time)
//                for (int i=0; i<myHabits.size(); i++){
//
//                    Calendar habitAlarmTime;
//                    habitAlarmTime = myHabits.get(i).getHomeAlarmTime();
//                    int habitAlarmTimeInt = habitAlarmTime.get(Calendar.HOUR_OF_DAY)*60 +
//                            habitAlarmTime.get(Calendar.MINUTE);
//
//                    if (((dayOfWeekBinary & scheduleHome) !=0) && (dayEndTimeInt <=
//                            habitAlarmTimeInt)) {
//
//                        // keep it
//                    }
//                    else {
//
//                        if (((nextDayOfWeekBinary & scheduleHome) !=0) && (dayEndTimeInt >
//                                habitAlarmTimeInt)) {
//
//                            // keep it
//                        }
//
//                        else {
//
//                            // remove it
//                            myHabits.remove(i);
//
//                        }
//                    }
//
//                }


                // Sorting the Habit ArrayList according to time
                Collections.sort(myHabits, new Comparator<HabitHome>() {
                    @Override
                    public int compare(HabitHome lhs, HabitHome rhs) {

                        Calendar habitAlarmLHS = lhs.getHomeDoWStatus();
                        int habitAlarmLHSInt = habitAlarmLHS.get(Calendar.HOUR_OF_DAY)*60 +
                                habitAlarmLHS.get(Calendar.MINUTE);

                        Calendar habitAlarmRHS = rhs.getHomeDoWStatus();
                        int habitAlarmRHSInt = habitAlarmRHS.get(Calendar.HOUR_OF_DAY)*60 +
                                habitAlarmRHS.get(Calendar.MINUTE);

                        if (habitAlarmLHSInt < dayEndTimeInt ) {
                            habitAlarmLHSInt +=1440;
                        }

                        if (habitAlarmRHSInt < dayEndTimeInt ) {
                            habitAlarmRHSInt +=1440;
                        }

                        return habitAlarmLHSInt - habitAlarmRHSInt;
                    }
                });


                myAdapter.notifyDataSetChanged();

                ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {

                        HabitHome swipedHabit = myHabits.get(viewHolder.getAdapterPosition());
                        int adapterPosition = viewHolder.getAdapterPosition();

                        if (swipeDir == 8) {
                            // Left to Right Swipe
                            markAsDone(swipedHabit, adapterPosition);
                        } else {
                            // Right to Left Swipe
                            markAsSkipped(swipedHabit, adapterPosition);
                        }


                        myHabits.remove(viewHolder.getAdapterPosition());
                        homeHabits.getAdapter().notifyItemRemoved(viewHolder.getAdapterPosition());

                        // remove from DB as well or show
                        Log.d("Swipe Direction", String.valueOf(swipeDir));

                    }

                    @Override
                    public boolean onMove(RecyclerView recyclerView,
                                          RecyclerView.ViewHolder viewHolder, RecyclerView
                                                                      .ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView
                            .ViewHolder viewHolder, float dX, float dY, int actionState, boolean
                                                    isCurrentlyActive) {
                        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                            // Get RecyclerView item from the ViewHolder
                            View itemView = viewHolder.itemView;

                            Paint p = new Paint();
                            if (dX > 0) {

                                // Set green color for L2R swipe
                                p.setARGB(255, 85, 139, 47);
                                // Draw Rect with varying right side, equal to displacement dX
                                c.drawRect((float) itemView.getLeft(), (float) itemView.getTop(), dX,
                                        (float) itemView.getBottom(), p);
                            } else {

                                // Set yellow color for R2L swipe
                                p.setARGB(255, 255, 235, 59);
                                // Draw Rect with varying left side, equal to the item's right side plus negative displacement dX
                                c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(),
                                        (float) itemView.getRight(), (float) itemView.getBottom(), p);
                            }

                            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                        }
                    }

                };

                ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);

                itemTouchHelper.attachToRecyclerView(homeHabits);

                myAdapter.setOnItemClickListener(new NewHomeAdapter.ClickListener() {

                    @Override
                    public void onItemClick(int position, View v) {
                        Log.d("ItemOnClick", "onItemClick position: " + position);
                    }
                });
            }

        }
    }

    // Swiped Left to Right
    public void markAsDone(final HabitHome swipedHabit, final int adapterPosition) {

        // Update DB to mark it as Done
        showSnackBar("Marked as Done", swipedHabit, adapterPosition);

    }

    // Swiped Right to Left
    public void markAsSkipped(final HabitHome swipedHabit, final int adapterPosition) {

        // show snooze options
        AlertDialog.Builder skipAlertBuilder = new AlertDialog.Builder(this);
        LayoutInflater layoutInflater = this.getLayoutInflater();
        View radioGroupView = layoutInflater.inflate(R.layout.snooze_dialog, null);
        snoozeRadioGroup = (RadioGroup) radioGroupView.findViewById(R.id.radio_group);
        snoozeHours = (EditText) radioGroupView.findViewById(R.id.radio_edittext);

        skipAlertBuilder.setTitle("Remind me...");
        skipAlertBuilder.setView(radioGroupView)
                .setPositiveButton("OKAY", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // Pass RadioGroup selection
                        int checkedRadioButton = snoozeRadioGroup.getCheckedRadioButtonId();
                        int checkedIndex = snoozeRadioGroup.indexOfChild(snoozeRadioGroup
                                .findViewById(checkedRadioButton));
                        Log.d("radiogroup_selection", String.valueOf(checkedIndex));

                        // Show Snackbar
                        switch (checkedIndex) {
                            case 0:
                                showSnackBar("Snoozed for " + snoozeHours.getText()
                                                .toString() + "hours", swipedHabit,
                                        adapterPosition);
                                break;

                            case 1:
                                showSnackBar("Snoozed for tomorrow", swipedHabit, adapterPosition);
                                break;

                            case 2:
                                showSnackBar("Skipped for today", swipedHabit, adapterPosition);
                                break;

                        }
                        showSnackBar("Snoozed", swipedHabit, adapterPosition);
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        myHabits.add(adapterPosition, swipedHabit);
                        myAdapter.notifyDataSetChanged();
                    }
                });


        AlertDialog skipAlert = skipAlertBuilder.create();
        skipAlert.show();

    }


    public void showSnackBar(String action, final HabitHome swipedHabit, final int adapterPosition) {

        Snackbar sbDone = Snackbar.make(homeHabits, action, Snackbar.LENGTH_LONG)
                .setAction("UNDO", new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        myHabits.add(adapterPosition, swipedHabit);
                        myAdapter.notifyDataSetChanged();

                    }
                });
        sbDone.show();

        // DB operation
//        new SaveToDB().execute(new String[] {action});

    }

//    private class SaveToDB extends AsyncTask<String, Void, Void> {
//
//
//        @Override
//        protected Void doInBackground(String... dbAction) {
//
//            HabitDatabase dbSingleton = HabitDatabase.getInstance(getApplicationContext());
//            SQLiteDatabase db = dbSingleton.getWritableDatabase();
//
//            ContentValues cv = new ContentValues();
//
//            switch(dbAction[0]){
//
//                case "Marked as Done":
//                    //
//
//
//                    break;
//
//
//            }
//
//            db.update();
//            db.close();
//
//            return null;
//        }
//    }

}
