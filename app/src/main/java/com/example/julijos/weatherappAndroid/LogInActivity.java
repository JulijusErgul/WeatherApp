package com.example.julijos.weatherappAndroid;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;


public class LogInActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "LogInActivity";
    private static final String EMAIL_EMPTY ="Email is required";
    private static final String NOT_VALID_EMAIL = "Email is not in valid format";
    private static final String PASSWORD_EMPTY = "Password is required";
    private static final String PASSWORD_SHORT = "Password must be atleat 6 charachters";

    EditText loginEmail, loginPassword;

    private User user;

    private FirebaseAuth firebaseAuthentication;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private double longitude, latitude;
    private String strLatitude, strLongitude;
    private CheckBox keepLoggedIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        firebaseAuthentication = FirebaseAuth.getInstance();

        user = new User();

        loginEmail = (EditText)findViewById(R.id.editTextLoginEmail);
        loginPassword = (EditText)findViewById(R.id.editTextLoginPassword);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                longitude = location.getLongitude();
                latitude = location.getLatitude();
                Log.i("GPS-Coordinates", R.string.coordinate_latitude + String.valueOf(latitude) + ", " + R.string.coordinate_longitude + String.valueOf(longitude));
                strLatitude = String.valueOf(latitude);
                strLongitude = String.valueOf(longitude);
                if(getUser() != null && FirebaseAuth.getInstance().getCurrentUser() != null){
                    redirectToWeatherActivity();
                }
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
        askForUserPermissions();
        findViewById(R.id.btnLogin).setOnClickListener(this);
        findViewById(R.id.textViewNotMember).setOnClickListener(this);
        findViewById(R.id.continueWithoutLoginIn).setOnClickListener(this);
        keepLoggedIn = (CheckBox)findViewById(R.id.keepUserLoggedInCheckBox);

    }

    private void redirectToWeatherActivity() {
        Intent intent = new Intent(LogInActivity.this, WeatherActivity.class)
                .putExtra("Longitude", strLongitude)
                .putExtra("Latitude", strLatitude);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: start");
        super.onPause();
        locationManager.removeUpdates(locationListener);
        Log.d(TAG, "onPause: lämnar");
    }

    private void askForUserPermissions() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);

        }
        else{
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 100, locationListener);
        }
    }

    private void userLogin(){
        user.setEmail(loginEmail.getText().toString().trim());
        user.setPassword(loginPassword.getText().toString().trim());

        if (user.validateEmailAndPassword() == 1) {
            loginEmail.setError(EMAIL_EMPTY);
            loginEmail.requestFocus();
            return;
        }
        if(user.validateEmailAndPassword() == 2){
            loginEmail.setError(NOT_VALID_EMAIL);
            loginEmail.requestFocus();
            return;
        }
        if(user.validateEmailAndPassword() == 3){
            loginPassword.setError(PASSWORD_EMPTY);
            loginPassword.requestFocus();
            return;
        }
        if(user.validateEmailAndPassword() == 4){
            loginPassword.setError(PASSWORD_SHORT);
            loginPassword.requestFocus();
            return;
        }

        firebaseAuthentication.signInWithEmailAndPassword(user.getEmail(), user.getPassword())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(LogInActivity.this, R.string.login_successful, Toast.LENGTH_SHORT).show();
                            if(keepLoggedIn.isChecked()){
                                saveUser(user);
                            }
                            redirectToWeatherActivity();
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
            case R.id.continueWithoutLoginIn:
                redirectToWeatherActivity();
                break;
            default:
                return;
        }
    }

    /*
     * Save City information arrays to Sharedpreferences
     * */
    public void saveUser(User user){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(user);
        editor.putString("userLoggedIn",json);
        editor.apply();
    }
    /*
     * Get City information arrays from Sharedpreferences
     * */
    public User getUser(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Gson gson = new Gson();
        String json = prefs.getString("userLoggedIn", null);
        Type type = new TypeToken<User>() {}.getType();
        return gson.fromJson(json, type);
    }
}
