package com.example.perfectday;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends Activity {

    Button btn;
    ListView homeHabits;
    ArrayList<HabitHome> myHabits = new ArrayList<HabitHome>();
    String nameHome, categoryHome, dayOfWeekHome, alarmHomeString;
    Calendar alarmHome;
    int idHome, dayOfWeek;
//    String[] daysHome;
    HomeAdapter myAdapter;
    Context context;
//    SwipeDetector swipeDetector;

    public View.OnTouchListener gestureListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        daysHome = new String[7];

        context = this;

        btn = (Button)findViewById(R.id.sample_button);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent iForAddActivity = new Intent(MainActivity.this, AddHabit.class);
                startActivity(iForAddActivity);
            }
        });

        // Showing the day's habits
        homeHabits = (ListView) findViewById(R.id.home_habits);
        Calendar now = Calendar.getInstance();
        dayOfWeek = now.get(Calendar.DAY_OF_WEEK);

        switch (dayOfWeek) {
            case (1):
                //Sunday
                dayOfWeekHome = "SU";
                break;

            case (2):
                //Monday
                dayOfWeekHome = "M";
                break;

            case (3):
                //Tuesday
                dayOfWeekHome = "T";
                break;

            case (4):
                //Wednesday
                dayOfWeekHome = "W";
                break;

            case (5):
                //Thursday
                dayOfWeekHome = "TH";
                break;

            case (6):
                //Friday
                dayOfWeekHome = "F";
                break;

            case (7):
                //Saturday
                dayOfWeekHome = "SA";
                break;
        }

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


    private class GetHabits extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... params) {

            HabitDatabase dbSingleton = HabitDatabase.getInstance(getApplicationContext());
            SQLiteDatabase db = dbSingleton.getReadableDatabase();

            Cursor cursor = db.rawQuery("select * from HABITS where " + dayOfWeekHome + " = 'Y'",
                    null);
            cursor.moveToFirst();

            if (cursor.moveToFirst()) {
                do {

                    HabitHome row;
                    idHome = cursor.getInt(0);
                    nameHome = cursor.getString(1);
                    categoryHome = cursor.getString(2);
                    alarmHomeString = cursor.getString(4);

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");
                    alarmHome = Calendar.getInstance();
                    try {
                        alarmHome.setTime(sdf.parse(alarmHomeString));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    row = new HabitHome(idHome, nameHome, categoryHome, alarmHome);

                    myHabits.add(row);

                }while (cursor.moveToNext());
            }

            cursor.close();
            db.close();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (myHabits.isEmpty()) {
                Log.d("db_habits", "No Goals?");
            }

            else {

                myAdapter = new HomeAdapter(context, myHabits);
                homeHabits.setAdapter(myAdapter);
                myAdapter.notifyDataSetChanged();

                homeHabits.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        Toast.makeText(MainActivity.this, "Clicked on " + position, Toast.LENGTH_SHORT)
                                .show();


                    }
                });

//                homeHabits.setOnTouchListener(swipeDetector);


            }

        }
    }


}
