package app.com.example.mubingliu.intrepidcheckin;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)

public class MyService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    public static final double INTREPID_LOCATION_LAT = 42.3671520;
    public static final double INTREPID_LOCATION_LON = -71.0801970;
    private static final String LOG_TAG = MyService.class.getSimpleName();
    private static final int UPDATE_INTERVAL = 1000 * 60 * 15; //15 mins interval
    private static final int DETECT_DISTANCE = 50;
    private static final String NOTIFICATION_MESSAGE = "You are here, press to send status to Slack channel.";
    private static final String NOTIFICATON_TITLE = "My notification";

    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private String lat;
    private String lon;

    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();
        Log.v(LOG_TAG, "START SERVICE");

        initClient();

        mGoogleApiClient.connect();

        return START_NOT_STICKY;
    }

    public void initClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApiIfAvailable(LocationServices.API)
                .addScope(Drive.SCOPE_FILE)
                .build();
    }

    public void onConnected(Bundle bundle) {
        Log.d(LOG_TAG, "connected");
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(UPDATE_INTERVAL);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        //get location
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            lat = String.valueOf(mLastLocation.getLatitude());
            lon = String.valueOf(mLastLocation.getLongitude());
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
        }
        else {
            Log.i(LOG_TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }

    }

    public void calculateDistance(Location location) {
        Location intrepidLoc = new Location("");
        //intrepid geo location
        intrepidLoc.setLatitude(INTREPID_LOCATION_LAT);
        intrepidLoc.setLongitude(INTREPID_LOCATION_LON);
        float distance = intrepidLoc.distanceTo(location);

        if (distance <= DETECT_DISTANCE) {
            Log.v(LOG_TAG, "WITHIN DISTANCE: " + distance);
            Log.v(LOG_TAG, "YOU'RE HERE!");
            startNotification();
        } else {
            Log.v(LOG_TAG, "DISTANCE: " + distance);
            Log.v(LOG_TAG, "NOT HERE!");
        }
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
                .setContentTitle(NOTIFICATON_TITLE)
                .setContentText(NOTIFICATION_MESSAGE);
        Intent resultIntent = new Intent(this, NotificationClickReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getBaseContext(),
                0,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT //update to new intent
        );
        mBuilder.setContentIntent(pendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder.setAutoCancel(true);
        //mId = 1, use to update notification later
        mNotificationManager.notify(1, mBuilder.build());
    }

}

