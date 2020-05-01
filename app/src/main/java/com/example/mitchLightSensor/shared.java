package com.example.mitchLightSensor;

public enum shared {

        Data;
        public final int timePause = 1; // Time delay between timer task operations
        public double lightValue = -99.0; // If -99 is displayed then obviously an error
        public int printLinesTracker = 0; // Used to help keep track of lines on the screen
}
