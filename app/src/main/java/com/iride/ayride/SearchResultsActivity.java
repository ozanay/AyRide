package com.iride.ayride;

import android.content.Context;
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

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.table.TableOperationCallback;

import java.net.MalformedURLException;
import java.util.ArrayList;

public class SearchResultsActivity extends AppCompatActivity {

    private final static String end = "---------------------------";
    private final static String loggerTag = SearchResultsActivity.class.getSimpleName();
    private ArrayList<Ride> searchResults;
    private ListView ridesListView;
    private ArrayAdapter<String> ridesAdapter;
    private MobileServiceTable rideMobileServiceTable;
    public static MobileServiceClient mobileServiceClient;
    private UserLocalStorage userLocalStorage;
    private RideLocalStorage rideLocalStorage;

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
            userLocalStorage = new UserLocalStorage(getSharedPreferences(StoragePreferences.USER_PREFERENCES, Context.MODE_PRIVATE));
            rideLocalStorage = new RideLocalStorage(getSharedPreferences(StoragePreferences.RIDE_PREFERENCES, Context.MODE_PRIVATE));
            this.initializeMobileService();
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
                                +"TIME: "+rides.get(i).getAppointmentTime()+"\n"
                                +"Available Seat: "+rides.get(i).getAvailableSeat()+"\n"
                                +"RIDE COMMENT: "+rides.get(i).getRideComment()+"\n"
                                +end;
        }

        return formattedRides;
    }

    private void makeRequestAlert(String message, final int position){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message+"\n\n"+"Do you want to make request?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Ride ride = searchResults.get(position);
                        updateRide(ride);
                        GcmSender.send(GcmRequestMessages.RIDE_REQUEST_MESSAGE,ride.getDriverInstanceId(),ride);
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

    private void initializeMobileService() {
        try {
            this.mobileServiceClient = new MobileServiceClient(
                    getString(R.string.azureApiUrl),
                    getString(R.string.azureApiKey),
                    this
            );
            this.rideMobileServiceTable = mobileServiceClient.getTable("ride_info", Ride.class);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.e(loggerTag, e.getCause().toString());
        }
    }

    private void updateRide(Ride ride){
        ride.setPedestrianId(userLocalStorage.getUserId());
        ride.setPedestrianName(userLocalStorage.getUserName());
        ride.setPedestrianSurName(userLocalStorage.getUserSurName());
        ride.setPedestrianInstanceId(rideLocalStorage.getOwnInstanceId());
        rideLocalStorage.storeRide(ride);
        rideMobileServiceTable.update(ride, new TableOperationCallback<Ride>() {
            public void onCompleted(Ride entity, Exception exception, ServiceFilterResponse response) {
                if (exception == null) {
                    Log.d(loggerTag, "Ride information was updated Successfully!");
                } else {
                    Log.e(loggerTag, exception.getMessage());
                }
            }
        });
    }

    private AdapterView.OnItemClickListener rideClickListener = new AdapterView.OnItemClickListener(){

        @Override
        public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
            SearchResultsActivity.this.makeRequestAlert((String)adapter.getItemAtPosition(position), position);
        }
    };

    @Override
    public void onBackPressed() {
        startActivity(new Intent(SearchResultsActivity.this,HomePageActivity.class));
        finish();
    }
}