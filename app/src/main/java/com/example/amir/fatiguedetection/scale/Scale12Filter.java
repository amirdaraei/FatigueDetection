package com.example.amir.fatiguedetection.scale;

import com.example.amir.fatiguedetection.RetinexFilter;

/**
 * Created by Amir on 23/01/2017.
 */

public class Scale12Filter extends RetinexFilter {
    public Scale12Filter(){
        super(
                new double[] { 0, 23, 157, 255 }, // vValIn
                new double[] { 0, 20, 210, 255 }, // vValOut
                new double[] { 0, 69, 213, 255 }, // rValIn
                new double[] { 0, 80, 240, 255 }, // rValOut
                new double[] { 0, 52, 189, 255 }, // gValIn
                new double[] { 0, 47, 196, 255 }, // gValOut
                new double[] { 0, 41, 231, 255 }, // bValIn
                new double[] { 0, 46, 228, 255 }); // bValOut

    }
}
