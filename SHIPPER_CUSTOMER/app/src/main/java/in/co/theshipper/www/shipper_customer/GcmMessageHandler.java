package in.co.theshipper.www.shipper_customer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import com.google.android.gms.gcm.GcmListenerService;

/**
 * Created by GB on 12/7/2015.
 */

public class GcmMessageHandler extends GcmListenerService {
    public static final int MESSAGE_NOTIFICATION_ID = 435345;
    public static final int PRIORITY_HIGH = 5;

    @Override
    public void onMessageReceived(String from, Bundle data) {
//        String message = data.getString("message");
        createNotification(from, data);
        data.clear();
    }

    // Creates notification based on title and body received
    private void createNotification(String title, Bundle body) {

        Fn.SystemPrintLn("message_body" + String.valueOf(body));
        Bundle bundle = new Bundle();
        String menu_fragment = Fn.getValueFromBundle(body, "menuFragment");
        bundle.putString("menuFragment",menu_fragment);
        bundle.putString("method", "push");
        String message =  Fn.getValueFromBundle(body, "message");
        String push_title =  Fn.getValueFromBundle(body, "title");
        if(menu_fragment.equals("BookingDetails")){
            bundle.putString("crn_no", Fn.getValueFromBundle(body, "crn_no"));
        }else if(menu_fragment.equals("FinishedBookingDetails")){
            bundle.putString("crn_no", Fn.getValueFromBundle(body, "crn_no"));
        }
        body.clear();
        Intent i = new Intent(this, FullActivity.class);
        i.putExtras(Fn.CheckBundle(bundle));
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pi = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
        Context context = getBaseContext();
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.vehicle_1).setContentTitle(push_title)
                .setContentText(message)
                .setContentIntent(pi)
                .setPriority(PRIORITY_HIGH) //private static final PRIORITY_HIGH = 5;
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS);

        NotificationManager mNotificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(MESSAGE_NOTIFICATION_ID, mBuilder.build());
    }

}
