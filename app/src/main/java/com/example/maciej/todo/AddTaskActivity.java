package com.example.maciej.todo;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;

public class AddTaskActivity extends ActionBarActivity {

    private static final String URL = "http://192.168.1.136:5000/tasks";
    public static final String TASK_INDEX = "index";
    private int lastIndex;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_task);

        Intent i = getIntent();
        lastIndex = i.getIntExtra(TASK_INDEX, 0) + 1;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_add, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.add: {
                addTask();
                finish();
                return true;
            }

            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    private void addTask() {
        final EditText taskTitleEditText = (EditText) findViewById(R.id.task_add_title);
        final RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioList);

        String title = taskTitleEditText.getText().toString();

        int selectedId = radioGroup.getCheckedRadioButtonId();
        final RadioButton checkedRatio = (RadioButton) findViewById(selectedId);
        int which = radioGroup.indexOfChild(checkedRatio);

        Task task = new Task (lastIndex, title, which);

        if (!title.isEmpty()) {
            if (isOnline()) {
                (new AsyncNetworkTasksProvider()).execute(createJSON(task));
                Toast.makeText(AddTaskActivity.this, "Dodano nowe zdarzenie", Toast.LENGTH_SHORT).show();
                finish();
            }
            else {
                Toast.makeText(AddTaskActivity.this, "Brak połączenia z internetem", Toast.LENGTH_SHORT).show();
            }
        }
        else Toast.makeText(AddTaskActivity.this, "Najpierw podaj tytuł!", Toast.LENGTH_SHORT).show();

    }

    public String sendToServer(String jsonObject) throws IOException {
        InputStream is = null;

        try {
            java.net.URL url = new java.net.URL(URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            OutputStreamWriter os = new OutputStreamWriter(conn.getOutputStream());
            os.write(jsonObject);
            os.flush();
            is = conn.getInputStream();

            return readStream(is);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (is != null) {
                is.close();
            }
        }
        return jsonObject;
    }

    public String createJSON(Task task) {
        JSONObject jObject = new JSONObject();
        try {
            jObject.put("id", task.getId());
            jObject.put("title", task.getTitle());
            jObject.put("priority", task.getPriority());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jObject.toString();
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

    public boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
                this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }




    private class AsyncNetworkTasksProvider extends AsyncTask<String, Void, String> {

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                return sendToServer(params[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

    }

}
