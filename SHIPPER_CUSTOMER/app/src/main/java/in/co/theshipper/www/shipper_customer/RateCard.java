package in.co.theshipper.www.shipper_customer;


import android.annotation.TargetApi;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class RateCard extends Fragment {

    private DBController controller;
    private SQLiteDatabase database;
    private View view;
    private Spinner cityspinner;
    private Spinner truckspinner;
    private String query1 = "select distinct(city_name) from view_city";
    private String query2 = "select distinct(vehicle_name) from view_vehicle_type";
    private ArrayList<String> city = new ArrayList<String>();
    private ArrayList<String> truck = new ArrayList<String>();
    private ArrayAdapter<String> adapter_city;
    private ArrayAdapter<String> adapter_truck;
    private TableLayout table_layout11;
    private TableLayout table_layout22;
    private TableLayout table_layout1;
    private TableLayout table_layout2;

    public RateCard() {
        // Required empty public constructor

    }
    @Override
    public void onCreate( Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        controller = new DBController(getActivity());
        database = controller.getWritableDatabase();
        Cursor cit = database.rawQuery(query1, null);
        Cursor truc = database.rawQuery(query2, null);
        city.add("Select City");
        truck.add("Select Truck");
        Fn.SystemPrintLn("rating entered onCreate");
        try {
            if (cit.moveToFirst()) {
                do {
                    city.add(cit.getString(0));
                } while (cit.moveToNext());
            }
        } catch (Exception e) {
            Fn.SystemPrintLn("no rows");
        }
        try {
            if (truc.moveToFirst()) {
                do {
                    truck.add(truc.getString(0));
                } while (truc.moveToNext());
            }
        } catch (Exception e) {
            Fn.SystemPrintLn("no rows");
        }
        database.close();
        Fn.SystemPrintLn(truck);

        Fn.SystemPrintLn("before adapter");
        adapter_truck = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, truck);
        adapter_city = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, city);
        Fn.SystemPrintLn(adapter_city);
        Fn.SystemPrintLn(truck);
        //ArrayAdapter.createFromResource()
        //adapterp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (container == null) {
            return null;
        } else {

            view = inflater.inflate(R.layout.fragment_rate_card, container, false);
            Fn.SystemPrintLn("rating entered onCreateViewElse");
            table_layout11 = (TableLayout) view.findViewById(R.id.tableLayout1);
            table_layout22 = (TableLayout) view.findViewById(R.id.tableLayout2);
            cityspinner = (Spinner) view.findViewById(R.id.cityspinner);
            table_layout1 = (TableLayout) view.findViewById(R.id.tableLayout1);
            table_layout2 = (TableLayout) view.findViewById(R.id.tableLayout2);
            // adapterc.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            truckspinner = (Spinner) view.findViewById(R.id.truckspinner);
            cityspinner = (Spinner)  view.findViewById(R.id.cityspinner);
            return view;
        }
    }
    @Override
    public void onActivityCreated( Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        Fn.SystemPrintLn("before setting adaptor");
        cityspinner.setAdapter(adapter_city);
        truckspinner.setAdapter(adapter_truck);
        //city_selected = cityspinner.getSelectedItem().toString();
        //ruck_selected =truckspinner.getSelectedItem().toString();
        Fn.SystemPrintLn("after settng adaprot");
        cityspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //@Override
            public void onItemSelected(AdapterView adapter, View v, int i, long lng) {

                String city_selected = adapter.getItemAtPosition(i).toString();
                String truck_selected = truckspinner.getSelectedItem().toString();
                Fn.SystemPrintLn("cityspinner entered");
                if (truck_selected.isEmpty() || truck_selected.compareTo("Select Truck")==0 || city_selected.compareTo("Select City")==0|| city_selected.isEmpty()) {
                   /* AlertDialog.Builder builder = new AlertDialog.Builder(rate_card_Activity.this);
                    builder.setMessage("Enter value for Truck")
                            .setTitle("Empty fields");
                    AlertDialog dialog = builder.create();
                    dialog.show();*/

                    table_layout1.removeAllViews();
                    table_layout2.removeAllViews();
                } else {
                    showTables(city_selected, truck_selected);
                }
            }

            //@Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        truckspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //@Override
            public void onItemSelected(AdapterView adapter, View v, int i, long lng) {

                String truck_selected = adapter.getItemAtPosition(i).toString();
                String city_selected = cityspinner.getSelectedItem().toString();
                Fn.SystemPrintLn("truckspinner entered");
                if (truck_selected.isEmpty() || truck_selected.compareTo("Select Truck")==0 || city_selected.compareTo("Select City")==0|| city_selected.isEmpty()) {

                    TableLayout table_layout1 = (TableLayout) view.findViewById(R.id.tableLayout1);
                    TableLayout table_layout2 = (TableLayout) view.findViewById(R.id.tableLayout2);
                    table_layout1.removeAllViews();
                    table_layout2.removeAllViews();
                } else {
                    showTables(city_selected, truck_selected);
                }
            }

            //@Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        //String[] city=new String[15];
        //String[] truck= new String[15];


    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    protected void showTables(String city, String truck) {
        table_layout11.removeAllViews();
        table_layout22.removeAllViews();
        SQLiteDatabase database = controller.getReadableDatabase();

        TableRow.LayoutParams RowLayoutParams= new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,getResources().getInteger(R.integer.table_row_height));
        TableRow.LayoutParams TextViewLayoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.MATCH_PARENT);
        int TextViewMargin = getResources().getInteger(R.integer.table_textview_margin);
        TextViewLayoutParams.setMargins(TextViewMargin, TextViewMargin, TextViewMargin, TextViewMargin);
        int textPadding = getResources().getInteger(R.integer.table_text_padding);
        int textSize = getResources().getInteger(R.integer.table_text_size);


        Fn.SystemPrintLn("enterred into show tables");
        String querytcb = "select base_fare,maximum_weight,freewaiting_time,waiting_charge,night_holding_charge,hard_copy_challan,dimension,transit_charge " +
                "from view_base_fare where vehicle_name=\"" + truck + "\" and city_id = " +
                "(select city_id from view_city where city_name = \"" + city + "\" )";
        Cursor ctcb = database.rawQuery(querytcb, null);
        String querytcp = "select from_distance,to_distance,price_km " +
                "from view_pricing where vehicle_name=\"" + truck + "\" and city_id = " +
                "(select city_id from view_city where city_name = \"" + city + "\" )";
        Cursor ctcp = database.rawQuery(querytcp, null);
        int rows1 = ctcb.getCount();
        int cols1 = ctcb.getColumnCount();
        int rows2 = ctcp.getCount();
        int cols2 = ctcp.getColumnCount();
        Fn.SystemPrintLn("hahha" + rows1 + cols1);
        String[] parameter = new String[2];
        parameter[0]= "Parameters";
        parameter[1]= "Values";
        String[] parameter_values = new String[8];
        parameter_values[0] = "Base Fare";
        parameter_values[1] = "Capacity";
        parameter_values[2] = "Free Loading/Unloading Time";
        parameter_values[3] = "Waiting Charge";
        parameter_values[4] = "Night Holding Charge";
        parameter_values[5] = "Hard Copy of Challan";
        parameter_values[6] = "Dimension";
        parameter_values[7] = "Transit Charge";
        String[] price=new String[3];
        price[0]="From( km )";
        price[1]="To( km )";
        price[2]="Fare( Rs/km )";
        try {
            if (ctcb.moveToFirst()) {
                TableRow roww = new TableRow(getContext());
                roww.setLayoutParams(RowLayoutParams);
//                roww.setBackgroundColor(getResources().getColor(R.color.dark_green));
                for(int k=0;k<2;k++)
                {
                    TextView tvv = new TextView(getContext());
                    tvv.setLayoutParams(TextViewLayoutParams);
                    tvv.setTextSize(textSize);
                    tvv.setPadding(textPadding, textPadding, textPadding, textPadding);
                    tvv.setBackgroundColor(getResources().getColor(R.color.dark_green));
                    tvv.setTextColor(getResources().getColor(R.color.pure_white));
                    tvv.setText(parameter[k]);
                    roww.addView(tvv);
                }
                table_layout11.addView(roww);
                for (int i = 0; i < cols1; i++) {
                    TableRow row = new TableRow(getContext());
                    row.setLayoutParams(RowLayoutParams);
                    for (int j = 0; j < 2; j++) {
                        TextView tv = new TextView(getContext());
                        tv.setBackground(getResources().getDrawable(R.drawable.abc_table_border));
                        tv.setLayoutParams(TextViewLayoutParams);//                        tv.setGravity(Gravity.CENTER);
                        tv.setWidth(50);
                        tv.setTextSize(textSize);
                        tv.setPadding(textPadding,textPadding,textPadding,textPadding);
                        if(j==0){
                            tv.setText(parameter_values[i]);
                        }
                        else{
                            switch(i) {
                                case 0:
                                    tv.setText(ctcb.getString(i)+" Rs.");
                                    break;
                                case 1:
                                    tv.setText(ctcb.getString(i)+" Tonnes");
                                    break;
                                case 2:
                                    tv.setText(ctcb.getString(i)+" minutes");
                                    break;
                                case 3:
                                    tv.setText(ctcb.getString(i)+" Rs/minute");
                                    break;
                                case 4:
                                    tv.setText(ctcb.getString(i)+" Rs");
                                    break;
                                case 5:
                                    tv.setText(ctcb.getString(i)+" Rs");
                                    break;
                                case 6:
                                    tv.setText(ctcb.getString(i));
                                    break;
                                case 7:
                                    tv.setText(ctcb.getString(i)+" Rs/minute");
                                    break;
                            }

                        }
                        row.addView(tv);
                    }
                    table_layout11.addView(row);
                }
            }
        } catch (Exception e) {
            Fn.SystemPrintLn("exception");
        }
        try {
            if (ctcp.moveToFirst()) {
                TableRow roww = new TableRow(getContext());
                roww.setLayoutParams(RowLayoutParams);
                for(int k=0;k<3;k++){
                    TextView tvv = new TextView(getContext());
                    tvv.setLayoutParams(TextViewLayoutParams);
                    tvv.setTextSize(textSize);
                    tvv.setPadding(textPadding, textPadding, textPadding, textPadding);
                    tvv.setBackgroundColor(getResources().getColor(R.color.dark_green));
                    tvv.setTextColor(getResources().getColor(R.color.pure_white));
                    tvv.setText(price[k]);
                    roww.addView(tvv);
                }
                table_layout22.addView(roww);
                for (int i = 0; i < rows2; i++) {
                    TableRow row = new TableRow(getContext());
                    row.setLayoutParams(RowLayoutParams);
                    // inner for loop
                    for (int j = 0; j < cols2; j++) {
                        TextView tv = new TextView(getContext());
                        tv.setLayoutParams(TextViewLayoutParams);
                        tv.setTextSize(textSize);
                        tv.setPadding(textPadding, textPadding, textPadding, textPadding);
                        tv.setText(ctcp.getString(j));
                        tv.setBackground(getResources().getDrawable(R.drawable.abc_table_border));
                        row.addView(tv);
                    }ctcp.moveToNext();
                    table_layout22.addView(row);
                }
            }
        } catch (Exception e) {
            Fn.SystemPrintLn("exception price");
        }
//        database.close();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        database.close();
    }
}
