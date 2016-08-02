package in.co.theshipper.www.shipper_customer;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Ashish on 7/1/2016.
 */
public class NavMenuAdapter extends ArrayAdapter<NavMenu>{


        Context context;
        int layoutResourceId;
        NavMenu data[] = null;

        public NavMenuAdapter(Context context, int layoutResourceId, NavMenu[] data) {
            super(context, layoutResourceId, data);
            this.layoutResourceId = layoutResourceId;
            this.context = context;
            this.data = data;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            NavMenuHolder holder = null;

            if(row == null)
            {
                LayoutInflater inflater = ((Activity)context).getLayoutInflater();
                row = inflater.inflate(layoutResourceId, parent, false);

                holder = new NavMenuHolder();
                holder.imgIcon = (ImageView)row.findViewById(R.id.imgIcon);
                holder.txtTitle = (TextView)row.findViewById(R.id.txtTitle);

                row.setTag(holder);
            }
            else
            {
                holder = (NavMenuHolder)row.getTag();
            }
            NavMenu weather = data[position];
            holder.txtTitle.setText(weather.title);
            holder.imgIcon.setImageResource(weather.icon);
            return row;
        }

        static class NavMenuHolder
        {
            ImageView imgIcon;
            TextView txtTitle;
        }
    }
