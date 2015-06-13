package com.example.maciej.todo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

public class TaskDetailsActivity extends ActionBarActivity {

    public static final String TASK_KEY = "task";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_details);

        Intent i = getIntent();
        Task task = (Task) i.getExtras().getSerializable(TASK_KEY);

        showTask(task);
    }

    private void showTask(Task task) {
        ImageView photo = (ImageView) findViewById(R.id.photo);
        TextView title = (TextView) findViewById(R.id.title);

        photo.setImageResource(task.getPhotoId());
        title.setText(task.getTitle());

    }

}
