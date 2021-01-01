package com.example.todoapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.todoapp.Helpers.ProgressDialogGenerator;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;


public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    EditText name, password, email;
    Button register;
    TextView login;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_activity);

        name        = findViewById(R.id.name);
        password    = findViewById(R.id.password);
        email       = findViewById(R.id.email);
        register    = findViewById(R.id.register);
        login       = findViewById(R.id.login);
        mAuth = FirebaseAuth.getInstance();

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nameStr = name.getText().toString();
                String passwordStr = password.getText().toString();
                String emailStr = email.getText().toString();
                boolean flag = true;
                if(nameStr.isEmpty()){
                    flag = false;
                    name.setError("Please enter your name");
                }
                if(passwordStr.isEmpty()) {
                    flag = false;
                    password.setError("Please enter a password");
                }
                if(emailStr.isEmpty()) {
                    flag = false;
                    email.setError("Please enter email address");
                }
                if(flag){
                    progressDialog = ProgressDialogGenerator.showLoadingDialog(SignUpActivity.this);
                    signUp(emailStr, passwordStr, nameStr);
                }

            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    private void signUp(String email, String password, String name) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.hide();
                        if (task.isSuccessful()) {
                            Toast.makeText(SignUpActivity.this, "Registered Successfully", Toast.LENGTH_SHORT).show();
                            FirebaseUser user = mAuth.getCurrentUser();
                            String uid = user.getUid();
                            DatabaseReference usersReference = database.getReference("users");
                            HashMap<String, String> map = new HashMap<>();
                            map.put("name", name);
                            usersReference.child(uid).setValue(map);
                            Intent intent = new Intent(SignUpActivity.this,CategoriesActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(SignUpActivity.this, task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {
            Intent intent = new Intent(SignUpActivity.this,CategoriesActivity.class);
            startActivity(intent);
            finish();
        }
    }

}