package in.co.theshipper.www.shipper_customer;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class BookNow extends Fragment implements View.OnClickListener {
    private View view;
    private PlaceAutocompleteFragment pickup_point, dropoff_point;
    private Button get_quote;
    private String pickup_address, dropoff_address, pickuppoint_name, dropoffpoint_name, booking_datetime,FullAddress;
    private DatePicker datePicker;
    private TimePicker timePicker;
    private Location location;
    private LatLng southwest,northeast;
    private ImageView material_image;
    private Spinner weight_spinner;
    private String[] weight_string_array;
    private ArrayList<String> weight_list;
    private Bitmap materialimage = null;
    private static int RESULT_LOAD_IMAGE = 1;
    private static int CAMERA_PIC_REQUEST= 0;

    public BookNow() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (container == null) {
            return null;
        } else {

            view = inflater.inflate(R.layout.fragment_book_now, container, false);
            get_quote = (Button) view.findViewById(R.id.get_quote);
            material_image = (ImageView) view.findViewById(R.id.material_image);
            weight_spinner = (Spinner) view.findViewById(R.id.weight_spinner);
            return view;
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(getActivity() != null) {
        if (FullActivity.mGoogleApiClient.isConnected()) {
            location = Fn.getAccurateCurrentlocation(FullActivity.mGoogleApiClient, getActivity());
            if (location != null) {
                southwest = new LatLng(location.getLatitude() - 2, location.getLongitude() - 2);
                northeast = new LatLng(location.getLatitude() + 2, location.getLongitude() + 2);
            }
        }
/*
* The following code example shows setting an AutocompleteFilter on a PlaceAutocompleteFragment to
* set a filter returning only results with a precise address.
*/
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_NONE)
                .build();
        if (pickup_point == null) {
            Fn.logD("BOOK_LATER_FRAGMENT", "autocompleteFragment_null");
            pickup_point = (PlaceAutocompleteFragment) getActivity().getFragmentManager().findFragmentById(R.id.pickup_point);
            pickup_point.setHint(getResources().getString(R.string.hint_pickup_point));
            Fn.logD("pickup_point_fragment", String.valueOf(pickup_point));
            pickup_point.setFilter(typeFilter);
            if ((southwest != null)) {
                Fn.SystemPrintLn("******haha**my curn loc is : " + southwest.longitude + " " + southwest.latitude);
                pickup_point.setBoundsBias(new LatLngBounds(southwest, northeast));
            }
            pickup_point.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(Place place) {
                    Fn.logD("BOOK_LATER_FRAGMENT", "onPlaceSelected");
                    pickuppoint_name = (String) place.getName();
                    pickup_address = (String) place.getAddress();
                }

                @Override
                public void onError(Status status) {
                    Fn.logD("BOOK_LATER_FRAGMENT", "onError");
                    // TODO: Handle the error.
                    Fn.logD("BOOK_LATER_FRAGMENT", "An error occurred: " + status);
                }
            });
//            pickup_point.setHint("Pickup Point");
        }
        if (dropoff_point == null) {
            Fn.logD("BOOK_LATER_FRAGMENT", "autocompleteFragment_null");
            dropoff_point = (PlaceAutocompleteFragment) getActivity().getFragmentManager().findFragmentById(R.id.dropoff_point);
            dropoff_point.setHint(getResources().getString(R.string.hint_dropoff_point));
            dropoff_point.setFilter(typeFilter);
            if ((southwest != null)) {
                dropoff_point.setBoundsBias(new LatLngBounds(southwest, northeast));
            }
            dropoff_point.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(Place place) {
                    Fn.logD("BOOK_LATER_FRAGMENT", "onPlaceSelected");
                    dropoffpoint_name = (String) place.getName();
                    dropoff_address = (String) place.getAddress();
                }

                @Override
                public void onError(Status status) {
                    Fn.logD("BOOKNOW_FRAGMENT", "onError");
                    // TODO: Handle the error.
                    Fn.logD("BOOK_LATER_FRAGMENT", "An error occurred: " + status);
                }
            });
        }
    }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        if(getActivity() != null) {
            String selected_vehicle_type = Fn.getPreference(getActivity(), "selected_vehicle");
            String[] weight_string_array = Fn.getWeightList(getActivity(), selected_vehicle_type);
            ArrayList<String> weight_list = new ArrayList(Arrays.asList(weight_string_array));
            ArrayAdapter<String> weight_adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, weight_list);
            weight_spinner.setAdapter(weight_adapter);
            material_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(i, RESULT_LOAD_IMAGE);
                }
            });

            Fn.logD("BOOKNOW_FRAGMENT", "onActivityCreated");
            get_quote.setOnClickListener(this);
        }
    }
    @Override
    public void onClick (View v){
        booking_datetime = Fn.getDateTimeNow();
        Bundle bundle = new Bundle();
        String imgstring="";
        if(materialimage==null)
            if(getActivity() != null) {
                Fn.ToastShort(getActivity(), Constants.Message.EMPTY_IMAGE);
            }
        else {
                imgstring = Fn.getStringImage(materialimage);
            if (isValid(pickup_address, dropoff_address)) {
                bundle.putString("selected_pickup_address", pickup_address);
                bundle.putString("selected_dropoff_address", dropoff_address);
                bundle.putString("selected_booking_datetime", booking_datetime);
                bundle.putString("selected_material_weight", weight_spinner.getSelectedItem().toString());
                bundle.putString("selected_material_image", imgstring);
                FragmentManager fragmentManager = FullActivity.fragmentManager;
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                Fragment fragment = new ConfirmBooking();
                fragment.setArguments(Fn.CheckBundle(bundle));
                transaction.replace(R.id.main_content, fragment, Constants.Config.CURRENT_FRAG_TAG);
                if ((FullActivity.homeFragmentIndentifier == -5)) {
                    FullActivity.homeFragmentIndentifier = transaction.commit();
                } else {
                    transaction.commit();
                    Fn.logD("fragment instanceof Book", "homeidentifier != -1");
                }
                if(getActivity() != null) {
                    ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_confirm_booking_fragment);
                }
            } else {
                if(getActivity() != null) {
                    Fn.ToastShort(getActivity(), Constants.Message.INVALID_ADDRESS);
                }
            }
        }
    }
    private boolean isValid(String pickup,String dropoff){
        if(pickup==null)
            return false;
        if(dropoff==null)
            return false;
        if(pickup.length()==0)
            return false;
        if(dropoff.length()==0)
            return false;
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(getActivity() != null) {
            if (requestCode == RESULT_LOAD_IMAGE && resultCode == getActivity().RESULT_OK && null != data) {
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getActivity().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();
//            Bitmap bmp = null;
                try {
                    Uri reduceSizePath = Fn.getImageContentUri(getActivity(), Fn.decodeFile(picturePath, Constants.Config.IMAGE_WIDTH, Constants.Config.IMAGE_HEIGHT));
                    materialimage = getBitmapFromUri(reduceSizePath);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                material_image.setImageBitmap(materialimage);
            }
        }
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        //TODO intitialise bitmap
        ParcelFileDescriptor parcelFileDescriptor = getActivity().getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Fn.logD("BOOK_LATER_FRAGMENT", "onDestroyView called");
        getActivity().getFragmentManager().beginTransaction().remove(getActivity().getFragmentManager().findFragmentById(R.id.pickup_point)).commitAllowingStateLoss();
        getActivity().getFragmentManager().beginTransaction().remove(getActivity().getFragmentManager().findFragmentById(R.id.dropoff_point)).commitAllowingStateLoss();

    }
}