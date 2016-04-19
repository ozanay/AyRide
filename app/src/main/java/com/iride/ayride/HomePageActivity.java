package com.iride.ayride;

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
        GoogleApiClient.OnConnectionFailedListener, LocationListener, VehicleRegistrationDialogFragment.VehicleRegistrationDialogListener {

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private final static String loggerTag = HomePageActivity.class.getSimpleName();
    private final static String vehicleRegistrationDialogFragmentTag = VehicleRegistrationDialogFragment.class.getSimpleName();
    private final static String mobileServiceUrl = "https://useraccount.azure-mobile.net/";
    private final static String mobileServiceAppKey = "BCGeAFQbjUEOGanLwVXslBzVMykgEM16";
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
        driverChatButton = (ImageButton) findViewById(R.id.driver_chat);
        driverChatButton.setOnClickListener(new DriverChatListener());
        driverRideButton = (ImageButton) findViewById(R.id.driver_ride);
        driverRideButton.setOnClickListener(new DriverRideListener());
        searchRideText = (TextView) findViewById(R.id.search_ride_text);
        userModeButton = (ToggleButton) findViewById(R.id.toggle_button);
        userModeButton.setOnCheckedChangeListener(new UserModeListener());
        userLocalStorage = new UserLocalStorage(getSharedPreferences(String.valueOf(StoragePreferences.PREFERENCES), Context.MODE_PRIVATE));
        vehicleLocalStorage = new VehicleLocalStorage(getSharedPreferences(String.valueOf(StoragePreferences.VEHICLEPREFERENCES), Context.MODE_PRIVATE));
        isHasVehicle = (vehicleLocalStorage.getVehicleModel() != null ?  true : false);

        if (isHasVehicle){
            userModeButton.setChecked(false);
            /*driverChatButton.setVisibility(View.VISIBLE);
            driverRideButton.setVisibility(View.VISIBLE);
            searchRideButton.setVisibility(View.GONE);
            searchRideText.setVisibility(View.GONE);*/
        }else {
            userModeButton.setChecked(true);
/*
            driverChatButton.setVisibility(View.GONE);
            driverRideButton.setVisibility(View.GONE);
            searchRideButton.setVisibility(View.VISIBLE);
            searchRideText.setVisibility(View.VISIBLE);
*/
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
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

    private void showVehicleRegistrationDialog() {
        new VehicleRegistrationDialogFragment().show(getFragmentManager(), vehicleRegistrationDialogFragmentTag);

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

    private boolean isDriverMode() {
        return !userModeButton.isChecked();
    }

    private void addVehicleInformationToDB(Vehicle vehicle, final VehicleRegistrationDialogFragment vehicleRegistrationDialogFragment) {
        if (vehicle == null) {
            Log.d(loggerTag, "Vehicle is NULL!");
            return;
        }

        initializeMobileService();
        vehicleMobileServiceTable.insert(vehicle, new TableOperationCallback<User>() {
            public void onCompleted(User entity, Exception exception, ServiceFilterResponse response) {
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

    private void storeVehicleInformationToLocal(Vehicle vehicle) {
        vehicle.setVehicleId(userLocalStorage.getUserId());
        vehicleLocalStorage.storeVehicleId(vehicle.getVehicleId());
        vehicleLocalStorage.storeVehicleModel(vehicle.getVehicleModel());
        vehicleLocalStorage.storeVehicleColor(vehicle.getVehicleColor());
        vehicleLocalStorage.storeVehicleYear(vehicle.getVehicleYear());
        vehicleLocalStorage.storeVehicleLicensePlate(vehicle.getVehicleLicensePlate());
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
                    Toast.makeText(getApplicationContext(), "Pedestrian Mode", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(loggerTag, "Driver Mode");
                    driverChatButton.setVisibility(View.VISIBLE);
                    driverRideButton.setVisibility(View.VISIBLE);
                    searchRideButton.setVisibility(View.GONE);
                    searchRideText.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "Driver Mode", Toast.LENGTH_SHORT).show();
                    if (!isHasVehicle) {
                        showVehicleRegistrationDialog();
                    }
                }
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

        }
    }
}
