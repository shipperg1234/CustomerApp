package in.co.theshipper.www.shipper_customer;

import android.app.Application;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Shubham on 19/07/2016.
 */

public class GlobalClass extends Application {
    private String current_response;
    private String future_response;
    private String finished_response;

    public void setCurrent_response(String s){
        current_response = s;
    }

    public void setFuture_response(String s){
        future_response = s;
    }

    public void setFinished_response(String s){
        finished_response = s;
    }

    public String getCurrent_response(){
        return current_response;
    }

    public String getFuture_response(){
        return future_response;
    }

    public String getFinished_response(){
        return finished_response;
    }
}
