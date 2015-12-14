package com.example.perfectday;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
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

public class AddHabit extends Activity {

    Spinner category;
    String category_value;
    TextView alarmTime, addError;
    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa");
    Calendar cal;
    ToggleButton mon, tue, wed, thu, fri, sat, sun;
    Button addButton;
    EditText addHabit;
    int schedule, seq, id;
    String[] addDays;
    Habit newHabit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_habit);

        getActionBar().setDisplayHomeAsUpEnabled(true);

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
        alarmTime.setText(sdf.format(cal.getTime()));

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
                        addDays[0] = "Y";
                    }
                    else {
                        addDays[0] = "N";
                    }

                    if (tue.isChecked()) {
                        schedule += 32;
                        addDays[1] = "Y";
                    }
                    else {
                        addDays[1] = "N";
                    }

                    if (wed.isChecked()) {
                        schedule += 16;
                        addDays[2] = "Y";
                    }
                    else {
                        addDays[2] = "N";
                    }

                    if (thu.isChecked()) {
                        schedule += 8;
                        addDays[3] = "Y";
                    }
                    else {
                        addDays[3] = "N";
                    }

                    if (fri.isChecked()) {
                        schedule += 4;
                        addDays[4] = "Y";
                    }
                    else {
                        addDays[4] = "N";
                    }

                    if (sat.isChecked()) {
                        schedule += 2;
                        addDays[5] = "Y";
                    }
                    else {
                        addDays[5] = "N";
                    }

                    if (sun.isChecked()) {
                        schedule += 1;
                        addDays[6] = "Y";
                    }
                    else {
                        addDays[6] = "N";
                    }


                    newHabit = new Habit(0, addHabit.getText().toString(), category_value, schedule,
                            cal, cal, cal, 0, 0, addDays);

                    Log.d("AddHabit_dbInsert", cal.toString());

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

            Log.d("AddHabit_newCal", cal.toString());

        }

    }


    public void setAlarm() {

        // Set alarms later

        // Save to DB

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
            SimpleDateFormat dateFormatDB = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");
            String dbTime = dateFormatDB.format(habitToAdd[0].getAlarmTime().getTime());
            String dbThisAlarm = dateFormatDB.format(habitToAdd[0].getThisAlarm().getTime());
            String dbNextAlarm = dateFormatDB.format(habitToAdd[0].getNextAlarm().getTime());


            cv.put("ALARMTIME", dbTime );
            cv.put("THISALARM", dbThisAlarm);
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

            // Go Home!
            Intent iForHome = new Intent(AddHabit.this, MainActivity.class);
            iForHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            iForHome.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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