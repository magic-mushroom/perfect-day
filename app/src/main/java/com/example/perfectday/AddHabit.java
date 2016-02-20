package com.example.perfectday;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AddHabit extends AppCompatActivity {

    Spinner category;
    String category_value, alarmTimeText, alarmTimeTextFull;
    TextView alarmTime, addError;
    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa");
    SimpleDateFormat sdfDB = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");
    Calendar cal, calAlarm;
    ToggleButton mon, tue, wed, thu, fri, sat, sun;
    Button addButton;
    EditText addHabit;
    int schedule, id;
    String[] addDays;
    Habit newHabit;

    int dayOfWeekAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_habit);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        category = (Spinner) findViewById(R.id.add_category);

        schedule = 0;
        addDays = new String[7];


        // Setting up Category spinner
        ArrayAdapter<CharSequence> categ_dropdown =  ArrayAdapter.createFromResource(this, R
                .array.categories, R.layout.category_spinner);
        categ_dropdown.setDropDownViewResource(R.layout.spinner_dropdown);
        category.setAdapter(categ_dropdown);
        category.setSelection(0);

        category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                category_value = (String) parent.getItemAtPosition(pos);
            }

            public void onNothingSelected(AdapterView<?> parent) {
                // Nothing
            }

        });


        // Setting listeners for DAY togglebuttons
        mon = (ToggleButton) findViewById(R.id.button_monday);
        tue = (ToggleButton) findViewById(R.id.button_tuesday);
        wed = (ToggleButton) findViewById(R.id.button_wednesday);
        thu = (ToggleButton) findViewById(R.id.button_thursday);
        fri = (ToggleButton) findViewById(R.id.button_friday);
        sat = (ToggleButton) findViewById(R.id.button_saturday);
        sun = (ToggleButton) findViewById(R.id.button_sunday);

        mon.setChecked(false);
        tue.setChecked(false);
        wed.setChecked(false);
        thu.setChecked(false);
        fri.setChecked(false);
        sat.setChecked(false);
        sun.setChecked(false);



        // Showing current time and timepickerdialog
        alarmTime = (TextView) findViewById(R.id.add_time);
        cal = Calendar.getInstance();
        alarmTimeText = sdf.format(cal.getTime());
        alarmTimeTextFull = sdfDB.format(cal.getTime());
        alarmTime.setText(alarmTimeText);

        alarmTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bundle bundle = new Bundle();
                bundle.putSerializable("calendar", cal);
                showTimePickerDialog(bundle);

            }
        });


        // Setting AddHabit button clicklistener
        addError = (TextView) findViewById(R.id.add_error);
        addHabit = (EditText) findViewById(R.id.add_habit);
        addButton = (Button) findViewById(R.id.add_button);

        addButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {

                // Error if no habit entered
                if (addHabit.getText().toString().trim().length() <= 0) {

                    addError.setText(R.string.error_nohabit);
                }

                // Error if no day selected
                else if (mon.isChecked()&tue.isChecked()&wed.isChecked()&thu.isChecked()&fri.isChecked
                            ()&sat.isChecked()&sun.isChecked()) {

                        addError.setText(R.string.error_noday);

                    }

                // Save Habit
                else {

                    if (mon.isChecked()) {
                        schedule += 64;
                        addDays[0] = alarmTimeTextFull;
                    }
                    else {
                        addDays[0] = "N";
                    }

                    Log.d("debug_monday", addDays[0]);

                    if (tue.isChecked()) {
                        schedule += 32;
                        addDays[1] = alarmTimeTextFull;
                    }
                    else {
                        addDays[1] = "N";
                    }

                    Log.d("debug_tuesday", addDays[1]);

                    if (wed.isChecked()) {
                        schedule += 16;
                        addDays[2] = alarmTimeTextFull;
                    }
                    else {
                        addDays[2] = "N";
                    }

                    Log.d("debug_wednesday", addDays[2]);

                    if (thu.isChecked()) {
                        schedule += 8;
                        addDays[3] = alarmTimeTextFull;
                    }
                    else {
                        addDays[3] = "N";
                    }

                    Log.d("debug_thursday", addDays[3]);

                    if (fri.isChecked()) {
                        schedule += 4;
                        addDays[4] = alarmTimeTextFull;
                    }
                    else {
                        addDays[4] = "N";
                    }

                    Log.d("debug_friday", addDays[4]);

                    if (sat.isChecked()) {
                        schedule += 2;
                        addDays[5] = alarmTimeTextFull;
                    }
                    else {
                        addDays[5] = "N";
                    }

                    Log.d("debug_saturday", addDays[5]);

                    if (sun.isChecked()) {
                        schedule += 1;
                        addDays[6] = alarmTimeTextFull;
                    }
                    else {
                        addDays[6] = "N";
                    }

                    Log.d("debug_sunday", addDays[6]);


                    newHabit = new Habit(0, addHabit.getText().toString(), category_value, schedule,
                            cal, cal, 0, 0, addDays);

                    Log.d("AddHabit_dbInsert", sdfDB.format(cal.getTime()));

                    setAlarm();

                }
            }
        });


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){

            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    // Function to initiate fragment for showing TimePicker
    public void showTimePickerDialog(Bundle bundle) {

        DialogFragment timeFragment = new TimePickerFragment();
        timeFragment.setArguments(bundle);
        timeFragment.show(getFragmentManager(), "timePicker");

    }


    // TimePickerFragment class
    public class TimePickerFragment extends DialogFragment implements TimePickerDialog
            .OnTimeSetListener {

        Calendar c;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            Bundle bundle = this.getArguments();
            if (bundle != null) {
                c = (Calendar) bundle.getSerializable("calendar");
            }

            TimePickerDialog tp = new TimePickerDialog(getActivity(), this, c.get(Calendar
                    .HOUR_OF_DAY), c.get(Calendar.MINUTE), false);
            tp.setCancelable(true);
            tp.setCanceledOnTouchOutside(true);
            return tp;

        }


        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

            Calendar newCal = Calendar.getInstance();
            newCal.set(Calendar.HOUR_OF_DAY, hourOfDay);
            newCal.set(Calendar.MINUTE, minute);

            alarmTime.setText(sdf.format(newCal.getTime()));
            cal = newCal;
            alarmTimeText = sdf.format(cal.getTime());
            alarmTimeTextFull = sdfDB.format(cal.getTime());

            Log.d("AddHabit_newCal", cal.toString());

        }

    }


    public void setAlarm() {

        // Set alarm times
        Calendar now = Calendar.getInstance();
        dayOfWeekAdd = now.get(Calendar.DAY_OF_WEEK);

        int dayOfWeekAddBinary = 0;
        int i;

        // If time for alarm has already passed, start from next day
        if (cal.compareTo(now)<=0) {

            i = 1;
            dayOfWeekAdd +=1;

            // Sunday comes after Saturday
            if (dayOfWeekAdd == 8){
                dayOfWeekAdd = 1;
            }
        }
        else {
            i = 0;
        }



        // 1 = Sun, 2 = Mon
        // schedule = MTWTFSS

        while ((dayOfWeekAddBinary & schedule) == 0) {

            calAlarm = cal;
            calAlarm.add(Calendar.DAY_OF_YEAR, i);

            switch (dayOfWeekAdd) {

                case 1:
                    dayOfWeekAddBinary = 1;
                    dayOfWeekAdd = 2;
                    break;

                case 2:
                    dayOfWeekAddBinary = 64;
                    dayOfWeekAdd = 3;
                    break;

                case 3:
                    dayOfWeekAddBinary = 32;
                    dayOfWeekAdd = 4;
                    break;

                case 4:
                    dayOfWeekAddBinary = 16;
                    dayOfWeekAdd = 5;
                    break;

                case 5:
                    dayOfWeekAddBinary = 8;
                    dayOfWeekAdd = 6;
                    break;

                case 6:
                    dayOfWeekAddBinary = 4;
                    dayOfWeekAdd = 7;
                    break;

                case 7:
                    dayOfWeekAddBinary = 2;
                    dayOfWeekAdd = 1;
                    break;

            }

            i++;

        }


//        Log.d("bitwise_test", String.valueOf(dayOfWeekAddBinary));
//
//        calThisAlarm = cal;
//
//        while ((dayOfWeekAddBinary & schedule) == 0){
//
//            calThisAlarm.add(Calendar.DAY_OF_YEAR, 1);
//
//            dayOfWeekAddBinary = dayOfWeekAddBinary >> 1;
//
//            Log.d("bitwise_test", String.valueOf(dayOfWeekAddBinary));
//
//        }

        // Only for Debug
        Log.d("bitwise_test", sdfDB.format(calAlarm.getTime()));


        // Save to DB

        newHabit.setNextAlarm(calAlarm);


        new GetLatestGoalId().execute();

//        new SaveHabit().execute(new Habit[] {newHabit});

    }

    private class SaveHabit extends AsyncTask<Habit, Void, Boolean> {


        @Override
        protected Boolean doInBackground(Habit... habitToAdd) {

            HabitDatabase dbSingleton = HabitDatabase.getInstance(getApplicationContext());
            SQLiteDatabase db = dbSingleton.getWritableDatabase();

            // Inserting values
            ContentValues cv = new ContentValues();
            cv.put("TITLE", habitToAdd[0].getName());
            cv.put("CATEGORY", habitToAdd[0].getCategory());
            cv.put("SCHEDULE", habitToAdd[0].getSchedule());
            cv.put("STREAK", habitToAdd[0].getCurrentStreak());
            cv.put("TOTAL", habitToAdd[0].getTotalDone());

            // Putting DoW data
            String[] daysStatus = habitToAdd[0].getDays();

            cv.put("M", daysStatus[0]);
            cv.put("T", daysStatus[1]);
            cv.put("W", daysStatus[2]);
            cv.put("TH", daysStatus[3]);
            cv.put("F", daysStatus[4]);
            cv.put("SA", daysStatus[5]);
            cv.put("SU", daysStatus[6]);

            // Putting Calendar dates
            String dbTime = sdfDB.format(habitToAdd[0].getAlarmTime().getTime());
            String dbNextAlarm = sdfDB.format(habitToAdd[0].getNextAlarm().getTime());


            cv.put("ALARMTIME", dbTime);
            cv.put("NEXTALARM", dbNextAlarm);

            db.insert("HABITS", null, cv);

            db.close();

            Log.d("AddHabit_insertDBOp", dbTime);

            //TITLE, CATEGORY, SCHEDULE, ALARMTIME, THISALARM, NEXTALARM, STREAK, TOTAL,
            // M, T, W, TH, F, SA, SU

            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {

            // Setting up alarm
            Intent iForAlarm = new Intent(AddHabit.this, LocalNotificationReceiver.class);
            iForAlarm.putExtra("ID", newHabit.getId());
            iForAlarm.putExtra("TITLE", newHabit.getName());

            PendingIntent piForAlarm = PendingIntent.getBroadcast(AddHabit.this, newHabit.getId()
                    , iForAlarm, PendingIntent.FLAG_ONE_SHOT);

            AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alarmMgr.set(AlarmManager.RTC_WAKEUP, newHabit.getNextAlarm().getTimeInMillis(), piForAlarm);

            Log.d("Alarm", "Alarm set for new habit");

            // Go Home!
            Intent iForHome = new Intent(AddHabit.this, MainActivity.class);
            iForHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            iForHome.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            iForHome.putExtra("toast", R.string.habit_added);
            startActivity(iForHome);

        }
    }

    private class GetLatestGoalId extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... params) {
            HabitDatabase dbInstance = HabitDatabase.getInstance(getApplicationContext());
            SQLiteDatabase db1 = dbInstance.getReadableDatabase();
            Cursor cursor = db1.rawQuery("select seq from sqlite_sequence where name=\"HABITS\"",
                    null);
            cursor.moveToFirst();

            if (cursor.moveToFirst()) {
                do {
                    id = cursor.getInt(0);


                    Log.d("db_read_seq", String.valueOf(id));
                } while (cursor.moveToNext());
            }
            else {
                id = 0;
            }
            cursor.close();
            db1.close();
            return null;
        }


        @Override
        protected void onPostExecute(Void args) {

            newHabit.setId(id);

            new SaveHabit().execute(new Habit[] {newHabit});

        }
    }



}