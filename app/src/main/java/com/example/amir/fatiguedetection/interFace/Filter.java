package com.example.amir.fatiguedetection.interFace;

import org.opencv.core.Mat;

/**
 * Created by Amir on 23/01/2017.
 */

public interface Filter {
    public abstract void apply(final Mat src, final Mat dst);
}
