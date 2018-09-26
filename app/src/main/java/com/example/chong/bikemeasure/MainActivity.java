package com.example.chong.bikemeasure;

import android.app.ActionBar;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback, SensorEventListener {
    Calendar cal;

    private SurfaceHolder surfaceHolder;
    private SurfaceView surfaceView;
    public MediaRecorder mrec = new MediaRecorder();
    private Button startRecording = null;
    private boolean recording = false;
    SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd hh mm ss");

    //For video
    File video;
    private Camera mCamera;

    //For accel
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    File file;
    FileOutputStream fos;

    //For gyroscope- angular velocity
    private Sensor mGyroscope;
    File file2;
    FileOutputStream fos2;

    //For compass-
    private Sensor mCompass;
    File file3;
    FileOutputStream fos3;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //Create directories if non-existent.
        File bikedir = new File("/sdcard","Bike Project");
        bikedir.mkdirs();

        File videodir = new File("/sdcard/Bike Project", "Video");
        File gyrodir = new File("/sdcard/Bike Project", "Gyro");
        File compassdir = new File("/sdcard/Bike Project", "Compass");
        File acceldir = new File("/sdcard/Bike Project", "Accel");


        videodir.mkdirs();
        gyrodir.mkdirs();
        compassdir.mkdirs();
        acceldir.mkdirs();


        //window features
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        //hide action bar.
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        setContentView(R.layout.activity_main);
        startRecording = (Button)findViewById(R.id.button);
        mCamera = Camera.open();
        surfaceView = (SurfaceView) findViewById(R.id.surface_camera);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);


        //Accelerometer
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        //Gyrosocpe
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        //Compass
        mCompass = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);


        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                TextView tv = findViewById(R.id.text);
                Button button = findViewById(R.id.button);

                if(recording){
                    recording = false;

                    tv.setVisibility(View.INVISIBLE);
                    button.setText("START");

                    mrec.stop();
                    mrec.release();
                    mrec = null;

                    //sensors
                    mSensorManager.unregisterListener((SensorEventListener) MainActivity.this);
                    try{
                        fos.close();
                        fos2.close();
                        fos3.close();
                    }catch(IOException e ){
                        e.printStackTrace();
                    }

                }else{
                    recording = true;


                    tv.setVisibility(View.VISIBLE);
                    button.setText("STOP");

                    try {
                        startRecording();
                    } catch (Exception e) {
                        String message = e.getMessage();
                        Log.i(null, "Problem Start"+message);
                        mrec.release();
                    }

                    //accel file.
                    cal = Calendar.getInstance();
                    String fileName = dateFormat.format(cal.getTime());
                    file = new File("/sdcard/Bike Project/Accel/" + fileName + ".csv");
                    try{
                        fos = new FileOutputStream(file, false);
                    }catch(FileNotFoundException e){
                        e.printStackTrace();
                    }
                    mSensorManager.registerListener(MainActivity.this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

                    //Gyro file.
                    file2 = new File("/sdcard/Bike Project/Gyro/" + fileName +".csv");
                    try{
                        fos2 = new FileOutputStream(file2, false);
                    }catch(FileNotFoundException e){
                        e.printStackTrace();
                    }
                    mSensorManager.registerListener(MainActivity.this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);


                    //compass file
                    file3= new File("/sdcard/Bike Project/Compass/" + fileName+".csv");
                    try{
                        fos3= new FileOutputStream(file3, false);
                    }catch(FileNotFoundException e){
                        e.printStackTrace();
                    }
                    mSensorManager.registerListener(MainActivity.this, mCompass, SensorManager.SENSOR_DELAY_NORMAL);
                }
            }
        });
    }


    protected void startRecording() throws IOException
    {
        cal = Calendar.getInstance();
        String fileName = dateFormat.format(cal.getTime());
        mrec = new MediaRecorder();  // Works well
        mCamera.unlock();

        mrec.setCamera(mCamera);

        mrec.setPreviewDisplay(surfaceHolder.getSurface());
        mrec.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mrec.setAudioSource(MediaRecorder.AudioSource.MIC);

        mrec.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_480P));
        mrec.setPreviewDisplay(surfaceHolder.getSurface());
        mrec.setOutputFile("/sdcard/Bike Project/Video/" + fileName +".mp4");

        mrec.prepare();
        mrec.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
//        if (mCamera != null){
//            Policy.Parameters params = (Policy.Parameters) mCamera.getParameters();
//            mCamera.setParameters((Camera.Parameters) params);
//        }
//        else {
//            Toast.makeText(getApplicationContext(), "Camera not available!", Toast.LENGTH_LONG).show();
//            finish();
//        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera.stopPreview();
        mCamera.release();
        finish();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        Sensor mySensor = sensorEvent.sensor;
        SimpleDateFormat dateFormat2 = new SimpleDateFormat("hh:mm:ss:SS");
        Calendar cal;
        String time;


        if(mySensor.getType() == Sensor.TYPE_ACCELEROMETER){
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            cal = Calendar.getInstance();
            time = dateFormat2.format(cal.getTime());

            String entry =  time + "," + Float.toString(x) + "," +
                    Float.toString(y) + "," +
                    Float.toString(z) + "\n";

            try {
                fos.write(entry.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        else if(mySensor.getType() == Sensor.TYPE_GYROSCOPE){
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            cal = Calendar.getInstance();
            time = dateFormat2.format(cal.getTime());

            String entry =  time + "," + Float.toString(x) + "," +
                    Float.toString(y) + "," +
                    Float.toString(z) + "\n";

            try {
                fos2.write(entry.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        else if(mySensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            cal = Calendar.getInstance();
            time = dateFormat2.format(cal.getTime());

            String entry =  time + "," + Float.toString(x) + "," +
                    Float.toString(y) + "," +
                    Float.toString(z) + "\n";

            try {
                fos3.write(entry.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}