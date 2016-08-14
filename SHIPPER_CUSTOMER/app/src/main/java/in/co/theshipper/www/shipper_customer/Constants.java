package in.co.theshipper.www.shipper_customer;

public final class Constants {
    public static final class Config{
//        protected static final String ROOT_PATH = "http://theshipper.ml/";
//        protected static final String ROOT_PATH = "http://theshipper.ml/loader_mobile/";
        protected static final String ROOT_PATH = "http://www.theshipper.co.in/loader_mobile/";
//        public static final String ROOT_PATH = "http://52.77.190.248/";
        protected static final int UPDATE_CUSTOMER_LOCATION_DELAY = 0*10000;
        protected static final int UPDATE_CUSTOMER_LOCATION_PERIOD = 30*1000;
        protected static final int GET_DRIVER_LOCATION_DELAY = 0*10000;
        protected static final int GET_DRIVER_LOCATION_PERIOD = 30*1000;
        protected static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0;
        protected static final long MIN_TIME_BW_UPDATES = 0;
        protected static final long MIN_DATE_DURATION = 1*1000;
        protected static final long MAX_DATE_DURATION = 3*24*60*60*1000;
        protected static final String SUPPORT_CONTACT = "08276097972";
        protected static final int NAME_FIELD_LENGTH = 50;
        protected static final int ADDRESS_FIELD_LENGTH = 50;
        protected static final int DELAY_AFTER_TOAST = 10;
        protected static final int DELAY_LOCATION_CHECK = 0*100;
        protected static final int PERIOD_LOCATION_CHECK  = 2*100;
        protected static final float MAP_HIGH_ZOOM_LEVEL = 17;
        protected static final float MAP_MID_ZOOM_LEVEL = 15;
        protected static final float MAP_SMALL_ZOOM_LEVEL = 13;
        protected static final int IMAGE_WIDTH = 500;
        protected static final int IMAGE_HEIGHT = 500;
        protected static final String CURRENT_FRAG_TAG = "current_fragment";
        protected static final int FLASH_TO_MAIN_DELAY = 3*1000;
        protected static final int GPS_INTERVAL = 2*1000;
        protected static final int GPS_FASTEST_INTERVAL = 1*1000;
        protected static final int PROGRESSBAR_DELAY = 2*1000;
        protected static final int BOOK_LATER_DELAY = 15*60*1000;
    }
    public static final class Message{
        protected static final String NEW_USER_ENTER_DETAILS = "Please enter your details";
        protected static final String NO_CURRENT_BOOKING = "No Current Booking";
        protected static final String VEHICLE_ALLOCATION_PENDING = "Vehicle Allocation Pending";
        protected static final String DRIVER_FOUND = "Driver Found";
        protected static final String NETWORK_ERROR = "Unable to connect to server.Check your Internet Connection";
        protected static final String SERVER_ERROR = "Server not responding to request";
        protected static final String GPS_NOT_ENABLED = "GPS not enabled !!";
        protected static final String INTERNET_NOT_ENABLED = "Internet not enabled!!";
        protected static final String CONNECTING = "Connecting...";
        protected static final String LOADING = "Loading...";
        protected static final String OTP_VERIFICATION_ERROR = "OTP could not be verified";
        protected static final String FORM_ERROR = "Form contains error";
        protected static final String TRACKING_ERROR = "Error while updating location";
        protected static final String NO_SERVICE = "Sorry we do not provide service in your area !!";
        protected static final String INVALID_DATETIME = "Form Contains Invalid Date Field";
        protected static final String INVALID_ADDRESS = "Form Contains Invalid Address Field(s)";
        protected static final String EMPTY_IMAGE = "Item Image Required";

    }
    public static final class Title{
        protected static final String NETWORK_ERROR = "NETWORK ERROR";
        protected static final String SERVER_ERROR = "SERVER ERROR";
        protected static final String OTP_VERIFICATION_ERROR = "VERIFICATION ERROR";
        protected static final String NO_SERVICE = "NO SERVICE AVAILABLE";
        protected static final String BOOKING_DATETIME = "SELECT BOOKING DATE TIME";

    }
    public static final class Keys{
        protected static final String VEHICLETYPE_ID = "vehicletype_id";
        protected static final String SELECTED_VEHICLETYPE = "selected_vehicletype";
        protected static final String USER_TOKEN = "user_token";
        protected static final String USER_ID = "user_id";
        protected static final String CITY_ID = "city_id";
        protected static final String EXACT_PICKUP_POINT = "exact_pickup_point";
        protected static final String EXACT_DROPOFF_POINT = "exact_dropoff_point";
        protected static final String ERR_MSG = "errMsg";
        protected static final String ERR_FLAG = "errFlag";
        protected static final String LIKES = "likes";
        protected static final String LATER_BOOKING_DATETIME = "later_booking_datetime";
        protected static final String MY_CURRENT_ADDRESS = "my_current_address";
    }
}
