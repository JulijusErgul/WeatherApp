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
    private static final String LONGITUDE = "Longitude";
    private static final String LATITUDE = "Latitude";
    private static final String EMAIL = "Email";
    private static final String EMAIL_EMPTY ="Email is required";
    private static final String NOT_VALID_EMAIL = "Email is not in valid format";
    private static final String PASSWORD_EMPTY = "Password is required";
    private static final String PASSWORD_SHORT = "Password must be atleat 6 charachters";

    EditText registerEmail, registerPassword;

    LocationManager locationManager;
    LocationListener locationListener;

    double longitude, latitude;
    String strLongitude, strLatitude;

    private FirebaseAuth firebaseAuthentication;

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(locationListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        registerEmail = (EditText) findViewById(R.id.editTextRegisterEmail) ;
        registerPassword = (EditText)findViewById(R.id.editTextRegisterPassword);

        findViewById(R.id.btnRegister).setOnClickListener(this);
        findViewById(R.id.textViewAlreadyMember).setOnClickListener(this);

        firebaseAuthentication = FirebaseAuth.getInstance();

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                longitude = location.getLongitude();
                latitude = location.getLatitude();
                strLatitude = String.valueOf(latitude);
                strLongitude = String.valueOf(longitude);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };


        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
        }
        else{
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 100, locationListener);
        }
    }

    private void registerUser(){
        final String email = registerEmail.getText().toString().trim();
        String password = registerPassword.getText().toString().trim();

        if (email.isEmpty()) {
            registerEmail.setError(EMAIL_EMPTY);
            registerEmail.requestFocus();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            registerEmail.setError(NOT_VALID_EMAIL);
            registerEmail.requestFocus();
            return;
        }

        if(password.isEmpty()){
            registerPassword.setError(PASSWORD_EMPTY);
            registerPassword.requestFocus();
            return;
        }

        if(password.length() < 6){
            registerPassword.setError(PASSWORD_SHORT);
            registerPassword.requestFocus();
            return;
        }

        firebaseAuthentication.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(SignUpActivity.this, R.string.register_successful, Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(SignUpActivity.this, WeatherActivity.class)
                                    .putExtra(LATITUDE, strLatitude)
                                    .putExtra(LONGITUDE, strLongitude)
                                    .putExtra(EMAIL, email));
                        }
                        else{
                            //if sign in fails, display a message to the user.
                            Toast.makeText(SignUpActivity.this, R.string.register_failure, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.btnRegister:
                registerUser();
                break;
            case R.id.textViewAlreadyMember:
                startActivity(new Intent(this, LogInActivity.class));
                break;
        }
    }
}
