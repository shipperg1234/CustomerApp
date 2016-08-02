package in.co.theshipper.www.shipper_customer;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by Apekshit on 02-07-2016.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class myDialog extends DialogFragment {
    View v;
    ImageView popup;
    Dialog d;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder b=new AlertDialog.Builder(getActivity());
        View v=getActivity().getLayoutInflater().inflate(R.layout.dialog, null);
         d=b.create();
        popup=(ImageView)d.findViewById(R.id.image_popup);
        popup.setImageResource(R.drawable.vehicle_1_on);
        b.setView(v);
        return d;
    }
}


