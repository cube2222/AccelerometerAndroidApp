package com.miralak.basicaccelerometer.model;

public class TrainingOrientation {
    private String userID;
    private String activity;
    private long starttime;
    private Orientation orientation;


    public void setStarttime(long starttime) {
        this.starttime = starttime;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
    }

    @Override
    public String toString() {
        return "TrainingAcceleration{" +
                "userID='" + userID + '\'' +
                ", starttime='" + starttime + '\'' +
                ", activity='" + activity + '\'' +
                ", orientation=" + orientation +
                '}';
    }
}
