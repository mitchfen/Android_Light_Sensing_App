package com.example.mitchLightSensor;

import java.util.TimerTask;

public class mitchTimerTask extends TimerTask {

    private MainActivity parent;

    // Parameterized Constructor
    mitchTimerTask(MainActivity parent)
    {
        this.parent = parent;
    }

    // What to do when running
    public void run(){
        parent.goGoGadgetTimer.sendEmptyMessage(0);
    }
}
