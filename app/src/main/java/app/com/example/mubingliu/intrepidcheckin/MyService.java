package app.com.example.mubingliu.intrepidcheckin;

/**
 * Created by mubingliu on 6/6/16.
 * get location and update every 15 mins
 * create notification if user is within 50 meters
 */

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)

public class MyService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private static final String LOG_TAG = MyService.class.getSimpleName();
    private GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    private LocationRequest mLocationRequest;
    String lat, lon;
    private static int UPDATE_INTERVAL = 1000 * 60 * 15; //15 mins interval

    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    //first call onCreat() method
    public void onCreate() {
        super.onCreate();
    }

    //then call onStartCommand(Intent, int, int)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();
        Log.v(LOG_TAG, "START SERVICE");
        //initialize client
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApiIfAvailable(LocationServices.API)
                .addScope(Drive.SCOPE_FILE)
                .build();

        mGoogleApiClient.connect(); //after connected call onStart(), then call onConnected()

        if (lat != null && lon != null) {
            Log.v(LOG_TAG, "LAT: " + lat);
            Log.v(LOG_TAG, "LON: " + lon);
        }

        //continue running util click to stop service
        return START_NOT_STICKY;
    }

    public void onConnected(Bundle bundle) {
        Log.d(LOG_TAG, "connected");
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        //get location
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            Log.v(LOG_TAG, "GETTING LOCATION");
            lat = String.valueOf(mLastLocation.getLatitude());
            Log.v(LOG_TAG, "LAT" + lat);
            lon = String.valueOf(mLastLocation.getLongitude());
            Log.v(LOG_TAG, "LON" + lon);
        }
        else {
            //ask for request if the last location is unknown(null)
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(LOG_TAG, "Location service suspended. Please reconnect.");
    }

    //onLocationChanged() will be called once location changed every 15 mins
    @Override
    public void onLocationChanged(Location location) {
        lat = String.valueOf(mLastLocation.getLatitude());
        lon = String.valueOf(mLastLocation.getLongitude());
        Log.v(LOG_TAG, "UPDATE LAT" + lat);
        Log.v(LOG_TAG, "UPDATE LON" + lon);
        //check if user is in Intrepid area
        calculateDistance(mLastLocation);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        if (connectionResult.hasResolution()) {
            Log.i(LOG_TAG, "try to resolve error.");
        }
        else {
            //build-in mechanisms for handling certain errors
            Log.i(LOG_TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }

    }

    public void calculateDistance(Location location) {
        Location intrepidLoc = new Location("");
        //intrepid geo location
        intrepidLoc.setLatitude(42.3671520);
        intrepidLoc.setLongitude(-71.0801970);
        float distance = intrepidLoc.distanceTo(location);

        if (distance <= 50) { // if distance from user to Intrepid is less than 50 meters, call startNotification();
            Log.v(LOG_TAG, "WITHIN DISTANCE: " + distance);
            Log.v(LOG_TAG, "YOU'RE HERE!");
            startNotification();
        } else {
            Log.v(LOG_TAG, "DISTANCE: " + distance);
            Log.v(LOG_TAG, "NOT HERE!");
        }
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Log.v(LOG_TAG, "START SERVICE");
        super.onStart(intent, startId);
        mGoogleApiClient.connect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(LOG_TAG, "DESTROY SERVICE");
        Toast.makeText(this, "Service destroyed", Toast.LENGTH_SHORT).show();

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

    }

    public void startNotification () {
        //create notification
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.cast_ic_notification_0)
                .setContentTitle("My notification")
                .setContentText("You are here, press to send status to Slack channel.");
        //creat an intent to start service
        Intent resultIntent = new Intent(this, NotificationClickReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getBaseContext(),
                0,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT //update to new intent
        );
        mBuilder.setContentIntent(pendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //gone after press the notification
        mBuilder.setAutoCancel(true);
        //mId = 1, use to update notification later
        mNotificationManager.notify(1, mBuilder.build());
    }

}

