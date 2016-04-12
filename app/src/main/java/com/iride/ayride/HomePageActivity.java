package com.iride.ayride;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.table.TableOperationCallback;

import java.net.MalformedURLException;

public class HomePageActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private final static String loggerTag = HomePageActivity.class.getSimpleName();
    private final static String mobileServiceUrl = "https://useraccount.azure-mobile.net/";
    private final static String mobileServiceAppKey = "BCGeAFQbjUEOGanLwVXslBzVMykgEM16";
    private GoogleMap googleMap;
    private Location currentLocation;
    private GoogleApiClient googleApiClient;
    private Marker currentLocationMarker;
    private ImageButton searchRideButton;
    private ImageButton settingsButton;
    private ToggleButton userModeButton;
    private Vehicle vehicle;
    private EditText vehicleModel;
    private EditText vehicleYear;
    private EditText vehicleColor;
    private EditText vehicleLicensePlate;
    private Button closeDialog;
    private Button addVehicleInformation;
    private Dialog dialog;
    private UserLocalStorage userLocalStorage;
    private VehicleLocalStorage vehicleLocalStorage;
    private MobileServiceClient mobileServiceClient;
    private MobileServiceTable vehicleMobileServiceTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);
        Toolbar toolbar = (Toolbar) findViewById(R.id.homepage_toolbar);
        setSupportActionBar(toolbar);
        // Obtain the SupportMapFragment and get notified when the googleMap is ready to be used.
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
        userModeButton = (ToggleButton) findViewById(R.id.toggle_button);
        userModeButton.setOnCheckedChangeListener(new UserModeListener());
        userLocalStorage = new UserLocalStorage(getSharedPreferences(String.valueOf(StoragePreferences.PREFERENCES),Context.MODE_PRIVATE));
        vehicleLocalStorage = new VehicleLocalStorage(getSharedPreferences(String.valueOf(StoragePreferences.VEHICLEPREFERENCES),Context.MODE_PRIVATE));
    }

    /**
     * Manipulates the googleMap once available.
     * This callback is triggered when the googleMap is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        //googleMap.setMyLocationEnabled(true);
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

    private void exitFromTheApp() {
        String exitMessage = "Do you want to exit?";
        AlertDialog.Builder builder = new AlertDialog.Builder(HomePageActivity.this);
        builder.setMessage(exitMessage);
        builder.setCancelable(true);
        builder.setNegativeButton("Logout",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (isFacebookUser()){
                            LoginManager.getInstance().logOut();
                            startActivity(new Intent(HomePageActivity.this, EntranceActivity.class));
                        } else {

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

    private boolean isFacebookUser(){
        return AccessToken.getCurrentAccessToken() != null;
    }

    private boolean isGPSEnable() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return false;
        }

        return true;
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

    private synchronized void buildGoogleApiClient() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    private boolean isDriver() {
        return !userModeButton.isChecked();
    }

    private void createVehicleRegistrationDialog(){
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.vehicle_registration);
        findViewById(R.id.vehicle_loading_panel).setVisibility(View.GONE);
        vehicleModel = (EditText) findViewById(R.id.vehicle_model_text);
        vehicleColor = (EditText) findViewById(R.id.vehicle_color_text);
        vehicleYear = (EditText) findViewById(R.id.vehicle_year_text);
        vehicleLicensePlate = (EditText) findViewById(R.id.vehicle_license_plate_text);
        addVehicleInformation = (Button) findViewById(R.id.add_vehicle_information_button);
        addVehicleInformation.setOnClickListener(new VehicleRegistrationListener());
        closeDialog = (Button) findViewById(R.id.dialog_close_button);
        closeDialog.setOnClickListener(new DialogCloseListener());
        dialog.show();
    }

    private void addVehicleInformationToDB(Vehicle vehicle){
        if (vehicle == null){
            Log.d(loggerTag, "Vehicle is NULL!");
            return;
        }

        initializeMobileService();
        vehicleMobileServiceTable.insert(vehicle, new TableOperationCallback<User>() {
            public void onCompleted(User entity, Exception exception, ServiceFilterResponse response) {
                if (exception == null) {
                    Log.i(loggerTag, "Service added the vehicle information successfully!");
                    findViewById(R.id.vehicle_loading_panel).setVisibility(View.GONE);
                    dialog.dismiss();
                } else {
                    Log.e(loggerTag, exception.getMessage());
                    Toast.makeText(getApplicationContext(), "Vehicle information was not added to system!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void initializeMobileService() {
        try {
            this.mobileServiceClient = new MobileServiceClient(
                    mobileServiceUrl,
                    mobileServiceAppKey,
                    this
            );
            this.vehicleMobileServiceTable = mobileServiceClient.getTable("vehicle_info", Vehicle.class);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.e(loggerTag, e.getCause().toString());
        }
    }

    private class VehicleRegistrationListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            findViewById(R.id.vehicle_loading_panel).setVisibility(View.VISIBLE);
            if (vehicleModel.getText() == null){
                vehicleModel.requestFocus();
                vehicleModel.setSelectAllOnFocus(true);
                return;
            }

            if (vehicleColor.getText() == null){
                vehicleColor.requestFocus();
                vehicleColor.setSelectAllOnFocus(true);
                return;
            }

            if (vehicleYear.getText() == null){
                vehicleYear.requestFocus();
                vehicleYear.setSelectAllOnFocus(true);
                return;
            }

            if (vehicleLicensePlate.getText() == null){
                vehicleLicensePlate.requestFocus();
                vehicleLicensePlate.setSelectAllOnFocus(true);
                return;
            }

            vehicle.setVehicleId(userLocalStorage.getUserId());
            vehicleLocalStorage.storeVehicleId(vehicle.getVehicleId());
            vehicle.setVehicleModel(vehicleModel.getText().toString());
            vehicleLocalStorage.storeVehicleModel(vehicle.getVehicleModel());
            vehicle.setVehicleColor(vehicleColor.getText().toString());
            vehicleLocalStorage.storeVehicleColor(vehicle.getVehicleColor());
            vehicle.setVehicleYear(vehicleYear.getText().toString());
            vehicleLocalStorage.storeVehicleYear(vehicle.getVehicleYear());
            vehicle.setVehicleLicensePlate(vehicleLicensePlate.getText().toString().replace(" ", ""));
            vehicleLocalStorage.storeVehicleLicensePlate(vehicle.getVehicleLicensePlate());
            addVehicleInformationToDB(vehicle);
        }
    }

    private class DialogCloseListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            dialog.dismiss();
        }
    }

    private class SearchRideListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            startActivity(new Intent(HomePageActivity.this, SearchRideActivity.class));
            finish();
        }
    }

    private class SettingsListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(HomePageActivity.this, SettingsActivity.class);
            intent.putExtra("isDriver", HomePageActivity.this.isDriver());
            startActivity(intent);
            finish();
        }
    }

    private class UserModeListener implements CompoundButton.OnCheckedChangeListener{

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked){
                Log.d(loggerTag,"Pedestrian Mode");
            } else {
                Log.d(loggerTag,"Driver Mode");
                try{
                    createVehicleRegistrationDialog();
                }catch (Exception exc){
                    Log.e(loggerTag, exc.getMessage());
                }
            }
        }
    }
}
