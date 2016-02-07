package com.example.perfectday;

import java.util.Calendar;

/**
 * Created by Raks on 12/2/2015.
 */
public class HabitHome {

    private Calendar alarmTime, doWStatus;
    private String name, category;
    private int id, schedule;

    public HabitHome (int id, String name, String category, int schedule, Calendar alarmTime,
                      Calendar doWStatus) {

        this.name = name;
        this.category = category;
        this.alarmTime = alarmTime;
        this.id = id;
        this.schedule = schedule;
        this.doWStatus = doWStatus;

    }

    public String getHomeName() {
        return name;
    }

    public String getHomeCategory() {
        return category;
    }

    public Calendar getHomeAlarmTime() {
        return alarmTime;
    }

    public int getHomeSchedule() {
        return schedule;
    }

    public int getHomeId() {
        return id;
    }

    public Calendar getHomeDoWStatus() {
        return doWStatus;
    }

}
