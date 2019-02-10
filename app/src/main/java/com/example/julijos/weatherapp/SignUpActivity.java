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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    User user;

    EditText registerEmail, registerPassword;
    ProgressBar progressBar;
    //private FirebaseAuth mAuth;
   // private FirebaseDatabase mDatabase;
    //private DatabaseReference databaseReference;


    LocationManager locationManager;
    LocationListener locationListener;

    double longitude, latitude;
    String strLong, strLat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        user = new User();

        registerEmail = (EditText) findViewById(R.id.editTextRegisterEmail) ;
        registerPassword = (EditText)findViewById(R.id.editTextRegisterPassword);
        progressBar = (ProgressBar)findViewById(R.id.registerProgressBar);

        findViewById(R.id.btnRegister).setOnClickListener(this);
        findViewById(R.id.textViewAlreadyMember).setOnClickListener(this);

       // mAuth = FirebaseAuth.getInstance();

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


    private void registerUser(){
        String email = registerEmail.getText().toString().trim();
        String password = registerPassword.getText().toString().trim();



        if (email.isEmpty()) {
            registerEmail.setError("Email is required");
            registerEmail.requestFocus();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            registerEmail.setError("Not a valid email");
            registerEmail.requestFocus();
            return;
        }
        if(password.isEmpty()){
            registerPassword.setError("Password is required");
            registerPassword.requestFocus();
            return;
        }
        if(password.length() < 6){
            registerPassword.setError("Password to short");
            registerPassword.requestFocus();
            return;
        }

        user.setmEmail(email);
        user.setmPassword(password);

    }


    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.btnRegister:
                registerUser();

                startActivity(new Intent(this, MainActivity.class).putExtra("latitude", strLat).putExtra("longitude", strLong));
                break;

            case R.id.textViewAlreadyMember:
                startActivity(new Intent(this, LogInActivity.class));
                break;
        }
    }
}
