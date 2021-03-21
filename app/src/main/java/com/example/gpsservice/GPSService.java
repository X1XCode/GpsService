package com.example.gpsservice;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.internal.Constants;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.SettingsClient;

public class GPSService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = "GPSService";
    LocationManager locationManager;
    LocationRequest locationRequest;
    private Location mCurrentLocation;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    GoogleApiClient mGoogleApiClient;
    LocationCallback mLocationCallback;
    private SettingsClient mSettingsClient;
    double lat, lng;


    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate: ");
        this.mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        this.mGoogleApiClient.connect();
        this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        startForeground();
    }

    private void startForeground() {
        PendingIntent activity = PendingIntent.getActivity(this, 0, new Intent(this, GPSService.class), 0);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentTitle("GpsService Online").setContentIntent(activity).setContentText("GPSService running").setOngoing(true).build();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(new NotificationChannel("NOTIFICATION_ID", "NOTIFICATION_CHANNEL", NotificationManager.IMPORTANCE_HIGH));
            builder.setChannelId("NOTIFICATION_ID");
        }
        startForeground(START_NOT_STICKY, builder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        getMyLocation();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onLocationChanged(Location location) {
        lat = location.getLatitude();
        lng = location.getLongitude();
        Toast.makeText(this, "LAT IS : " + lat + "LNG IS :" + lng, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.e(TAG, "onConnected()");
        getMyLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "onConnectionSuspended()");
        connectGoogleClient();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed()");
        buildGoogleApiClient();
    }

    public void onTaskRemoved(Intent intent) {
        super.onTaskRemoved(intent);
    }

    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "Service Stopped");

        GoogleApiClient googleApiClient = this.mGoogleApiClient;
        if (googleApiClient != null && googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(this.mGoogleApiClient, this);
        }

    }

    public synchronized void buildGoogleApiClient() {
        this.mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        this.mSettingsClient = LocationServices.getSettingsClient(this);
        this.mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        connectGoogleClient();
        this.mLocationCallback = new LocationCallback() {
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Log.e(GPSService.TAG, "Location Received");
                GPSService.this.mCurrentLocation = locationResult.getLastLocation();
                GPSService gpsService = GPSService.this;
                gpsService.onLocationChanged(gpsService.mCurrentLocation);
            }

            public void onLocationAvailability(LocationAvailability locationAvailability) {
                super.onLocationAvailability(locationAvailability);
                String str = GPSService.TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Location Availability ");
                stringBuilder.append(locationAvailability.isLocationAvailable());
                Log.e(str, stringBuilder.toString());
            }
        };
    }

    private void connectGoogleClient() {
        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) == 0) {
            this.mGoogleApiClient.connect();
        }
    }


    private void getMyLocation() {
        Log.e(TAG, "CALL/////...........////getMyLocation: ");
        GoogleApiClient googleApiClient = this.mGoogleApiClient;
        if (googleApiClient == null) {
            return;
        }
        if (googleApiClient.isConnected()) {
            int checkSelfPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            Log.e("Chk", "Permission ");
            if (checkSelfPermission == 0) {
                this.locationRequest = LocationRequest.create();
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Update Interval: ");
                stringBuilder.append(checkSelfPermission);
                stringBuilder.append("");
                Log.e(str, stringBuilder.toString());
                this.locationRequest.setFastestInterval(5000);
                this.locationRequest.setInterval(10000);
                this.locationRequest.setPriority(100);
                LocationServices.FusedLocationApi.requestLocationUpdates(this.mGoogleApiClient, this.locationRequest, this);
                if (LocationServices.FusedLocationApi.getLastLocation(this.mGoogleApiClient) == null) {
                    Log.e(TAG, "last location null");
                    return;
                }
                return;
            }
            Log.e(TAG, "PERMISSION REQUIRE");
            return;
        }
        String str2 = TAG;
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append("Google client not connected: ");
        stringBuilder2.append(this.mGoogleApiClient.isConnected());
        Log.e(str2, stringBuilder2.toString());
    }
}
