package com.iride.ayride;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class SearchResultsActivity extends AppCompatActivity {

    private final static String end = "---------------------------";
    private final static String loggerTag = SearchResultsActivity.class.getSimpleName();
    private ArrayList<Ride> searchResults;
    private ListView ridesListView;
    ArrayAdapter<String> ridesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);
        try {
            this.searchResults = (ArrayList<Ride>) getIntent().getSerializableExtra("searchResults");
            ridesListView = (ListView) findViewById(R.id.list_of_rides);

            ridesAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1, formatRides(searchResults));
            ridesListView.setAdapter(ridesAdapter);
            ridesListView.setOnItemClickListener(rideClickListener);
        } catch(Exception exc){
            Log.e(loggerTag, exc.getMessage());
        }
    }

    private String[] formatRides(ArrayList<Ride> rides){
        String[] formattedRides = new String[rides.size()];
        for (int i =0; i<rides.size(); i++) {
            formattedRides[i] = rides.get(i).getDriverName()+" "+rides.get(i).getDriverSurName()+"\n"
                                +"FROM: "+rides.get(i).getRideFrom()+"\n"
                                +"TO: "+rides.get(i).getRideTo()+"\n"
                                +"TIME: "+rides.get(i).getAppointmentTime()+" Available Seat: "+rides.get(i).getAvailableSeat()+"\n"
                                +"RIDE COMMENT: "+rides.get(i).getRideComment()+"\n"
                                +end;
        }

        return formattedRides;
    }

    private void makeRequestAlert(String message){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message+"\n\n"+"Do you want to make request?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private AdapterView.OnItemClickListener rideClickListener = new AdapterView.OnItemClickListener(){

        @Override
        public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
            SearchResultsActivity.this.makeRequestAlert((String)adapter.getItemAtPosition(position));
        }
    };
}