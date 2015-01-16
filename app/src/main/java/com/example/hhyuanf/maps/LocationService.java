package com.example.hhyuanf.maps;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.location.Location;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

public class LocationService extends Service implements LocationListener{
    private final static String TAG = "LocationService";
    LocationManager locationManager;

    public LocationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }

    @Override
    public void onCreate() {
        subscribeToLocationUpdates();
    }

    public void subscribeToLocationUpdates() {
        this.locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        this.locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Constants.MIN_TIME, Constants.MIN_DISTANCE, this);
    }

    @Override
    public void onLocationChanged(Location loc) {
        Location desLocation = MainActivity.retrieveLoc();
        float dis = loc.distanceTo(desLocation);
        if (Constants.set == true) {
            Toast.makeText(getApplicationContext(), "Distance from the destination is:" + dis, Toast.LENGTH_LONG).show();
            Constants.set = false;
            if (dis <= Constants.RADIUS && Constants.alarm == false) {
                Constants.alarm = true;
                notificationPopUp();
            }
        }
    }

    @Override
    public void onProviderEnabled(String s) {
        this.locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Constants.MIN_TIME, Constants.MIN_DISTANCE, this);
    }

    @Override
    public void onProviderDisabled(String s) {
        this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Constants.MIN_TIME, Constants.MIN_DISTANCE, this);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle b) {

    }

    public void notificationPopUp() {
        long[] pattern ={0, 1500, 500,1500,500,1500,500,1500,500,1500,500,1500,500,1500,500,1500,500,1500,500,1500,500};
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("ProximityAlert!")
                        .setContentText("You are near the poi!")
                        .setTicker("You are near the poi")
                        .setLights(1, 500, 500)
                        .setVibrate(pattern)
                        .setAutoCancel(true);
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(1, mBuilder.build());
    }
}
