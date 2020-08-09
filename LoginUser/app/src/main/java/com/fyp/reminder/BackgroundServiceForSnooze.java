package com.fyp.reminder;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.fyp.reminder.Utils.AppController;
import com.fyp.reminder.model.Reminder;

import java.util.Timer;
import java.util.TimerTask;

public class BackgroundServiceForSnooze extends Service {

    public Reminder reminder;
    String locationName = "";
    private int request_code = 0;


    public Context context = this;
    public Handler handler = null;
    public static Runnable runnable = null;
    private final int dismissServiceRequestId = 200;
    int SNOOZE_NOTIFICATION_ID = 0;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    private PowerManager.WakeLock wakeLock;
    @Override
    public void onCreate() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyReminder:WakeLock");
        wakeLock.acquire();
        createNotificationChannel1();

        Intent intent = new Intent(this, RecieverStopServiceSnooze.class);
        PendingIntent contentIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        PendingIntent dismissIntent = NotificationActivityForAuto.getDismissIntent(dismissServiceRequestId, getApplicationContext());
        Notification notification = new NotificationCompat.Builder(this, "88")
                .setSmallIcon(R.drawable.location)
                .setContentTitle("MyReminder")
                .setContentText("My Reminder is running.")
                .addAction(R.drawable.location,"Stop",contentIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();

        startForeground(dismissServiceRequestId, notification);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (!locationName.equals("")) {
                    sendNotif(context, locationName);
                } else {
                    sendNotif(context, reminder);
                }
            }
        }, 10*60*1000);


      /*  handler = new Handler();
        runnable = new Runnable() {
            public void run() {
                if (!locationName.equals("")) {
                    sendNotif(context, locationName);
                } else {
                    sendNotif(context, reminder);
                }
                handler.postDelayed(runnable, 5000);
            }
        };

        handler.postDelayed(runnable, 5000);*/
    }
/*10*60*1000*/
    @Override
    public void onDestroy() {
        /* IF YOU WANT THIS SERVICE KILLED WITH THE APP THEN UNCOMMENT THE FOLLOWING LINE */
        //handler.removeCallbacks(runnable);
        //Toast.makeText(this, "Service stopped", Toast.LENGTH_LONG).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            String action = intent.getAction();
            Bundle bundle = intent.getBundleExtra("bundle");

            switch (action) {
                case "startservice":
                    reminder = (Reminder) bundle.getSerializable("reminder");
                    request_code = reminder.getRequest_code();
                    if(intent.getStringExtra("locationName") != null){
                        locationName = intent.getStringExtra("locationName");
                    }
                    break;
                case "stopservice":
                    Log.i("stopservice", "Received Stop Foreground Intent");
                    //your end servce code
                    stopForeground(true);
                    stopSelf();
                    //wakeLock.release();
                    break;
            }
        }

        //return super.onStartCommand(intent, flags, startId);
        return Service.START_STICKY;
    }
    private void createNotificationChannel1() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("88", "ll", importance);
            channel.setDescription("desc");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

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

        SnoozeReciever.notificationManager.notify(SNOOZE_NOTIFICATION_ID, builder.build());
        SnoozeReciever.isSnooze = true;

    }


    public void sendNotif(Context context, Reminder reminder) {


        SNOOZE_NOTIFICATION_ID = SnoozeReciever.SNOOZE_NOTIFICATION_ID;

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

            SnoozeReciever.notificationManager.notify(SNOOZE_NOTIFICATION_ID, builder.build());
            SnoozeReciever.isSnooze = true;
        }



    }
}
