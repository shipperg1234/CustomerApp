package in.co.theshipper.www.shipper_customer;

import android.app.AlertDialog;
import android.content.DialogInterface;
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

import java.util.HashMap;


public class OtpVerification extends AppCompatActivity {
    private  String TAG = OtpVerification.class.getName();
    protected RequestQueue requestQueue;
    private  EditText otp_value;
    private String entered_otp;
    private String OTP;
    private Bundle b;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Fn.logW("OTP_PROFILE_ACTIVITY_LIFECYCLE", "onCreate called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);
        otp_value = (EditText) findViewById(R.id.editText2);
        if(getIntent().getExtras() != null) {
            b = getIntent().getExtras();
            OTP = String.valueOf(b.getInt("OTP"));
//            Fn.Toast(this, String.valueOf(OTP));
        }
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        Fn.logW("OTP_PROFILE_ACTIVITY_LIFECYCLE", "onRestart called");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Fn.logW("OTP_PROFILE_ACTIVITY_LIFECYCLE","onStart called");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Fn.logW("OTP_PROFILE_ACTIVITY_LIFECYCLE","onResume called");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Fn.logW("OTP_PROFILE_ACTIVITY_LIFECYCLE","onPause called");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Fn.logW("OTP_PROFILE_ACTIVITY_LIFECYCLE","onStop called");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Fn.logW("OTP_PROFILE_ACTIVITY_LIFECYCLE","onDestroy called");
    }
    public void verifyOtp(View view){
        if(checkValidation()) {
//            Fn.showProgressDialog(Constants.Message.LOADING,this);
            entered_otp = otp_value.getText().toString();
            if (OTP.equals(entered_otp)) {
                String mobile_no = "";
                String get_user_info_url = Constants.Config.ROOT_PATH + "get_customer_info";
                Fn.logD("get_user_info_url", get_user_info_url);
                mobile_no = Fn.getPreference(this, "mobile_no");
                Fn.logD("mobile_no", mobile_no);
                HashMap<String,String> hashMap = new HashMap<String, String>();
                hashMap.put("mobile_no", mobile_no);
                sendVolleyRequest(get_user_info_url,Fn.checkParams(hashMap));

            } else {
                Fn.showDialog(this,Constants.Title.OTP_VERIFICATION_ERROR,Constants.Message.OTP_VERIFICATION_ERROR);
            }
        }
        else {
           Fn.Toast(this, Constants.Message.FORM_ERROR);
        }
    }

    private boolean checkValidation() {
        boolean ret = true;
        if (!FormValidation.isValidOTP(otp_value, true)) ret = false;
        return ret;
    }

    public void sendVolleyRequest(String URL, final HashMap<String,String> hMap){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Fn.logD("onResponse", String.valueOf(response));
                OtpVerificationSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Fn.logD("onErrorResponse", String.valueOf(error));
                ErrorDialog(Constants.Title.NETWORK_ERROR,Constants.Message.NETWORK_ERROR);
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
    public void OtpVerificationSuccess(String response){
        if(!Fn.CheckJsonError(response)){
            Intent intent = new Intent(this, EditProfile.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("JSON_STRING", response);
            startActivity(intent);
        }else{
            ErrorDialog(Constants.Title.SERVER_ERROR,Constants.Message.SERVER_ERROR);
        }
    }
}
