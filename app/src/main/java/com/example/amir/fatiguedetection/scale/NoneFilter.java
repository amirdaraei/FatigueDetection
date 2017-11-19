package com.example.amir.fatiguedetection.scale;

import com.example.amir.fatiguedetection.interFace.Filter;

import org.opencv.core.Mat;

/**
 * Created by Amir on 23/01/2017.
 */

public class NoneFilter implements Filter {
    @Override
    public void apply(Mat src, Mat dst) {
        //Do Nothing
    }
}
