package in.co.theshipper.www.shipper_customer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import static com.google.android.gms.internal.zzir.runOnUiThread;

public class FlashActivity extends AppCompatActivity implements  GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
    protected  String TAG = FlashActivity.class.getName();
    protected RequestQueue requestQueue;
    protected  DBController controller;
    protected SQLiteDatabase database;
    private  HashMap<String, String> queryValues;
    private TextView error_message;
//    ProgressBar progressBar;
    private int googleCount = 0;
    private Boolean stopTimer = false,stopForEver = false;
    private Timer timer;
    private Location location;
    private  ProgressDialog progressDialog;
    private Boolean isNetworkEnabled = false;
    private Boolean isGpsEnabled = false;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private GoogleApiClient mGoogleApiClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_flash);
        Fn.logE("FLASH_ACTIVITY_LIFECYCLE", "onCreate");
        getSupportActionBar().hide();
        controller = new DBController(this);
        error_message = (TextView) findViewById(R.id.error_message);
// Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            mGoogleApiClient.connect();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
// Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
//                        startLocationUpdates();
                        TimerProgramm();
                        break;
                    case Activity.RESULT_CANCELED:
                        Fn.showGpsAutoEnableRequest(mGoogleApiClient, this);//keep asking if imp or do whatever
                        break;
                }
                break;
        }
    }
    @Override
    protected void onResume() {
        Fn.logE("FLASH_ACTIVITY_LIFECYCLE", "onResume");
        super.onResume();
        Fn.startAllVolley(requestQueue);
        stopTimer = false;
        mGoogleApiClient.connect();
        Fn.logE("google_connected", "true");
        if (checkPlayServices())
        {
            Fn.logE("checkPlayServices", "true");
            if (Fn.isGpsEnabled(this)) {
                if (Fn.isNetworkEnabled(this)) {
                    Fn.logE("isNetworkEnabled", "true");
                    if (mGoogleApiClient.isConnected()) {
                        Fn.logE("mGoogleApiClient", "true");
                        isNetworkEnabled = true;
                        location = Fn.getAccurateCurrentlocation(mGoogleApiClient, this);
                        if (location != null) {
                            String current_address = Fn.getLocationAddress(location.getLatitude(),location.getLongitude(),this);
                            Fn.putPreference(this,Constants.Keys.MY_CURRENT_ADDRESS,current_address);
                            getCity(location);
                        } else {
                            if (timer == null) {
                                TimerProgramm();
                            }
                        }
                    } else {
                        googleCount++;
                        if (timer == null) {
                            TimerProgramm();
                        }
                    }
                } else {
                    ErrorDialog(Constants.Title.NETWORK_ERROR, Constants.Message.NETWORK_ERROR);
                    if (timer == null) {
                        TimerProgramm();
                    }
                }
            } else {
                Fn.showGpsAutoEnableRequest(mGoogleApiClient, this);
            }
        }
    }
    public void TimerProgramm() {
        Fn.showProgressDialogLong(Constants.Message.CONNECTING, this);
        Fn.logD("TimerProgram", "TimerProgram");
        int delay = Constants.Config.DELAY_LOCATION_CHECK; // delay for 0 sec.
        int period = Constants.Config.PERIOD_LOCATION_CHECK; // repeat every 100 msec.
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Fn.logD("TimerProgram_running", "TimerProgram_running");
                        if ((stopTimer != true)&&(stopForEver != true)) {
                            checkLocation();
                        }
                    }
                });
            }
        }, delay, period);
    }
    public void checkLocation(){
        if(Fn.isNetworkEnabled(this)){
              if (mGoogleApiClient.isConnected()) {
                   Fn.logE("mGoogleApiClient", "true");
                Fn.logE("isNetworkEnabled", "true");
                  location = Fn.getAccurateCurrentlocation(mGoogleApiClient, this);
                if (location != null) {
                    stopForEver = true;
                   getCity(location);
                }
            }
        } else {
            googleCount++;
            if(googleCount == 3) {
                stopForEver = true;
                ErrorDialog(Constants.Title.NETWORK_ERROR, Constants.Message.NETWORK_ERROR);
            }
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        Fn.logE("FLASH_ACTIVITY_LIFECYCLE", "onPause Called");
        Fn.stopAllVolley(requestQueue);
        if(mGoogleApiClient.isConnected()){
            mGoogleApiClient.disconnect();
        }
        stopTimer = true;
    }
    @Override
    protected void onDestroy() {
        Fn.logE("FLASH_ACTIVITY_LIFECYCLE","onDestroy Called");
        super.onDestroy();
        Fn.cancelAllRequest(requestQueue,TAG);
        if(timer != null){
            timer.cancel();
            timer = null;
        }
    }
    private boolean checkPlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if(result != ConnectionResult.SUCCESS) {
            if(googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this,result,1).show();
            }
            return false;
        }
        return true;
    }
    public void NextActivity() {
        String user_token = Fn.getPreference(this, "user_token");
        Fn.logD("user_token", user_token);
        if (!user_token.equals("defaultStringIfNothingFound")) {
            Intent intent1 = new Intent(this, FullActivity.class);
            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent1);
            finish();
        }else {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }
    class fetch extends AsyncTask<String, String, Void> {
        // private ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        InputStream is = null;
        String result = "";
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(String... params) {
            String url_select = Constants.Config.ROOT_PATH+"fetch_view";
            String view_name[] = new String[4];
            view_name[0] = controller.TABLE_VIEW_BASE_FARE;
            view_name[1] = controller.TABLE_VIEW_CITY;
            view_name[2] = controller.TABLE_VIEW_PRICING;
            view_name[3] = controller.TABLE_VIEW_VEHICLE_TYPE;
            String primary_key[] = new String[4];
            primary_key[0] = controller.ID;
            primary_key[1] = controller.CITY_ID;
            primary_key[2] = controller._ID;
            primary_key[3] = controller.VEHICLETYPE_ID;
            String timestamp[] = new String[4];
            String q1="select max(update_date) from ";
            //String q2=" order by update_date desc limit 1";
            String s = "";
            //String query3 = "SELECT update_date FROM ";
            database = controller.getWritableDatabase();
            for (int k = 0; k < 4; k++) {
                long cnt = 0;
                try {
                    cnt = DatabaseUtils.queryNumEntries(database, view_name[k]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Fn.SystemPrintLn("count" + cnt);
                if (cnt > 0) {
//                     Fn.SystemPrintLn("entered");
                    Cursor d =database.rawQuery(q1+view_name[k],null);
                    d.moveToFirst();
                    s=d.getString(0);
                    timestamp[k] = s;
                }
                else {
                    timestamp[k] = "";
                }
            }
            for (int j = 0; j < 4; j++) {
                try {
                    URL url = new URL(url_select);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setDoOutput(true);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    // write out form parameters
                    String postParamaters = "view_name=" + view_name[j] + "&latest_date="+timestamp[j];
                    conn.setFixedLengthStreamingMode(postParamaters.getBytes().length);
                    PrintWriter out = new PrintWriter(conn.getOutputStream());
                    out.print(postParamaters);
                    out.close();
                    // conn.setRequestMethod("GET");
                    conn.connect();
                    is = conn.getInputStream();
                } catch (Exception e) {
                    Fn.logE("log_tag", "Error in http connection " + j + e.toString());
                }
                try {
                    BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                    StringBuilder sb = new StringBuilder();
                    String line = "";
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    is.close();
                    result = sb.toString();

                    Fn.SystemPrintLn("hahaha" + result + "hahaha");

                } catch (Exception e) {
                    //  handle exception
                    Fn.logE("log_tag", "Error converting result " + e.toString());
                }
                try {
                    String errFlag;
                    String errMsg;
                    JSONObject jsonObject = new JSONObject(result);
                    errFlag = jsonObject.getString("errFlag");
                    errMsg = jsonObject.getString("errMsg");
//                     Fn.SystemPrintLn(errFlag + errMsg);
                    JSONObject UpdationObject;
                    JSONArray jsonArray;
                    if (jsonObject.has("likes"))
                    {
                        controller.deleteTable(j);
                        Fn.logD("table"+j+"deleted","delete table called");
                        ////controller.createTable(j);
                        jsonArray = jsonObject.getJSONArray("likes");
                        int count = 0;
                        while (count < jsonArray.length()) {
                            UpdationObject = jsonArray.getJSONObject(count);
                            queryValues = new HashMap<String, String>();
                            if (j == 0) {
                                queryValues.put(controller.VEHICLETYPE_ID, UpdationObject.get(controller.VEHICLETYPE_ID).toString());
                                queryValues.put(controller.CITY_ID, UpdationObject.get(controller.CITY_ID).toString());
                                queryValues.put(controller.VEHICLE_NAME, UpdationObject.get(controller.VEHICLE_NAME).toString());
                                queryValues.put(controller.BASE_FARE, UpdationObject.get(controller.BASE_FARE).toString());
                                queryValues.put(controller.MAXIMUM_WEIGHT, UpdationObject.get(controller.MAXIMUM_WEIGHT).toString());
                                queryValues.put(controller.FREEWAITING_TIME, UpdationObject.get(controller.FREEWAITING_TIME).toString());
                                queryValues.put(controller.WAITING_CHARGE, UpdationObject.get(controller.WAITING_CHARGE).toString());
                                queryValues.put(controller.NIGHT_HOLDING_CHARGE, UpdationObject.get(controller.NIGHT_HOLDING_CHARGE).toString());
                                queryValues.put(controller.HARD_COPY_CHALLAN, UpdationObject.get(controller.HARD_COPY_CHALLAN).toString());
                                queryValues.put(controller.DIMENSION, UpdationObject.get(controller.DIMENSION).toString());
                                queryValues.put(controller.TRANSIT_CHARGE, UpdationObject.get(controller.TRANSIT_CHARGE).toString());
                                queryValues.put(controller.IS_ACTIVE, UpdationObject.get(controller.IS_ACTIVE).toString());
                                queryValues.put(controller.UPDATE_DATE, UpdationObject.get(controller.UPDATE_DATE).toString());
                                controller.insert(queryValues, j);
                            } else if (j == 1) {
                                queryValues.put(controller.CITY_ID, UpdationObject.get(controller.CITY_ID).toString());
                                queryValues.put(controller.CITY_NAME, UpdationObject.get(controller.CITY_NAME).toString());
                                queryValues.put(controller.IS_ACTIVE, UpdationObject.get(controller.IS_ACTIVE).toString());
                                queryValues.put(controller.UPDATE_DATE, UpdationObject.get(controller.UPDATE_DATE).toString());
                                controller.insert(queryValues, j);
                            } else if (j == 2) {
                                queryValues.put(controller.VEHICLETYPE_ID, UpdationObject.get(controller.VEHICLETYPE_ID).toString());
                                queryValues.put(controller.CITY_ID, UpdationObject.get(controller.CITY_ID).toString());
                                queryValues.put(controller.VEHICLE_NAME, UpdationObject.get(controller.VEHICLE_NAME).toString());
                                queryValues.put(controller.FROM_DISTANCE, UpdationObject.get(controller.FROM_DISTANCE).toString());
                                queryValues.put(controller.TO_DISTANCE, UpdationObject.get(controller.TO_DISTANCE).toString());
                                queryValues.put(controller.PRICE_KM, UpdationObject.get(controller.PRICE_KM).toString());
                                queryValues.put(controller.IS_ACTIVE, UpdationObject.get(controller.IS_ACTIVE).toString());
                                queryValues.put(controller.UPDATE_DATE, UpdationObject.get(controller.UPDATE_DATE).toString());
                                controller.insert(queryValues, j);
                            } else {
                                queryValues.put(controller.VEHICLETYPE_ID, UpdationObject.get(controller.VEHICLETYPE_ID).toString());
                                queryValues.put(controller.VEHICLE_NAME, UpdationObject.get(controller.VEHICLE_NAME).toString());
                                queryValues.put(controller.IS_ACTIVE, UpdationObject.get(controller.IS_ACTIVE).toString());
                                queryValues.put(controller.UPDATE_DATE, UpdationObject.get(controller.UPDATE_DATE).toString());
                                controller.insert(queryValues, j);
                            }
                            count++;
                        }
                    }
                    else if(errMsg.compareTo("Your request successful")==0)
                    {
                        controller.deleteTable(j);
                        Fn.logD("table" + j + "deleted", "delete table called");
                        Fn.logD("Entered","where errMsg=your request successful");
                    }
                } catch (Exception e) {
                    // handle exception
                    Fn.logE("log_tag", "Error parsing data " + e.toString());
                }
            }
            return null;
        }
        protected void onPostExecute(Void v) {
            try {
                controller.getAll();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            database.close();
        }
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }
    @Override
    public void onConnectionSuspended(int i) {

    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Fn.logE(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }
    @Override
    public void onLocationChanged(Location location) {
    }
    public void getCity(Location location){
        double current_lat = location.getLatitude();
        double current_lng = location.getLongitude();
        String get_city_url = Constants.Config.ROOT_PATH+"get_city";
        HashMap<String,String> hashMap = new HashMap<>();
        hashMap.put("current_lat",String.valueOf(current_lat));
        hashMap.put("current_lng",String.valueOf(current_lng));
        sendVolleyRequest(get_city_url,Fn.checkParams(hashMap));
    }
    public void sendVolleyRequest(String URL, final HashMap<String,String> hMap){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Fn.logD("onResponse", String.valueOf(response));
                CityGetSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Fn.logD("onErrorResponse", String.valueOf(error));
                ErrorDialog(Constants.Title.NETWORK_ERROR, Constants.Message.NETWORK_ERROR);
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
    private void ErrorDialog(String Title,String Message){
        Fn.showDialog(this, Title, Message);
    }
    private void CityGetSuccess(String response){
        try {
            JSONObject jsonObject = new JSONObject(response);
           String errFlag = jsonObject.getString("errFlag");
            String   errMsg = jsonObject.getString("errMsg");
            Fn.logD("errFlag",errFlag);
            Fn.logD("errMsg",errMsg);
            if(errFlag.equals("1")){
                Fn.Toast(this,errMsg);
                Fn.logD("toastNotdone", "toastNotdone");
                ErrorDialog(Constants.Title.SERVER_ERROR, Constants.Message.SERVER_ERROR);
            }
            else if(errFlag.equals("0"))
            {
                if(jsonObject.has("likes")) {
                    JSONArray jsonArray = jsonObject.getJSONArray("likes");
//                    Fn.Toast(this,errMsg);
                    Fn.logD("toastdone", "toastdone");
                    int count = 0;
                    while (count < jsonArray.length())
                    {
                        Fn.logD("likes_entered", "likes_entered");
                        JSONObject JO = jsonArray.getJSONObject(count);
                        String received_city_id= JO.getString("city_id");
                        Fn.putPreference(this,Constants.Keys.CITY_ID,received_city_id);
                        new fetch().execute();
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                NextActivity();
                            }
                        }, Constants.Config.FLASH_TO_MAIN_DELAY);
                        count++;
                    }
                }
                else
                {
                    ErrorDialog(Constants.Title.NO_SERVICE,Constants.Message.NO_SERVICE);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
