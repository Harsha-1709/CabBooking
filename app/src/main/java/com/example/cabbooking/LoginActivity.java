package com.example.cabbooking;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cabbooking.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    Button backBtn, loginBtn;
    EditText emailEditText, passwordEditText;
    TextView moveToRegister;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        linkViewElements();
        setRegisterTextViewAction();
        setLoginBtnAction();
    }

    //Get View variables from xml id
    private void linkViewElements() {
        loginBtn = findViewById(R.id.loginLoginBtn);
        emailEditText = findViewById(R.id.loginEmailEditText);
        passwordEditText = findViewById(R.id.loginPasswordEditText);
        moveToRegister = findViewById(R.id.moveToRegisterTextView);
    }

    //Login process when clicking 'login' button
    private void setLoginBtnAction() {
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = emailEditText.getText().toString();
                final String password = passwordEditText.getText().toString();

                //Check if the input email or password is empty
                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, Constants.ToastMessage.emptyInputError,
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                //Call FirebaseAuth for authentication process
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(LoginActivity.this, Constants.ToastMessage.signInSuccess,
                                            Toast.LENGTH_SHORT).show();
                                    moveToHomePage(); //Move to HomeActivity
                                } else {
                                    Toast.makeText(LoginActivity.this, Constants.ToastMessage.signInFailure,
                                            Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
            }
        });
    }

    //Move to user's homepage if successfully logged in
    private void moveToHomePage() {
        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        i.putExtra("email", emailEditText.getText().toString());
        startActivity(i);
        finish();
    }

    private void setRegisterTextViewAction() {
        moveToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(i);
                finish();
            }
        });
    }

}