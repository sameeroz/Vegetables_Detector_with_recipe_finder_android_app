package com.sameer.vegetablesdetector;

import android.graphics.RectF;

class DetectionResult
{
    RectF boundingBox;
    String text;

    public DetectionResult(RectF boundingBox, String text) {
        this.boundingBox = boundingBox;
        this.text = text;
    }
}