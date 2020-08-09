package com.fyp.reminder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.fyp.reminder.Utils.PreferencesHandler;
import com.fyp.reminder.model.User;
import com.fyp.reminder.sql.DatabaseHelper;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class UserLogin extends AppCompatActivity {

    Button btn_login,btn_signup;
    EditText et_email,et_password;

    private DatabaseHelper databaseHelper;
    private User user;
    private FirebaseAuth mAuth;
    SignInButton google_btn;
    GoogleSignInClient mGoogleSignInClient;
    int RC_SIGN_IN = 1 ;
    SessionManager sessionManager;
    PreferencesHandler preferencesHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);
        getSupportActionBar().hide();
        sessionManager = new SessionManager(this);
        preferencesHandler = new PreferencesHandler(this);
        et_email = findViewById(R.id.et_email);
        et_password = findViewById(R.id.et_password);
        btn_login = findViewById(R.id.btn_login);
        btn_signup = findViewById(R.id.btn_signup);
        google_btn = findViewById(R.id.google_btn);

        databaseHelper = new DatabaseHelper(UserLogin.this);
        user = new User();



        // Configure Google Sign In

        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(UserLogin.this,gso);

        google_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

        setGooglePlusButtonText(google_btn);


        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    if(et_email.getText().toString().trim().length()<=0){
                        et_email.setError("Email Required");
                    }else if(et_password.getText().toString().trim().length()<=0){
                        et_password.setError("Password Required");
                    }else{
                        verifyFromSQLite();
                    }

            }
        });



        btn_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UserLogin.this, UserSignup.class));
            }
        });


    }


    //methods for google sigin
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);

            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("TAG", "Google sign in failed", e);
                // ...
            }
        }
    }

//check to see if the user is currently signed in
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser currentUser) {

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());

    }


    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d("TAG", "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG", "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            verifyGoogleSignInFromSQLite(user);

                         /*
                            int user_id = databaseHelper.GetUserID(et_email.getText().toString().trim());
                            preferencesHandler.setUserIdId(user_id);
                            Toast.makeText(UserLogin.this,"Successfully SignedIn",Toast.LENGTH_SHORT).show();
                            Intent accountsIntent = new Intent(UserLogin.this, AddReminder.class);
                            startActivity(accountsIntent);
                            sessionManager.setLogin(true);*/
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG", "signInWithCredential:failure", task.getException());
                           // Snackbar.make(findViewById(R.id.), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // ...
                    }
                });
    }


    private void verifyFromSQLite() {

            if (databaseHelper.checkUser(et_email.getText().toString().trim()
                    , et_password.getText().toString().trim())) {
                int user_id = databaseHelper.GetUserID(et_email.getText().toString().trim());
                preferencesHandler.setUserIdId(user_id);
                Intent accountsIntent = new Intent(UserLogin.this, AddReminder.class);
                accountsIntent.putExtra("EMAIL", et_email.getText().toString().trim());
               // emptyInputEditText();
                startActivity(accountsIntent);
                sessionManager.setLogin(true);
            } else {
//            Snackbar.make(UserLogin.this, getString(R.string.error_valid_email_password), Snackbar.LENGTH_LONG).show();
                Toast.makeText(UserLogin.this, "Something wrong", Toast.LENGTH_SHORT).show();
            }
        }

    private void verifyGoogleSignInFromSQLite(FirebaseUser firebaseUser){

        if (!databaseHelper.checkUser(firebaseUser.getEmail())) {

            user.setName(firebaseUser.getDisplayName());
            user.setEmail(firebaseUser.getEmail());
            //user.setPassword(et_pass.getText().toString().trim());

            databaseHelper.addUser(user);
            int user_id = databaseHelper.GetUserID(firebaseUser.getEmail());
            preferencesHandler.setUserIdId(user_id);
            Toast.makeText(UserLogin.this, getString(R.string.success_message), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this,AddReminder.class));
            finish();
            sessionManager.setLogin(true);
            //emptyInputEditText();
        }
        else
        {
            int user_id = databaseHelper.GetUserID(firebaseUser.getEmail());
            preferencesHandler.setUserIdId(user_id);
            Intent accountsIntent = new Intent(UserLogin.this, AddReminder.class);
            accountsIntent.putExtra("EMAIL", firebaseUser.getEmail());
            // emptyInputEditText();
            Toast.makeText(UserLogin.this,"Successfully SignedIn",Toast.LENGTH_SHORT).show();

            startActivity(accountsIntent);
            sessionManager.setLogin(true);        }

    }



    protected void setGooglePlusButtonText(SignInButton signInButton) {
        // Find the TextView that is inside of the SignInButton and set its text
        for (int i = 0; i < signInButton.getChildCount(); i++) {
            View v = signInButton.getChildAt(i);

            if (v instanceof TextView) {
                TextView tv = (TextView) v;
                tv.setText("Sign in With Google");
                return;
            }
        }
    }



    private void emptyInputEditText () {
        et_email.setText(null);
        et_password.setText(null);
    }
}
