package com.fyp.reminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RecieverStopServiceSnooze extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Start Activity
        Intent activityIntent = new Intent(context, MainActivity.class);
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(activityIntent);
        // Start Services
        Intent intent2 = new Intent(context, BackgroundServiceForSnooze.class);
        intent2.setAction("stopservice");
        context.startService(intent2);
    }
}

