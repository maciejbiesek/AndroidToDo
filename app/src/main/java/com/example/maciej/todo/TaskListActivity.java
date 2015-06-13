package com.example.maciej.todo;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View.OnTouchListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class TaskListActivity extends ActionBarActivity {

    private TaskAdapter adapter;
    public static List<Task> taskList = new ArrayList<Task>();
    private int index;
    public AlarmManager am;
    public Intent alarmIntent;
    public PendingIntent pendingIntent;
    private static final String URL = "http://192.168.1.136:5000/tasks";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_list);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        Gson gson = new Gson();
        String json = sp.getString("list", "init");
        Type type = new TypeToken<ArrayList<Task>>(){}.getType();

        if (!json.equals("init")) {
            taskList = gson.fromJson(json, type);
        }

        initializeList();
        setAlarm();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ViewAnimator viewAnimator = (ViewAnimator) findViewById(R.id.animator);
        viewAnimator.setDisplayedChild(0);

        initializeList();
        setAlarm();
    }

    private void initializeList() {
        ListView list = (ListView) findViewById(R.id.task_list);
        adapter = new TaskAdapter(this);
        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Task task = adapter.getItem(position);
                showTask(task);
            }
        });

        if(isOnline()) {

            list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(TaskListActivity.this);

                    builder.setMessage("Jesteś tego pewien?")
                            .setTitle("Usuwanie zadania")
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Task task = adapter.getItem(position);
                                    String deleteURL = URL + "/" + task.getId();
                                    (new AsyncDeleteTask()).execute(deleteURL);
                                    dialog.cancel();
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });

                    builder.create().show();

                    return true;
                }
            });

            (new AsyncNetworkTasksProvider()).execute();
        }

        else {
            Toast.makeText(this, "Brak połączenia z internetem", Toast.LENGTH_SHORT).show();

            ViewAnimator animator = (ViewAnimator) findViewById(R.id.animator);
            animator.setDisplayedChild(1);

            adapter.setTasks(taskList);
            adapter.notifyDataSetChanged();
        }
    }

    private void showTask(Task task) {
        Intent i = new Intent(this, TaskDetailsActivity.class);

        i.putExtra(TaskDetailsActivity.TASK_KEY, task);
        startActivity(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_task_list, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_task: {
                if(isOnline()) {
                    addTask();
                }
                else Toast.makeText(this, "Brak połaczenia internetowego", Toast.LENGTH_SHORT).show();
                return true;
            }
            case R.id.settings: {
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
            }

            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    private void addTask() {
        Intent i = new Intent(this, AddTaskActivity.class);
        i.putExtra(AddTaskActivity.TASK_INDEX, index);
        startActivity(i);
    }

    public boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
                this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    public void setAlarm() {
        am = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmIntent = new Intent(TaskListActivity.this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(TaskListActivity.this, 0, alarmIntent, 0);

        Calendar alarmStartTime = Calendar.getInstance();

        Toast.makeText(this, "" + SettingsActivity.hour + ":" + SettingsActivity.minutes, Toast.LENGTH_SHORT).show();

        alarmStartTime.set(Calendar.HOUR_OF_DAY, SettingsActivity.hour);
        alarmStartTime.set(Calendar.MINUTE, SettingsActivity.minutes);
        alarmStartTime.set(Calendar.SECOND, 0);

        Calendar now = Calendar.getInstance();

        if (now.after(alarmStartTime)) {
            alarmStartTime.add(Calendar.DATE, 1);
        }
        am.setRepeating(AlarmManager.RTC, alarmStartTime.getTimeInMillis(), getInterval(), pendingIntent);
    }

    private int getInterval() {
        int days = 1;
        int hours = 24;
        int minutes = 60;
        int seconds = 60;
        int miliseconds = 1000;
        int repeatMS = days * hours * minutes * seconds * miliseconds;
        return repeatMS;
    }

    /*
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        taskList.clear();
        taskList = (List<Task>) savedInstanceState.getSerializable("savedList");

        Collections.sort(taskList, new Comparator<Task>() {
            public int compare(Task t1, Task t2) {

                return t2.getPriority() - t1.getPriority();
            }
        });
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("savedList", (java.io.Serializable) taskList);
    }
    */

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sp.edit();
        Gson gson = new Gson();
        String json = gson.toJson(taskList);
        editor.putString("list", json);
        editor.commit();
    }

    private class AsyncNetworkTasksProvider extends AsyncTask<String, Void, List<Task>> {

        private final ProgressDialog dialog = new ProgressDialog(TaskListActivity.this);

        @Override
        protected void onPostExecute(List<Task> result) {
            super.onPostExecute(result);
            taskList.clear();
            taskList.addAll(result);
            index = taskList.get(taskList.size() - 1).getId();

            Collections.sort(taskList, new Comparator<Task>() {
                public int compare(Task t1, Task t2) {

                    return t2.getPriority() - t1.getPriority();
                }
            });

            adapter.setTasks(taskList);
            adapter.notifyDataSetChanged();

            ViewAnimator animator = (ViewAnimator) findViewById(R.id.animator);
            animator.setDisplayedChild(1);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<Task> doInBackground(String... params) {
            try {
                return getTasks();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        public List<Task> getTasks() throws IOException, JSONException {
            List<Task> tasks = new ArrayList<Task>();

            String s = downloadTasks();
            JSONArray jArray = new JSONArray(s);
            for (int i = 0; i < jArray.length(); i++) {
                JSONObject jsonData = jArray.getJSONObject(i);

                Task task = new Task(jsonData.getInt("id"), jsonData.getString("title"), jsonData.getInt("priority"));
                tasks.add(task);
            }

            return tasks;
        }

        private String downloadTasks() throws IOException {
            InputStream is = null;

            try {
                java.net.URL url = new java.net.URL(URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.connect();
                is = conn.getInputStream();

                return readStream(is);
            }
            finally {
                if (is != null) {
                    is.close();
                }
            }
        }

        public String readStream(InputStream stream) throws IOException {
            BufferedReader r = new BufferedReader(new InputStreamReader(stream));
            StringBuilder total = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                total.append(line);
            }
            return total.toString();
        }
    }

    private class AsyncDeleteTask extends AsyncTask<String, Void, Integer> {

        private final ProgressDialog dialog = new ProgressDialog(TaskListActivity.this);

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            if (result == 200) {
                Toast.makeText(TaskListActivity.this, "Usunięto poprawnie", Toast.LENGTH_SHORT).show();
                (new AsyncNetworkTasksProvider()).execute();
            }
            else {
                Toast.makeText(TaskListActivity.this, "Ups, coś poszło nie tak", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(String... params) {
            return deleteTask(params[0]);
        }



        private int deleteTask(String deleteUrl) {
            int responseCode = 0;
            try {
                java.net.URL url = new java.net.URL(deleteUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("DELETE");
                responseCode = connection.getResponseCode();
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            return responseCode;

        }


    }

}
