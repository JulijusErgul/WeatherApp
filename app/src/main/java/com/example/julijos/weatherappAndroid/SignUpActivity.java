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

    EditText registerEmail, registerPassword;

    LocationManager locationManager;
    LocationListener locationListener;

    double longitude, latitude;
    String strLong, strLat;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        registerEmail = (EditText) findViewById(R.id.editTextRegisterEmail) ;
        registerPassword = (EditText)findViewById(R.id.editTextRegisterPassword);

        findViewById(R.id.btnRegister).setOnClickListener(this);
        findViewById(R.id.textViewAlreadyMember).setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                longitude = location.getLongitude();
                latitude = location.getLatitude();
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

    private void registerUser(){
        final String email = registerEmail.getText().toString().trim();
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

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(SignUpActivity.this, "Authentication Successful", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(SignUpActivity.this, MainActivity.class)
                                    .putExtra("latitude", strLat)
                                    .putExtra("longitude", strLong)
                                    .putExtra("email", email));
                        }
                        else{
                            //if sign in fails, display a message to the user.
                            Toast.makeText(SignUpActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
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
