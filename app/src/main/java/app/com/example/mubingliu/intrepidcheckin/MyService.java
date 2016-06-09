package app.com.example.mubingliu.intrepidcheckin;

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
/**
 * Created by mubingliu on 6/6/16.
 */
public class MyService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private static final String LOG_TAG = MyService.class.getSimpleName();
    private GoogleApiClient mGoogleApiClient;
    TextView txtOutputLat, txtOutputLon;
    Location mLastLocation;

    private LocationRequest mLocationRequest;
    String lat, lon;

    private static int UPDATE_INTERVAL = 1000 * 30 * 15; //15 mins interval

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(this)
//                .addApiIfAvailable(LocationServices.API)
//                .addScope(Drive.SCOPE_FILE)
//                .build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();
        Log.v(LOG_TAG, "START SERVICE");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApiIfAvailable(LocationServices.API)
                .addScope(Drive.SCOPE_FILE)
                .build();

        mGoogleApiClient.connect();

        if (lat != null && lon != null) {
            Log.v(LOG_TAG, "LAT: " + lat);
            Log.v(LOG_TAG, "LON: " + lon);
        }

        return START_NOT_STICKY;
    }

    public void onConnected(Bundle bundle) {
        Log.d(LOG_TAG, "connected");
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL)
        .setFastestInterval(1 * 1000);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            Log.v(LOG_TAG, "GETTING LOCATION");
            lat = String.valueOf(mLastLocation.getLatitude());
            Log.v(LOG_TAG, "LAT" + lat);
            lon = String.valueOf(mLastLocation.getLongitude());
            Log.v(LOG_TAG, "LON" + lon);
            //calculateDistance(mLastLocation);
        }
        else {
            //blank for now
            //ask for request is the last location is unknown
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(LOG_TAG, "Location service suspended. Please reconnect.");
    }

    @Override
    public void onLocationChanged(Location location) {
        lat = String.valueOf(mLastLocation.getLatitude());
        lon = String.valueOf(mLastLocation.getLongitude());
        Log.v(LOG_TAG, "UPDATE LAT" + lat);
        Log.v(LOG_TAG, "UPDATE LON" + lon);
        calculateDistance(mLastLocation);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            Log.i(LOG_TAG, "try to resolve error.");
//            try {
//                connectionResult.startResolutionForResult(MainActivity, CONNECTION_FAILURE_RESOLUTION_REQUEST);
//            } catch (IntentSender.SendIntentException e) {
//                e.printStackTrace();
//            }
        } else {
            Log.i(LOG_TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    public void calculateDistance(Location location) {
        Location intrepidLoc = new Location("");
//        intrepidLoc.setLatitude(42.3478740);
//        intrepidLoc.setLongitude(-71.1389920);
        //intrepid geo location
        intrepidLoc.setLatitude(42.3671520);
        intrepidLoc.setLongitude(-71.0801970);

        float distance = intrepidLoc.distanceTo(location);

        if (distance <= 50) {
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
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.cast_ic_notification_0)
                .setContentTitle("My notification")
                .setContentText("Hello world Notification");
        //creat an intent to start service
        Intent resultIntent = new Intent(this, NotificationClickReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(getBaseContext(),
                0,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        mBuilder.setContentIntent(pendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder.setAutoCancel(true);
        mNotificationManager.notify(1, mBuilder.build());
    }

}

