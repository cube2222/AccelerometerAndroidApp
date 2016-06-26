package com.miralak.basicaccelerometer.services;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import com.miralak.basicaccelerometer.api.RestApi;
import com.miralak.basicaccelerometer.model.*;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


import java.util.Date;


public class CollectingDataService extends IntentService {

    private final long DELAY_BEFORE_RUN = 5000;

    private String restUri = "";
    private String userID = "";
    private ActivityType activityType;
    private int acquisitionTime;
    private long startTime;
    private CollectingDataServiceActions.Mode mode = CollectingDataServiceActions.Mode.DEFAULT;

    private RestApi restApi;
    private CountDownTimer timer;
    private SensorManager sm;
    private Sensor accelerometer;
    private Sensor gyrocope;

    private Handler handler;
    private final ToneGenerator toneGenerator;

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (CollectingDataServiceActions.STOP_ACTION.equals(intent.getAction())) {
                toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD);
                stopGyroscope();
                stopAccelerometer();

            }
        }
    };

    private final SensorEventListener accelerometerListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(final SensorEvent sensorEvent) {
            if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                final Acceleration acceleration = getAccelerationFromSensor(sensorEvent);
                final TrainingAcceleration training = new TrainingAcceleration();
                training.setStarttime(startTime);
                training.setAcceleration(acceleration);
                training.setUserID(userID);
                training.setActivity(activityType.getLabel());
                //updateTextView(acceleration);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        restApi.sendTrainingAccelerationValues(training).enqueue(new Callback<TrainingAcceleration>() {
                            @Override
                            public void onResponse(Call<TrainingAcceleration> call, Response<TrainingAcceleration> response) {

                            }

                            @Override
                            public void onFailure(Call<TrainingAcceleration> call, Throwable t) {

                            }
                        });
                    }
                }).run();
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
                final TrainingOrientation training = new TrainingOrientation();
                training.setOrientation(orientation);
                training.setUserID(userID);
                training.setActivity(activityType.getLabel());
                training.setStarttime(startTime);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        restApi.sendTrainingOrientationValues(training).enqueue(new Callback<TrainingAcceleration>() {
                            @Override
                            public void onResponse(Call<TrainingAcceleration> call, Response<TrainingAcceleration> response) {

                            }

                            @Override
                            public void onFailure(Call<TrainingAcceleration> call, Throwable t) {

                            }
                        });
                    }
                }).run();
            }
        }

        @Override
        public void onAccuracyChanged(final Sensor sensor, final int i) {
        }
    };

    private final class CollectTimer extends CountDownTimer {

        public CollectTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(final long l) {

        }

        @Override
        public void onFinish() {
            toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD);
            stopGyroscope();
            stopAccelerometer();

        }
    }

    public CollectingDataService() {
        super("Collection Data Service");
        toneGenerator = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//        StrictMode.setThreadPolicy(policy);

        //Init accelerometer sensor
        sm = (SensorManager) getApplication().getSystemService(SENSOR_SERVICE);
        accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyrocope = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        handler = new Handler(Looper.getMainLooper());
        IntentFilter intentFilter = new IntentFilter(CollectingDataServiceActions.STOP_ACTION);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        final String action = intent.getAction();

        if (CollectingDataServiceActions.START_ACTION.equals(action)){
            restUri = intent.getStringExtra(CollectingDataServiceActions.EXTRA_REST_URI);
            userID = intent.getStringExtra(CollectingDataServiceActions.EXTRA_USER);
            acquisitionTime = intent.getIntExtra(CollectingDataServiceActions.EXTRA_ACQUISITION_TIME, 20000);
            activityType = (ActivityType) intent.getSerializableExtra(CollectingDataServiceActions.EXTRA_ACTIVITY_TYPE);
            mode = (CollectingDataServiceActions.Mode) intent.getSerializableExtra(CollectingDataServiceActions.EXTRA_MODE);
            startTime = new Date().getTime();

            init();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        stopGyroscope();
//        stopAccelerometer();
        unregisterReceiver(receiver);
    }

    private void init() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(restUri)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        restApi = retrofit.create(RestApi.class);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Start sensors
                switch (mode) {
                    case GYRO:
                        startGyroscope(SensorManager.SENSOR_DELAY_NORMAL);
                    case DEFAULT:
                        startAccelerometer(SensorManager.SENSOR_DELAY_NORMAL);
                        break;
                    case FAST:
                        startGyroscope(SensorManager.SENSOR_DELAY_FASTEST);
                        startAccelerometer(SensorManager.SENSOR_DELAY_FASTEST);
                        break;
                }
                timer = new CollectTimer(acquisitionTime, acquisitionTime / 10);
                timer.start();
                toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
            }
        }, DELAY_BEFORE_RUN);
    }

    private void startGyroscope(final int sensorMode) {
        sm.registerListener(accelerometerListener, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        sm.registerListener(gyroListener, gyrocope, sensorMode);
    }

    private void stopGyroscope() {
        sm.unregisterListener(gyroListener);
    }

    private void startAccelerometer(final int sensorMode) {
        sm.registerListener(accelerometerListener, accelerometer, sensorMode);
    }

    private void stopAccelerometer() {
        sm.unregisterListener(accelerometerListener);
    }

    private Acceleration getAccelerationFromSensor(SensorEvent event) {
        long timestamp = (new Date()).getTime() + (event.timestamp - System.nanoTime()) / 1000000L;
        return new Acceleration(event.values[0], event.values[1], event.values[2], timestamp);
    }

    private Orientation getOrientationFromSensor(SensorEvent event) {
        long timestamp = (new Date()).getTime() + (event.timestamp - System.nanoTime()) / 1000000L;
        return new Orientation(event.values[0], event.values[1], event.values[2], timestamp);
    }
}
