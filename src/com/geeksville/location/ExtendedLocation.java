package com.geeksville.location;

import android.location.Location;

public class ExtendedLocation extends Location {

    float[] accel = new float[3];
    float vertSpeed;
    public ExtendedLocation() {
        super("");
        // TODO Auto-generated constructor stub
    }
    public float[] getAccel() {
        return accel;
    }
    public void setAccel(float[] accel) {
        this.accel = accel;
    }
    public float getVertSpeed() {
        return vertSpeed;
    }
    public void setVertSpeed(float vertSpeed) {
        this.vertSpeed = vertSpeed;
    }

    
}
