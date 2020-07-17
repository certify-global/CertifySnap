package com.certify.snap.arcface.model;

import android.graphics.Rect;

public class TemperatureRect {
    private int trackId;
    private Rect rect;

    public TemperatureRect(int trackId, Rect rect){
        this.trackId = trackId;
        this.rect = rect;
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
}
