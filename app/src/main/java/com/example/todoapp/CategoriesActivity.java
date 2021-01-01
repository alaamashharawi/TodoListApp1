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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.todoapp.Adapters.CategoriesAdapter;
import com.example.todoapp.Adapters.TasksAdapter;
import com.example.todoapp.Helpers.ProgressDialogGenerator;
import com.example.todoapp.Helpers.TasksSearch;
import com.example.todoapp.Models.Category;
import com.example.todoapp.Models.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class CategoriesActivity extends AppCompatActivity {

    EditText create,search;
    RecyclerView categories_rv;
    TextView lists, logout;
    Category[] categories;
    CategoriesAdapter categoriesAdapter;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        TasksSearch tasksSearch = new TasksSearch();
        create = findViewById(R.id.create);
        categories_rv = findViewById(R.id.categories_rv);
        lists = findViewById(R.id.lists);
        search = findViewById(R.id.search);
        logout = findViewById(R.id.logout);

        /* Recycler View */
        categories_rv.setLayoutManager(new LinearLayoutManager(this));
        getCategories();


        /* create a new category */
        create.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if( -1 != s.toString().indexOf("\n") ){

                    String newCategory = create.getText().toString();
                    newCategory = newCategory.substring(0,newCategory.length() - 1);
                    create.clearFocus();
                    create.setText("");

                    // Hide KeyBoard
                    InputMethodManager imm = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        imm = (InputMethodManager) CategoriesActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                    }
                    imm.hideSoftInputFromWindow(create.getWindowToken(),0);

                    Category category = new Category(newCategory);

                    // add new category to firebase
                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("users/"+uid+"/categories");
                    String categoryId = mRef.push().getKey();
                    category.setCategoryId(categoryId);
                    mRef.child(categoryId).setValue(category);

                    Toast.makeText(CategoriesActivity.this, "Added Successfully", Toast.LENGTH_SHORT).show();

                }
            }
        });

        /* search for task */
        search.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                boolean isEmptySearch = search.getText().toString().isEmpty();
                showOrHideViews(isEmptySearch);
                if(!isEmptySearch){
                    Task[] tasksMatchSearch = TasksSearch.getSearchTasks(search.getText().toString().toLowerCase());
                    TasksAdapter tasksMatchSearchAdapter = new TasksAdapter(getApplicationContext(), tasksMatchSearch, true);
                    categories_rv.setAdapter(tasksMatchSearchAdapter);
                } else {
                    getCategories();
                }

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        /* logout */
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(CategoriesActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

    }

    private void showOrHideViews(boolean isShown) {
        if(isShown) {
            lists.setText("Lists:");
            create.setVisibility(View.VISIBLE);
        } else {
            lists.setText("Results:");
            create.setVisibility(View.GONE);
        }
    }

    private void getCategories() {

        progressDialog = ProgressDialogGenerator.showLoadingDialog(CategoriesActivity.this);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        database.getReference("users").child(uid).child("categories").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categories = new Category[(int) snapshot.getChildrenCount()];
                int i = 0;
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    Category category = dataSnapshot.getValue(Category.class);
                    categories[i] = category;
                    ++i;
                }
                categoriesAdapter = new CategoriesAdapter(getApplicationContext(), categories);
                categories_rv.setAdapter(categoriesAdapter);

                progressDialog.hide();
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

    }
}