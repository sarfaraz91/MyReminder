package com.fyp.reminder;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.fyp.reminder.Utils.AppController;
import com.fyp.reminder.model.Reminder;

public class SnoozeReciever extends BroadcastReceiver {
    public static NotificationManagerCompat notificationManager;
    public static int SNOOZE_NOTIFICATION_ID;
    public static boolean isSnooze = false;
    Reminder reminder;
    String locationName = "";
    private int request_code = 0;
    @Override
    public void onReceive(final Context context, Intent intent) {
        notificationManager = NotificationManagerCompat.from(context);

        if (GeofenceTrasitionService.isLocation) {
            notificationManager.cancel(GeofenceTrasitionService.GEOFENCE_NOTIFICATION_ID);
            GeofenceTrasitionService.isLocation = false;
            locationName = intent.getStringExtra("locationName");
        } else if (GeofenceServiceAutoReminder.isLocationAuto) {
            notificationManager.cancel(GeofenceServiceAutoReminder.GEOFENCE_NOTIFICATION_ID);
            GeofenceServiceAutoReminder.isLocationAuto = false;
            locationName = intent.getStringExtra("locationName");
            request_code = intent.getIntExtra("requestcode",0);
        } else if (ReminderReciever.isOneTime) {
            notificationManager.cancel(ReminderReciever.REMINDER_NOTIFICATION_ID);
            ReminderReciever.isOneTime = false;
        } else if (isSnooze) {
            notificationManager.cancel(SNOOZE_NOTIFICATION_ID);
            isSnooze = false;
        }

        Bundle bundle = intent.getBundleExtra("bundle");
        if (bundle != null) {
            reminder = (Reminder) bundle.getSerializable("reminder");
            SNOOZE_NOTIFICATION_ID = reminder.getRequest_code();
        } else {
            locationName = intent.getStringExtra("locationName");
            request_code = intent.getIntExtra("requestcode",0);
        }

        Intent serviceIntent = new Intent(context, BackgroundServiceForSnooze.class);
        serviceIntent.setAction("startservice");
        Bundle bundleService = new Bundle();
        bundleService.putSerializable("reminder", reminder);
        bundleService.putInt("request_code",request_code);
        bundleService.putString("locationName",locationName);
        serviceIntent.putExtra("bundle", bundle);

        ContextCompat.startForegroundService(context, serviceIntent);


        /*new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (!locationName.equals("")) {
                    sendNotif(context, locationName);
                } else {
                    sendNotif(context, reminder);
                }
            }
        }, 10*60*1000);*/

    }

/*10*60000*/
    public void sendNotif(Context context, String locationName) {

        Intent intent = new Intent(context, AddReminder.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, 0);

        PendingIntent dismissIntent = NotificationActivity.getDismissIntent(SNOOZE_NOTIFICATION_ID, context);

        Intent action = new Intent(context, SnoozeReciever.class);
        action.putExtra("locationName", locationName);
        action.putExtra("request_code_location", request_code);

        PendingIntent actionIntent = PendingIntent.getBroadcast(context, request_code, action, 0);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, AppController.CHANNEL_3_ID)
                .setSmallIcon(R.drawable.location)
                .setContentTitle("Reminder")
                .setContentText("You are near to "+locationName)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(contentIntent)
                .addAction(R.drawable.location, "Done", dismissIntent)
                .addAction(R.drawable.location, "Remind in 10 Minutes", actionIntent)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setOngoing(true);

        notificationManager.notify(SNOOZE_NOTIFICATION_ID, builder.build());
        isSnooze = true;

    }


    public void sendNotif(Context context, Reminder reminder) {

        Intent reminderIntent = new Intent(context, ReminderReciever.class);

        PendingIntent pendingIntentReminder = PendingIntent.getBroadcast(
                context, reminder.getRequest_code(), reminderIntent, 0);

        final boolean alarmUp = (pendingIntentReminder != null);

        if(alarmUp){
            Intent intent = new Intent(context, AddReminder.class);
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, 0);

            PendingIntent dismissIntent = NotificationActivity.getDismissIntent(SNOOZE_NOTIFICATION_ID, context);

            Intent action = new Intent(context, SnoozeReciever.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("reminder", reminder);
            action.putExtra("bundle", bundle);
            PendingIntent actionIntent = PendingIntent.getBroadcast(context, reminder.getRequest_code(), action, 0);


            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, AppController.CHANNEL_3_ID)
                    .setSmallIcon(R.drawable.location)
                    .setContentTitle(reminder.getTitle())
                    .setContentText(reminder.getDescr())
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(contentIntent)
                    .addAction(R.drawable.location, "Done", dismissIntent)
                    .addAction(R.drawable.location, "Remind in 10 Minutes", actionIntent)
                    .setAutoCancel(true)
                    .setOnlyAlertOnce(true)
                    .setOngoing(true);

            notificationManager.notify(SNOOZE_NOTIFICATION_ID, builder.build());
            isSnooze = true;
        }



    }
}