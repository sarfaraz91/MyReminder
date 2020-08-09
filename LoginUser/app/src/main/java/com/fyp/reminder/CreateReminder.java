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
import android.content.IntentFilter;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.fyp.reminder.Utils.PreferencesHandler;
import com.fyp.reminder.model.Reminder;
import com.fyp.reminder.sql.DatabaseHelper;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class CreateReminder extends AppCompatActivity {

    ImageView add_time;
    RelativeLayout date_, time_, RepeatType, priority, location;
    TextView set_date, set_time, set_repeat_type, set_priority, set_location;
    EditText reminder_title, details;
    private DatabaseHelper databaseHelper;
    private Reminder reminder;
    private int mYear, mMonth, mDay, mHour, mMinute;
    Button save;
    private Uri mCurrentReminderUri;
    private String mRepeatType, mpriority;

    Calendar myCalendar;
    Spinner spinner_options;
    Spinner spinner_location_options;
    ScrollView location_based_container;
    LinearLayout date_time_container;
    LinearLayout container_date;
    LinearLayout container_time;
    LinearLayout container_priority;
    RelativeLayout container_layout;
    LinearLayout container_location_options;

    boolean date_time_reminder = true;
    boolean location_based_reminder = false;
    String type = "date_time_reminder";
    String sub_type = "manual";
    RunGeoFenceBroadCaster runGeoFenceBroadCaster;
    PreferencesHandler preferencesHandler;
    private int request_code_location = 0;
    private ArrayList<Location> locationArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_reminder);
        setTitle("Add Task");
        getSupportActionBar().hide();
        preferencesHandler = new PreferencesHandler(this);
        container_location_options = findViewById(R.id.container_location_options);
        container_layout = findViewById(R.id.container_layout);
        container_date = findViewById(R.id.container_date);
        container_time = findViewById(R.id.container_time);
        container_priority = findViewById(R.id.container_priority);
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
        spinner_options = findViewById(R.id.spinner_options);
        spinner_location_options = findViewById(R.id.spinner_location_options);

        myCalendar = Calendar.getInstance();

        List<String> reminderOptionslist = new ArrayList<String>();
        reminderOptionslist.add("One Time Reminder");
        reminderOptionslist.add("Location Based Reminder");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                R.layout.spinner_item, reminderOptionslist);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_options.setAdapter(dataAdapter);

        spinner_options.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                switch (position) {
                    case 0:
                        type = "date_time_reminder";
                        date_time_container.setVisibility(View.VISIBLE);
                        location_based_container.setVisibility(View.GONE);
                        date_time_reminder = true;
                        location_based_reminder = false;
                        set_location.setText("");
                        container_location_options.setVisibility(View.GONE);
                        break;
                    case 1:
                        type = "location_based_reminder";
                        location_based_container.setVisibility(View.VISIBLE);
                        date_time_container.setVisibility(View.GONE);
                        location_based_reminder = true;
                        date_time_reminder = false;
                        set_date.setText("");
                        set_time.setText("");
                        set_priority.setText("");
                        container_location_options.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

                // sometimes you need nothing here
            }
        });

        List<String> locationOptionslist = new ArrayList<String>();
        locationOptionslist.add("Manual Location");
        locationOptionslist.add("Automatic Location");

        ArrayAdapter<String> locationOptionsAdapter = new ArrayAdapter<String>(this,
                R.layout.spinner_item, locationOptionslist);
        locationOptionsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_location_options.setAdapter(locationOptionsAdapter);

        spinner_location_options.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = getIntent();
                switch (position) {
                    case 0:
                        sub_type = "manual";
                        if (intent.hasExtra("manual")) {
                            String location_name = intent.getStringExtra("location");
                            set_location.setText(location_name);
                        } else
                            set_location.setText("");
                        break;
                    case 1:
                        if (intent.hasExtra("automatic")) {
                            String location_name = intent.getStringExtra("location");
                            set_location.setText(location_name);
                        } else
                            set_location.setText("");
                        sub_type = "automatic";
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


//notification wala kam start
 /*       Intent intent = getIntent();
        mCurrentReminderUri = intent.getData();


        myCalendar.set(Calendar.MONTH, --mMonth);
        myCalendar.set(Calendar.YEAR, mYear);
        myCalendar.set(Calendar.DAY_OF_MONTH, mDay);
        myCalendar.set(Calendar.HOUR_OF_DAY, mHour);
        myCalendar.set(Calendar.MINUTE, mMinute);
        myCalendar.set(Calendar.SECOND, 0);

        long selectedTimestamp =  myCalendar.getTimeInMillis();


        new AlarmScheduler().setAlarm(getApplicationContext(), selectedTimestamp, mCurrentReminderUri);
*/

        //notification wala kam end
        databaseHelper = new DatabaseHelper(CreateReminder.this);
        reminder = new Reminder();

        Intent i = getIntent();
        String location_name = i.getStringExtra("location");
        String title = i.getStringExtra("title");
        String desc = i.getStringExtra("desc");
        String date = i.getStringExtra("date");
        String time = i.getStringExtra("time");
        String repeat = i.getStringExtra("repeat");
        String prio = i.getStringExtra("prio");
        request_code_location = i.getIntExtra("request_code_location",0);

        if(i.hasExtra("nearbyLocationsList")){
            locationArrayList = (ArrayList<Location>) i.getExtras().get("nearbyLocationsList");
        }

        if (location_name != null) {
            spinner_options.setSelection(1);
        }

        if (i.hasExtra("automatic")) {
            boolean auto = i.getBooleanExtra("automatic", false);
            if (auto) {
                spinner_location_options.setSelection(1);
            } else {
                spinner_location_options.setSelection(0);
            }
        }


        set_location.setText(location_name);

        container_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Get Current Date
                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);


                DatePickerDialog datePickerDialog = new DatePickerDialog(CreateReminder.this,
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


        container_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Get Current Time
                final Calendar c = Calendar.getInstance();
                mHour = c.get(Calendar.HOUR_OF_DAY);
                mMinute = c.get(Calendar.MINUTE);

                // Launch Time Picker Dialog
                TimePickerDialog timePickerDialog = new TimePickerDialog(CreateReminder.this,
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                  int minute) {

                                String AM_PM ;
                                if(hourOfDay < 12) {
                                    AM_PM = "AM";
                                } else {
                                    AM_PM = "PM";
                                }

                                set_time.setText(String.format("%02d:%02d", hourOfDay, minute)+" "+AM_PM);
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

                Intent i = new Intent(CreateReminder.this, OptionScreenActivity.class);
                i.putExtra("title", reminder_title.getText().toString());
                i.putExtra("desc", details.getText().toString());
                i.putExtra("date", set_date.getText().toString());
                i.putExtra("time", set_time.getText().toString());
                i.putExtra("repeat", set_repeat_type.getText().toString());
                i.putExtra("prio", set_priority.getText().toString());
                i.putExtra("save", "1");
                i.putExtra("sub_type", sub_type);

                Random rand = new Random();
                int request_code = rand.nextInt(1000);
                reminder.setRequest_code(request_code);
                i.putExtra("request_code_location", request_code);

                startActivity(i);

                /* startActivity(new Intent(CreateReminder.this, OptionScreenActivity.class));*/
            }
        });


        reminder_title.setText(title);
        details.setText(desc);
        set_date.setText(date);
        set_time.setText(time);
        set_repeat_type.setText(repeat);
        set_priority.setText(prio);
        set_location.setText(location_name);


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
                AlertDialog.Builder builder = new AlertDialog.Builder(CreateReminder.this);
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


        container_priority.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String[] items = new String[2];

                items[0] = "High";
                items[1] = "Low";


                // Create List Dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(CreateReminder.this);
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
                if (validateFields()) {
                    postDataToSQLite();

//                    if(sub_type.equals("automatic")){
//                        runGeoFenceBroadCaster = new RunGeoFenceBroadCaster();
//                        IntentFilter intentFilter = new IntentFilter();
//                        intentFilter.addAction("auto_geofence_broadcaster");
//                        registerReceiver(runGeoFenceBroadCaster,intentFilter);
//                    }

                    startActivity(new Intent(CreateReminder.this, AddReminder.class));
                }
            }
        });

    }


    private void postDataToSQLite() {

        reminder.setTitle(reminder_title.getText().toString());
        reminder.setDescr(details.getText().toString());
        reminder.setDate(set_date.getText().toString().trim());
        reminder.setTime(set_time.getText().toString().trim());
        reminder.setLocation(set_location.getText().toString().trim());
        reminder.setLocation(set_location.getText().toString().trim());
        reminder.setType(type);
        reminder.setPriority(set_priority.getText().toString());
        reminder.setSub_type(sub_type);
        reminder.setUser_id(preferencesHandler.getUserIdId());

        if(sub_type.equals("automatic")){

            Gson gson = new Gson();

            String arrayListLocations = gson.toJson(locationArrayList);

            reminder.setLocationsList(arrayListLocations);

        }

        if(!location_based_reminder){
            Random rand = new Random();
            int request_code = rand.nextInt(1000);

            reminder.setRequest_code(request_code);
        }else{
            reminder.setRequest_code(request_code_location);
        }


        databaseHelper.saveNewReminder(reminder);
        Snackbar.make(container_layout, "Reminder Added Successfully", Snackbar.LENGTH_LONG)
                .show();

        //Toast.makeText(CreateReminder.this, "Reminder Added Successfully", Toast.LENGTH_SHORT).show();
        Log.d("TAG", "LOACTION? :" + reminder.getLocation());

        if (!location_based_reminder) {
            setAlarm(myCalendar, reminder);
        }else{
            if(sub_type.equals("automatic")){
                try{
                    Intent intent = new Intent(this, AutoLocationService.class);
                    intent.setAction("startservice");
                    intent.putParcelableArrayListExtra("nearbyLocationsList", locationArrayList);
                    ContextCompat.startForegroundService(this, intent);
                    preferencesHandler.setServiceAuto(true);
                }catch (Exception e){
                    Log.d("error",e.getMessage());
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

        PendingIntent pi = PendingIntent.getBroadcast(this, reminder.getRequest_code(), i, 0);

        if (c.before(Calendar.getInstance())) {
            c.add(Calendar.DATE, 1);
        }

        if (Build.VERSION.SDK_INT >= 23) {

            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pi);

        } else if (Build.VERSION.SDK_INT >= 19) {

            am.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pi);

        } else {

            am.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pi);

        }

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


    @Override
    protected void onDestroy() {
        super.onDestroy();
       // unregisterReceiver(runGeoFenceBroadCaster);
    }

    public void onBackPressed() {
        finish();
    }

}


/*
    private void selecttime(final Activity activity) {

        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.activity_set_details);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Button btn_ok = dialog.findViewById(R.id.btn_ok);

        CardView alert = dialog.findViewById(R.id.alert);




        TextView txt_error_description = dialog.findViewById(R.id.txt_error_description);
        TextView txt_dialog_title = dialog.findViewById(R.id.txt_dialog_title);

        txt_error_description.setText(getString(R.string.internet_message));


        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}*/


   /* <?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:app="http://schemas.android.com/apk/res-auto"
        xmlns:tools="http://schemas.android.com/tools"
        android:layout_width="300dp"
        android:layout_height="300dp"
        android:layout_gravity="center"
        android:orientation="vertical"
        android:padding="5dp">

<RelativeLayout
        android:id="@+id/relativeLayout"
                android:layout_width="0dp"
                android:layout_height="0dp"

                android:layout_marginStart="5dp"
                android:layout_marginTop="29dp"
                android:layout_marginEnd="5dp"
                android:layout_marginBottom="29dp"
                android:background="@drawable/border_round_full"
                app:layout_constraintBottom_toBottomOf="parent"
                app:layout_constraintEnd_toEndOf="parent"
                app:layout_constraintStart_toStartOf="parent"
                app:layout_constraintTop_toTopOf="parent">

<RelativeLayout
            android:id="@+id/linearLayout3"
                    android:layout_width="match_parent"
                    android:layout_height="274dp"
                    android:layout_marginStart="8dp"
                    android:layout_marginTop="-2dp"
                    android:layout_marginEnd="8dp"
                    android:orientation="vertical"
                    app:layout_constraintEnd_toEndOf="parent"
                    app:layout_constraintHorizontal_bias="0.2"
                    app:layout_constraintStart_toStartOf="parent">


<TextView
                android:id="@+id/p1"
                        android:layout_width="265dp"
                        android:layout_height="79dp"
                        android:layout_alignParentTop="true"
                        android:layout_gravity="center_horizontal"
                        android:layout_marginLeft="15dp"
                        android:layout_marginTop="56dp"
                        android:text="you  "
                        android:textColor="#333"
                        android:textSize="15dp"
                        android:textStyle="bold" />

<TextView
                android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_below="@+id/p1"
                        android:layout_gravity="center_horizontal"
                        android:layout_marginLeft="15dp"
                        android:layout_marginTop="20dp"
                        android:fontFamily="@font/raleway"
                        android:text="@string/precaution2"
                        android:textColor="#333"
                        android:textSize="15dp"
                        android:textStyle="bold" />

<ImageView
                android:id="@+id/imageView7"
                        android:layout_width="12dp"
                        android:layout_height="13dp"
                        android:layout_alignParentStart="true"
                        android:layout_alignParentTop="true"
                        android:layout_marginStart="1dp"
                        android:layout_marginTop="61dp"
                        android:background="@drawable/scroll_bar_btn" />

<ImageView
                android:id="@+id/imageView8"
                        android:layout_width="12dp"
                        android:layout_height="13dp"
                        android:layout_below="@+id/imageView7"
                        android:layout_alignParentStart="true"
                        android:layout_marginStart="0dp"
                        android:layout_marginTop="86dp"
                        android:background="@drawable/scroll_bar_btn" />

</RelativeLayout>


</RelativeLayout>


<Button
        android:id="@+id/btnfollow"
                android:layout_width="wrap_content"
                android:layout_height="31dp"
                android:layout_gravity="center_horizontal"
                android:layout_marginStart="8dp"
                android:layout_marginEnd="8dp"
                android:layout_marginBottom="16dp"
                android:background="@drawable/okbtn"
                android:fontFamily="@font/raleway_bold"
                android:text="Ok"
                android:textColor="#333"
                app:layout_constraintBottom_toBottomOf="parent"
                app:layout_constraintEnd_toEndOf="parent"
                app:layout_constraintStart_toStartOf="parent" />


<ImageView
        android:id="@+id/imageView2"
                android:layout_width="56dp"
                android:layout_height="51dp"
                android:layout_alignParentStart="true"
                android:layout_marginStart="8dp"
                android:layout_marginTop="4dp"
                android:layout_marginEnd="8dp"
                android:background="@drawable/checked1"
                app:layout_constraintEnd_toEndOf="parent"
                app:layout_constraintStart_toStartOf="parent"
                app:layout_constraintTop_toTopOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>

*/