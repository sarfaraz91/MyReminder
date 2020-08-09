package com.fyp.reminder.Utils;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.fyp.reminder.GoogleApiHelper;

/**
 * Created by iamcs on 2017-04-29.
 */

public class AppController extends Application {

    //get the Tag name of the application
    public static final String TAG = AppController.class.getSimpleName();

    private RequestQueue mRequestQueue;

    //AppController Instance
    private static AppController mInstance;

    private GoogleApiHelper googleApiHelper;

    public static final String CHANNEL_1_ID = "channel_1";
    public static final String CHANNEL_2_ID = "channel_2";
    public static final String CHANNEL_3_ID = "channel_3";
    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        createNotificationChannel();

        googleApiHelper = new GoogleApiHelper(mInstance);
    }

    public static synchronized AppController getInstance() {
        return mInstance;
    }

    //get the instance of request Queue
    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(this);
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    private void createNotificationChannel() {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel Channel1 = new
                    NotificationChannel(CHANNEL_1_ID,"Channel 1", NotificationManager.IMPORTANCE_HIGH);
            Channel1.setDescription("this is channel 1");

            NotificationChannel Channel2 = new
                    NotificationChannel(CHANNEL_2_ID,"Channel 2", NotificationManager.IMPORTANCE_LOW);
            Channel2.setDescription("this is channel 2");

            NotificationChannel Channel3 = new
                    NotificationChannel(CHANNEL_3_ID,"Channel 3", NotificationManager.IMPORTANCE_HIGH);
            Channel3.setDescription("this is channel 2");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            //notificationManager.createNotificationChannel(Channel1);
            notificationManager.createNotificationChannel(Channel2);
            notificationManager.createNotificationChannel(Channel3);
        }

    }

    public GoogleApiHelper getGoogleApiHelperInstance() {
        return this.googleApiHelper;
    }
    public static GoogleApiHelper getGoogleApiHelper() {
        return getInstance().getGoogleApiHelperInstance();
    }


}