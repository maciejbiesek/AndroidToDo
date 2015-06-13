package com.example.maciej.todo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TaskAdapter extends BaseAdapter {

    private List<Task> taskList = new ArrayList<>();
    private Context context;

    public TaskAdapter(Context context) {
        this.context = context;
    }

    public void setTasks(Collection<Task> tasks) {
        taskList.clear();
        taskList.addAll(tasks);
    }

    public List<Task> getAllTasks() {
        return taskList;
    }

    @Override
    public int getCount() {
        return taskList.size();
    }

    @Override
    public Task getItem(int position) {
        return taskList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View taskView;

        if (convertView == null) {
            taskView = LayoutInflater.from(context).inflate(R.layout.task_row, parent, false);
        } else {
            taskView = convertView;
        }

        bindTaskToView(getItem(position), taskView);

        return taskView;
    }

    private void bindTaskToView(Task task, View taskView) {
        ImageView taskPhoto = (ImageView) taskView.findViewById(R.id.task_photo);
        taskPhoto.setImageResource(task.getPhotoId());

        TextView tagLabel = (TextView) taskView.findViewById(R.id.task_label);
        tagLabel.setText(task.getTitle());

    }

}
