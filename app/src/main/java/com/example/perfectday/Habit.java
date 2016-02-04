package com.example.perfectday;

import java.security.cert.Certificate;
import java.sql.Time;
import java.util.Calendar;

public class Habit {

    private String name, category;
    private int schedule, currentStreak, totalDone;
    private Calendar nextAlarm, alarmTime;
    private String[] days;
    private int id;

    // Constructor
    public Habit(int id, String name, String category, int schedule, Calendar alarmTime,
                 Calendar nextAlarm, int currentStreak, int totalDone, String[] days) {

        this.id = id;
        this.name = name;
        this.category = category;
        this.schedule = schedule;
        this.alarmTime = alarmTime;
        this.nextAlarm = nextAlarm;
        this.currentStreak = currentStreak;
        this.totalDone = totalDone;
        this.days = days;

    }

    // Getter and Setter for all items
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getSchedule() {
        return schedule;
    }

    public void setSchedule(int schedule) {
        this.schedule = schedule;
    }

    public Calendar getAlarmTime() {
        return alarmTime;
    }

    public void setAlarmTime(Calendar alarmTime) {
        this.alarmTime = alarmTime;
    }

    public Calendar getNextAlarm() {
        return nextAlarm;
    }

    public void setNextAlarm(Calendar nextAlarm) {
        this.nextAlarm = nextAlarm;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
    }

    public int getTotalDone() {
        return totalDone;
    }

    public void setTotalDone(int totalDone) {
        this.totalDone = totalDone;
    }

    public String[] getDays() {
        return days;
    }

    public void setDays(String[] days) {
        this.days = days;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


}
