package com.fyp.reminder.Utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesHandler {

    private static SharedPreferences pref;
    private static SharedPreferences.Editor editor;
    private static final String USER_ID = "user_id";
    private static final String OLD_REQUEST_CODE = "old_req_code";
    private static final String AUTO_SERVICE_RUNNING = "auto_service";

    public PreferencesHandler() {

    }

    public PreferencesHandler(Context context) {
        pref = context.getSharedPreferences("REMINDER", Context.MODE_PRIVATE);
        editor = pref.edit();
    }


    public int getUserIdId() {
        return pref.getInt(USER_ID, 0);
    }

    public void setUserIdId(int operid) {
        editor.putInt(USER_ID, operid);
        editor.apply();
        editor.commit();
    }

    public int getOldRequestCode() {
        return pref.getInt(OLD_REQUEST_CODE, 0);
    }

    public void setOldRequestCode(int oldRequestCode) {
        editor.putInt(OLD_REQUEST_CODE, oldRequestCode);
        editor.apply();
        editor.commit();
    }

    public boolean getServiceAuto() {
        return pref.getBoolean(AUTO_SERVICE_RUNNING, false);
    }

    public void setServiceAuto(boolean isTrue) {
        editor.putBoolean(AUTO_SERVICE_RUNNING, isTrue);
        editor.apply();
        editor.commit();
    }



}
