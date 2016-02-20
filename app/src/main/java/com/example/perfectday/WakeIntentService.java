package com.example.perfectday;


import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

public abstract class WakeIntentService extends IntentService {

    public static final String LOCK_NAME = "com.example.perfectday";
    private static PowerManager.WakeLock lock = null;

    public WakeIntentService(String name) {
        super(name);
    }

    public static void aquireStaticLock(Context ctxt) {

        getLock(ctxt).acquire();

    }

    synchronized private static PowerManager.WakeLock getLock(Context ctxt){

        if (lock==null){

            PowerManager pwrMngr = (PowerManager)ctxt.getSystemService(Context.POWER_SERVICE);
            lock = pwrMngr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, LOCK_NAME);
            lock.setReferenceCounted(true);
        }

        return lock;
    }

    protected void onHandleIntent(Intent intent){
        getLock(this).release();
    }
}
