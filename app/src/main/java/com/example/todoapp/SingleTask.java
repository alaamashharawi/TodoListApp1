package com.example.todoapp;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.example.todoapp.Helpers.ProgressDialogGenerator;
import com.example.todoapp.Models.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class SingleTask extends AppCompatActivity {

    TextView title_txt, description_txt, category, edit, date_txt, delete;
    EditText title_edt, description_edt;
    ImageButton back;
    Task task;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_task);

        title_txt = findViewById(R.id.title_txt);
        description_txt = findViewById(R.id.description_txt);
        title_edt = findViewById(R.id.title_edt);
        description_edt = findViewById(R.id.description_edt);
        category = findViewById(R.id.category);
        edit = findViewById(R.id.edit);
        back = findViewById(R.id.back);
        date_txt = findViewById(R.id.date_txt);
        delete = findViewById(R.id.delete);

        String taskId = getIntent().getStringExtra("taskId");

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        progressDialog = ProgressDialogGenerator.showLoadingDialog(SingleTask.this);
        FirebaseDatabase.getInstance().getReference("users/"+uid+"/tasks/"+taskId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                task = snapshot.getValue(Task.class);

                if(task != null) {
                    title_txt.setText(task.getTitle());
                    title_edt.setText(task.getTitle());
                    description_edt.setText(task.getDescription());
                    description_txt.setText(task.getDescription());
                    date_txt.setText(task.getDate());

                    // Add Category
                    FirebaseDatabase.getInstance().getReference("users/"+uid+"/categories/"+task.getCategoryId()+"/name").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.getValue() != null) {
                                category.setText(snapshot.getValue().toString());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                }

                progressDialog.hide();
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        title_txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                title_txt.setVisibility(View.GONE);
                title_edt.setVisibility(View.VISIBLE);
                title_edt.requestFocus();
            }
        });

        description_txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                description_txt.setVisibility(View.GONE);
                description_edt.setVisibility(View.VISIBLE);
                description_edt.requestFocus();
            }
        });

        date_txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDatePicker();
            }
        });

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String title = title_edt.getText().toString();
                String description = description_edt.getText().toString();
                String date = date_txt.getText().toString();

                task.setTitle(title);
                task.setDescription(description);
                task.setDate(date);

                FirebaseDatabase.getInstance().getReference("users/"+uid+"/tasks/"+taskId).setValue(task);
                finish();
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            DatabaseReference mRef;
            @Override
            public void onClick(View v) {

                progressDialog = ProgressDialogGenerator.showLoadingDialog(SingleTask.this);

                FirebaseDatabase.getInstance().getReference("users/"+uid+"/tasks/"+taskId).removeValue();
                // update num of tasks for category
                mRef = FirebaseDatabase.getInstance().getReference("users/"+uid+"/categories/"+task.getCategoryId()+"/numOfTasks");
                mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int num = Integer.parseInt(snapshot.getValue().toString());
                        mRef.setValue(num - 1);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
                Toast.makeText(SingleTask.this, "Task deleted successfully", Toast.LENGTH_SHORT).show();
                progressDialog.hide();
                progressDialog.dismiss();
                finish();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void openDatePicker() {
        final Calendar myCalendar = Calendar.getInstance();

        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                String myFormat = "dd-MM-yyyy"; //In which you need put here
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

                date_txt.setText(sdf.format(myCalendar.getTime()));
            }

        };


        new DatePickerDialog(SingleTask.this,
                date,
                myCalendar
                        .get(Calendar.YEAR),
                myCalendar
                        .get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }
}