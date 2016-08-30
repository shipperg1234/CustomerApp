package in.co.theshipper.www.shipper_customer;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationListener;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import static com.google.android.gms.internal.zzir.runOnUiThread;

/**
 * A simple {@link Fragment} subclass.
 */
public class Book extends Fragment implements View.OnClickListener {

    protected final String get_vehicle_url = Constants.Config.ROOT_PATH+"get_available_vehicle";
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    protected RequestQueue requestQueue;
    protected HashMap<String,String> hashMap;
    private View view;
    private String TAG = FullActivity.class.getName();
    private boolean stopTimer = false;
    private Location location;
    private SupportMapFragment mMapFragment;
    private GoogleMap mMap = null;
    private Double lattitude, longitude;
    private Button bookNow, bookLater;
    private TextView error_message;
    private LinearLayout lower_view;
    private Timer timer;
    public Context context;
    private int vehicle_type = 0;
    private String cached_json_response = "";
    protected  DBController controller;
    protected SQLiteDatabase database;
    private RadioGroup radiogrp;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Fn.logE("BOOK_FRAGMENT_LIFECYCLE", "onAttach Called");
        this.context = context;
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fn.logE("BOOK_FRAGMENT_LIFECYCLE", "onCreate Called");
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Fn.logE("BOOK_FRAGMENT_LIFECYCLE", "onCreateView Called");
        if (container == null) {
            return null;
        } else {
            view = inflater.inflate(R.layout.fragment_book, container, false);
            bookNow = (Button) view.findViewById(R.id.book_now);
            bookLater = (Button) view.findViewById(R.id.book_later);
            radiogrp = (RadioGroup) view.findViewById(R.id.check_box_selector);
            error_message = (TextView) view.findViewById(R.id.error_message);
            lower_view = (LinearLayout) view.findViewById(R.id.lower_view);
            return view;
        }
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Fn.logD("onViewCreated", "onViewCreated");
        bookNow.setEnabled(false);
        bookLater.setEnabled(false);
        mMapFragment = SupportMapFragment.newInstance();
        getChildFragmentManager().beginTransaction().replace(R.id.map, mMapFragment, "MAP_FRAGMENT").commit();
        TimerProgramm();
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        bookNow.setOnClickListener(this);
        bookLater.setOnClickListener(this);
        Fn.logD("ENTERED", "ONACTIVITYCREATED OF BOOK()");
        controller = new DBController(getActivity());
        database = controller.getWritableDatabase();
        String query = "SELECT " + controller.VEHICLETYPE_ID + "," + controller.VEHICLE_NAME + " FROM " + controller.TABLE_VIEW_VEHICLE_TYPE;
        final Cursor c = database.rawQuery(query, null);
        final long cnt = DatabaseUtils.queryNumEntries(database, controller.TABLE_VIEW_VEHICLE_TYPE);
        long count = cnt;

        if (count > 0) {
            final int id[]=new int[(int) count];
            c.moveToFirst();
            while (count > 0) {
                Fn.logD("Entered","count>0 for adding vehicletypes to book()");
                RadioButton b = new RadioButton(getContext());
                b.setLayoutParams(new ActionBar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                b.setButtonDrawable(Fn.getVehicleImage(c.getInt(0)));
                radiogrp.addView(b);
                id[(int) (cnt-count)]=c.getInt(0);
                final long finalCount = count;
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        vehicle_type =id[(int) (cnt- finalCount)];
//                        Toast.makeText(context, "vehicle type" + vehicle_type, Toast.LENGTH_LONG).show();
                        mMap.clear();
                        bookLater.setEnabled(true);
                        LocationChanged();
                    }
                });
                count--;
                try{c.moveToNext();}
                catch (Exception e){Fn.logE("ERROR","PARSING THE CURSOR FOR THE VEHICLE_TYPE");}
            }

        }
    }
    @Override
    public void onClick(View view) {
        bookNow.setEnabled(false);
        switch (view.getId()) {
            case R.id.book_now:
                BookNow();
                break;
            case R.id.book_later:
                BookLater();
                break;
        }
//        Toast.makeText(context, "vehicle type" + vehicle_type, Toast.LENGTH_LONG).show();
    }
    public void BookNow() {

            Fn.putPreference(context, "selected_vehicle", String.valueOf(vehicle_type));
            FragmentManager fragmentManager =FullActivity.fragmentManager;
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            Fragment fragment = new BookNow();
            transaction.replace(R.id.main_content, fragment, Constants.Config.CURRENT_FRAG_TAG);
            if((FullActivity.homeFragmentIndentifier == -5)){
                transaction.addToBackStack(null);
                FullActivity.homeFragmentIndentifier =  transaction.commit();
            }else{
                transaction.commit();
                Fn.logD("fragment instanceof Book","homeidentifier != -1");
            }
        if(getActivity()!=null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_book_now_fragment);
        }
    }
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void BookLater() {
            Fn.putPreference(context, "selected_vehicle", String.valueOf(vehicle_type));
            FragmentManager fragmentManager =FullActivity.fragmentManager;
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            Fragment fragment = new BookLater();
            transaction.replace(R.id.main_content, fragment,Constants.Config.CURRENT_FRAG_TAG);
            if((FullActivity.homeFragmentIndentifier == -5)){
                transaction.addToBackStack(null);
                FullActivity.homeFragmentIndentifier =  transaction.commit();
            }else{
                transaction.commit();
                Fn.logD("fragment instanceof Book","homeidentifier != -1");
            }
        if(getActivity()!=null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_book_later_fragment);
        }
            Fn.logD("BookLater", "BookLater");
    }
    private void setUpMapIfNeeded() {
        if(getActivity()!=null) {
            Fn.logD("map_setup", "map_setup");
            // Do a null check to confirm that we have not already instantiated the map.
            if (mMap == null) {
                // Try to obtain the map from the SupportMapFragment.
//            mMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentByTag("MAP_FRAGMENT");
                Fn.logD("mMapFragment", String.valueOf(mMapFragment));
                mMap = mMapFragment.getMap();
                // Check if we were successful in obtaining the map.
                Fn.logD("map_not_null", String.valueOf(mMap));
                if (mMap != null) {
                    if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    mMap.setMyLocationEnabled(true);
                    if (FullActivity.mGoogleApiClient.isConnected()) {
                        Fn.logD("mGoogleApiClient", "true");
                        do {
                            location = Fn.getAccurateCurrentlocation(FullActivity.mGoogleApiClient, getActivity());
                        } while (location == null);
                        if (location != null) {
                            LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());// This methods gets the users current longitude and latitude.
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));//Moves the camera to users current longitude and latitude
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, Constants.Config.MAP_SMALL_ZOOM_LEVEL));//Animates camera and zooms to preferred state on the user's current location.
                        }
                    }
                }
            }
        }
}
    /**** The mapfragment's id must be removed from the FragmentManager
     **** or else if the same it is passed on the next time then
     **** app will crash ****/
    public void TimerProgramm() {
        Fn.logD("TimerProgram", "TimerProgram");
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Fn.logD("TimerProgram_running", "TimerProgram_running");
                        if (!stopTimer) {
                            Fn.SystemPrintLn("****Vehicle type " + vehicle_type);
                            if (vehicle_type != 0) {
                                LocationChanged();
                            }
                        }
                    }
                });
            }
        }, Constants.Config.GET_DRIVER_LOCATION_DELAY, Constants.Config.GET_DRIVER_LOCATION_PERIOD);
    }
    public void LocationChanged() {
        //bookLater.setEnabled(true);
        try {
            if(FullActivity.mGoogleApiClient.isConnected()) {
                    Fn.logD("mGoogleApiClient", "true");
                    do{
                        if(getActivity()!=null) {
                            location = Fn.getAccurateCurrentlocation(FullActivity.mGoogleApiClient, getActivity());
                        }
                    }while(location == null);
                    if (location != null) {
                        Fn.logD("location_not_null", "location_not_null");
                        lattitude = location.getLatitude();
                        longitude = location.getLongitude();
                        HashMap<String, String> hashMap = new HashMap<String, String>();
                        hashMap.put("vehicle_type", String.valueOf(vehicle_type));
                        hashMap.put("current_lat", String.valueOf(lattitude));
                        hashMap.put("current_lng", String.valueOf(longitude));
                        sendVolleyRequest(get_vehicle_url, Fn.checkParams(hashMap));
                    }
            }else{
                error_message.setText(Constants.Message.SERVER_ERROR);
                error_message.setVisibility(View.VISIBLE);
                lower_view.setVisibility(View.GONE);
                if(mMap != null){
                    mMap.clear();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void sendVolleyRequest(String URL, final HashMap<String,String> hMap){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Fn.logD("onResponse", String.valueOf(response));
                vehicleFindSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Fn.logD("onErrorResponse", String.valueOf(error));
                error_message.setText(Constants.Message.NETWORK_ERROR);
                error_message.setVisibility(View.VISIBLE);
                lower_view.setVisibility(View.GONE);
                if(mMap != null){
                    mMap.clear();
                }
            }
        }){
            @Override
            protected HashMap<String,String> getParams(){
                return hMap;
            }
        };
        stringRequest.setTag(TAG);
        if(getActivity()!=null) {
            Fn.addToRequestQue(requestQueue, stringRequest, getActivity());
        }
    }
    public void vehicleFindSuccess(String response) {
        if(cached_json_response != response){
            Fn.logD("previous_cached_response",cached_json_response);
            Fn.logD("present_cached_response",response);
            cached_json_response = response;
            if (!Fn.CheckJsonError(response)) {
                error_message.setVisibility(View.GONE);
                lower_view.setVisibility(View.VISIBLE);
//                JSONObject jsonObject;
//                JSONArray jsonArray;
//                String received_current_lat, received_current_lng, json_string, errFlag, errMsg;
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String errFlag = jsonObject.getString("errFlag");
                    String errMsg = jsonObject.getString("errMsg");
                    Fn.logD("errFlag", errFlag);
                    Fn.logD("errMsg", errMsg);
                    //jsonArray = jsonObject.getJSONArray("likes");
                    if (errFlag.equals("1")) {
                        // Toast.makeText(ctx, errMsg, Toast.LENGTH_LONG).show();
                        Fn.logD("toastNotdone", "toastNotdone");
                    } else if (errFlag.equals("0")) {
                        if (jsonObject.has("likes")) {
                            JSONArray jsonArray = jsonObject.getJSONArray("likes");
                            //Toast.makeText(ctx,errMsg,Toast.LENGTH_LONG).show();
                            Fn.logD("toastdone", "toastdone");
                            int count = 0;
                            if(mMap != null) {
                                mMap.clear();
                            }
                            bookNow.setEnabled(true);
                            int marker_image = Fn.getMarkerImage(vehicle_type);
    //                        bookNow.setTextColor(R.color.pure_white);
                            while (count < jsonArray.length()) {
                                Fn.logD("likes_entered", "likes_entered");
                                JSONObject JO = jsonArray.getJSONObject(count);
                                String received_current_lat = JO.getString("location_lat");
                                String received_current_lng = JO.getString("location_lng");
                                Fn.logD("received_current_lat", received_current_lat);
                                Fn.logD("received_current_lng", received_current_lng);
                                //received_useremail = JO.getString("email");
                                if(mMap != null) {
                                    mMap.addMarker(new MarkerOptions()
                                            .position(new LatLng(Double.parseDouble(received_current_lat), Double.parseDouble(received_current_lng)))
                                            .icon(BitmapDescriptorFactory.fromResource(marker_image))
                                            .title("Hello world"));
                                }
                                count++;
                            }
                        } else {
                            bookNow.setEnabled(false);
    //                        bookNow.setTextColor(R.color.pure_white);
                            //Toast.makeText(ctx,errMsg, Toast.LENGTH_LONG).show();
                        }
//                        Fn.logE("BOOK_KNOW_FRAGMENT_LIFECYCLE", "onPostExecute Called");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else{
                error_message.setText(Constants.Message.SERVER_ERROR);
                error_message.setVisibility(View.VISIBLE);
                lower_view.setVisibility(View.GONE);
                if(mMap != null){
                    mMap.clear();
                }
            }
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(getActivity()!=null) {
            switch (requestCode) {
// Check for the integer request code originally supplied to startResolutionForResult().
                case REQUEST_CHECK_SETTINGS:
                    switch (resultCode) {
                        case Activity.RESULT_OK:
//                        startLocationUpdates();
//                        TimerProgramm();
                            break;
                        case Activity.RESULT_CANCELED:
                            Fn.showGpsAutoEnableRequest(FullActivity.mGoogleApiClient, getActivity());//keep asking if imp or do whatever
                            break;
                    }
                    break;
            }
        }
    }
    @Override
    public void onStart() {
        super.onStart();
        setUpMapIfNeeded();
    }
    @Override
    public void onResume() {
        super.onResume();
        Fn.startAllVolley(requestQueue);
        stopTimer = false;
        Fn.logE("BOOK_FRAGMENT_LIFECYCLE", "onResume Called");
    }
    @Override
    public void onPause() {
        super.onPause();
        Fn.logE("BOOK_FRAGMENT_LIFECYCLE", "onPause Called");
        Fn.stopAllVolley(requestQueue);
        stopTimer = true;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(timer != null){
            timer.cancel();
            timer = null;
        }
        Fn.cancelAllRequest(requestQueue, TAG);
        if (mMap != null) {
            mMap = null;
        }
        database.close();
//        FullActivity.fragmentManager.beginTransaction().remove(getChildFragmentManager().findFragmentByTag("MAP_FRAGMENT")).commitAllowingStateLoss();
        Fn.logE("BOOK_FRAGMENT_LIFECYCLE", "onDestroyView Called");
    }
    @Override
    public void onDetach() {
        super.onDetach();
        Fn.logE("BOOK_FRAGMENT_LIFECYCLE", "onDetach Called");
    }
}
