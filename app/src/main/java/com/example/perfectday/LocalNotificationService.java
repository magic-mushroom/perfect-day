package com.example.perfectday;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class LocalNotificationService extends WakeIntentService {

    String title;
    int id, schedule, dayOfWeek, dayOfWeekBinary;

    Calendar nextAlarm, prevAlarm, now;

    String[] indexToName = {"SU", "M", "T", "W", "TH", "F", "SA"};

    SimpleDateFormat sdfDB = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    public LocalNotificationService() {
        super("LocalNotificationService");
    }


    protected void onHandleIntent(Intent i){

        id = i.getIntExtra("ID", 0);
        title = i.getStringExtra("TITLE");
        schedule = i.getIntExtra("SCHEDULE", 0);
        prevAlarm = (Calendar) i.getSerializableExtra("NEXTALARM");
        nextAlarm = (Calendar) i.getSerializableExtra("ALARMTIME");


        NotificationManager notifMgr = (NotificationManager) this.getSystemService
                (NOTIFICATION_SERVICE);

        Intent notifIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingNotifIntent = PendingIntent.getActivity(this, id, notifIntent,
                PendingIntent.FLAG_ONE_SHOT);

        Notification notif = new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(title)
                .setSmallIcon(android.R.drawable.gallery_thumb)
                .setContentIntent(pendingNotifIntent)
                .setAutoCancel(true)
                .build();

        notif.defaults |= Notification.DEFAULT_ALL;

        notifMgr.notify(id, notif);


        // Setting up next alarm
        setAlarm();

        super.onHandleIntent(i);

    }

    public void setAlarm(){

        now = Calendar.getInstance();

        nextAlarm.set(Calendar.DAY_OF_YEAR, prevAlarm.get(Calendar.DAY_OF_YEAR));

        dayOfWeek = now.get(Calendar.DAY_OF_WEEK);
        dayOfWeekBinary = 0;

        // 1 = Sun, 2 = Mon
        // schedule = MTWTFSS

        while ((dayOfWeekBinary & schedule) == 0) {

            switch(dayOfWeek){

                case 1:
                    dayOfWeekBinary = 64;
                    dayOfWeek = 2;
                    break;

                case 2:
                    dayOfWeekBinary = 32;
                    dayOfWeek = 3;
                    break;

                case 3:
                    dayOfWeekBinary = 16;
                    dayOfWeek = 4;
                    break;

                case 4:
                    dayOfWeekBinary = 8;
                    dayOfWeek = 5;
                    break;

                case 5:
                    dayOfWeekBinary = 4;
                    dayOfWeek = 6;
                    break;

                case 6:
                    dayOfWeekBinary = 2;
                    dayOfWeek = 7;
                    break;

                case 7:
                    dayOfWeekBinary = 1;
                    dayOfWeek = 1;
                    break;

            }

            nextAlarm.add(Calendar.DAY_OF_YEAR, 1);

        }

        new SaveAlarm().execute();


    }


    private class SaveAlarm extends AsyncTask<Void, Void, Void>{


        @Override
        protected Void doInBackground(Void... params) {

            HabitDatabase dbSingleton = HabitDatabase.getInstance(getApplicationContext());
            SQLiteDatabase db = dbSingleton.getWritableDatabase();

            ContentValues cv = new ContentValues();
            cv.put("NEXTALARM", sdfDB.format(nextAlarm.getTime()));
            cv.put(indexToName[dayOfWeek-1], sdfDB.format(nextAlarm.getTime()));

            db.update("HABITS", cv, "ID=" + id, null);

            db.close();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            Log.d("Notification", "Reached end of Service, alarm saved");

        }
    }


}
