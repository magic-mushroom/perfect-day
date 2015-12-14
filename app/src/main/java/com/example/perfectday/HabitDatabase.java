package com.example.perfectday;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class HabitDatabase extends SQLiteOpenHelper {

    private static HabitDatabase singleton = null;

    private static final String DB_NAME = "myhabits.db";
    private static final int SCHEMA = 1;

    synchronized static HabitDatabase getInstance(Context ctxt) {
        if (singleton == null) {
            singleton = new HabitDatabase(ctxt.getApplicationContext());
        }

        return (singleton);
    }

    public HabitDatabase(Context ctxt) {
        super(ctxt, DB_NAME, null, SCHEMA);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.beginTransaction();

            db.execSQL("CREATE TABLE HABITS (ID INTEGER PRIMARY KEY AUTOINCREMENT, TITLE, " +
                    "CATEGORY, SCHEDULE, ALARMTIME, THISALARM, NEXTALARM, STREAK, TOTAL, M, T, W," +
                    " TH, F, SA, SU);");

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
