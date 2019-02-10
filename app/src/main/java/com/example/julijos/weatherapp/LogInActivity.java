package com.example.julijos.weatherapp;

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
import android.widget.ProgressBar;
import android.widget.Toast;




public class LogInActivity extends AppCompatActivity implements View.OnClickListener {


    EditText loginEmail, loginPassword;
    ProgressBar progressBar;


    LocationManager locationManager;
    LocationListener locationListener;

    double longitude, latitude;
    String strLong, strLat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        loginEmail = (EditText)findViewById(R.id.editTextLoginEmail);
        loginPassword = (EditText)findViewById(R.id.editTextLoginPassword);
        progressBar = (ProgressBar)findViewById(R.id.loginProgressBar);

        findViewById(R.id.btnLogin).setOnClickListener(this);
        findViewById(R.id.textViewNotMember).setOnClickListener(this);




        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                longitude = location.getLongitude();
                latitude = location.getLatitude();
                //Toast.makeText(getApplicationContext(), String.valueOf(latitude)+ String.valueOf(longitude), Toast.LENGTH_LONG).show();
                Log.i("GPS-Coordinates", "Lat: " + String.valueOf(latitude) + ", Long: " + String.valueOf(longitude));
                strLat = String.valueOf(latitude);
                strLong = String.valueOf(longitude);
                // Log.i("Coordinates: ", strLat+", "+strLong);
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


    private void userLogin(){
        String email = loginEmail.getText().toString().trim();
        String password = loginPassword.getText().toString().trim();

        if (email.isEmpty()) {
            loginEmail.setError("Email is required");
            loginEmail.requestFocus();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            loginEmail.setError("Not a valid email");
            loginEmail.requestFocus();
            return;
        }
        if(password.isEmpty()){
            loginPassword.setError("Password is required");
            loginPassword.requestFocus();
            return;
        }
        if(password.length() < 6){
            loginPassword.setError("Password to short");
            loginPassword.requestFocus();
            return;
        }
        startActivity(new Intent(this, MainActivity.class).putExtra("latitude", strLat).putExtra("longitude", strLong));


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
