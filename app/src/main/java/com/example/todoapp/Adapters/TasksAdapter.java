package com.example.todoapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todoapp.Models.Task;
import com.example.todoapp.R;
import com.example.todoapp.SingleTask;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class TasksAdapter extends RecyclerView.Adapter<TasksAdapter.ViewHolder> {

    private Task[] tasks;
    private Context context;
    private boolean withCategory;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView category;
        private final TextView task;
        private final CheckBox checkbox;

        public ViewHolder(View view) {
            super(view);
            checkbox = view.findViewById(R.id.checkbox);
            task = view.findViewById(R.id.task);
            category = view.findViewById(R.id.category);
        }

        public CheckBox getCheckbox() {
            return checkbox;
        }

        public TextView getCategory() {
            return category;
        }

        public TextView getTask() {
            return task;
        }
    }

    public TasksAdapter(Context context, Task[] tasks, boolean withCategory) {
        this.tasks = tasks;
        this.context = context;
        this.withCategory = withCategory;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.single_task_item, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        TextView task = viewHolder.getTask();
        CheckBox checkbox = viewHolder.getCheckbox();
        TextView category = viewHolder.getCategory();

        task.setText(tasks[position].getTitle());

        // For search results
        if(withCategory) {
            String categoryId = tasks[position].getCategoryId();
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseDatabase.getInstance().getReference("users/"+uid+"/categories/"+categoryId+"/name").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String categoryStr = snapshot.getValue().toString();
                    category.setText(categoryStr);
                    category.setVisibility(View.VISIBLE);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        } else {
            // For list viewing without search
            category.setVisibility(View.GONE);
        }

        if(tasks[position].isDone()) {
            task.setPaintFlags(checkbox.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            checkbox.setChecked(true);
        }

        checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    task.setPaintFlags(checkbox.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                } else {
                    task.setPaintFlags(checkbox.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                }
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                FirebaseDatabase.getInstance().getReference("users/"+uid+"/tasks/"+tasks[position].getTaskId()+"/done")
                        .setValue(isChecked);
            }
        });

        task.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SingleTask.class);
                intent.putExtra("taskId",tasks[position].getTaskId());
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        if(tasks == null) return 0;
        return tasks.length;
    }
}