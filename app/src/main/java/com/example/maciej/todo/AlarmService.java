package com.example.maciej.todo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;


@SuppressWarnings("ALL")
public class AlarmService extends Service {
    private static final int MID = 123456;
    private NotificationManager notificationManager;
    private PendingIntent pendingIntent;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @SuppressWarnings("static-access")
    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        String title;
        try {
            title = TaskListActivity.taskList.get(0).getTitle();
        }
        catch (Exception e) {
            title = "test";
        }

        Context context = this.getApplicationContext();
        notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        Intent mIntent = new Intent(this.getApplicationContext(), TaskListActivity.class);
        mIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        pendingIntent = PendingIntent.getActivity(context, 0, mIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new Notification(R.drawable.task3, "ToDo", System.currentTimeMillis());
        notification.setLatestEventInfo(context, "ToDo", title, pendingIntent);

        /*
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentTitle("Alaram Fired")
                .setContentText("Events To be PErformed")
                .setContentIntent(pendingIntent)
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000});
        */

        Log.e("Serwis", "działa");

        notificationManager.notify(MID, notification);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();;

    }

}
