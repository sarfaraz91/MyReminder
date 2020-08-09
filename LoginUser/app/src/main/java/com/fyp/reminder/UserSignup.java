package com.fyp.reminder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.fyp.reminder.Utils.PreferencesHandler;
import com.fyp.reminder.model.User;
import com.fyp.reminder.sql.DatabaseHelper;

public class UserSignup extends AppCompatActivity {

    EditText et_name , et_email,et_pass,et_cpass;
    private DatabaseHelper databaseHelper;
    private User user;
    Button btn_signup;
    TextView txtSignin;
    private SessionManager sessionManager;
    PreferencesHandler preferencesHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_signup);
        getSupportActionBar().hide();
        sessionManager = new SessionManager(this);
        preferencesHandler = new PreferencesHandler(this);
        et_name = findViewById(R.id.et_name);
        et_email = findViewById(R.id.et_email);
        et_pass = findViewById(R.id.et_pass);
        et_cpass = findViewById(R.id.et_cpass);
        btn_signup= findViewById(R.id.btn_signup);
        txtSignin = findViewById(R.id.txtSignin);

        databaseHelper = new DatabaseHelper(UserSignup.this);
        user = new User();




        btn_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (et_email.getText().toString().trim().length() <= 0) {
                    et_email.setError("Email Required");
                } else if (et_pass.getText().toString().trim().length() <= 0) {
                    et_pass.setError("Password Required");
                } else {
                    postDataToSQLite();
                }
            }
        });

        txtSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(UserSignup.this, UserLogin.class));

            }
        });





    }



    private void postDataToSQLite(){

        if (!databaseHelper.checkUser(et_email.getText().toString().trim())) {

            user.setName(et_name.getText().toString().trim());
            user.setEmail(et_email.getText().toString().trim());
            user.setPassword(et_pass.getText().toString().trim());

            databaseHelper.addUser(user);
            int user_id = databaseHelper.GetUserID(et_email.getText().toString().trim());
            preferencesHandler.setUserIdId(user_id);
            // Snack Bar to show success message that record saved successfully


            Toast.makeText(UserSignup.this, getString(R.string.success_message), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this,AddReminder.class));
            finish();
            sessionManager.setLogin(true);
            //emptyInputEditText();
        }
        else
        {
            Toast.makeText(UserSignup.this, getString(R.string.error_email_exists),Toast.LENGTH_SHORT).show();
        }

        }




    private void emptyInputEditText(){
        et_name.setText(null);
        et_email.setText(null);
        et_pass.setText(null);
        et_cpass.setText(null);
    }

}
