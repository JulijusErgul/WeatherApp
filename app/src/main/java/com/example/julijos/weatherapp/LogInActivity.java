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
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


public class LogInActivity extends AppCompatActivity implements View.OnClickListener {


    EditText loginEmail, loginPassword;

    LocationManager locationManager;
    LocationListener locationListener;

    double longitude, latitude;
    String strLong, strLat;

    private FirebaseAuth mAuth;
    boolean loginStatus = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        loginEmail = (EditText)findViewById(R.id.editTextLoginEmail);
        loginPassword = (EditText)findViewById(R.id.editTextLoginPassword);

        findViewById(R.id.btnLogin).setOnClickListener(this);
        findViewById(R.id.textViewNotMember).setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();


        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                longitude = location.getLongitude();
                latitude = location.getLatitude();
                Log.i("GPS-Coordinates", "Lat: " + String.valueOf(latitude) + ", Long: " + String.valueOf(longitude));
                strLat = String.valueOf(latitude);
                strLong = String.valueOf(longitude);
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

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful()){
                            Log.d("Login: ", "login successful");
                            Toast.makeText(LogInActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                            loginStatus = true;
                        }
                        else{
                            Log.d("Login", "Failed to login");
                            Toast.makeText(LogInActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                            loginStatus = false;
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
                if(loginStatus == true)
                    startActivity(new Intent(this, MainActivity.class).putExtra("latitude", strLat).putExtra("longitude", strLong));
                break;
        }

    }
}
