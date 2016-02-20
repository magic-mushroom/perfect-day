package com.example.perfectday;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;

public class LocalNotificationService extends WakeIntentService {

    String title;
    int id;

    public LocalNotificationService(String name) {
        super(name);
    }

    protected void onHandleIntent(Intent i){

        id = i.getIntExtra("ID", 0);
        title = i.getStringExtra("TITLE");

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


    }
}
