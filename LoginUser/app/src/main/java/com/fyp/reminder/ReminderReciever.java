package com.fyp.reminder;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.fyp.reminder.Utils.AppController;
import com.fyp.reminder.model.Reminder;

public class ReminderReciever extends BroadcastReceiver {

    NotificationManagerCompat notificationManager;
    public static int REMINDER_NOTIFICATION_ID ;
    private final String CHANNEL_WITH_SOUND = "with_sound";
    private final String CHANNEL_WITHOUT_SOUND = "without_sound";
    private int requestcode;
    public static boolean isOneTime = false;
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getBundleExtra("bundle");

        if (bundle != null) {
            Reminder reminder = (Reminder)bundle.getSerializable("reminder");
            requestcode = reminder.getRequest_code();
            REMINDER_NOTIFICATION_ID = requestcode;
            sendNotif(context,reminder);
        }
    }

    public void sendNotif(Context context,Reminder reminder) {
        notificationManager = NotificationManagerCompat.from(context);
        int priority = 0;

        if(reminder.getPriority().equals("High")){
            priority = 1;
        }else if(reminder.getPriority().equals("Low")){
            priority = -1;
        }

        //createNotificationChannel(context,priority);

        Intent intent = new Intent(context,AddReminder.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context,REMINDER_NOTIFICATION_ID,intent,0);

        Intent action = new Intent(context,SnoozeReciever.class);

        Bundle bundle = new Bundle();
        bundle.putSerializable("reminder", reminder);
        action.putExtra("bundle", bundle);

        PendingIntent actionIntent = PendingIntent.getBroadcast(context,REMINDER_NOTIFICATION_ID,action,0);

        PendingIntent dismissIntent = NotificationActivity.getDismissIntent(REMINDER_NOTIFICATION_ID, context);


        if(priority == 1){
            createNotificationChannelWithSound(context);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_WITH_SOUND)
                    .setDefaults(Notification.FLAG_NO_CLEAR)
                    .setSmallIcon(R.drawable.location)
                    .setContentTitle(reminder.getTitle())
                    .setContentText(reminder.getDescr())
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(contentIntent)
                    .addAction(R.drawable.location,"Done",dismissIntent)
                    .addAction(R.drawable.location,"Remind in 10 Minutes",actionIntent)
                    .setOngoing(true)
                    ;
            notificationManager.notify(REMINDER_NOTIFICATION_ID, builder.build());
        }else{
            createNotificationChannelWithoutSound(context);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_WITHOUT_SOUND)
                    .setDefaults(Notification.FLAG_NO_CLEAR)
                    .setSmallIcon(R.drawable.location)
                    .setContentTitle(reminder.getTitle())
                    .setContentText(reminder.getDescr())
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setContentIntent(contentIntent)
                    .addAction(R.drawable.location,"Done",dismissIntent)
                    .addAction(R.drawable.location,"Remind in 10 Minutes",actionIntent)
                    .setOngoing(true)
                    ;
            notificationManager.notify(REMINDER_NOTIFICATION_ID, builder.build());
        }

        isOneTime = true;

    }

    private void createNotificationChannelWithSound(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_WITH_SOUND, "with sound", importance);
            channel.setDescription("desc");
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void createNotificationChannelWithoutSound(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_WITHOUT_SOUND, "without sound", importance);
            channel.setDescription("desc");
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


}
