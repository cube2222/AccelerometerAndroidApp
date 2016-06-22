package com.miralak.basicaccelerometer.model;

public class Orientation {

    private final long timestamp;
    private final float roll;
    private final float pitch;
    private final float yaw;

    public Orientation(final float roll, final float pitch, final float yaw, final long timestamp) {
        this.roll = roll;
        this.pitch = pitch;
        this.yaw = yaw;
        this.timestamp = timestamp;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public float getRoll() {
        return roll;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
