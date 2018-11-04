package com.example.julijos.weatherapp;

import android.content.Intent;
import android.support.annotation.NonNull;
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

import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {



    EditText registerEmail, registerPassword;
    ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);


        registerEmail = (EditText) findViewById(R.id.editTextRegisterEmail) ;
        registerPassword = (EditText)findViewById(R.id.editTextRegisterPassword);
        progressBar = (ProgressBar)findViewById(R.id.registerProgressBar);

        findViewById(R.id.btnRegister).setOnClickListener(this);
        findViewById(R.id.textViewAlreadyMember).setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();

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

        progressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    progressBar.setVisibility(View.GONE);
                    Log.i("SUCCESS", "onComplete: Registration Successful");
                    Toast.makeText(getApplicationContext(), "You are successfully registered", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//to not come back to loginActivity when back is tapped
                    startActivity(intent);
                }else{
                    Log.i("FAILURE", "onComplete: Registration Failed");
                    if(task.getException() instanceof FirebaseAuthUserCollisionException){
                        Log.i("UserCollision", "onComplete: Email already registere");
                        registerEmail.setError("Email already registered");
                        registerEmail.requestFocus();
                        progressBar.setVisibility(View.GONE);
                    }else {
                        Log.i("ERROR", "onComplete: Error occured");
                        Toast.makeText(getApplicationContext(), "Some error occured", Toast.LENGTH_LONG).show();
                    }
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
