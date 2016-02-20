package com.example.perfectday;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class LocalNotificationReceiver extends BroadcastReceiver{


    @Override
    public void onReceive(Context context, Intent intent) {

        WakeIntentService.aquireStaticLock(context);
        Intent i = new Intent(context, LocalNotificationService.class);
        i.putExtra("ID", intent.getIntExtra("ID", 0));
        i.putExtra("TITLE", intent.getStringExtra("TITLE"));
        context.startService(i);

    }
}
