package com.fyp.reminder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.fyp.reminder.Utils.PreferencesHandler;
import com.fyp.reminder.model.Reminder;
import com.fyp.reminder.sql.DatabaseHelper;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

public class Update_reminder extends AppCompatActivity {

    ImageView add_time;
    LinearLayout date_, time_, priority;
    RelativeLayout location, RepeatType;
    TextView set_date, set_time, set_repeat_type, set_priority, set_location;
    EditText reminder_title, details;
    private DatabaseHelper databaseHelper;
    private Reminder reminder;
    private int mYear, mMonth, mDay, mHour, mMinute;
    Button save;
    String back;
    ScrollView location_based_container;
    LinearLayout date_time_container;
    RelativeLayout container_layout;
    private String mRepeatType, mpriority;

    Calendar myCalendar;
    long reminder_id;

    boolean date_time_reminder = true;
    boolean location_based_reminder = false;
    private int old_request_code;
    private int new_request_code;
    TextView location_txt;
    private String sub_type = "";
    private long reminder_id_for_update;
    PreferencesHandler preferencesHandler;
    private ArrayList<Location> locationArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_reminder);
        getSupportActionBar().hide();
        preferencesHandler = new PreferencesHandler(this);

        location_txt = findViewById(R.id.location_txt);
        container_layout = findViewById(R.id.container_layout);
        date_time_container = findViewById(R.id.date_time_container);
        location_based_container = findViewById(R.id.location_based_container);
        set_time = findViewById(R.id.set_time);
        set_date = findViewById(R.id.set_date);
        date_ = findViewById(R.id.date);
        time_ = findViewById(R.id.time);
        reminder_title = findViewById(R.id.reminder_title);
        details = findViewById(R.id.details);
        RepeatType = findViewById(R.id.RepeatType);
        set_repeat_type = findViewById(R.id.set_repeat_type);
        save = findViewById(R.id.save);
        set_priority = findViewById(R.id.set_priority);
        priority = findViewById(R.id.priority);
        location = findViewById(R.id.location);
        set_location = findViewById(R.id.set_location);

        Intent i = getIntent();
        String location_name = i.getStringExtra("location");
        if (location_name != null) {
            date_time_container.setVisibility(View.GONE);
            location_based_container.setVisibility(View.VISIBLE);
            location_based_reminder = true;
            date_time_reminder = false;
        } else if (i.getSerializableExtra("reminder") != null) {
            Reminder myReminder = (Reminder) i.getSerializableExtra("reminder");

            if (!myReminder.getDate().equals("")) {
                date_time_container.setVisibility(View.VISIBLE);
                location_based_container.setVisibility(View.GONE);
                date_time_reminder = true;
                location_based_reminder = false;
            } else if (!myReminder.getLocation().equals("")) {
                date_time_container.setVisibility(View.GONE);
                location_based_container.setVisibility(View.VISIBLE);
                location_based_reminder = true;
                date_time_reminder = false;
            }
        }


        String title = i.getStringExtra("title");
        String desc = i.getStringExtra("desc");
        String date = i.getStringExtra("date");
        String time = i.getStringExtra("time");
        String repeat = i.getStringExtra("repeat");
        String prio = i.getStringExtra("prio");
        back = i.getStringExtra("back");

        if (i.hasExtra("nearbyLocationsList")) {
            locationArrayList = (ArrayList<Location>) i.getExtras().get("nearbyLocationsList");
        }


        set_location.setText(location_name);
        Log.d("TAG", "backintent: " + back);

        myCalendar = Calendar.getInstance();

        databaseHelper = new DatabaseHelper(Update_reminder.this);


        try {
            //get intent to get reminder id
            reminder_id = getIntent().getLongExtra("REMINDER_ID", 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        /***populate reminder data before update***/
        Log.d("TAG", "reminder_id_: " + getIntent().getLongExtra("REMINDER_ID", 1));
        reminder = databaseHelper.getReminder(reminder_id, preferencesHandler.getUserIdId());

        Reminder reminder_details = (Reminder) i.getSerializableExtra("reminder");


        reminder_title.setText(reminder.getTitle());
        details.setText(reminder.getDescr());
        set_time.setText(reminder.getTime());
        set_date.setText(reminder.getDate());
        set_location.setText(reminder.getLocation());


        if (i.hasExtra("manual")) {
            boolean manual = i.getBooleanExtra("manual", false);
            if (manual) {
                location_txt.setText("Manual Location");
            }
            reminder_id_for_update = getIntent().getLongExtra("reminder_id", 0);
            sub_type = "manual";
            new_request_code = i.getIntExtra("request_code_location", 0);
            old_request_code = preferencesHandler.getOldRequestCode();
        } else if (i.hasExtra("automatic")) {
            boolean automatic = i.getBooleanExtra("automatic", false);
            if (automatic) {
                location_txt.setText("Automatic Location");
            }
            reminder_id_for_update = getIntent().getLongExtra("reminder_id", 0);
            sub_type = "automatic";
            new_request_code = i.getIntExtra("request_code_location", 0);
            old_request_code = preferencesHandler.getOldRequestCode();
        } else {
            sub_type = reminder_details.getSub_type();
            old_request_code = reminder_details.getRequest_code();
            preferencesHandler.setOldRequestCode(old_request_code);
            if (sub_type.equals("manual")) {
                location_txt.setText("Manual Location");
            } else if (sub_type.equals("automatic")) {
                location_txt.setText("Automatic Location");
                if(!reminder_details.getLocationsList().equals("")){
                    Gson gson = new Gson();
                    Type type = new TypeToken<ArrayList<Location>>() {}.getType();
                    locationArrayList = gson.fromJson(reminder.getLocationsList(), type);
                }
            }
            reminder_id_for_update = getIntent().getLongExtra("REMINDER_ID", 1);

        }


        Log.d("TAG", "backintent: " + reminder.getLocation());


        date_.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Get Current Date
                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);


                DatePickerDialog datePickerDialog = new DatePickerDialog(Update_reminder.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {

                                set_date.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                                myCalendar.set(Calendar.MONTH, monthOfYear);
                                myCalendar.set(Calendar.YEAR, year);
                                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });


        time_.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Get Current Time
                final Calendar c = Calendar.getInstance();
                mHour = c.get(Calendar.HOUR_OF_DAY);
                mMinute = c.get(Calendar.MINUTE);

                // Launch Time Picker Dialog
                TimePickerDialog timePickerDialog = new TimePickerDialog(Update_reminder.this,
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                  int minute) {

                                String AM_PM;
                                if (hourOfDay < 12) {
                                    AM_PM = "AM";
                                } else {
                                    AM_PM = "PM";
                                }


                                set_time.setText(String.format("%02d:%02d", hourOfDay, minute) + " " + AM_PM);
                                myCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                myCalendar.set(Calendar.MINUTE, minute);
                                myCalendar.set(Calendar.SECOND, 0);
                            }
                        }, mHour, mMinute, false);
                timePickerDialog.show();
            }
        });


        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(Update_reminder.this, OptionScreenActivity.class);
                i.putExtra("title", reminder_title.getText().toString());
                i.putExtra("desc", details.getText().toString());
                i.putExtra("date", set_date.getText().toString());
                i.putExtra("time", set_time.getText().toString());
                i.putExtra("repeat", set_repeat_type.getText().toString());
                i.putExtra("prio", set_priority.getText().toString());
                i.putExtra("save", "2");
                i.putExtra("sub_type", sub_type);
                i.putExtra("reminder_id", reminder_id_for_update);


                Random rand = new Random();
                int request_code = rand.nextInt(1000);
                reminder.setRequest_code(request_code);
                i.putExtra("request_code_location", request_code);
                i.putExtra("request_code_old", old_request_code);

                startActivity(i);


//                startActivity(new Intent(Update_reminder.this, OptionScreenActivity.class));
            }
        });
        if (back.equals("1")) {
            reminder_title.setText(title);
            details.setText(desc);
            set_date.setText(date);
            set_time.setText(time);
            set_repeat_type.setText(repeat);
            set_priority.setText(prio);
            set_location.setText(location_name);
        } else if (back.equals("0")) {
            reminder_title.setText(reminder.getTitle());
            details.setText(reminder.getDescr());
            set_time.setText(reminder.getTime());
            set_date.setText(reminder.getDate());
            set_priority.setText(prio);
            set_location.setText(reminder.getLocation());
        }


        RepeatType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String[] items = new String[5];

                items[0] = "Minute";
                items[1] = "Hour";
                items[2] = "Day";
                items[3] = "Week";
                items[4] = "Month";

                // Create List Dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(Update_reminder.this);
                builder.setTitle("Select Type");
                builder.setItems(items, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int item) {

                        mRepeatType = items[item];
                        set_repeat_type.setText(mRepeatType);

                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });


        priority.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String[] items = new String[2];

                items[0] = "High";
                items[1] = "Low";


                // Create List Dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(Update_reminder.this);
                builder.setTitle("Select Priority");
                builder.setItems(items, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int item) {

                        mpriority = items[item];
                        set_priority.setText(mpriority);

                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });


        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


          /*      if(reminder_title.getText().toString().trim().length()<=0){
                    reminder_title.setError("Title Required");
                }else if(details.getText().toString().trim().length()<=0){
                    details.setError("Description Required");
                }else{*/
                if (validateFields()) {
                    postDataToSQLite();
                    startActivity(new Intent(Update_reminder.this, AddReminder.class));
                }

            }
        });


    }

    public boolean validateFields() {
        if (reminder_title.getText().toString().trim().length() <= 0) {
            Snackbar.make(container_layout, "Title Required", Snackbar.LENGTH_LONG)
                    .show();
            return false;
            //reminder_title.setError("Title Required");
        } else if (details.getText().toString().trim().length() <= 0) {
            Snackbar.make(container_layout, "Description Required", Snackbar.LENGTH_LONG)
                    .show();
            return false;
            //details.setError("Description Required");
        } else if (date_time_reminder) {
            if (set_date.getText().toString().trim().length() <= 0) {
                Snackbar.make(container_layout, "Date Required", Snackbar.LENGTH_LONG)
                        .show();
                return false;
                //set_date.setError("Date Required");
            } else if (set_time.getText().toString().trim().length() <= 0) {
                Snackbar.make(container_layout, "Time Required", Snackbar.LENGTH_LONG)
                        .show();
                return false;
                //set_time.setError("Time Required");
            } else if (set_priority.getText().toString().trim().length() <= 0) {
                Snackbar.make(container_layout, "Priority Required", Snackbar.LENGTH_LONG)
                        .show();
                return false;
                //set_priority.setError("Priority Required");
            }
        } else if (location_based_reminder) {
            if (set_location.getText().toString().trim().length() <= 0) {
                Snackbar.make(container_layout, "Location Required", Snackbar.LENGTH_LONG)
                        .show();
                return false;
            }
        }
        return true;
    }


    private void postDataToSQLite() {


        String title = reminder_title.getText().toString();
        String detail = details.getText().toString();
        String date = set_date.getText().toString().trim();
        String time = set_time.getText().toString().trim();
        String location = set_location.getText().toString().trim();
        String priority = set_priority.getText().toString().trim();

        Reminder updatedReminder = new Reminder();
        updatedReminder.setTitle(title);
        updatedReminder.setDescr(detail);
        updatedReminder.setDate(date);
        updatedReminder.setTime(time);
        updatedReminder.setLocation(location);
        updatedReminder.setPriority(priority);
        updatedReminder.setSub_type(sub_type);

        if (!location_based_reminder) {
            Random rand = new Random();
            new_request_code = rand.nextInt(1000);

            updatedReminder.setRequest_code(new_request_code);

            if(sub_type.equals("automatic")){
                Gson gson = new Gson();
                String arrayListLocations = gson.toJson(locationArrayList);
                updatedReminder.setLocationsList(arrayListLocations);
            }

        } else {
            updatedReminder.setRequest_code(new_request_code);
        }


        //Reminder updatedReminder = new Reminder(title, detail, date, time, location, priority, new_request_code);

        databaseHelper.updateReminderRecord(reminder_id_for_update, preferencesHandler.getUserIdId(), Update_reminder.this, updatedReminder);

        Toast.makeText(Update_reminder.this, "Reminder Updated Successfully", Toast.LENGTH_SHORT).show();

        if (!location_based_reminder) {
            setAlarm(myCalendar, updatedReminder);
        } else {
            if (sub_type.equals("automatic")) {
                try {
                    Intent intent = new Intent(this, AutoLocationService.class);
                    intent.setAction("startservice");
                    intent.putParcelableArrayListExtra("nearbyLocationsList", locationArrayList);
                    ContextCompat.startForegroundService(this, intent);
                    preferencesHandler.setServiceAuto(true);
                } catch (Exception e) {
                    Log.d("error", e.getMessage());
                }
            }
        }


    }

    public void setAlarm(Calendar c, Reminder reminder) {
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(this, ReminderReciever.class);

        Bundle bundle = new Bundle();
        bundle.putSerializable("reminder", reminder);
        i.putExtra("bundle", bundle);
        i.putExtra("requestcode", new_request_code);

        PendingIntent old_pi = PendingIntent.getBroadcast(this, old_request_code, i, 0);
        am.cancel(old_pi);

        PendingIntent new_pi = PendingIntent.getBroadcast(this, new_request_code, i, 0);


        if (c.before(Calendar.getInstance())) {
            c.add(Calendar.DATE, 1);
        }

        if (Build.VERSION.SDK_INT >= 23) {

            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), new_pi);

        } else if (Build.VERSION.SDK_INT >= 19) {

            am.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), new_pi);

        } else {

            am.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), new_pi);

        }

    }


    public void onBackPressed() {
        finish();
    }
}
