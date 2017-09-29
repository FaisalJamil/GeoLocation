package com.freelance.faisal.within;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

public class GeoLocation extends Service implements com.google.android.gms.location.LocationListener {

    Notification notification;
    Location destination = new Location("");
    float distance;
    NotificationManager notifier;
    private GoogleApiClient googleApiClient;
    // Binder given to clients
    private final IBinder binder = new LocalBinder();

    // Class used for the client Binder.
    public class LocalBinder extends Binder {
        public GeoLocation getService() {
            // Return this instance of GeoLocation so clients can call public methods
            return GeoLocation.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public void initialize (GoogleApiClient googleApiClient, LocationRequest locationRequest, LatLng latLng){
        this.googleApiClient = googleApiClient;
        destination.setLatitude(latLng.latitude);
        destination.setLongitude(latLng.longitude);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onLocationChanged(Location location) {

        distance = location.distanceTo(destination);
            // check if we are in 1 km distance
            if ((distance / 1000) < 1) {
                NotificationManager notificationManager = (NotificationManager) this
                        .getSystemService(Context.NOTIFICATION_SERVICE);
                Notification noti = new Notification.Builder(this)
                        .setContentTitle(getString(R.string.reaching))
                        .setContentText(getString(R.string.within_destination))
                        .setSmallIcon(R.drawable.ic_message_grey_400_24dp)
                        .build();
                notificationManager.notify(11, noti);
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.VIBRATE)
                        == PackageManager.PERMISSION_GRANTED) {
                    Vibrator vi = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    vi.vibrate(1000);
                }
                // Because we've already reached within 1 km of destination we do not need updated location
                LocationServices.FusedLocationApi.removeLocationUpdates(this.googleApiClient, this);
                stopSelf();
            }
    }
}