package com.example.maciej.todo;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.TimePicker;
import android.widget.Toast;

/**
 * Created by Maciej on 2015-06-12.
 */
@SuppressWarnings("ALL")
public class SettingsActivity extends Activity {

    private TimePicker timePicker;

    public static int hour = 12;
    public static int minutes = 00;

    static final int TIME_DIALOG_ID = 999;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        showDialog(TIME_DIALOG_ID);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case TIME_DIALOG_ID:
                return new TimePickerDialog(this, timePickerListener, hour, minutes, false);
        }
        return null;
    }

    private TimePickerDialog.OnTimeSetListener timePickerListener = new TimePickerDialog.OnTimeSetListener() {

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            hour = hourOfDay;
            minutes = minute;

            //Toast.makeText(SettingsActivity.this, "" + hour + ":" + minutes, Toast.LENGTH_SHORT).show();
            finish();
        }
    };
}
