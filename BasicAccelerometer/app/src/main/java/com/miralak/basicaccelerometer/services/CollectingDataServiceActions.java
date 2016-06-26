package com.miralak.basicaccelerometer.services;

public interface CollectingDataServiceActions {
    String START_ACTION = "com.miralak.basicaccelerometer.actions.START_ACTION";
    String STOP_ACTION = "com.miralak.basicaccelerometer..actions.STOP_ACTION";
    String EXTRA_ACQUISITION_TIME = "com.miralak.basicaccelerometer.extra.EXTRA_ACQUISITION_TIME";
    String EXTRA_REST_URI = "pl.tracking.androidapp.extra.EXTRA_REST_URI";
    String EXTRA_ACTIVITY_TYPE = "pl.tracking.androidapp.extra.EXTRA_ACTIVITY_TYPE";
    String EXTRA_MODE= "pl.tracking.androidapp.extra.EXTRA_MODE";
    String EXTRA_USER= "pl.tracking.androidapp.extra.USER";

    enum Mode {
        DEFAULT,
        GYRO,
        FAST
    }
}
