package com.miralak.basicaccelerometer.activity;


import android.content.Intent;
import android.content.SharedPreferences;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.miralak.basicaccelerometer.R;
import com.miralak.basicaccelerometer.SettingsKey;
import com.miralak.basicaccelerometer.model.Acceleration;
import com.miralak.basicaccelerometer.model.ActivityType;
import com.miralak.basicaccelerometer.services.CollectingDataService;
import com.miralak.basicaccelerometer.services.CollectingDataServiceActions;

public class CollectDataActivity extends ActionBarActivity {

    private TextView acceleration;
    private Spinner activitySpinner;
    private Button myStartButton;
    private Button myStopButton;


    private SharedPreferences prefs;
    private ActivityType selectedActivity = ActivityType.DOWNSTAIRS;



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.collect_data);
        acceleration = (TextView) findViewById(R.id.acceleration);

        prefs = getSharedPreferences(SettingsKey.appSetting, MODE_PRIVATE);

        initActivitySpinner();
        initActionButtons();

        //Init an exit button
        Button myBackButton = (Button) findViewById(R.id.button_collect_exit);
        myBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startTimerTask.cancel();
//                stopTimerTask.cancel();
//                timer.cancel();
//                finish();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_accelerometer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initActionButtons() {
        myStartButton = (Button) findViewById(R.id.button_start_training);
        myStopButton = (Button) findViewById(R.id.button_stop_training);

        myStartButton.setVisibility(View.VISIBLE);
        myStopButton.setVisibility(View.GONE);

        myStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(getBaseContext(), CollectingDataService.class);
                intent.setAction(CollectingDataServiceActions.START_ACTION);
                intent.putExtra(CollectingDataServiceActions.EXTRA_USER, prefs.getString(SettingsKey.userID, ""));
                intent.putExtra(CollectingDataServiceActions.EXTRA_MODE, CollectingDataServiceActions.Mode.DEFAULT);
                intent.putExtra(CollectingDataServiceActions.EXTRA_ACTIVITY_TYPE, selectedActivity);
                intent.putExtra(CollectingDataServiceActions.EXTRA_REST_URI, prefs.getString(SettingsKey.restUri, ""));
                intent.putExtra(CollectingDataServiceActions.EXTRA_ACQUISITION_TIME, prefs.getInt(SettingsKey.acquisitionTime, 20000));
                startService(intent);

                myStartButton.setVisibility(View.GONE);
                myStopButton.setVisibility(View.VISIBLE);

//                userID = ((EditText) findViewById(R.id.userID)).getText().toString();
//                selectedActivity = (String) activitySpinner.getSelectedItem();
            }
        });


        myStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBroadcast(new Intent(CollectingDataServiceActions.STOP_ACTION));
                myStartButton.setVisibility(View.VISIBLE);
                myStopButton.setVisibility(View.GONE);

                //finish();
            }
        });
    }

    private void initActivitySpinner() {
        activitySpinner = (Spinner) findViewById(R.id.spinner_activity);

        ArrayAdapter<ActivityType> adapter = new ArrayAdapter<ActivityType> (
                this,
                android.R.layout.simple_spinner_item,
                ActivityType.values()
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        activitySpinner.setAdapter(adapter);

        activitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedActivity = (ActivityType) activitySpinner.getSelectedItem();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                selectedActivity = ActivityType.DOWNSTAIRS;
            }

        });
    }

    private void updateTextView(final Acceleration capturedAcceleration) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                acceleration.setText("X:" + capturedAcceleration.getX() +
                        "\nY:" + capturedAcceleration.getY() +
                        "\nZ:" + capturedAcceleration.getZ() +
                        "\nTimestamp:" + capturedAcceleration.getTimestamp());
            }
        });
    }
}
