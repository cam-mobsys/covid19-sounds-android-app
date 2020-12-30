package uk.ac.cam.cl.covid19sounds.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import uk.ac.cam.cl.covid19sounds.MainApplication;
import uk.ac.cam.cl.covid19sounds.R;
import uk.ac.cam.cl.covid19sounds.dataEntry.DataConstants;
import uk.ac.cam.cl.covid19sounds.dataEntry.DataFileWriter;
import uk.ac.cam.cl.covid19sounds.dataEntry.user.UserOnboardingPrefs;


//code to get location sample
public class LocationActivity extends AppCompatActivity {
    FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            try {
                logLocationEntry(mLastLocation, LocationActivity.this);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    public static int iSGPSEnabled(Context context) {
        //check if location services are enabled
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            return DataConstants.FAILURE;
        } else {
            // check if the GPS is turned off
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                return DataConstants.SUCCESS;
            } else {
                return DataConstants.FAILURE;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getLocation();

    }

    public void getLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //check for the user permission for location services. we only access coarse location once.
            boolean permissionAccessCoarseLocationApproved =
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED;
            if (permissionAccessCoarseLocationApproved) {
                //if permission is granted we get the location
                if (iSGPSEnabled(getApplicationContext()) == DataConstants.SUCCESS) {
                    mFusedLocationClient.getLastLocation().addOnCompleteListener(
                            new OnCompleteListener<Location>() {
                                @Override
                                public void onComplete(@NonNull Task<Location> task) {
                                    Location location = task.getResult();
                                    if (location == null) {
                                        requestNewLocationData();
                                    } else {
                                        try {
                                            logLocationEntry(location, LocationActivity.this);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                    );
                    this.finish();

                } else {
                    showSettingDialog(); // ask user to enable location services if not already on
                }
            } else {
                //request permissions
                ActivityCompat.requestPermissions(this, new String[]{
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                        },
                        DataConstants.LOC_PERMISSION_REQUEST_CODE);
            }
        }

        Toast.makeText(this, getString(R.string.thanks), Toast.LENGTH_LONG).show();
        MainApplication.requestSync(this); //try to send the location data

    }

    private void showSettingDialog() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY); // balanced priority
        locationRequest.setInterval(DataConstants.LOC_UPDATE_INTERVAL); //desired interval for active location updates, in millisec
        locationRequest.setFastestInterval(DataConstants.LOC_UPDATE_FASTEST_INTERVAL); //set the fastest interval for location updates,
        // in milliseconds.
        locationRequest.setNumUpdates(DataConstants.LOC_NUM_UPDATES); //get location only once

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        Task<LocationSettingsResponse> result =
                LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {

                try {
                    task.getResult(ApiException.class);
                } catch (ApiException exception) {
                    switch (exception.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                resolvable.startResolutionForResult(
                                        LocationActivity.this,
                                        DataConstants.LOC_REQUEST_CODE);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            } catch (ClassCastException e) {
                                // Ignore, should not happen.
                            }
                            break;

                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we will not show the dialog.
                            break;
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DataConstants.LOC_REQUEST_CODE) {
            switch (resultCode) {
                case RESULT_OK:
                    //permission granted
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                            PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                        return;
                    }
                    mFusedLocationClient.getLastLocation().addOnCompleteListener(
                            new OnCompleteListener<Location>() {
                                @Override
                                public void onComplete(@NonNull Task<Location> task) {
                                    Location location = task.getResult();
                                    if (location == null) {
                                        requestNewLocationData();
                                    }
                                }
                            }
                    );
                    this.finish();
                    break;
                case RESULT_CANCELED:
                    //user did not approve
                    this.finish();
                    break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == DataConstants.LOC_PERMISSION_REQUEST_CODE) {
            //permission granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (iSGPSEnabled(getApplicationContext()) == DataConstants.SUCCESS) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                            PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                        return;
                    }
                    mFusedLocationClient.getLastLocation().addOnCompleteListener(
                            new OnCompleteListener<Location>() {
                                @Override
                                public void onComplete(@NonNull Task<Location> task) {
                                    Location location = task.getResult();
                                    if (location == null) {
                                        requestNewLocationData();
                                    } else {
                                        try {
                                            logLocationEntry(location, LocationActivity.this);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                    );
                    this.finish();

                } else {
                    showSettingDialog(); //ask user to enable location services if not already on
                }
            } else {
                //permission denied
                this.finish();
            }
        }

    }

    private void requestNewLocationData() {
        //create location object and register callback
        FusedLocationProviderClient mFusedLocationClient;
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setInterval(DataConstants.LOC_UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(DataConstants.LOC_UPDATE_FASTEST_INTERVAL);
        mLocationRequest.setNumUpdates(DataConstants.LOC_NUM_UPDATES);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
        );

    }

    private void logLocationEntry(Location location, Context context) throws JSONException {
        //log the location data in the logs
        JSONObject json = new JSONObject();
        json.put(DataConstants.PARTICIPANT_ID, UserOnboardingPrefs.getInstance(context).getUserID());
        json.put(DataConstants.FINISH_DATETIME, System.currentTimeMillis());
        json.put(DataConstants.LATITUDE, String.valueOf(location.getLatitude()));
        json.put(DataConstants.LONGITUDE, String.valueOf(location.getLongitude()));
        DataFileWriter.log(json.toString().concat("\n"), DataFileWriter.FileNames.LOCATION, context);
    }

}

