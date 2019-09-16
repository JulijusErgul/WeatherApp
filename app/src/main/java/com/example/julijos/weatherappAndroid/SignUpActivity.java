package com.example.julijos.weatherappAndroid;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "SignUpActivity";
    private static final String EMAIL_EMPTY ="Email is required";
    private static final String NOT_VALID_EMAIL = "Email is not in valid format";
    private static final String PASSWORD_EMPTY = "Password is required";
    private static final String PASSWORD_SHORT = "Password must be atleat 6 charachters";

    private User user;

    EditText registerEmail, registerPassword;

    private FirebaseAuth firebaseAuthentication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        Log.d(TAG, "onCreate: starts");

        user = new User();

        registerEmail = (EditText) findViewById(R.id.editTextRegisterEmail) ;
        registerPassword = (EditText)findViewById(R.id.editTextRegisterPassword);

        findViewById(R.id.btnRegister).setOnClickListener(this);
        findViewById(R.id.textViewAlreadyMember).setOnClickListener(this);

        firebaseAuthentication = FirebaseAuth.getInstance();
        Log.d(TAG, "onCreate: lämnar");
    }

    private void registerUser(){
        Log.d(TAG, "registerUser: starts");
        user.setEmail( registerEmail.getText().toString().trim());
        user.setPassword(registerPassword.getText().toString().trim());

        user.setEmail(registerEmail.getText().toString().trim());
        user.setPassword(registerPassword.getText().toString().trim());

        if (user.validateEmailAndPassword() == 1) {
            registerEmail.setError(EMAIL_EMPTY);
            registerEmail.requestFocus();
            return;
        }
        if(user.validateEmailAndPassword() == 2){
            registerEmail.setError(NOT_VALID_EMAIL);
            registerEmail.requestFocus();
            return;
        }
        if(user.validateEmailAndPassword() == 3){
            registerPassword.setError(PASSWORD_EMPTY);
            registerPassword.requestFocus();
            return;
        }
        if(user.validateEmailAndPassword() == 4){
            registerPassword.setError(PASSWORD_SHORT);
            registerPassword.requestFocus();
            return;
        }

        firebaseAuthentication.createUserWithEmailAndPassword(user.getEmail(), user.getPassword())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(SignUpActivity.this, R.string.register_successful, Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(SignUpActivity.this, WeatherActivity.class));
                        }
                        else{
                            //if sign in fails, display a message to the user.
                            Toast.makeText(SignUpActivity.this, R.string.register_failure, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        Log.d(TAG, "registerUser: lämnar");
    }

    @Override
    public void onClick(View view) {
        Log.d(TAG, "onClick: starts");
        switch(view.getId()){
            case R.id.btnRegister:
                registerUser();
                Log.d(TAG, "onClick: btnRegister clicked");
                break;
            case R.id.textViewAlreadyMember:
                startActivity(new Intent(this, LogInActivity.class));
                Log.d(TAG, "onClick: btnAlreadyMember clicked");
                break;
        }
        Log.d(TAG, "onClick: lämnar");
    }
}
