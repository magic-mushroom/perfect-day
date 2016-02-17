package com.example.perfectday;

import android.app.AlertDialog;
import android.content.ContentValues;
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
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
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
    HomeAdapter myAdapter;
    RecyclerView.LayoutManager myLayoutManager;

    RadioGroup snoozeRadioGroup;
    EditText snoozeHours;
    String snoozeHoursText;
    TextView snoozeError;

    // Sharedpreferences to save day start-end time
    SharedPreferences sharedPref;
    Calendar dayEndTime;
    int dayEndTimeInt;

    // To get the day's field
    String doWStatusString;
    int doWInt, doWInt1, DBcol, DBcol2;
    Calendar doWStatus, now;
    Boolean isDay;

    // DB Save
    boolean DBop;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");

    // Mapping column index with column name
    String[] indexToName = {"M", "T", "W", "TH", "F", "SA", "SU"};
    int[] indexToBinary = {64,32,16,8,4,2,1};


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

        myAdapter = new HomeAdapter(myHabits);
        homeHabits.setAdapter(myAdapter);

        // Getting today's date
        now = Calendar.getInstance();
        dayOfWeek = now.get(Calendar.DAY_OF_WEEK);

        //M (=8), T, W, TH, F, SA, SU

        doWInt = dayOfWeek+6;


        // If before dayEndTime, get the day and previous day's status
        if ((now.get(Calendar.HOUR_OF_DAY)*60 + now.get(Calendar.MINUTE))<dayEndTimeInt) {

            doWInt1 = doWInt - 1;
            isDay = false;
            dayEndTime.set(Calendar.DAY_OF_YEAR, now.get(Calendar.DAY_OF_YEAR));

        }
        else {

            // get day and next day's status
            doWInt1 = doWInt + 1;
            isDay = true;
            dayEndTime.set(Calendar.DAY_OF_YEAR, now.get(Calendar.DAY_OF_YEAR) + 1);

        }

        Log.d("snooze_dayendtime", sdf.format(dayEndTime.getTime()));

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
                                doWStatus.set(Calendar.DAY_OF_YEAR, now.get(Calendar.DAY_OF_YEAR));

                            }
                            else if ((isDay) && ((doWStatus.get(Calendar.HOUR_OF_DAY)*60 +
                                    doWStatus.get(Calendar
                                            .MINUTE)) >= dayEndTimeInt)) {

                                toAdd = true;
                                doWStatus.set(Calendar.DAY_OF_YEAR, now.get(Calendar
                                        .DAY_OF_YEAR));

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
                                    doWStatus.set(Calendar.DAY_OF_YEAR, now.get(Calendar
                                            .DAY_OF_YEAR) - 1);


                                }
                                else if ((isDay) && ((doWStatus.get(Calendar.HOUR_OF_DAY)*60 +
                                        doWStatus.get(Calendar
                                                .MINUTE)) < dayEndTimeInt)) {

                                    toAdd = true;
                                    doWStatus.set(Calendar.DAY_OF_YEAR, now.get(Calendar
                                            .DAY_OF_YEAR) + 1);

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

                // Sorting the Habit ArrayList according to time
                myHabits = sortHabits(myHabits);

//                Collections.sort(myHabits, new Comparator<HabitHome>() {
//                    @Override
//                    public int compare(HabitHome lhs, HabitHome rhs) {
//
//                        Calendar habitAlarmLHS = lhs.getHomeDoWStatus();
//                        int habitAlarmLHSInt = habitAlarmLHS.get(Calendar.HOUR_OF_DAY)*60 +
//                                habitAlarmLHS.get(Calendar.MINUTE);
//
//                        Calendar habitAlarmRHS = rhs.getHomeDoWStatus();
//                        int habitAlarmRHSInt = habitAlarmRHS.get(Calendar.HOUR_OF_DAY)*60 +
//                                habitAlarmRHS.get(Calendar.MINUTE);
//
//                        if (habitAlarmLHSInt < dayEndTimeInt ) {
//                            habitAlarmLHSInt +=1440;
//                        }
//
//                        if (habitAlarmRHSInt < dayEndTimeInt ) {
//                            habitAlarmRHSInt +=1440;
//                        }
//
//                        return habitAlarmLHSInt - habitAlarmRHSInt;
//                    }
//                });


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

                myAdapter.setOnItemClickListener(new HomeAdapter.ClickListener() {

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
        Calendar doWStatus = swipedHabit.getHomeDoWStatus();
        showSnackBar("Marked as Done", doWStatus, swipedHabit, adapterPosition);

    }

    // Swiped Right to Left
    public void markAsSkipped(final HabitHome swipedHabit, final int adapterPosition) {

        // show snooze options
        AlertDialog.Builder skipAlertBuilder = new AlertDialog.Builder(this);
        LayoutInflater layoutInflater = this.getLayoutInflater();

        View radioGroupView = layoutInflater.inflate(R.layout.snooze_dialog, null);
        snoozeRadioGroup = (RadioGroup) radioGroupView.findViewById(R.id.radio_group);
        snoozeHours = (EditText) radioGroupView.findViewById(R.id.radio_edittext);
        snoozeError = (TextView) radioGroupView.findViewById(R.id.snooze_error);

        snoozeHours.setText("1");

        // Clearing the EditText when value is changed
        snoozeHours.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                snoozeError.setText("");

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        // Building the alertDialog
        skipAlertBuilder.setTitle("Remind me...");
        skipAlertBuilder.setView(radioGroupView)
                .setPositiveButton("OKAY", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // This method is overridden later
                    }

                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        myHabits.add(adapterPosition, swipedHabit);
                        myAdapter.notifyDataSetChanged();
                    }
                });


        final AlertDialog skipAlert = skipAlertBuilder.create();
        skipAlert.show();

        // Overriding the alertDialog PositiveButton onclick handler
        skipAlert.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int checkedRadioButton = snoozeRadioGroup.getCheckedRadioButtonId();
                int checkedIndex = snoozeRadioGroup.indexOfChild(snoozeRadioGroup
                        .findViewById(checkedRadioButton));
                Log.d("radiogroup_selection", String.valueOf(checkedIndex));

                Calendar doWStatus = swipedHabit.getHomeDoWStatus();

                // Show Snackbar
                switch (checkedIndex) {
                    case 0:

                        // Get the updated time
                        int hrs = Integer.parseInt(snoozeHours.getText().toString());
                        doWStatus.add(Calendar.HOUR_OF_DAY, hrs);

                        Log.d("snooze_doWStatus", sdf.format(doWStatus.getTime()));

                        Log.d("snooze_diff", String.valueOf(doWStatus.compareTo
                                (dayEndTime)));

                        // If snoozed till day-end, show error
                        if (doWStatus.compareTo(dayEndTime) >= 0) {

                            snoozeError.setText(R.string.error_snooze);

                        }
                        else if (hrs==0) {

                            snoozeError.setText(R.string.error2_snooze);
                        }
                        else {

                            // Dismiss alert, Show snackbar, then Save to DB
                            skipAlert.dismiss();

                            if (hrs==1) {
                                showSnackBar("Snoozed for " + hrs + " hour", doWStatus, swipedHabit,
                                        adapterPosition);
                            }
                            else {
                                showSnackBar("Snoozed for " + hrs + " hours", doWStatus,
                                        swipedHabit,
                                        adapterPosition);
                            }

                        }

                        break;

                    case 1:
                        showSnackBar("Snoozed for tomorrow", doWStatus, swipedHabit,
                                adapterPosition);
                        break;

                    case 2:
                        showSnackBar("Skipped for today", doWStatus, swipedHabit, adapterPosition);
                        break;

                }

            }
        });


    }



    public void showSnackBar(final String action, final Calendar doWStatus, final HabitHome
            swipedHabit, final int adapterPosition) {

        DBop = true;
        Snackbar sbDone = Snackbar.make(homeHabits, action, Snackbar.LENGTH_LONG)
                .setAction("UNDO", new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        myHabits.add(adapterPosition, swipedHabit);
                        myAdapter.notifyDataSetChanged();
                        DBop = false;

                    }
                });
        sbDone.show();

        // DB operation
        Runnable r = new Runnable() {
            @Override
            public void run(){

                if (DBop) {

                    Calendar swipedAlarm = swipedHabit.getHomeAlarmTime();
                    if (isDay & ((swipedAlarm.get(Calendar.HOUR_OF_DAY)*60 + swipedAlarm
                            .get(Calendar.MINUTE)) >= dayEndTimeInt)) {

                        DBcol = doWInt;

                        if (action == "Snoozed for tomorrow") {
                            DBcol2 = doWInt1;
                        }
                    }
                    else if (isDay & ((swipedAlarm.get(Calendar.HOUR_OF_DAY)*60 + swipedAlarm
                            .get(Calendar.MINUTE)) < dayEndTimeInt)){

                        DBcol = doWInt1;

                        if (action == "Snoozed for tomorrow") {
                            DBcol2 = doWInt1 + 1;
                        }
                    }
                    else if (!isDay & ((swipedAlarm.get(Calendar.HOUR_OF_DAY)*60 + swipedAlarm
                            .get(Calendar.MINUTE)) < dayEndTimeInt)) {

                        DBcol = doWInt;

                        if (action == "Snoozed for tomorrow") {
                            DBcol2 = doWInt + 1;
                        }
                    }
                    else if (!isDay & ((swipedAlarm.get(Calendar.HOUR_OF_DAY)*60 +
                            swipedAlarm.get(Calendar.MINUTE)) >= dayEndTimeInt)) {

                        DBcol = doWInt1;

                        if (action == "Snoozed for tomorrow") {
                            DBcol2 = doWInt;
                        }
                    }
                    else {

                        Log.d("snooze_done", "We have a problem!");
                    }

                    // Adjusting DBCol2 to the correct range of 8-14
                    if (DBcol2 > 14) {
                        DBcol2 -= 7;
                    }


                    if (((action.split("\\s")[0]) == "Snoozed") && (action.split("\\s")[2] !=
                            "tomorrow")) {

                        Log.d("snooze_habit", sdf.format(doWStatus.getTime()));
                        swipedHabit.setHomeDoWStatus(doWStatus);

                        myHabits.add(swipedHabit);
                        myHabits = sortHabits(myHabits);

                        myAdapter.notifyDataSetChanged();

                    }

                    new SaveToDB(action, swipedHabit).execute();

//                    switch(action) {
//
//                        case "Marked as Done":
//
//                            break;
//
//                        case "Snoozed for tomorrow":
//
//                            break;
//
//                        case "Skipped for today":
//
//                            break;
//
//                        default:
//                            Log.d("string_split", action.split("\\s")[2]);
//                            Log.d("snooze_habit", sdf.format(doWStatus.getTime()));
//                            swipedHabit.setHomeDoWStatus(doWStatus);
//
//                            myHabits.add(swipedHabit);
//                            myHabits = sortHabits(myHabits);
//
//                            myAdapter.notifyDataSetChanged();
//
//                            break;
//
//                    }

                }

            }
        };

        Handler h = new Handler();
        h.postDelayed(r, 3500);

//        new SaveToDB().execute(new String[] {action});

    }

    private class SaveToDB extends AsyncTask<Void, Void, Void> {

        String DBaction;
        HabitHome DBswipedHabit;

        public SaveToDB(String DBaction,HabitHome DBswipedHabit) {

            super();

            this.DBaction = DBaction;
            this.DBswipedHabit = DBswipedHabit;

        }


        @Override
        protected Void doInBackground(Void... params) {
            HabitDatabase dbSingleton = HabitDatabase.getInstance(getApplicationContext());
            SQLiteDatabase db = dbSingleton.getWritableDatabase();

            ContentValues cv = new ContentValues();

            int originalTime = DBswipedHabit.getHomeAlarmTime().get(Calendar.HOUR_OF_DAY)
                    *60 + DBswipedHabit.getHomeAlarmTime().get(Calendar.MINUTE);
            int newTime = DBswipedHabit.getHomeDoWStatus().get(Calendar.HOUR_OF_DAY)*60 +
                    DBswipedHabit.getHomeDoWStatus().get(Calendar.MINUTE);

            switch(DBaction) {

                case "Marked as Done":

                    cv.put(indexToName[DBcol-8], "Done");

                    if ((originalTime >= dayEndTimeInt) & (newTime < dayEndTimeInt)) {

                        if (isDay) {
                            DBcol2 = doWInt1;
                        } else {
                            DBcol2 = doWInt;
                        }

                        if ((indexToBinary[DBcol2-8] & DBswipedHabit.getHomeSchedule()) == 0){

                            cv.put(indexToName[DBcol2-8], sdf.format(DBswipedHabit.getHomeAlarmTime()
                                    .getTime()));

                        }
                        else {

                            cv.put(indexToName[DBcol2-8], "N");

                        }

                    }

                    break;

                case "Snoozed for tomorrow":

                    cv.put(indexToName[DBcol-8], "Skip");
                    cv.put(indexToName[DBcol2-8], sdf.format(DBswipedHabit.getHomeAlarmTime()
                            .getTime()));
                    break;

                case "Skipped for today":

                    cv.put(indexToName[DBcol-8], "Skip");

                    if ((originalTime >= dayEndTimeInt) & (newTime < dayEndTimeInt)) {

                        if (isDay) {
                            DBcol2 = doWInt1;
                        } else {
                            DBcol2 = doWInt;
                        }

                        if ((indexToBinary[DBcol2-8] & DBswipedHabit.getHomeSchedule()) == 0){

                            cv.put(indexToName[DBcol2-8], sdf.format(DBswipedHabit.getHomeAlarmTime()
                                    .getTime()));

                        }
                        else {

                            cv.put(indexToName[DBcol2-8], "N");

                        }

                    }

                    break;

                default:

                    if ((originalTime >= dayEndTimeInt) & (newTime < dayEndTimeInt)) {

                        if (isDay){
                            DBcol2 = doWInt1;
                        }
                        else {
                            DBcol2 = doWInt;
                        }

                        cv.put(indexToName[DBcol-8], "Skipped");
                        cv.put(indexToName[DBcol2-8], sdf.format(DBswipedHabit.getHomeDoWStatus()
                                .getTime()));

                        // Add a service with Habit ID to run at DayEndTime to set HomeAlarmTime for
                        // the day - Cancel it when any other action takes place

                    }
                    else {

                        cv.put(indexToName[DBcol-8], sdf.format(DBswipedHabit.getHomeDoWStatus()
                                .getTime()));
                    }

            }

            db.update("HABITS", cv, "ID = "+ DBswipedHabit.getHomeId(), null);

            db.close();

            return null;
        }
    }


    public ArrayList<HabitHome> sortHabits( ArrayList<HabitHome> myHabitsSorted) {

        Collections.sort(myHabitsSorted, new Comparator<HabitHome>() {
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

        return myHabitsSorted;

    }
}
