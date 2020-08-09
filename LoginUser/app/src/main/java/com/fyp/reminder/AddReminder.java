package com.fyp.reminder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fyp.reminder.Adapater.ReminderAdapater;
import com.fyp.reminder.Utils.PreferencesHandler;
import com.fyp.reminder.model.Reminder;
import com.fyp.reminder.sql.DatabaseHelper;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class AddReminder extends AppCompatActivity {


    private AppCompatActivity activity = AddReminder.this;
    Context context = AddReminder.this;
    private RecyclerView mRecyclerView;
    private ArrayList<Reminder> listreminder;
    private ReminderAdapater adapter;
    private DatabaseHelper dbHelper;
    private RecyclerView.LayoutManager mLayoutManager;
    private String filter = "";
    Reminder reminder;
    Button logout;
    private FirebaseAuth mAuth;
    SignInButton google_btn;
    GoogleSignInClient mGoogleSignInClient;

    SessionManager sessionManager;
    ImageView btn_create;
    public static LinearLayout container_no_tasks;
    public static AlarmManager manager;
    ;
    public static NotificationManager notifManager;
    PreferencesHandler preferencesHandler;
    private Toolbar main_page_toolbar;
    private TextView log_out;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Reminders");
        setContentView(R.layout.activity_add_reminder);
        getSupportActionBar().hide();
        main_page_toolbar = findViewById(R.id.main_page_toolbar);
        main_page_toolbar.setTitle("Reminders");

        log_out = main_page_toolbar.findViewById(R.id.log_out);
        log_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGoogleSignInClient.signOut();
                startActivity(new Intent(AddReminder.this, UserLogin.class));
                finish();
                sessionManager.setLogin(false);
            }
        });

        //setSupportActionBar(main_page_toolbar);
        preferencesHandler = new PreferencesHandler(this);
        notifManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        sessionManager = new SessionManager(this);
        logout = findViewById(R.id.logout);
        btn_create = findViewById(R.id.btn_create);
        container_no_tasks = findViewById(R.id.container_no_tasks);
        btn_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AddReminder.this, CreateReminder.class));

//                Log.d("TAG", "onBindViewHolder1: " + reminder.getLocation());
            }
        });

        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(AddReminder.this, gso);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGoogleSignInClient.signOut();
                startActivity(new Intent(AddReminder.this, UserLogin.class));
                finish();
                sessionManager.setLogin(false);
            }
        });


    /*    initViews();
        initObjects();
*/

        //initialize the variables
        mRecyclerView = (RecyclerView) findViewById(R.id.rv);
        mRecyclerView.setHasFixedSize(true);
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        //populate recyclerview
        populaterecyclerView(filter);


    }

    private void populaterecyclerView(String filter) {
        dbHelper = new DatabaseHelper(this);
        if (dbHelper.peopleList(filter, preferencesHandler.getUserIdId()).size() > 0) {
            container_no_tasks.setVisibility(View.GONE);
        } else {
            container_no_tasks.setVisibility(View.VISIBLE);
        }
        adapter = new ReminderAdapater(dbHelper.peopleList(filter, preferencesHandler.getUserIdId()), this, mRecyclerView);
        mRecyclerView.setAdapter(adapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
     /*   MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home_menu, menu);

        MenuItem item = menu.findItem(R.id.filterSpinner);
        Spinner spinner = (Spinner) MenuItemCompat.getActionView(item);

        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.filterOptions, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String filter = parent.getSelectedItem().toString();
                populaterecyclerView(filter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                populaterecyclerView(filter);
            }
        });


        spinner.setAdapter(adapter);*/
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.btn_create:
                goToAddUserActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void goToAddUserActivity() {
        Intent intent = new Intent(AddReminder.this, CreateReminder.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        adapter.notifyDataSetChanged();
    }

    public void onBackPressed() {

        AlertDialog.Builder builder = new AlertDialog.Builder(AddReminder.this);
        builder.setTitle("Exit!");
        builder.setMessage("Do You Really Want To Exit? ");

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finishAffinity();
            }
        });


        builder.create().show();

    }
}






/*  *//**
 * This method is to initialize views
 * <p>
 * This method is to initialize objects to be used
 * <p>
 * This method is to fetch all user records from SQLite
 * <p>
 * This method is to initialize objects to be used
 * <p>
 * This method is to fetch all user records from SQLite
 * <p>
 * This method is to initialize objects to be used
 * <p>
 * This method is to fetch all user records from SQLite
 * <p>
 * This method is to initialize objects to be used
 * <p>
 * This method is to fetch all user records from SQLite
 *//*
    private void initViews() {
        rv = (RecyclerView) findViewById(R.id.rv);
    }

    *//**
 * This method is to initialize objects to be used
 *//*
    private void initObjects() {
        listreminder = new ArrayList<>();
        adapter = new ReminderAdapater(listreminder, this);


        rv.setLayoutManager(new LinearLayoutManager(AddReminder.this));
        rv.setHasFixedSize(true);
        rv.setAdapter(adapter);
        databaseHelper = new DatabaseHelper(activity);

        getDataFromSQLite();

    }





    *//**
 * This method is to fetch all user records from SQLite
 *//*
    private void getDataFromSQLite() {
        // AsyncTask is used that SQLite operation not blocks the UI Thread.
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                listreminder.clear();
                listreminder.addAll(databaseHelper. getAllReminder());

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                adapter.notifyDataSetChanged();
            }
        }.execute();
    }

*/




