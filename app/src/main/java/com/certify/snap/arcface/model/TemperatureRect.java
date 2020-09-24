package com.certify.snap.arcface.model;

import android.graphics.Rect;

public class TemperatureRect {
    private int trackId;
    private Rect rect;
    private float distance;

    public TemperatureRect(int trackId, Rect rect, float distance){
        this.trackId = trackId;
        this.rect = rect;
        this.distance = distance;
    }

    public int getTrackId() {
        return trackId;
    }

    public void setTrackId(int trackId) {
        this.trackId = trackId;
    }

    public Rect getRect() {
        return rect;
    }

    public void setRect(Rect rect) {
        this.rect = rect;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }
}
