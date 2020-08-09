package com.fyp.reminder;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.fyp.reminder.Utils.HelperClass;
import com.fyp.reminder.Utils.PreferencesHandler;
import com.google.android.gms.common.internal.Constants;

import java.util.ArrayList;
import java.util.Random;

public class AutoLocationService extends Service {


    LocationListener locationListener;
    LocationManager locationManager;
    Location lastLocation;
    private ArrayList<Location> locationArrayList = null;
    public static boolean isLocationAuto = false;
    private static final String CHANNEL_ID = "location_auto_id";
    public static boolean isDoneAuto = false;
    private final int dismissServiceRequestId = 100;
    private PowerManager.WakeLock wakeLock;
    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        super.onCreate();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyReminder:WakeLock");
        wakeLock.acquire();
        createNotificationChannel1();

        Intent intent = new Intent(this, RecieverStopService.class);
        PendingIntent contentIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        PendingIntent dismissIntent = NotificationActivityForAuto.getDismissIntent(dismissServiceRequestId, getApplicationContext());
        Notification notification = new NotificationCompat.Builder(this, "1")
                .setSmallIcon(R.drawable.location)
                .setContentTitle("MyReminder")
                .setContentText("Auto Location Reminder is running.")
                .addAction(R.drawable.location,"Stop",contentIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();

        startForeground(dismissServiceRequestId, notification);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                lastLocation = location;

                if (locationArrayList != null) {
                    if (locationArrayList.size() > 0) {
                        for (int i = 0; i < locationArrayList.size(); i++) {

                            Bundle bundle = locationArrayList.get(i).getExtras();
                            final String locationName = bundle.getString("LocationName");

                            if (measureDistance(lastLocation,locationArrayList.get(i))) {
                                Log.d("locationService","arrived");
                                //Toast.makeText(AutoLocationService.this, "locationName = "+locationName, Toast.LENGTH_SHORT).show();
                                Random rand = new Random();
                                final int request_code = rand.nextInt(1000);
                                createNotificationChannel2();
                                createNotification(locationName,request_code);

                               /* if(!(isDoneAuto && HelperClass.tempLocationName.equals(locationName))){


                                }*/

                            }

                        }
                    }
                }

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        };

        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);

    }

    private void createNotificationChannel1() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("1", "ll", importance);
            channel.setDescription("desc");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(locationListener);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
/*
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        locationArrayList = (ArrayList<Location>) intent.getExtras().get("nearbyLocationsList");

    }*/

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            String action = intent.getAction();

            switch (action) {
                case "startservice":
                    locationArrayList = (ArrayList<Location>) intent.getExtras().get("nearbyLocationsList");
                    break;
                case "stopservice":
                    Log.i("stopservice", "Received Stop Foreground Intent");
                    //your end servce code
                    stopForeground(true);
                    stopSelf();
                    PreferencesHandler preferencesHandler = new PreferencesHandler();
                    preferencesHandler.setServiceAuto(false);
                    //wakeLock.release();
                    break;
            }
        }

        //return super.onStartCommand(intent, flags, startId);
        return Service.START_STICKY;
    }

    private void createNotification(String locationName,int request_code) {
        createNotificationChannel2();

        Intent intent = new Intent(getApplicationContext(),AddReminder.class);
        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(),0,intent,0);

        Intent action = new Intent(getApplicationContext(),SnoozeReciever.class);
        action.putExtra("locationName", locationName);
        action.putExtra("request_code_location", request_code);

        PendingIntent actionIntent = PendingIntent.getBroadcast(getApplicationContext(),request_code,action,0);

        PendingIntent dismissIntent = NotificationActivity.getDismissIntent(request_code, getApplicationContext());


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,CHANNEL_ID);
        notificationBuilder
                .setSmallIcon(R.drawable.location)
                .setContentTitle("Reminder")
                .setContentText("You are near to "+locationName)
                .setContentIntent(contentIntent)
                .addAction(R.drawable.location,"Done",dismissIntent)
                //.addAction(R.drawable.location,"Snooze in 10 Minutes",actionIntent)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setOngoing(false);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        notificationManager.notify(request_code, notificationBuilder.build());
        HelperClass.tempLocationName = locationName;
        isDoneAuto = false;
        isLocationAuto = true;
    }

    private void createNotificationChannel2() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "location_auto", importance);
            channel.setDescription("this is location auto");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }




    private boolean measureDistance(Location current, Location reminder) {

        float distance = current.distanceTo(reminder);
        if (distance <= 150) {
            return true;
        } else {
            return false;
        }

    }
}

