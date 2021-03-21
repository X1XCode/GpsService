package com.example.gpsservice;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.SyncStateContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.SettingsClient;

public class MainActivity extends AppCompatActivity {

    Button btnStart, btnStop;
    LocationManager locationManager;
    private SettingsClient mSettingsClient;

    private FusedLocationProviderClient mFusedLocationClient;
    protected GoogleApiClient mGoogleApiClient;
    private LocationCallback mLocationCallback;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnStart = findViewById(R.id.btnStartService);
        btnStop = findViewById(R.id.btnStopService);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
                if (isLocationEnabled()){
                    displayLocationSettingsRequest();
                }
            }else{
                buildGoogleApiClient();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GPSService.class);
                startService(intent);
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GPSService.class);
                stopService(intent);
            }
        });
    }

    private void displayLocationSettingsRequest() {
        GoogleApiClient build = new GoogleApiClient.Builder(this.getApplicationContext()).addApi(LocationServices.API).build();
        build.connect();
        LocationRequest create = LocationRequest.create();
        create.setPriority(100);
        create.setInterval(10000);
        create.setFastestInterval(5000);
        LocationSettingsRequest.Builder addLocationRequest = new LocationSettingsRequest.Builder().addLocationRequest(create);
        addLocationRequest.setAlwaysShow(true);
        LocationServices.SettingsApi.checkLocationSettings(build, addLocationRequest.build()).setResultCallback(new ResultCallback<LocationSettingsResult>() {
            public void onResult(LocationSettingsResult locationSettingsResult) {
                Status status = locationSettingsResult.getStatus();
                int statusCode = status.getStatusCode();
                if (statusCode == 0) {
                    Log.i("LOCATIONCATEGORY", "All location settings are satisfied.");
                } else if (statusCode == 6) {
                    Log.i("LOCATIONCATEGORY", "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");
                    try {
                        status.startResolutionForResult(MainActivity.this, 1);
                    } catch (IntentSender.SendIntentException unused) {
                        Log.i("LOCATIONCATEGORY", "PendingIntent unable to execute request.");
                    }
                } else if (statusCode == 8502) {
                    Log.i("LOCATIONCATEGORY", "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                }
            }
        });
    }

    protected boolean isLocationEnabled(){
        String le = Context.LOCATION_SERVICE;
        locationManager = (LocationManager) getSystemService(le);
        if(!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {

            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // permission was granted, yay! Do the
                // contacts-related task you need to do.


            } else {
                Toast.makeText(this, "Unable to fetch location. Please allow the location", Toast.LENGTH_SHORT).show();
                finish();
                // permission denied, boo! Disable the
                // functionality that depends on this permission.
            }
            return;
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
    public synchronized void buildGoogleApiClient() {
        mSettingsClient = LocationServices.getSettingsClient(this);
        mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks((GoogleApiClient.ConnectionCallbacks) this).addOnConnectionFailedListener((GoogleApiClient.OnConnectionFailedListener) this).addApi(LocationServices.API).build();
        connectGoogleClient();
    }

    private void connectGoogleClient() {
        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) == 0) {
            this.mGoogleApiClient.connect();
        } else {
            Toast.makeText(this, "Please update play services to continue.", Toast.LENGTH_SHORT).show();
        }
    }
}
