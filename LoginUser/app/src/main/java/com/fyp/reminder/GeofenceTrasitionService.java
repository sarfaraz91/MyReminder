package com.fyp.reminder;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;


public class GeofenceTrasitionService extends IntentService {

    private static final String TAG = GeofenceTrasitionService.class.getSimpleName();
    private PowerManager.WakeLock wakeLock;
    public static int GEOFENCE_NOTIFICATION_ID ;
    private static final String CHANNEL_ID = "geofence_id" ;

    public GeofenceTrasitionService() {
        super(TAG);
    }
    public static boolean isLocation = false;
    private int requestcode;

    @Override
    public void onCreate() {
        super.onCreate();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyReminder:WakeLock");
        wakeLock.acquire();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        // Handling errors

        requestcode = intent.getIntExtra("requestcode",0);
        GEOFENCE_NOTIFICATION_ID =requestcode;

        if ( geofencingEvent.hasError() ) {
            String errorMsg = getErrorString(geofencingEvent.getErrorCode() );
            Log.e( TAG, errorMsg );
            return;
        }

        int geoFenceTransition = geofencingEvent.getGeofenceTransition();
        // Check if the transition type is of interest
        if ( geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT ) {
            // Get the geofence that were triggered
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            String geofenceTransitionDetails = getGeofenceTrasitionDetails(geoFenceTransition, triggeringGeofences );

            // Send notification details as a String
            String locationName = intent.getStringExtra("name");

            sendNotification( geofenceTransitionDetails, locationName);
        }
    }


    private String getGeofenceTrasitionDetails(int geoFenceTransition, List<Geofence> triggeringGeofences) {
        // get the ID of each geofence triggered
        ArrayList<String> triggeringGeofencesList = new ArrayList<>();
        for ( Geofence geofence : triggeringGeofences ) {
            triggeringGeofencesList.add( geofence.getRequestId() );
        }

        String status = null;
        if ( geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER )
            status = "Entering ";
        else if ( geoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT )
            status = "Exiting ";
        return status + TextUtils.join( ", ", triggeringGeofencesList);
    }

    private void sendNotification( String msg,String locationName ) {
        Log.i(TAG, "sendNotification: " + msg );
//
//        // Intent to start the main Activity
//        Intent notificationIntent = GeoFencingActivity.makeNotificationIntent(
//                getApplicationContext(), msg
//        );
//
//        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
//        stackBuilder.addParentStack(CreateReminder.class);
//        stackBuilder.addNextIntent(notificationIntent);
//        PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);


        // Creating and sending Notification
        NotificationManager notificatioMng =
                (NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE );
        notificatioMng.notify(
                GEOFENCE_NOTIFICATION_ID,
                createNotification(msg,locationName));

    }


    private Notification createNotification(String msg,String locationName) {
        createNotificationChannel();

        Intent intent = new Intent(getApplicationContext(),AddReminder.class);
        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(),0,intent,0);

        Intent action = new Intent(getApplicationContext(),SnoozeReciever.class);
        action.putExtra("locationName", locationName);
        action.putExtra("request_code_location", requestcode);

        PendingIntent actionIntent = PendingIntent.getBroadcast(getApplicationContext(),0,action,PendingIntent.FLAG_UPDATE_CURRENT);

        PendingIntent dismissIntent = NotificationActivity.getDismissIntent(GEOFENCE_NOTIFICATION_ID, getApplicationContext());


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,CHANNEL_ID);
        notificationBuilder
                .setSmallIcon(R.drawable.location)
                .setContentTitle("Reminder")
                .setContentText("You are near to "+locationName)
                .setContentIntent(contentIntent)
                .addAction(R.drawable.location,"Done",dismissIntent)
                .addAction(R.drawable.location,"Snooze in 10 Minutes",actionIntent)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setOngoing(true);

        isLocation = true;
        return notificationBuilder.build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "geofence", importance);
            channel.setDescription("this is geofencing");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    private static String getErrorString(int errorCode) {
        switch (errorCode) {
            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                return "GeoFence not available";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                return "Too many GeoFences";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                return "Too many pending intents";
            default:
                return "Unknown error.";
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        wakeLock.release();
    }
}

