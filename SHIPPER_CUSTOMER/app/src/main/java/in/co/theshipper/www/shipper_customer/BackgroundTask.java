package in.co.theshipper.www.shipper_customer;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Random;

/**
 * Created by GB on 3/15/2016.
 */
public class BackgroundTask extends AsyncTask<String,Void, String> {
    private String method = "None";
    private String return_param = "Failed";
    private String JSON_STRING = "";
    private Context ctx;
    BackgroundTask(Context ctx){
        this.ctx = ctx;
    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }
    @Override
    protected String doInBackground(String... params) {
        method = params[0];
        String user_token = Fn.getPreference(ctx,"user_token");
        if(method.equals("register"))
        {
            String reg_url = Constants.Config.ROOT_PATH+"customer_registration";
            String mobile_no = params[1];
            //String post_to_server;
            Random ran = new Random();
            int otp= (100000 + ran.nextInt(900000));
            //PreferenceManager.getDefaultSharedPreferences(ctx).edit().putString("OTP", String.valueOf(otp)).putString("mobile_no", String.valueOf(mobile_no)).commit();
            Fn.putPreference(ctx,"OTP",String.valueOf(otp));
            Fn.putPreference(ctx,"mobile_no",String.valueOf(mobile_no));
            Fn.logD("ONE_TIME_PASS",String.valueOf(otp));
            //Fn.Toast(ctx,reg_url);
            Log.d("reg_url", reg_url);
            try {
                String data = URLEncoder.encode("mobile_no", "UTF-8")+"="+URLEncoder.encode(mobile_no,"UTF-8")+"&"+
                        URLEncoder.encode("OTP","UTF-8")+"="+URLEncoder.encode(String.valueOf(otp),"UTF-8");
                JSON_STRING = Fn.AndroidToServer(ctx, reg_url,data);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        else if(method.equals("get_customer_info"))
        {
            String mobile_no = "";
            String get_user_info_url = Constants.Config.ROOT_PATH+"get_customer_info";
            Fn.logD("get_user_info_url",get_user_info_url);
            mobile_no =  Fn.getPreference(ctx,"mobile_no");
            Fn.logD("mobile_no",mobile_no);
            try {
                String data = URLEncoder.encode("mobile_no","UTF-8")+"="+URLEncoder.encode(mobile_no, "UTF-8");
                JSON_STRING =  Fn.AndroidToServer(ctx,get_user_info_url,data);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        else if(method.equals("edit_customer_profile"))
        {
            String edit_customer_profile_url = Constants.Config.ROOT_PATH+"edit_customer_profile";
            Fn.logD("edit_profile_url",edit_customer_profile_url);
            String name = params[1];
            String email = params[2];
            String postal_address = params[3];
            //Log.d("reg_url",edit_customer_profile_url);
            //PreferenceManager.getDefaultSharedPreferences(ctx).edit().putString("name",name).commit();
            Fn.putPreference(ctx,"name",name);
            try {
                String data = URLEncoder.encode("name","UTF-8")+"="+URLEncoder.encode(name,"UTF-8")+"&"+
                        URLEncoder.encode("email","UTF-8")+"="+URLEncoder.encode(email,"UTF-8")+"&"+
                        URLEncoder.encode("postal_address","UTF-8")+"="+URLEncoder.encode(postal_address,"UTF-8")+"&"+
                        URLEncoder.encode("user_token","UTF-8")+"="+URLEncoder.encode(user_token,"UTF-8");
                JSON_STRING = Fn.AndroidToServer(ctx,edit_customer_profile_url,data);
                Fn.logD("edit_JSON_STRING", JSON_STRING);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        else if(method.equals("update_customer_location"))
        {
            String update_customer_location_url = Constants.Config.ROOT_PATH+"update_customer_location";
            Fn.logD("customer_location",update_customer_location_url);
            String lattitude = params[1];
            String longitude = params[2];
            try {
                String data = URLEncoder.encode("lattitude","UTF-8")+"="+URLEncoder.encode(lattitude,"UTF-8")+"&"+
                        URLEncoder.encode("longitude","UTF-8")+"="+URLEncoder.encode(longitude,"UTF-8")+"&"+
                        URLEncoder.encode("user_token","UTF-8")+"="+URLEncoder.encode(user_token,"UTF-8");
                JSON_STRING = Fn.AndroidToServer(ctx,update_customer_location_url,data);
                Fn.logD("custom_location_json", JSON_STRING);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        else if(method.equals("book_now"))
        {
            String book_now_url = Constants.Config.ROOT_PATH+"book_now";
            String vehicle_type = params[1];
            String datetime = Fn.getDateTimeNow();
            Fn.logD("book_now_url",book_now_url);
            try {
                String data = URLEncoder.encode("user_token","UTF-8")+"="+URLEncoder.encode(user_token,"UTF-8")+"&"+
                        URLEncoder.encode("vehicle_type","UTF-8")+"="+URLEncoder.encode(vehicle_type,"UTF-8")+"&"+
                        URLEncoder.encode("datetime","UTF-8")+"="+URLEncoder.encode(datetime,"UTF-8");
                JSON_STRING = Fn.AndroidToServer(ctx,book_now_url,data);
                Fn.logD("custom_location_json", JSON_STRING);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return JSON_STRING;
    }
    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        if (method.equals("register")) {
            Intent intent = new Intent(ctx, OtpVerification.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            ctx.startActivity(intent);
        }
        if(!Fn.CheckJsonError(result))
        {
            if (method.equals("get_customer_info")) {
                //Log.d("json_result",result);
                Intent intent = new Intent(ctx, EditProfile.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("JSON_STRING", result);
                ctx.startActivity(intent);
            } else if (method.equals("edit_customer_profile")) {
                Intent intent = new Intent(ctx, FullActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                ctx.startActivity(intent);
            }
        }
        else{
            Fn.Toast(ctx,"Error While Connecting to Server");
        }
    }
    public String getCrnNo(String JsonString){
        String errFlag;
        String errMsg;
        String received_crn_no = "DefaultIsNothing";
        JSONObject jsonObject;
        JSONArray jsonArray;
        try {
            jsonObject = new JSONObject(JsonString);
            //jsonArray = jsonObject.getJSONArray("likes");
            errFlag = jsonObject.getString("errFlag");
            errMsg = jsonObject.getString("errMsg");
            if(errFlag.equals("1")){
                Fn.logD("toastNotdone","toastNotdone");
            }
            else if(errFlag.equals("0"))
            {
                if(jsonObject.has("likes")) {
                    jsonArray = jsonObject.getJSONArray("likes");
                    int count = 0;
                    while (count < jsonArray.length()) {
                        Fn.logD("likes_entered", "likes_entered");
                        JSONObject JO = jsonArray.getJSONObject(count);
                        received_crn_no = JO.getString("crn_no");
//                        Fn.putPreference(this,"crn_no",received_crn_no);
                        count++;
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

         return received_crn_no;
    }
}