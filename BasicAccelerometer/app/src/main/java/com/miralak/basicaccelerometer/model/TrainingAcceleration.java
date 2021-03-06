package com.miralak.basicaccelerometer.model;


public class TrainingAcceleration {
    private String userID;
    private String activity;
    private long starttime;
    private Acceleration acceleration;


    public void setStarttime(long starttime) {
        this.starttime = starttime;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public void setAcceleration(Acceleration acceleration) {
        this.acceleration = acceleration;
    }

    @Override
    public String toString() {
        return "TrainingAcceleration{" +
                "userID='" + userID + '\'' +
                ", starttime='" + starttime + '\'' +
                ", activity='" + activity + '\'' +
                ", acceleration=" + acceleration +
                '}';
    }
}
