package com.example.todoapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todoapp.Adapters.TasksAdapter;
import com.example.todoapp.Helpers.ProgressDialogGenerator;
import com.example.todoapp.Helpers.TasksSearch;
import com.example.todoapp.Models.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;


public class TasksActivity extends AppCompatActivity {

    TextView category,delete,logout;
    RecyclerView tasks_rv;
    EditText search, create;
    String mainCategoryText;
    Task[] tasks;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    String categoryId;
    ImageButton back;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);

        tasks_rv = findViewById(R.id.tasks_rv);
        search = findViewById(R.id.search);
        category = findViewById(R.id.category);
        delete = findViewById(R.id.delete);
        create = findViewById(R.id.create);
        back = findViewById(R.id.back);
        logout = findViewById(R.id.logout);

        categoryId = getIntent().getStringExtra("categoryId");

        /* retrieve category name */
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        database.getReference("users/"+uid+"/categories/"+categoryId+"/name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String categoryName = snapshot.getValue().toString();
                mainCategoryText = categoryName;
                category.setText(categoryName+" List");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        /* Tasks RecyclerView */
        tasks_rv.setLayoutManager(new LinearLayoutManager(this));
        getTasks();

        /* Search */
        search.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean isEmptySearch = search.getText().toString().isEmpty();
                showOrHideViews(isEmptySearch);

                if(!isEmptySearch) {
                    Task[] tasksMatchSearch = TasksSearch.getSearchTasks(search.getText().toString().toLowerCase());
                    TasksAdapter tasksMatchSearchAdapter = new TasksAdapter(getApplicationContext(), tasksMatchSearch, true);
                    tasks_rv.setAdapter(tasksMatchSearchAdapter);
                } else {
                    getTasks();
                }

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        /* create a new category */
        create.addTextChangedListener(new TextWatcher() {

            DatabaseReference mRef;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if( -1 != s.toString().indexOf("\n") ){

                    String newTask = s.toString();
                    newTask = newTask.substring(0, newTask.length() - 1);

                    create.clearFocus();
                    create.setText("");
                    // Hide KeyBoard
                    InputMethodManager imm = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        imm = (InputMethodManager) TasksActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                    }
                    imm.hideSoftInputFromWindow(create.getWindowToken(),0);

                    // Current Date
                    SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
                    String dateAndTime = formatter.format(new Date());

                    Task task = new Task(categoryId, newTask, dateAndTime);

                    // add new task to firebase
                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    mRef = FirebaseDatabase.getInstance().getReference("users/"+uid+"/tasks");
                    String taskId = mRef.push().getKey();
                    task.setTaskId(taskId);
                    mRef.child(taskId).setValue(task);

                    // update num of tasks for category
                    mRef = FirebaseDatabase.getInstance().getReference("users/"+uid+"/categories/"+categoryId+"/numOfTasks");
                    mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            int num = Integer.parseInt(snapshot.getValue().toString());
                            mRef.setValue(num + 1);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });

                    Toast.makeText(TasksActivity.this, "new task added successfully", Toast.LENGTH_SHORT).show();
                }
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                database.getReference("users/"+uid+"/categories/"+categoryId).removeValue();
                database.getReference("users/"+uid+"/tasks").orderByChild("categoryId").equalTo(categoryId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            dataSnapshot.getRef().removeValue();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
                Intent intent = new Intent(TasksActivity.this, CategoriesActivity.class);
                startActivity(intent);
                Toast.makeText(TasksActivity.this, "Removed successfully", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        /* logout */
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(TasksActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    private void showOrHideViews(boolean isShown) {
        int showOrHide = 0;
        if(isShown) {
            category.setText(mainCategoryText);
            showOrHide = View.VISIBLE;
        } else {
            category.setText("Results:");
            showOrHide = View.GONE;
        }
        delete.setVisibility(showOrHide);
        create.setVisibility(showOrHide);
    }

    private void getTasks() {

        progressDialog = ProgressDialogGenerator.showLoadingDialog(TasksActivity.this);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        database.getReference("users").child(uid).child("tasks").orderByChild("categoryId").equalTo(categoryId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tasks = new Task[(int) snapshot.getChildrenCount()];
                int i = 0;
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    Task task = dataSnapshot.getValue(Task.class);
                    tasks[i] = task;
                    ++i;
                }
                TasksAdapter tasksAdapter = new TasksAdapter(getApplicationContext(), tasks, false);
                tasks_rv.setAdapter(tasksAdapter);

                progressDialog.hide();
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

    }
}