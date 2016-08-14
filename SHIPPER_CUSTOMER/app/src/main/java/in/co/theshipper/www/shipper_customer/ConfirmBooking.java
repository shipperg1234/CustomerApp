package in.co.theshipper.www.shipper_customer;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.DataTruncation;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 */
public class ConfirmBooking extends Fragment implements View.OnClickListener{
    private String TAG = ConfirmBooking.class.getName();
    private String truck_name="NOT AVAILABLE";
    private DateFormat date;
    private Date d;
    private int active=0;
    private DBController controller;
    private double base_fare_min,base_fare_max,total_fare_min,total_fare_max,distance_fare=0;
    private View view;
    private String actual_booking_datetime, pickup_address,dropoff_address, booking_datetime,selected_vehicle,journey_distance,journey_duration,material_weight,material_image;
    private TextView pickup_point_view,dropoff_point_view,vehicle_name_view,booking_datetime_view,total_distance_view,total_time_view,total_fare_view;
    protected RequestQueue requestQueue;
    private Button confirm_boking;
    public ConfirmBooking() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (container == null) {
            return null;
        } else {

            view = inflater.inflate(R.layout.fragment_confirm_booking, container, false);
            confirm_boking = (Button) view.findViewById(R.id.confirm_booking);
            pickup_point_view = (TextView) view.findViewById(R.id.pickup_point);
            dropoff_point_view = (TextView) view.findViewById(R.id.dropoff_point);
            booking_datetime_view = (TextView) view.findViewById(R.id.booking_datetime);
            vehicle_name_view = (TextView) view.findViewById(R.id.vehicle_name);
            total_distance_view = (TextView) view.findViewById(R.id.total_distance);
            total_time_view = (TextView) view.findViewById(R.id.total_time);
            total_fare_view = (TextView) view.findViewById(R.id.total_fare);
            return view;
        }
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        controller = new DBController(getContext());
        super.onActivityCreated(savedInstanceState);
        confirm_boking.setOnClickListener(this);
        if(this.getArguments()!= null) {
            Bundle bundle = this.getArguments();
            pickup_address = Fn.getValueFromBundle(bundle,"selected_pickup_address");
            dropoff_address = Fn.getValueFromBundle(bundle, "selected_dropoff_address");
            booking_datetime = Fn.getValueFromBundle(bundle, "selected_booking_datetime");
            material_weight = Fn.getValueFromBundle(bundle, "selected_material_weight");
            material_image = Fn.getValueFromBundle(bundle, "selected_material_image");
            this.getArguments().clear();
            Fn.logD("booking_datetime:", booking_datetime);
        }
        selected_vehicle =  Fn.getPreference(getActivity(), "selected_vehicle");
        String URL = makeURL(pickup_address,dropoff_address);
        Fn.logD("URL:", URL);
        HashMap<String,String> hashMap = new HashMap<String,String>();
        sendVolleyRequest(URL,Fn.checkParams(hashMap),"googleAPI");
    }
    public String makeURL(String pickup_address, String dropoff_address){
        StringBuilder urlString = new StringBuilder();
        try {
            urlString.append(" https://maps.googleapis.com/maps/api/distancematrix/json?units=metric");
            urlString.append("&origins=");// from
            urlString.append(URLEncoder.encode(pickup_address, "UTF-8"));
            urlString.append("&destinations=");// to
            urlString.append(URLEncoder.encode(dropoff_address,"UTF-8"));
            urlString.append("&key="+URLEncoder.encode(getResources().getString(R.string.server_APIkey1), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return urlString.toString();
    }
    @Override
    public void onClick(View v) {
//        Fn.showProgressDialog(Constants.Message.LOADING,getActivity());
        String confirm_booking_url = Constants.Config.ROOT_PATH+"confirm_booking";
        String user_token = Fn.getPreference(getActivity(),"user_token");
        HashMap<String,String> hashMap = new HashMap<String,String>();
        hashMap.put("user_token",user_token);
        hashMap.put("vehicle_type",selected_vehicle);
        hashMap.put("pickup_point",pickup_address);
        hashMap.put("dropoff_point",dropoff_address);
        hashMap.put("booking_date",actual_booking_datetime);
        hashMap.put("total_distance",journey_distance);
        hashMap.put("total_time",journey_duration);
        hashMap.put("fare_max", String.valueOf(total_fare_max));
        hashMap.put("fare_min", String.valueOf(total_fare_min));
        hashMap.put("material_weight", String.valueOf(material_weight));
        hashMap.put("material_image", String.valueOf(material_image));
        sendVolleyRequest(confirm_booking_url,Fn.checkParams(hashMap),"confirm_booking");
    }
    public void sendVolleyRequest(String URL, final HashMap<String,String> hMap, final String method){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Fn.logD("onResponse", String.valueOf(response));
                //Fn.logD("onResponse_booking_status", String.valueOf(response));
                if(method.equals("googleAPI")) {
                    DistanceRecieveSuccess(response);
                }else if(method.equals("confirm_booking")){
                    Fn.logD("confirm_booking_called", "confirm_booking_called");
                    String trimmed_response = response.substring(response.indexOf("{"));
                    Fn.logD("trimmed_response", trimmed_response);
                    confirmBookingSuccess(trimmed_response);
                }
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
        Fn.addToRequestQue(requestQueue, stringRequest, getActivity());
    }
    private void ErrorDialog(String Title,String Message){
        Fn.showDialog(getActivity(), Title, Message);
    }
    protected void DistanceRecieveSuccess(String response)  {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray rows = jsonObject.getJSONArray("rows");
            JSONObject firstObject = rows.getJSONObject(0);
            JSONArray elements = firstObject.getJSONArray("elements");
            JSONObject elementsFirst = elements.getJSONObject(0);
            JSONObject distanceObject = elementsFirst.getJSONObject("distance");
            journey_distance = distanceObject.getString("text");
            double distancef =  ((distanceObject.getDouble("value"))/1000);
            double tot_dist=distancef;
            Fn.logD("DISTANCE TO TRAVEL", String.valueOf(distancef));
            JSONObject durationObject = elementsFirst.getJSONObject("duration");
            journey_duration = durationObject.getString("text");
            double durationf = ((durationObject.getDouble("value"))/60);
            Fn.logD("DURATION OF TRAVEL", String.valueOf(durationf));
//            int i = Integer.parseInt(selected_vehicle);
            //System.out.println(i);
            int city_id = Integer.parseInt(Fn.getPreference(getActivity(), Constants.Keys.CITY_ID));
            SQLiteDatabase database = controller.getWritableDatabase();
            String query_truck = "SELECT "+controller.VEHICLE_NAME+" FROM "+controller.TABLE_VIEW_VEHICLE_TYPE+
                    " WHERE "+controller.VEHICLETYPE_ID+" = "+selected_vehicle;
            String query1="SELECT "+controller.BASE_FARE+", "+controller.TRANSIT_CHARGE+" FROM "+controller.TABLE_VIEW_BASE_FARE+" WHERE "
                    +controller.VEHICLETYPE_ID+"="+ selected_vehicle+" AND "+controller.CITY_ID+"="+ city_id;
            String query2 = "SELECT "+controller.FROM_DISTANCE+", "+controller.TO_DISTANCE+", "+controller.PRICE_KM+" FROM "+controller.TABLE_VIEW_PRICING+" WHERE "
                    +controller.VEHICLETYPE_ID+"="+selected_vehicle+" AND "+controller.CITY_ID+"="+ city_id +" ORDER BY "+controller.TO_DISTANCE+" ASC";
            String querycnt = "SELECT COUNT(*) FROM view_vehicle_type WHERE vehicletype_id ="+selected_vehicle;
            Cursor cnt = database.rawQuery(querycnt,null);
            if(cnt.moveToFirst())
            {
                active=cnt.getInt(0);
                Fn.logD("to check whether the vehicletype_id exists",cnt.getString(0)+active);
            }
            if(active>0) {
                try{
                    Cursor q1 = database.rawQuery(query_truck, null);
                    if (q1.moveToFirst()) {
                       Fn.SystemPrintLn("gtt");
                        Fn.logD(" TRUCK_NAME", q1.getString(0));
                        truck_name=q1.getString(0);
                    }
                }
                catch (Exception e)
                {

                }
                try {

                    Cursor q1 = database.rawQuery(query1, null);
                    if (q1.moveToFirst())
                    {
                        Fn.logD(" BASE FARE AND TRANSIT CHARGE", q1.getFloat(0) + " AND " + q1.getFloat(1));
                        base_fare_min=q1.getDouble(0)+(q1.getDouble(1)*durationf);
                        base_fare_max=  (q1.getDouble(0)+(q1.getDouble(1)*durationf*2.5));
                    }
                } catch (Exception e) {
                    Fn.logE("ERROR", "DATABASE CURSOR FOR QUERY1");
                }
                try {
                    Cursor q2 = database.rawQuery(query2, null);
                    if (q2.moveToFirst()) {

                        do {
                            Fn.logD(" FROM DISTANCE, TO DISTANCE AND PRICE PER KM", q2.getDouble(0) + ", " + q2.getFloat(1) + " AND " + q2.getFloat(2));
                            double separation = q2.getDouble(1)-q2.getDouble(0);
                            if(tot_dist>0){
                                if(tot_dist>separation)
                                {
                                    distance_fare=distance_fare+separation*q2.getDouble(2);
                                    tot_dist=tot_dist-separation;
                                }
                                else
                                {
                                    distance_fare=distance_fare+tot_dist*q2.getDouble(2);
                                    tot_dist=0;
                                }
                            }
                        } while (q2.moveToNext());
                    }
                } catch (Exception e) {

                    Fn.logE("ERROR", "DATABASE CURSOR FOR QUERY2");
                }
                database.close();
                total_fare_max=distance_fare+base_fare_max;
                total_fare_min=distance_fare+base_fare_min;
                Fn.logD(" "+distance_fare," "+Math.round(base_fare_max)+" "+base_fare_min);
                // total_fare.setText( Math.round(total_fare_min)+" - "+Math.round(total_fare_max));
            }
            d=new Date();
            try {
                DateFormat f = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                Date d = f.parse(booking_datetime);
                date = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
                actual_booking_datetime = date.format(d);
//                time = new SimpleDateFormat("hh:mm a");
//                System.out.println("Date: " + date.format(d));
//                System.out.println("Time: " + time.format(d));
            } catch (ParseException e) {
                e.printStackTrace();
            }
//
            pickup_point_view.setText(pickup_address);
            dropoff_point_view.setText(dropoff_address);
            booking_datetime_view.setText(Fn.getDateName(String.valueOf(booking_datetime)));
            vehicle_name_view.setText(truck_name);
            total_distance_view.setText(journey_distance);
            total_time_view.setText(journey_duration);
            total_fare_view.setText(Math.round(total_fare_min)+" - "+Math.round(total_fare_max)+"  Rs");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    protected void confirmBookingSuccess(String response){
//        Fn.logD("confirm_booking_responsed", "confirm_booking_responsed");
        if(!Fn.CheckJsonError(response)){
            try {
                JSONObject jsonObject = new JSONObject(response);
                //jsonArray = jsonObject.getJSONArray("likes");
                String errFlag = jsonObject.getString("errFlag");
                String errMsg = jsonObject.getString("errMsg");
                Fn.logD("errFlag",errFlag);
                Fn.logD("errMsg",errMsg);
                    if(jsonObject.has("likes")) {
                        JSONArray  jsonArray = jsonObject.getJSONArray("likes");
//                     Fn.Toast(this,errMsg);
                        Fn.logD("toastdone", "toastdone");
                        int count = 0;
                        while (count < jsonArray.length())
                        {
                            Fn.logD("likes_entered", "likes_entered");
                            JSONObject JO = jsonArray.getJSONObject(count);
                            String  crn_no = JO.getString("crn_no");
                            Fragment fragment = new BookingDetails();
                            Bundle bundle = new Bundle();
                            bundle.putString("crn_no",crn_no);
                            Fn.logD("passed_crn_no", crn_no);
                            fragment.setArguments(Fn.CheckBundle(bundle));
                            FragmentManager fragmentManager =FullActivity.fragmentManager;
                            FragmentTransaction transaction = fragmentManager.beginTransaction();
                            //                Fragment fragment = new BookNow();
                            transaction.replace(R.id.main_content, fragment,Constants.Config.CURRENT_FRAG_TAG);
                            if((FullActivity.homeFragmentIndentifier == -5)){
                                transaction.addToBackStack(null);
                                FullActivity.homeFragmentIndentifier =  transaction.commit();
                            }else{
                                transaction.commit();
                                Fn.logD("fragment instanceof Book","homeidentifier != -1");
                            }
                            ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Booking Details");
                            count++;
                        }
                    }
                    else
                    {
                        Fn.Toast(getActivity(),Constants.Message.NEW_USER_ENTER_DETAILS);
                    }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            ErrorDialog(Constants.Title.SERVER_ERROR,Constants.Message.SERVER_ERROR);
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        Fn.stopAllVolley(requestQueue);
    }
    @Override
    public void onResume() {
        super.onResume();
        Fn.startAllVolley(requestQueue);
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Fn.cancelAllRequest(requestQueue,TAG);

    }
}
