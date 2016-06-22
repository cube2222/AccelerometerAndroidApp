package com.miralak.basicaccelerometer.activity;


import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.miralak.basicaccelerometer.R;
import com.miralak.basicaccelerometer.api.CassandraRestApi;
import com.miralak.basicaccelerometer.model.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import retrofit.RestAdapter;

public class CollectDataActivity extends ActionBarActivity {

    private String restURL;
    private String userID;
    private String selectedActivity;
    private Timer timer;
    private TimerTask startTimerTask;
    private TimerTask stopTimerTask;

    private TextView acceleration;
    private Spinner activitySpinner;
    private ToneGenerator toneG;
    private Button myStartButton;
    private Button myStopButton;

    private CassandraRestApi cassandraRestApi;

    private SensorManager sm;
    private Sensor accelerometer;
    private Sensor gyrocope;

    private long startTime;

    private final SensorEventListener accelerometerListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(final SensorEvent sensorEvent) {
            if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                final Acceleration acceleration = getAccelerationFromSensor(sensorEvent);
                updateTextView(acceleration);
                new SendAccelerationAsyncTask().execute(acceleration);

//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        api.sendAccelerationValues(acceleration);
//                    }
//                }).run();
            }
        }

        @Override
        public void onAccuracyChanged(final Sensor sensor, final int i) {
        }
    };

    private final SensorEventListener gyroListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(final SensorEvent sensorEvent) {
            if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                final Orientation orientation = getOrientationFromSensor(sensorEvent);
                new SendOrientationAsyncTask().execute(orientation);
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        api.sendAccelerationValues(gyro);
//                    }
//                }).run();
            }
        }

        @Override
        public void onAccuracyChanged(final Sensor sensor, final int i) {
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.collect_data);
        acceleration = (TextView) findViewById(R.id.acceleration);
        userID = ((EditText) findViewById(R.id.userID)).getText().toString();

        //Init accelerometer sensor
        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyrocope = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        initActivitySpinner();
        initRestApi();
        initTimerTasksWithAlertSound();
        initActionButtons();

        //Init an exit button
        Button myBackButton = (Button) findViewById(R.id.button_collect_exit);
        myBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopSensor();
                startTimerTask.cancel();
                stopTimerTask.cancel();
                timer.cancel();
                finish();
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
                userID = ((EditText) findViewById(R.id.userID)).getText().toString();
                selectedActivity = (String) activitySpinner.getSelectedItem();
                startTime = new Date().getTime();

                myStartButton.setVisibility(View.GONE);
                myStopButton.setVisibility(View.VISIBLE);

                //Sensor starts after 3 seconds
                timer.schedule(startTimerTask, 3000);

                //Sensor stops after 20 seconds
                timer.schedule(stopTimerTask, 20000);
            }
        });


        myStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopSensor();
                startTimerTask.cancel();
                stopTimerTask.cancel();
                timer.cancel();
                toneG.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 200);

                myStartButton.setVisibility(View.VISIBLE);
                myStopButton.setVisibility(View.GONE);

                finish();
            }
        });
    }

    private void initTimerTasksWithAlertSound() {
        toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        timer = new Timer();

        startTimerTask = new TimerTask() {
            @Override
            public void run() {
                toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
                startSensor();
            }
        };

        stopTimerTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD);
                        startTimerTask.cancel();
                        stopSensor();

                        myStartButton.setVisibility(View.VISIBLE);
                        myStopButton.setVisibility(View.GONE);
                    }
                });
            }
        };
    }

    private void initRestApi() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            restURL = extras.getString(StartActivity.URL);
        }

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(restURL)
                .build();

        cassandraRestApi = restAdapter.create(CassandraRestApi.class);
    }

    private void initActivitySpinner() {
        activitySpinner = (Spinner) findViewById(R.id.spinner_activity);

        final List activityList = new ArrayList();
        for(ActivityType activityType : ActivityType.values()){
            activityList.add(activityType.getLabel());
        }

        ArrayAdapter adapter = new ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                activityList
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        activitySpinner.setAdapter(adapter);

        activitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedActivity = activitySpinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                selectedActivity = activityList.get(0).toString();
            }

        });
    }

    private void startSensor() {
        sm.registerListener(accelerometerListener, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        sm.registerListener(gyroListener, gyrocope, SensorManager.SENSOR_DELAY_FASTEST);
    }

    private void stopSensor() {
        sm.unregisterListener(accelerometerListener);
        sm.unregisterListener(gyroListener);
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

    private Acceleration getAccelerationFromSensor(SensorEvent event) {
        long timestamp = (new Date()).getTime() + (event.timestamp - System.nanoTime()) / 1000000L;
        return new Acceleration(event.values[0], event.values[1], event.values[2], timestamp);
    }

    private Orientation getOrientationFromSensor(SensorEvent event) {
        long timestamp = (new Date()).getTime() + (event.timestamp - System.nanoTime()) / 1000000L;
        return new Orientation(event.values[0], event.values[1], event.values[2], timestamp);
    }

    /**
     * Asyncronous task to post request to a Rest API.
     */
    private class SendAccelerationAsyncTask extends AsyncTask<Acceleration, Void, Void> {

        @Override
        protected Void doInBackground(Acceleration... params) {
            try {
                TrainingAcceleration training = new TrainingAcceleration();
                training.setAcceleration(params[0]);
                training.setUserID(userID);
                training.setActivity(selectedActivity);
                training.setStarttime(startTime);

                cassandraRestApi.sendTrainingAccelerationValues(training);

            } catch(Exception e) {

            }
            return null;
        }
    }

    private class SendOrientationAsyncTask extends AsyncTask<Orientation, Void, Void> {

        @Override
        protected Void doInBackground(Orientation... params) {
            try {
                TrainingOrientation training = new TrainingOrientation();
                training.setOrientation(params[0]);
                training.setUserID(userID);
                training.setActivity(selectedActivity);
                training.setStarttime(startTime);

                cassandraRestApi.sendTrainingOrientationValues(training);

            } catch(Exception e) {

            }
            return null;
        }
    }
}
