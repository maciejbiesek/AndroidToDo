package com.example.maciej.todo;

import java.io.Serializable;
import android.content.Context;

public class Task implements Serializable {

    private int id;
    private int photoId;
    private String title;
    private int priority;

    public Task(int id, String title, int priority) {
        this.id = id;
        this.title = title;
        this.priority = priority;
        switch(priority) {
            case 1:
                this.photoId = R.drawable.task1;
                break;
            case 2:
                this.photoId = R.drawable.task2;
                break;
            case 3:
                this.photoId = R.drawable.task3;
                break;
        }
    }

    public int getId() { return id; }
    public int getPhotoId() { return photoId; }
    public String getTitle() { return title; }
    public int getPriority() { return priority; }
}
