package com.example.cabbooking;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RegisterFinalActivity extends AppCompatActivity {
    private Button backBtn, registerBtn;
    private EditText emailEditText, passwordEditText, aadharEditText, insuranceNumberEditText;
    private Spinner insuranceTypeSpinner;

    private String username, phone, birthDate, gender, role, transportationType, vehiclePlateNumber;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_final);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        linkViewElements();
        setupInsuranceTypeSpinner();
        getPreviousRegisterFormInfo();
        setBackBtnAction();
        setRegisterBtnAction();
    }

    private void linkViewElements() {
        backBtn = findViewById(R.id.registerFinalBackBtn);
        registerBtn = findViewById(R.id.registerFinalRegisterBtn);
        emailEditText = findViewById(R.id.registerFinalEmailEditText);
        passwordEditText = findViewById(R.id.registerFinalPasswordEditText);
        aadharEditText = findViewById(R.id.registerFinalAadharEditText);
        insuranceNumberEditText = findViewById(R.id.registerFinalInsuranceNumberEditText);
        insuranceTypeSpinner = findViewById(R.id.registerFinalInsuranceTypeSpinner);
    }

    private void setupInsuranceTypeSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, new String[]{"Private", "Government"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        insuranceTypeSpinner.setAdapter(adapter);
    }

    private void setBackBtnAction() {
        backBtn.setOnClickListener(v -> {
            startActivity(new Intent(RegisterFinalActivity.this, RegisterActivity.class));
            finish();
        });
    }

    private void getPreviousRegisterFormInfo() {
        Intent intent = getIntent();
        username = intent.getStringExtra(Constants.FSUser.usernameField);
        phone = intent.getStringExtra(Constants.FSUser.phoneField);
        birthDate = intent.getStringExtra(Constants.FSUser.birthDateField);
        gender = intent.getStringExtra(Constants.FSUser.genderField);
        role = intent.getStringExtra(Constants.FSUser.roleField);
        transportationType = intent.getStringExtra(Constants.FSUser.transportationType);
        vehiclePlateNumber = intent.getStringExtra(Constants.FSUser.vehiclePlateNumber);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void saveUserInfo() throws ParseException {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String aadhar = aadharEditText.getText().toString();
        String insuranceNumber = insuranceNumberEditText.getText().toString();
        String insuranceType = insuranceTypeSpinner.getSelectedItem().toString();

        @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
        Date birthDateNew = df.parse(birthDate);

        Map<String, Object> data = new HashMap<>();
        data.put(Constants.FSUser.usernameField, username);
        data.put(Constants.FSUser.phoneField, phone);
        data.put(Constants.FSUser.birthDateField, birthDateNew);
        data.put(Constants.FSUser.genderField, gender);
        data.put(Constants.FSUser.emailField, email);
        data.put(Constants.FSUser.roleField, role);
        data.put(Constants.FSUser.transportationType, transportationType);
        data.put(Constants.FSUser.vehiclePlateNumber, vehiclePlateNumber);
        data.put(Constants.FSUser.aadharNumber, aadhar);
        data.put(Constants.FSUser.insuranceNumber, insuranceNumber);
        data.put(Constants.FSUser.insuranceType, insuranceType);
        data.put(Constants.FSUser.currentPositionLatitude, 0.0);
        data.put(Constants.FSUser.currentPositionLongitude, 0.0);

        ArrayList<Integer> rating = new ArrayList<>();
        rating.add(5);
        data.put(Constants.FSUser.rating, rating);

        db.collection(Constants.FSUser.userCollection).add(data);
    }

    private void moveToLoginActivity() {
        startActivity(new Intent(RegisterFinalActivity.this, LoginActivity.class));
        finish();
    }

    private void setRegisterBtnAction() {
        registerBtn.setOnClickListener(v -> {
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(RegisterFinalActivity.this, Constants.ToastMessage.emptyInputError, Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(RegisterFinalActivity.this, new OnCompleteListener<AuthResult>() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(RegisterFinalActivity.this, Constants.ToastMessage.registerSuccess, Toast.LENGTH_SHORT).show();
                        try {
                            saveUserInfo();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        moveToLoginActivity();
                    } else {
                        Toast.makeText(RegisterFinalActivity.this, Constants.ToastMessage.registerFailure, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });
    }
}
