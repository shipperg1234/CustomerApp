package in.co.theshipper.www.shipper_customer;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Create this Class from tutorial :
 * http://www.androidhive.info/2012/07/android-gps-location-manager-tutorial
 *
 * For Geocoder read this : http://stackoverflow.com/questions/472313/android-reverse-geocoding-getfromlocation
 *
 */

public class GpsTracker extends Service implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
    // Get Class Name
    protected RequestQueue requestQueue;
    protected HashMap<String,String> hashMap;
    private String TAG = GpsTracker.class.getName();
    //private final Context mContext;
    // flag for GPS Status
    boolean isGPSEnabled = false;
    // flag for network status
    boolean isNetworkEnabled = false;
    // flag for GPS Tracking is enabled
    boolean isGPSTrackingEnabled = false;
    Location location;
    double latitude;
    double longitude;
    // How many Geocoder should return our GPSTracker
    int geocoderMaxResults = 1;
    // The minimum distance to change updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 10 meters
    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 10000 * 15 * 1; // 1 minute
    // Declaring a Location Manager
    protected LocationManager locationManager;
    // Store LocationManager.GPS_PROVIDER or LocationManager.NETWORK_PROVIDER information
    private String provider_info;
    private String user_token ;
    private GoogleApiClient mGoogleApiClient;
    private Timer timer;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    /*public GpsTracker(Context context) {
        this.mContext = context;
        TimerProgramm();
    }*/

    @Override
    public void onCreate() {
        super.onCreate();
        user_token = Fn.getPreference(this,"user_token");
        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
//            mGoogleApiClient.connect();
        }
        Fn.logD("GPStracker_FRAGMENT_SERVICE_LIFECYCLE", "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mGoogleApiClient.connect();
        Fn.logD("getLocation", "getLocation");
        Intent i = new Intent(this, FullActivity.class);
        // Log.d("GPS_SERVICE_START","GPS_SERVICE_START");
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pi = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        Notification notification = builder.setContentIntent(pi)
                .setSmallIcon(R.drawable.vehicle_1).setTicker("SHIPPER").setWhen(System.currentTimeMillis())
                .setAutoCancel(true).setContentTitle("SHIIPER")
                .setContentText("Location Tracking in Progress").build();
        // mNM.notify(NOTIFICATION, notification);
        notification.flags |= Notification.FLAG_NO_CLEAR;
        startForeground(1317, notification);
        TimerProgramm();
        return (START_NOT_STICKY);
    }
    @Override
    public void onDestroy() {
        if(mGoogleApiClient.isConnected()){
//            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
        stopForeground(true);
        if(timer != null) {
            timer.cancel();
            timer = null;
        }
        Fn.logD("GPS onDestroy Called", "onDestroy");
    }
    public void TimerProgramm(){
        Fn.logD("TimerProgram", "TimerProgram");
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                Fn.logD("TimerProgram_running", "TimerProgram_running");
                getLocation();
            }
        }, Constants.Config.UPDATE_CUSTOMER_LOCATION_DELAY, Constants.Config.UPDATE_CUSTOMER_LOCATION_PERIOD);
    }
    /**
     * Try to get my current location by GPS or Network Provider
     */
    public void getLocation() {
        Fn.logD("getLocation", "getLocation");
        if(mGoogleApiClient.isConnected()){
            Fn.logD("mGoogleApiClient.isConnected()", "mGoogleApiClient.isConnected()");
            do{
            location = Fn.getAccurateCurrentlocationService(mGoogleApiClient,this);
            }while(location == null);
            updateGPSCoordinates();
        }
    }
    public void updateGPSCoordinates() {
        if (location != null) {
            String update_location_url = Constants.Config.ROOT_PATH+"update_customer_location";
            String lattitude = String.valueOf(location.getLatitude());
            String longitude = String.valueOf(location.getLongitude());
            HashMap<String,String> hashMap = new HashMap<String,String>();
            hashMap.put("location_lat", lattitude);
            hashMap.put("location_lng", longitude);
            hashMap.put("user_token", user_token);
            Fn.logD("latitude", lattitude);
            Fn.logD("longitude",longitude);
            sendVolleyRequest(update_location_url,Fn.checkParams(hashMap));
        }
    }
    public void sendVolleyRequest(String URL, final HashMap<String,String> hMap){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Fn.logD("onResponse", String.valueOf(response));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Fn.logD("onErrorResponse", String.valueOf(error));
                Fn.ToastShort(getApplicationContext(), Constants.Message.TRACKING_ERROR);
            }
        }){
            @Override
            protected HashMap<String,String> getParams(){
                return hMap;
            }
        };
        stringRequest.setTag(TAG);
        Fn.addToRequestQue(requestQueue, stringRequest, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        //getLocation();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}