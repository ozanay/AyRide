package com.iride.ayride;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.table.TableQueryCallback;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

public class SearchRideActivity extends AppCompatActivity {

    private final static String loggerTag = SearchRideActivity.class.getSimpleName();
    private PlaceAutocompleteFragment fromAutocompleteFragment;
    private PlaceAutocompleteFragment toAutocompleteFragment;
    private Place fromPlace;
    private Place toPlace;
    private Button findRideButton;
    private MobileServiceClient mobileServiceClient;
    private MobileServiceTable mobileServiceTable;
    private ArrayList<Ride> ridesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_ride);
        findViewById(R.id.search_ride_loading_panel).setVisibility(View.GONE);
        fromAutocompleteFragment = (PlaceAutocompleteFragment)getFragmentManager().findFragmentById(R.id.search_from_autocomplete_fragment);
        fromAutocompleteFragment.setOnPlaceSelectedListener(fromPlaceSelectionListener);
        toAutocompleteFragment = (PlaceAutocompleteFragment)getFragmentManager().findFragmentById(R.id.search_to_autocomplete_fragment);
        toAutocompleteFragment.setOnPlaceSelectedListener(toPlaceSlectionListener);
        findRideButton = (Button) findViewById(R.id.find_ride_button);
        findRideButton.setOnClickListener(findRideListener);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(SearchRideActivity.this,HomePageActivity.class));
        finish();
    }

    private void resetPlaces(){
        fromPlace = null;
        toPlace = null;
    }

    private void searchRide(String from, String to){
        try {
            if (from.isEmpty() && to.isEmpty()){
                Log.d(loggerTag, "NULL inputs For Search Ride");
                Toast.makeText(getApplicationContext(), "Inputs are NULL!", Toast.LENGTH_SHORT).show();
                return;
            }

            this.mobileServiceClient = new MobileServiceClient(
                    getString(R.string.azureApiUrl),
                    getString(R.string.azureApiKey),
                    this
            );
            this.mobileServiceTable = mobileServiceClient.getTable("ride_info", Ride.class);
            if (mobileServiceTable == null){
                Log.d(loggerTag,"Mobile Service Table is Null!");
                return;
            }

            mobileServiceTable.where().field("ride_from").eq(from).or().field("ride_to").eq(to).execute(new TableQueryCallback<Ride>() {
                public void onCompleted(List<Ride> result, int count, Exception exception, ServiceFilterResponse response) {
                    if (exception == null) {
                        findViewById(R.id.search_ride_loading_panel).setVisibility(View.GONE);
                        if (result.isEmpty() || result == null) {
                            Log.i(loggerTag, "There is NO Ride!");
                            Toast.makeText(getApplicationContext(), "NO Ride is Found", Toast.LENGTH_SHORT).show();
                        } else {
                            ridesList = (ArrayList<Ride>) result;
                            Toast.makeText(getApplicationContext(), "There is at least one result", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(SearchRideActivity.this, SearchResultsActivity.class);
                            intent.putExtra("searchResults", ridesList);
                            startActivity(intent);
                            finish();
                        }

                        resetPlaces();
                    } else {
                        findViewById(R.id.search_ride_loading_panel).setVisibility(View.GONE);
                        Log.e(loggerTag, exception.getCause().toString());
                    }
                }
            });
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.e(loggerTag, e.getCause().toString());
        }
    }

    private PlaceSelectionListener fromPlaceSelectionListener = new PlaceSelectionListener() {
        @Override
        public void onPlaceSelected(Place place) {
            fromPlace = place;
            Log.i(loggerTag, fromPlace.getName().toString());
        }

        @Override
        public void onError(Status status) {
            Log.e(loggerTag, status.getStatusMessage());
        }
    };

    private PlaceSelectionListener toPlaceSlectionListener = new PlaceSelectionListener() {
        @Override
        public void onPlaceSelected(Place place) {
            toPlace = place;
            Log.i(loggerTag, toPlace.getName().toString());
        }

        @Override
        public void onError(Status status) {
            Log.e(loggerTag, status.getStatusMessage());
        }
    };

    private View.OnClickListener findRideListener = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            try {
                findViewById(R.id.search_ride_loading_panel).setVisibility(View.VISIBLE);
                if (SearchRideActivity.this.fromPlace == null &&
                        SearchRideActivity.this.toPlace == null){
                    findViewById(R.id.search_ride_loading_panel).setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "Locations are NULL!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (SearchRideActivity.this.fromPlace == null ){
                    searchRide("", SearchRideActivity.this.toPlace.getName().toString());
                } else if (SearchRideActivity.this.toPlace == null){
                    searchRide(SearchRideActivity.this.fromPlace.getName().toString(), "");
                }else {
                    searchRide(SearchRideActivity.this.fromPlace.getName().toString(), SearchRideActivity.this.toPlace.getName().toString());
                }
            } catch (Exception exc) {
                Log.e(loggerTag, exc.getMessage());
            }
        }
    };
}
