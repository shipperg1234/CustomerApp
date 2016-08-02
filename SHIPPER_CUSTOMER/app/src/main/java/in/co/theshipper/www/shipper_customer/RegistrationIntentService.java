package in.co.theshipper.www.shipper_customer;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Button;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by GB on 12/7/2015.
 */
// abbreviated tag name
public class RegistrationIntentService extends IntentService {

    // abbreviated tag name
    protected RequestQueue requestQueue;
    protected HashMap<String,String> hashMap;
    private static final String TAG = RegistrationIntentService.class.getName();
    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
    public static final String GCM_TOKEN = "gcmToken";

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Make a call to Instance API
        String token = "";
        InstanceID instanceID = InstanceID.getInstance(this);
        String senderId = getResources().getString(R.string.gcm_senderID);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Fetch token here
        try {

            // request token that will be used by the server to send push notifications
            token = instanceID.getToken(senderId, GoogleCloudMessaging.INSTANCE_ID_SCOPE);
            Log.d(TAG, "GCM Registration Token: " + token);
            // pass along this data
            sendRegistrationToServer(token);
//
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "Failed to complete token refresh", e);
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            sharedPreferences.edit().putBoolean(SENT_TOKEN_TO_SERVER, false).apply();
        }
        // save token
        sharedPreferences.edit().putString(GCM_TOKEN, token).apply();
        // pass along this data
        sendRegistrationToServer(token);
    }
    private void sendRegistrationToServer(String token) {
        Fn.logD("sendRegistrationToServer", "sendRegistrationToServer");
        // send network request
        // if registration sent was successful, store a boolean that indicates whether the generated token has been sent to server
        String update_device_id_url = Constants.Config.ROOT_PATH+"update_customer_device";
        String user_token = Fn.getPreference(this,"user_token");
        HashMap<String,String>  hashMap = new HashMap<String,String>();
        hashMap.put("gcm_regid",token);
        hashMap.put("user_token", user_token);
        sendVolleyRequest(update_device_id_url, hashMap);
    }
    public void sendVolleyRequest(String URL, final HashMap<String,String> hMap){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Fn.logD("onResponse", String.valueOf(response));
                //Fn.logD("onResponse_booking_status", String.valueOf(response));
                UpdateDeviceSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Fn.logD("onErrorResponse", String.valueOf(error));
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
    public void UpdateDeviceSuccess(String response){
        if(!Fn.CheckJsonError(response)){
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            sharedPreferences.edit().putBoolean(SENT_TOKEN_TO_SERVER, true).apply();
        }
    }
}
