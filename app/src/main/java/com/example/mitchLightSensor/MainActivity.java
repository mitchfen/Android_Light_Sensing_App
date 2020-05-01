package com.example.mitchLightSensor;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;
import java.util.Timer;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    // Variables used across multiple functions
    private TextView textView1;
    private Timer timer;
    private SensorManager sensorManager;
    private Sensor lightSensor;
    public final String internalFile = "internalData.txt";
    private String currentFile;
    private AlertDialog.Builder alertCreator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        alertCreator = new AlertDialog.Builder(MainActivity.this);
        textView1 = findViewById(R.id.textView1);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        assert sensorManager != null;
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        Toolbar toolBar = findViewById(R.id.tbrMain);
        setSupportActionBar(toolBar);
        toolBar.setNavigationIcon(R.mipmap.lightbulb);
        String currentDirectory = getFilesDir().getAbsolutePath();
        currentFile = currentDirectory + File.separator + internalFile;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // Onclick listeners for the context menu about item
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == R.id.About) {
            aboutDialogBuilder();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void aboutDialogBuilder() {

        alertCreator.setTitle("Mitch's Light Sensor");
        alertCreator.setMessage("Author: Mitchell Fenner\nDate: 4/25/2020\nVersion: 1.0");
        alertCreator.show();
    }

    //---TIMER STUFF-----------------------------------------------------------------------------
    public void startTimer(View v) {

        // Destroy current timer and establish new one if button is pressed while timer is running.
        if (timer != null) timer.cancel();
        // Create the timer and schedule it
        timer = new Timer();
        timer.schedule( new mitchTimerTask(this), (shared.Data.timePause * 1000), (shared.Data.timePause * 1000));

        // Feedback on starting timer
        Toast.makeText(getApplicationContext(),"Started polling light sensor.", Toast.LENGTH_SHORT).show();
    }

    public void stopTimer(View v) {

        // Kill the timer
        if (timer != null) timer.cancel();
        timer = null;

        // Feedback on stopping timer
        Toast.makeText(getApplicationContext(),"Stopped polling light sensor.", Toast.LENGTH_SHORT).show();
    }

    // DO STUFF BASED ON TIMER
    @SuppressLint("HandlerLeak")
    public Handler goGoGadgetTimer = new Handler() {

        @Override
        public void handleMessage(Message msg)
        {

            shared.Data.printLinesTracker++;
            // Clear the screen of light values after 45 have been displayed.
            if (shared.Data.printLinesTracker >= 45) {
                textView1.setText("");
                shared.Data.printLinesTracker = 0;
            }

            // Write continuously to screen by appending new value to older values
            // Its not glamorous but it works!
            String currentString = (String) textView1.getText();
            String lightString = String.valueOf(shared.Data.lightValue);
            String printString = currentString + "\n" + lightString;
            // Keep track of how often I do this so I can clear before it gets too long
            shared.Data.printLinesTracker++;
            textView1.setText(printString);
        }
    };

    //---SENSOR STUFF------------------------------------------------------------------------------
    @Override
    public void onSensorChanged(SensorEvent event) {

        shared.Data.lightValue = event.values[0];
        // Write sensor data to the file
        // Disable writing for now
        //writeToFile(true, String.valueOf(shared.Data.lightValue));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

        // Do nothing - but this method must be overriden
    }

    @Override
    protected void onResume() {

        super.onResume();
        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {

        // Unregister the sensor when the activity pauses.
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    //---FILE HANDLING STUFF-----------------------------------------------------------------------
    public void writeToFile(boolean appendFlag, String writeMe) {

        FileOutputStream fileOut;
        PrintStream streamOut;

        try {

            if (appendFlag) {
                fileOut = openFileOutput(internalFile, MODE_APPEND);
            }
            else {
                fileOut = openFileOutput(internalFile, MODE_PRIVATE);
            }

            streamOut = new PrintStream(fileOut);

            // Write line then close file
            streamOut.println(writeMe);
            fileOut.close();
            streamOut.close();
        }
        catch (IOException e) {
            redAlert("I/O error on file '" + currentFile + "'.");
        }
    }

    // TODO:
    // This method needs some work - and it is not currently being called anywhere
    public String readFile() {

        FileInputStream file;
        Scanner fileDataIn;
        StringBuilder stringbuilder = null;

        try {

            // Assign file and scanner objects
            file = openFileInput(internalFile);
            fileDataIn = new Scanner(file);

            // Loop to read lines from input file
            while (fileDataIn.hasNextLine())
            {
                stringbuilder.append(fileDataIn.nextLine());
            }
            file.close();

        }
        catch (IOException e) {
            redAlert("I/O error on file '" + currentFile + "'.");
        }
        // Returns a long string.. need to work on this before using this method
        // there are much better ways
        assert stringbuilder != null;
        return(stringbuilder.toString());
    }

    // This alert function is only used when an IO error occurs on the internal file.
    public void redAlert(String message)
    {
        alertCreator.setMessage(message);
        alertCreator.create().show();
    }
}