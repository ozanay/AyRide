package com.iride.ayride;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.table.TableOperationCallback;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HomePageActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, VehicleRegistrationDialogFragment.VehicleRegistrationDialogListener,
        CreateRideDialogFragment.CreateRideDialogListener{

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private final static String loggerTag = HomePageActivity.class.getSimpleName();
    private final static String vehicleRegistrationDialogFragmentTag = VehicleRegistrationDialogFragment.class.getSimpleName();
    private final static String createRideDialogFragmentTag = CreateRideDialogFragment.class.getSimpleName();
    private static final String SENDER_ID = "1072435786165";
    private static boolean isHasVehicle;
    private GoogleMap googleMap;
    private Location currentLocation;
    private GoogleApiClient googleApiClient;
    private Marker currentLocationMarker;
    private ImageButton searchRideButton;
    private ImageButton settingsButton;
    private ImageButton driverChatButton;
    private ImageButton driverRideButton;
    private TextView searchRideText;
    private ToggleButton userModeButton;
    private Vehicle vehicle;
    private UserLocalStorage userLocalStorage;
    private VehicleLocalStorage vehicleLocalStorage;
    private MobileServiceTable vehicleMobileServiceTable;
    private MobileServiceTable rideMobileServiceTable;
    private MobileServiceClient mobileServiceClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);
        Toolbar toolbar = (Toolbar) findViewById(R.id.homepage_toolbar);
        setSupportActionBar(toolbar);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        if (!isGPSEnable()) {
            buildAlertMessageNoGps();
        }

        buildGoogleApiClient();
        searchRideButton = (ImageButton) findViewById(R.id.searh_ride);
        searchRideButton.setOnClickListener(new SearchRideListener());
        settingsButton = (ImageButton) findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(new SettingsListener());
        driverChatButton = (ImageButton) findViewById(R.id.driver_chat);
        driverChatButton.setOnClickListener(new DriverChatListener());
        driverRideButton = (ImageButton) findViewById(R.id.driver_ride);
        driverRideButton.setOnClickListener(new DriverRideListener());
        searchRideText = (TextView) findViewById(R.id.search_ride_text);
        userModeButton = (ToggleButton) findViewById(R.id.toggle_button);
        userModeButton.setOnCheckedChangeListener(new UserModeListener());
        userLocalStorage = new UserLocalStorage(getSharedPreferences(String.valueOf(StoragePreferences.PREFERENCES), Context.MODE_PRIVATE));
        vehicleLocalStorage = new VehicleLocalStorage(getSharedPreferences(String.valueOf(StoragePreferences.VEHICLEPREFERENCES), Context.MODE_PRIVATE));
        this.initializeMobileService();
        isHasVehicle = (vehicleLocalStorage.getVehicleModel() != null);
        if (userLocalStorage.isDriverMode()){
            userModeButton.setChecked(false);
        }else {
            userModeButton.setChecked(true);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.setTrafficEnabled(true);
    }

    @Override
    protected void onStart() {
        googleApiClient.connect();
        super.onStart();
        Log.d(loggerTag, "Google Api Client Connected");
    }

    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
        Log.d(loggerTag, "Google Api Client Disconnected");
    }

    @Override
    public void onBackPressed() {
        exitFromTheApp();
    }

    @Override
    public void onConnected(Bundle bundle) {
        currentLocation = LocationServices.FusedLocationApi.getLastLocation(
                googleApiClient);
        if (currentLocation != null) {
            centerInLocation(currentLocation);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(loggerTag, "Connection Suspended!");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                Log.e(loggerTag, e.getMessage());
            }
        } else {
            Log.e(loggerTag, Integer.toString(connectionResult.getErrorCode()));
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
        centerInLocation(currentLocation);
    }

    @Override
    public void onDialogPositiveClick(VehicleRegistrationDialogFragment dialogFragment) {
        vehicle = dialogFragment.getVehicleInformations();
        if (vehicle.getVehicleModel() == null || vehicle.getVehicleLicensePlate() == null
                || vehicle.getVehicleColor() == null || vehicle.getVehicleYear() == null) {
            Log.d(loggerTag, "Vehicle Is NULL!");
            userModeButton.setChecked(true);
            dialogFragment.dismiss();
            return;
        }

        storeVehicleInformationToLocal(vehicle);
        addVehicleInformationToDB(vehicle, dialogFragment);
    }

    @Override
    public void onDialogNegativeClick(VehicleRegistrationDialogFragment dialogFragment) {
        dialogFragment.dismiss();
        userModeButton.setChecked(true);
    }

    @Override
    public void onDialogPositiveClick(CreateRideDialogFragment dialogFragment) {
        try {
            Ride ride = dialogFragment.getRideInformation();
            if (!Ride.isEmptyRide(ride)){
                LatLng origin = dialogFragment.getFromCoordinate();
                Log.d(loggerTag, "Origin Lat: " + origin.latitude + " and Lng: " + origin.longitude);
                LatLng destination = dialogFragment.getToCoordinate();
                Log.d(loggerTag, "Destination Lat: "+destination.latitude+" and Lng: "+destination.longitude);
                HomePageActivity.this.googleMap.clear();
                centerInLocation(origin);
                MarkerOptions markerOpts = new MarkerOptions().position(destination)
                        .title("Destination!")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                googleMap.addMarker(markerOpts);
                String url = getDirectionsUrl(origin, destination);
                DownloadTask downloadTask = new DownloadTask();
                downloadTask.execute(url);
                ride.setDriverId(userLocalStorage.getUserId());
                ride.setDriverName(userLocalStorage.getUserName());
                ride.setDriverSurName(userLocalStorage.getUserSurName());
                addRideToDB(ride, dialogFragment);
            }
        }catch (Exception exc){
            Log.e(loggerTag, exc.getMessage());
        }
    }

    @Override
    public void onDialogNegativeClick(CreateRideDialogFragment dialogFragment) {
        dialogFragment.dismiss();
    }

    private void showVehicleRegistrationDialog() {
        new VehicleRegistrationDialogFragment().show(getFragmentManager(), vehicleRegistrationDialogFragmentTag);

    }

    private void showCreateRideDialog() {
        new CreateRideDialogFragment().show(getFragmentManager(), createRideDialogFragmentTag);

    }

    private void exitFromTheApp() {
        String exitMessage = "Do you want to exit?";
        AlertDialog.Builder builder = new AlertDialog.Builder(HomePageActivity.this);
        builder.setMessage(exitMessage);
        builder.setCancelable(true);
        builder.setNegativeButton("Logout",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (isFacebookUser()) {
                            LoginManager.getInstance().logOut();
                            startActivity(new Intent(HomePageActivity.this, EntranceActivity.class));
                        } else {
                            //Will be implemented
                        }
                    }
                });
        builder.setPositiveButton("Exit",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });

        AlertDialog alert11 = builder.create();
        alert11.show();
    }

    private boolean isFacebookUser() {
        return AccessToken.getCurrentAccessToken() != null;
    }

    private boolean isGPSEnable() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);

    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("GPS is disabled, do you want to enable it?")
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

    private void centerInLocation(Location location) {
        if (currentLocationMarker != null) {
            currentLocationMarker.remove();
        }

        LatLng myLaLn = new LatLng(location.getLatitude(), location.getLongitude());
        CameraPosition camPos = new CameraPosition.Builder().target(myLaLn).zoom(15).bearing(45).tilt(70).build();
        CameraUpdate camUpd3 = CameraUpdateFactory.newCameraPosition(camPos);
        googleMap.animateCamera(camUpd3);
        MarkerOptions markerOpts = new MarkerOptions().position(myLaLn).title("You're Here!");
        currentLocationMarker = googleMap.addMarker(markerOpts);
    }

    private void centerInLocation(LatLng latLng) {
        if (currentLocationMarker != null) {
            currentLocationMarker.remove();
        }

        CameraPosition camPos = new CameraPosition.Builder().target(latLng).zoom(15).bearing(45).tilt(70).build();
        CameraUpdate camUpd3 = CameraUpdateFactory.newCameraPosition(camPos);
        googleMap.animateCamera(camUpd3);
        MarkerOptions markerOpts = new MarkerOptions().position(latLng).title("You're Here!");
        currentLocationMarker = googleMap.addMarker(markerOpts);
    }

    private synchronized void buildGoogleApiClient() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .build();
        }
    }

    private boolean isDriverMode() {
        return !userModeButton.isChecked();
    }

    private void addVehicleInformationToDB(Vehicle vehicle, final VehicleRegistrationDialogFragment vehicleRegistrationDialogFragment) {
        if (vehicle == null) {
            Log.d(loggerTag, "Vehicle is NULL!");
            return;
        }


        vehicleMobileServiceTable.insert(vehicle, new TableOperationCallback<Vehicle>() {
            public void onCompleted(Vehicle entity, Exception exception, ServiceFilterResponse response) {
                if (exception == null) {
                    Log.i(loggerTag, "Service added the vehicle information successfully!");
                    vehicleRegistrationDialogFragment.dismiss();
                } else {
                    Log.e(loggerTag, exception.getMessage());
                    Toast.makeText(getApplicationContext(), "Vehicle information was not added to system!", Toast.LENGTH_SHORT).show();
                    vehicleRegistrationDialogFragment.dismiss();
                }
            }
        });
    }

    private void initializeMobileService() {
        try {
            this.mobileServiceClient = new MobileServiceClient(
                    getString(R.string.azureApiUrl),
                    getString(R.string.azureApiKey),
                    this
            );
            this.vehicleMobileServiceTable = mobileServiceClient.getTable("vehicle_info", Vehicle.class);
            this.rideMobileServiceTable = mobileServiceClient.getTable("ride_info", Ride.class);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.e(loggerTag, e.getCause().toString());
        }
    }

    private void storeVehicleInformationToLocal(Vehicle vehicle) {
        vehicle.setVehicleId(userLocalStorage.getUserId());
        vehicleLocalStorage.storeVehicleId(vehicle.getVehicleId());
        vehicleLocalStorage.storeVehicleModel(vehicle.getVehicleModel());
        vehicleLocalStorage.storeVehicleColor(vehicle.getVehicleColor());
        vehicleLocalStorage.storeVehicleYear(vehicle.getVehicleYear());
        vehicleLocalStorage.storeVehicleLicensePlate(vehicle.getVehicleLicensePlate());
    }

    private String getDirectionsUrl(LatLng origin,LatLng dest){

        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;

        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        return  "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while( ( line = br.readLine()) != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            Log.d(loggerTag, e.getMessage());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }

        return data;
    }

    private void addRideToDB(Ride ride, final CreateRideDialogFragment createRideDialogFragment){
        if (ride == null) {
            Log.d(loggerTag, "Ride is NULL!");
            return;
        }

        rideMobileServiceTable.insert(ride, new TableOperationCallback<Ride>() {
            public void onCompleted(Ride entity, Exception exception, ServiceFilterResponse response) {
                if (exception == null) {
                    Log.i(loggerTag, "Service added the ride successfully!");
                } else {
                    Log.e(loggerTag, exception.getMessage());
                    Toast.makeText(getApplicationContext(), "Ride was not created!", Toast.LENGTH_SHORT).show();
                }

                createRideDialogFragment.dismiss();
            }
        });
    }

    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();

            // Traversing through all the routes
            if (result.size() == 0){
                Log.d(loggerTag, "Result Size is NULL!");
                return;
            }

            for(int i=0;i<result.size();i++){
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(20);
                lineOptions.color(Color.BLUE);
                lineOptions.geodesic(true);
            }

            // Drawing polyline in the Google Map for the i-th route
            HomePageActivity.this.googleMap.addPolyline(lineOptions);
        }
    }

    private class SearchRideListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            startActivity(new Intent(HomePageActivity.this, SearchRideActivity.class));
            finish();
        }
    }

    private class SettingsListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(HomePageActivity.this, SettingsActivity.class);
            intent.putExtra("isDriver", isDriverMode());
            startActivity(intent);
            finish();
        }
    }

    private class UserModeListener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            try {
                if (isChecked) {
                    Log.d(loggerTag, "Pedestrian Mode");
                    driverChatButton.setVisibility(View.GONE);
                    driverRideButton.setVisibility(View.GONE);
                    searchRideButton.setVisibility(View.VISIBLE);
                    searchRideText.setVisibility(View.VISIBLE);
                    userLocalStorage.storeIsDriver(false);
                    HomePageActivity.this.googleMap.clear();
                    Toast.makeText(getApplicationContext(), "Pedestrian Mode", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(loggerTag, "Driver Mode");
                    driverChatButton.setVisibility(View.VISIBLE);
                    driverRideButton.setVisibility(View.VISIBLE);
                    searchRideButton.setVisibility(View.GONE);
                    searchRideText.setVisibility(View.GONE);
                    userLocalStorage.storeIsDriver(true);
                    HomePageActivity.this.googleMap.clear();
                    Toast.makeText(getApplicationContext(), "Driver Mode", Toast.LENGTH_SHORT).show();
                    if (!isHasVehicle) {
                        showVehicleRegistrationDialog();
                    }
                }

                centerInLocation(HomePageActivity.this.currentLocation);
            } catch (Exception exc) {
                Log.e(loggerTag, exc.getMessage());
            }
        }
    }

    private class DriverChatListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {

        }
    }

    private class DriverRideListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            showCreateRideDialog();
        }
    }
}
