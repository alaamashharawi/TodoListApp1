package com.example.todoapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.todoapp.Models.Category;
import com.example.todoapp.R;
import com.example.todoapp.TasksActivity;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.ViewHolder> {

    private Category[] categories;
    private Context context;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView name;
        private final TextView numOfTasks;

        public ViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.name);
            numOfTasks = (TextView) view.findViewById(R.id.number_of_tasks);
        }

        public TextView getName() {
            return name;
        }

        public TextView getNumOfTasks() {
            return numOfTasks;
        }
    }

    public CategoriesAdapter(Context context, Category[] categories) {
        this.categories = categories;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.single_category_item, viewGroup, false);


        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        TextView name = viewHolder.getName();
        TextView numOfTasks = viewHolder.getNumOfTasks();

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, TasksActivity.class);
                intent.putExtra("categoryId",categories[position].getCategoryId());
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
        name.setText(categories[position].getName());
        numOfTasks.setText(categories[position].getNumOfTasks()+" tasks");

    }

    @Override
    public int getItemCount() {
        if(categories == null) return 0;
        return categories.length;
    }
}
