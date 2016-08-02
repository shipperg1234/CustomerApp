package in.co.theshipper.www.shipper_customer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;


public class EditProfile extends AppCompatActivity {
    protected  String TAG = EditProfile.class.getName();
    protected RequestQueue requestQueue;
    protected HashMap<String,String> hashMap;
//    String user_token = Fn.getPreference(this,"user_token");
    EditText name,email,address;
    String username,useremail,useraddress,received_username,received_useremail,received_useraddress,received_usertoken,json_string,errFlag,errMsg;
    JSONObject  jsonObject;
    JSONArray jsonArray;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Fn.logW("EDIT_PROFILE_ACTIVITY_LIFECYCLE","onCreate called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
     //   profile_pic = (ImageView) findViewById(R.id.profile_pic);
        /*
        * Receive the user information from server
        * display it on layout
        * then edit it to change the user ifno
        * */
        name = (EditText) findViewById(R.id.name);
        email = (EditText) findViewById(R.id.email);
        address = (EditText) findViewById(R.id.address);
        json_string = getIntent().getStringExtra("JSON_STRING");
        Fn.logD("json_string",json_string);
//        Fn.Toast(this,json_string);
        try {
            jsonObject = new JSONObject(json_string);
            errFlag = jsonObject.getString("errFlag");
            errMsg = jsonObject.getString("errMsg");
            Fn.logD("errFlag",errFlag);
            Fn.logD("errMsg",errMsg);
            if(errFlag.equals("1")){
                Fn.Toast(this,errMsg);
                Fn.logD("toastNotdone","toastNotdone");
            }
            else if(errFlag.equals("0"))
            {
                if(jsonObject.has("likes")) {
                    jsonArray = jsonObject.getJSONArray("likes");
                    Fn.Toast(this,errMsg);
                    Fn.logD("toastdone", "toastdone");
                    int count = 0;
                    while (count < jsonArray.length())
                    {
                        Fn.logD("likes_entered", "likes_entered");
                        JSONObject JO = jsonArray.getJSONObject(count);
                        received_username = JO.getString("name");
                        received_usertoken = JO.getString("user_token");
                        received_useremail = JO.getString("email");
                        received_useraddress = JO.getString("postal_address");
                        if(received_username.length()>0) {
                            name.setText(received_username);
                        }
                        if(received_useremail.length()>0) {
                            email.setText(received_useremail);
                        }
                        if(received_useraddress.length()>0) {
                            address.setText(received_useraddress);
                        }
                        Fn.logD("received_usertoken",received_usertoken);
                        String stored_usertoken = Fn.getPreference(this,"user_token");
                        Fn.logD("stored_usertoken",stored_usertoken);
                        count++;
                    }
                }
                else
                {
                    Fn.Toast(this,Constants.Message.NEW_USER_ENTER_DETAILS);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    //  profile_pic.setOnClickListener(this);
    }
    public void editProfile(View view)
    {
        if(checkValidation()) {
//            Fn.showProgressDialog(Constants.Message.LOADING,this);
            username = name.getText().toString();
            useremail = email.getText().toString();
            useraddress = address.getText().toString();
            String edit_customer_profile_url = Constants.Config.ROOT_PATH + "edit_customer_profile";
            Fn.logD("useraddress", useraddress);
            Fn.logD("useremail", useremail);
            Fn.logD("username", username);
            hashMap = new HashMap<String, String>();
            hashMap.put("name", username);
            hashMap.put("email", useremail);
            hashMap.put("postal_address", useraddress);
            hashMap.put("user_token", received_usertoken);
            sendVolleyRequest(edit_customer_profile_url,Fn.checkParams(hashMap));
        }
        else{
            Toast.makeText(EditProfile.this, "Form Contains Error", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkValidation() {
        boolean ret = true;
        if (!FormValidation.isEmailAddress(email, true)) ret = false;
        if (!FormValidation.isRequired(name,Constants.Config.NAME_FIELD_LENGTH)) ret = false;
        if(!FormValidation.isRequired(address,Constants.Config.ADDRESS_FIELD_LENGTH)) ret = false;
        return ret;
    }

    public void sendVolleyRequest(String URL, final HashMap<String,String> hMap){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Fn.logD("onResponse", String.valueOf(response));
                editProfileSuccess(response);
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
    public void editProfileSuccess(String response){
        if(!Fn.CheckJsonError(response)){
            Intent i = new Intent(this, RegistrationIntentService.class);
            startService(i);
            Fn.putPreference(this,"user_token",received_usertoken);
            Intent intent = new Intent(this, FullActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }else{
            ErrorDialog(Constants.Title.SERVER_ERROR,Constants.Message.SERVER_ERROR);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Fn.cancelAllRequest(requestQueue,TAG);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Fn.startAllVolley(requestQueue);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Fn.stopAllVolley(requestQueue);
    }
}
