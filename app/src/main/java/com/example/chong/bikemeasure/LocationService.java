package com.example.chong.bikemeasure;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

//Create a bound service when you want to interact with the service from activities and other components in your application
public class LocationService extends Service implements LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{


    private static final long INTERVAL = 1000 * 2;
    private static final long FASTEST_INTERVAL = 1000 * 1;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation, lStart, lEnd;
    static double distance = 0;
    double speed;
    private final IBinder mBinder = new LocalBinder();

    //speed file.
    File file4;
    FileOutputStream fos4;

    File speedir = new File("/sdcard/Bike Project", "Speed");
    String fileName;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        fileName = intent.getStringExtra("fileName");


        file4 = new File("/sdcard/Bike Project/Speed/" + fileName +".csv");
        try {
            fos4 = new FileOutputStream(file4, false);
        }catch(FileNotFoundException e ){
            e.printStackTrace();
        }

        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
        return mBinder;
    }


    @Override
    public boolean onUnbind(Intent intent) {
        try{
            fos4.close();
        }catch(IOException e ){
            e.printStackTrace();
        }

        stopLocationUpdates();
        if (mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
        lStart = null;
        lEnd = null;
        distance = 0;
        return super.onUnbind(intent);
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onConnected(Bundle bundle) {
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        } catch (SecurityException e) {
        }
    }


    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
        distance = 0;
    }


    @Override
    public void onConnectionSuspended(int i) {

    }


    @Override
    public void onLocationChanged(Location location) {

        MainActivity.locate.dismiss();

        SimpleDateFormat dateFormat2 = new SimpleDateFormat("hh:mm:ss:SS");
        Calendar cal;
        String time;

        cal = Calendar.getInstance();
        time = dateFormat2.format(cal.getTime());


        mCurrentLocation = location;
        if (lStart == null) {
            lStart = mCurrentLocation;
            lEnd = mCurrentLocation;
        } else
            lEnd = mCurrentLocation;

        //Calling the method below updates the  live values of distance and speed to the TextViews.
//        updateUI();
        //calculating the speed with getSpeed method it returns speed in m/s so we are converting it into kmph
        speed = location.getSpeed() * 18 / 5;

        String entry =  time + "," + Double.toString(speed) + "\n";

        try {
            fos4.write(entry.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public class LocalBinder extends Binder {

        public LocationService getService() {
            return LocationService.this;
        }


    }

    //The live feed of Distance and Speed are being set in the method below .
//    private void updateUI() {
//        if (MainActivity.p == 0) {
//            distance = distance + (lStart.distanceTo(lEnd) / 1000.00);
//            MainActivity.endTime = System.currentTimeMillis();
//            long diff = MainActivity.endTime - MainActivity.startTime;
//            diff = TimeUnit.MILLISECONDS.toMinutes(diff);
//            MainActivity.time.setText("Total Time: " + diff + " minutes");
//            if (speed > 0.0)
//                MainActivity.speed.setText("Current speed: " + new DecimalFormat("#.##").format(speed) + " km/hr");
//            else
//                MainActivity.speed.setText(".......");
//
//            MainActivity.dist.setText(new DecimalFormat("#.###").format(distance) + " Km's.");
//
//            lStart = lEnd;
//
//        }
//
//    }



}
