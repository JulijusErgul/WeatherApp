package com.example.julijos.weatherapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthEmailException;

public class LogInActivity extends AppCompatActivity implements View.OnClickListener {


    EditText loginEmail, loginPassword;
    ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        loginEmail = (EditText)findViewById(R.id.editTextLoginEmail);
        loginPassword = (EditText)findViewById(R.id.editTextLoginPassword);
        progressBar = (ProgressBar)findViewById(R.id.loginProgressBar);

        findViewById(R.id.btnLogin).setOnClickListener(this);
        findViewById(R.id.textViewNotMember).setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();

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

        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "You are successfully logged in", Toast.LENGTH_SHORT).show();
                    Log.i("SUCCESS", "onComplete: Login successful");
                    //startActivity(new Intent(this, MainActivity.class));
                    Intent intent = new Intent(LogInActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//to not come back to loginActivity when back is tapped
                    startActivity(intent);
                }else{
                    if(task.getException() instanceof FirebaseAuthEmailException){
                        Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        Log.i("FAILURE ", "onComplete: " + task.getException().getMessage());
                        loginEmail.setError("Use registered Email");
                        loginEmail.requestFocus();
                        progressBar.setVisibility(View.GONE);
                    }else {
                        Log.i("ERROR ", "onComplete: Exception" + task.getException().getMessage());
                    }
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
