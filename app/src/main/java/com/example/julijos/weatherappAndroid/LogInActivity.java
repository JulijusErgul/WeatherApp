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


public class LogInActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "LogInActivity";
    private static final String LONGITUDE = "Longitude";
    private static final String LATITUDE = "Latitude";
    private static final String EMAIL = "Email";
    private static final String EMAIL_EMPTY ="Email is required";
    private static final String NOT_VALID_EMAIL = "Email is not in valid format";
    private static final String PASSWORD_EMPTY = "Password is required";
    private static final String PASSWORD_SHORT = "Password must be atleat 6 charachters";



    EditText loginEmail, loginPassword;

    LocationManager locationManager;
    LocationListener locationListener;

    double longitude, latitude;
    String strLongitude, strLatitude;

    private FirebaseAuth firebaseAuthentication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        firebaseAuthentication = FirebaseAuth.getInstance();

        loginEmail = (EditText)findViewById(R.id.editTextLoginEmail);
        loginPassword = (EditText)findViewById(R.id.editTextLoginPassword);

        findViewById(R.id.btnLogin).setOnClickListener(this);
        findViewById(R.id.textViewNotMember).setOnClickListener(this);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                longitude = location.getLongitude();
                latitude = location.getLatitude();
                Log.i("GPS-Coordinates", R.string.coordinate_latitude + String.valueOf(latitude) + ", " + R.string.coordinate_longitude + String.valueOf(longitude));
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

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(locationListener);
    }

    private void userLogin(){
        final String email = loginEmail.getText().toString().trim();
        String password = loginPassword.getText().toString().trim();

        if (email.isEmpty()) {
            loginEmail.setError(EMAIL_EMPTY);
            loginEmail.requestFocus();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            loginEmail.setError(NOT_VALID_EMAIL);
            loginEmail.requestFocus();
            return;
        }
        if(password.isEmpty()){
            loginPassword.setError(PASSWORD_EMPTY);
            loginPassword.requestFocus();
            return;
        }
        if(password.length() < 6){
            loginPassword.setError(PASSWORD_SHORT);
            loginPassword.requestFocus();
            return;
        }

        firebaseAuthentication.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful()){
                            Toast.makeText(LogInActivity.this, R.string.login_successful, Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LogInActivity.this, WeatherActivity.class)
                                     .putExtra(LATITUDE, strLatitude)
                                     .putExtra(LONGITUDE, strLongitude)
                                     .putExtra(EMAIL, email));

                        }
                        else{
                            Toast.makeText(LogInActivity.this, R.string.login_failure, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.textViewNotMember:
                startActivity(new Intent(this, SignUpActivity.class));
                break;
            case R.id.btnLogin:
                userLogin();
                break;
        }
    }
}
