package com.example.perfectday;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class LocalNotificationReceiver extends BroadcastReceiver{


    @Override
    public void onReceive(Context context, Intent intent) {

        WakeIntentService.aquireStaticLock(context);
        Intent i = new Intent(context, LocalNotificationService.class);

        i.putExtra("ID", intent.getIntExtra("ID", 0));
        i.putExtra("TITLE", intent.getStringExtra("TITLE"));
        i.putExtra("SCHEDULE", intent.getIntExtra("SCHEDULE", 0));
        i.putExtra("ALARMTIME", intent.getSerializableExtra("ALARMTIME"));
        i.putExtra("NEXTALARM", intent.getSerializableExtra("NEXTALARM"));

        context.startService(i);

        Log.d("Notification", "Reached end of Receiver");

    }
}
