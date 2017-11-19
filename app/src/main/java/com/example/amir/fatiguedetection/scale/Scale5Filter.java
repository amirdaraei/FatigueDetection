package com.example.amir.fatiguedetection.scale;


import com.example.amir.fatiguedetection.RetinexFilter;

public final class Scale5Filter extends RetinexFilter {
    
    public Scale5Filter() {
        super(
                new double[] { 0, 255 }, // vValIn
                new double[] { 0, 255 }, // vValOut
                
                new double[] { 0, 59, 202, 255 }, // rValIn
                new double[] { 0, 54, 210, 255 }, // rValOut
                
                new double[] { 0, 27, 196, 255 }, // gValIn
                new double[] { 0, 21, 207, 255 }, // gValOut
                
                new double[] { 0, 35, 205, 255 },  // bValIn
                new double[] { 0, 25, 227, 255 }); // bValOut
    }
}
