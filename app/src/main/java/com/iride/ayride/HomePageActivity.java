package com.iride.ayride;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceList;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.table.TableOperationCallback;
import com.microsoft.windowsazure.mobileservices.table.TableQueryCallback;

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
import java.util.concurrent.ExecutionException;

public class HomePageActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, VehicleRegistrationDialogFragment.VehicleRegistrationDialogListener,
        CreateRideDialogFragment.CreateRideDialogListener, GoogleMap.OnCameraChangeListener, GeoQueryEventListener {

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private final static String loggerTag = HomePageActivity.class.getSimpleName();
    private final static String vehicleRegistrationDialogFragmentTag = VehicleRegistrationDialogFragment.class.getSimpleName();
    private final static String createRideDialogFragmentTag = CreateRideDialogFragment.class.getSimpleName();
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private GoogleMap googleMap;
    private Location currentLocation;
    private GoogleApiClient googleApiClient;
    private Marker currentLocationMarker;
    private ImageButton searchRideButton;
    private ImageButton driverRideButton;
    private ImageButton rideCancelButton;
    private TextView searchRideText;
    private ToggleButton userModeButton;
    private Vehicle vehicle;
    private UserLocalStorage userLocalStorage;
    private VehicleLocalStorage vehicleLocalStorage;
    private RideLocalStorage rideLocalStorage;
    private MobileServiceTable vehicleMobileServiceTable;
    private MobileServiceTable rideMobileServiceTable;
    private ArrayList<Ride> ridesList;
    private GoogleCloudMessaging googleCloudMessaging;
    private Circle searchCircle;
    private HashMap<String, Marker> markers;
    private HashMap<String, String> markersIdKeyPair;
    private Firebase firebase;
    private GeoFire geoFire;
    private GeoQuery geoQuery;
    private ArrayList<String> hashTags;
    private double latitude;
    private double longitude;
    private LatLng origin;
    private LatLng destination;
    private BroadcastReceiver registrationBroadcastReceiver;
    private boolean isReceiverRegistered;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);
        try {
            this.markers = new HashMap<>();
            this.markersIdKeyPair = new HashMap<>();
            this.ridesList = new ArrayList<>();
            this.hashTags = new ArrayList<>();
            Toolbar toolbar = (Toolbar) findViewById(R.id.homepage_toolbar);
            setSupportActionBar(toolbar);
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
            if (!isGPSEnable()) {
                buildAlertMessageNoGps();
            }

            this.buildGoogleApiClient();
            this.initializeLocalStores();
            this.initializeMobileService();
            if (rideLocalStorage.getOwnInstanceId() == null) {
                this.storeRegistrationInstanceId();
            }

            this.initializeLayoutEntities();
            registrationBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    SharedPreferences sharedPreferences =
                            PreferenceManager.getDefaultSharedPreferences(context);
                    boolean sentToken = sharedPreferences
                            .getBoolean(StoragePreferences.SENT_TOKEN_TO_SERVER, false);
                    if (sentToken) {
                        Log.d(loggerTag, getString(R.string.gcmSendMessage));
                    } else {
                        Log.d(loggerTag,getString(R.string.tokenErrorMessage));
                    }
                }
            };

            // Registering BroadcastReceiver
            registerReceiver();

            if (checkPlayServices()) {
                // Start IntentService to register this application with GCM.
                Intent intent = new Intent(this, RegistrationIntentService.class);
                startService(intent);
            }
            this.initializeFirebaseEntities();
            this.getAllRides();
        } catch (Exception exc) {
            Log.e(loggerTag, "THIS MSG: " + exc.getMessage());
            Log.e(loggerTag, "THIS CAUSE:" + String.valueOf(exc.getCause()));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        this.googleMap.setTrafficEnabled(true);
        this.googleMap.setOnCameraChangeListener(this);
        this.googleMap.setOnMarkerClickListener(new UserMarkerClickListener());
    }

    @Override
    protected void onStart() {
        googleApiClient.connect();
        super.onStart();
        Log.d(loggerTag, "Google Api Client Connected");
    }

    @Override
    protected void onStop() {
        super.onStop();
        googleApiClient.disconnect();
        for (Marker marker : this.markers.values()) {
            marker.remove();
        }

        this.markers.clear();
        this.markersIdKeyPair.clear();
        Log.d(loggerTag, "Google Api Client Disconnected");
    }

    @Override
    public void onBackPressed() {
        exitFromTheApp();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(loggerTag, "GOOGLE API CONNECTED!");
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        currentLocation = LocationServices.FusedLocationApi.getLastLocation(
                googleApiClient);
        if (currentLocation == null) {
            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setInterval(30000);
            locationRequest.setFastestInterval(1000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setSmallestDisplacement(2);
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
            currentLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        }

        if (userLocalStorage.isDriverMode() && rideLocalStorage.getRideId() != null && !rideLocalStorage.getRideId().isEmpty()) {
            drawRoute(HomePageActivity.this.rideLocalStorage.getRideOrigin(), HomePageActivity.this.rideLocalStorage.getRideDestination());
        } else {
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
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        centerInLocation(currentLocation);
        geoFire.setLocation(userLocalStorage.getUserId(), new GeoLocation(currentLocation.getLatitude(), currentLocation.getLongitude()));
    }

    @Override
    public void onDialogPositiveClick(VehicleRegistrationDialogFragment dialogFragment) {
        vehicle = dialogFragment.getVehicleInformations();
        if (vehicle.getVehicleModel() == null || vehicle.getVehicleLicensePlate() == null
                || vehicle.getVehicleColor() == null || vehicle.getVehicleYear() == null) {
            Log.d(loggerTag, "Vehicle Is NULL!");

            userModeButton.setChecked(true);
            Toast.makeText(HomePageActivity.this, "Vehicle Information is Necessary", Toast.LENGTH_SHORT).show();
            dialogFragment.setCancelable(true);
            dialogFragment.dismiss();
        } else {
            addVehicleInformationToDB(vehicle, dialogFragment);
        }
    }

    @Override
    public void onDialogNegativeClick(VehicleRegistrationDialogFragment dialogFragment) {
        dialogFragment.setCancelable(true);
        dialogFragment.dismiss();
        userModeButton.setChecked(true);
    }

    @Override
    public void onDialogPositiveClick(CreateRideDialogFragment dialogFragment) {
        try {
            Ride ride = dialogFragment.getRideInformation();
            if (!Ride.isEmptyRide(ride)) {
                this.origin = dialogFragment.getFromCoordinate();
                Log.d(loggerTag, "Origin Lat: " + origin.latitude + " and Lng: " + origin.longitude);
                this.destination = dialogFragment.getToCoordinate();
                Log.d(loggerTag, "Destination Lat: " + destination.latitude + " and Lng: " + destination.longitude);
                HomePageActivity.this.googleMap.clear();
                HomePageActivity.this.drawRoute(this.origin, this.destination);
                ride.setDriverId(userLocalStorage.getUserId());
                ride.setDriverName(userLocalStorage.getUserName());
                ride.setDriverSurName(userLocalStorage.getUserSurName());
                ride.setDriverInstanceId(rideLocalStorage.getOwnInstanceId());
                addRideToDB(ride, dialogFragment);
                this.geoFire.setLocation(userLocalStorage.getUserId(), new GeoLocation(this.currentLocation.getLatitude(), this.currentLocation.getLongitude()));
                driverRideButton.setVisibility(View.GONE);
                rideCancelButton.setVisibility(View.VISIBLE);
            }
        } catch (Exception exc) {
            Log.e(loggerTag, exc.getMessage());
        }
    }

    @Override
    public void onDialogNegativeClick(CreateRideDialogFragment dialogFragment) {
        dialogFragment.dismiss();
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        try {
            LatLng center = cameraPosition.target;
            Log.d(loggerTag, center.latitude + " " + center.longitude);
            double radius = zoomLevelToRadius(cameraPosition.zoom);
            this.searchCircle.setCenter(center);
            this.searchCircle.setRadius(radius);
            this.geoQuery.setCenter(new GeoLocation(center.latitude, center.longitude));
            this.geoQuery.setRadius(radius / 1000);
        } catch (Exception exc) {
            Log.d(loggerTag, "OnCameraChange: " + exc.getMessage());
        }
    }

    @Override
    public void onKeyEntered(final String key, final GeoLocation location) {
        if (userLocalStorage.isDriverMode()) {
            HomePageActivity.this.firebase.child(key).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    //If not driver
                    if (snapshot.getValue() == String.valueOf(false)) {
                        Marker marker = HomePageActivity.this.googleMap.addMarker(getPedestrianMarker(key, location));
                        HomePageActivity.this.markers.put(key, marker);
                        HomePageActivity.this.markersIdKeyPair.put(marker.getId(), key);
                    }
                }

                @Override
                public void onCancelled(FirebaseError error) {
                    Log.e(loggerTag, error.getMessage());
                }
            });
        } else {
            HomePageActivity.this.firebase.child(key).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    //If driver
                    if (snapshot.getValue() == String.valueOf(true)) {
                        MarkerOptions markerOptions = getDriverMarker(key, location);
                        if (markerOptions == null) {
                            Log.i(loggerTag, "No driver is online");
                            return;
                        }

                        Marker marker = HomePageActivity.this.googleMap.addMarker(markerOptions);
                        HomePageActivity.this.markers.put(key, marker);
                        HomePageActivity.this.markersIdKeyPair.put(marker.getId(), key);
                    }
                }

                @Override
                public void onCancelled(FirebaseError error) {
                    Log.e(loggerTag, error.getMessage());
                }
            });
        }
    }

    @Override
    public void onKeyExited(String key) {
        Marker marker = HomePageActivity.this.markers.get(key);
        if (marker != null) {
            HomePageActivity.this.markersIdKeyPair.remove(marker.getId());
            marker.remove();
            HomePageActivity.this.markers.remove(key);
        }
    }

    @Override
    public void onKeyMoved(String key, GeoLocation location) {
        Marker marker = this.markers.get(key);
        if (marker != null) {
            this.animateMarkerTo(marker, location.latitude, location.longitude);
        }
    }

    @Override
    public void onGeoQueryReady() {

    }

    @Override
    public void onGeoQueryError(FirebaseError error) {
        Log.e(loggerTag, error.getMessage());
        Toast.makeText(getApplicationContext(), "Unexpected Error!", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(registrationBroadcastReceiver);
        isReceiverRegistered = false;
        super.onPause();
    }

    private void drawRoute(LatLng from, LatLng to) {
        centerInLocation(from);
        MarkerOptions markerOpts = new MarkerOptions().position(to)
                .title("Destination!")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        googleMap.addMarker(markerOpts);
        String url = getDirectionsUrl(from, to);
        DownloadTask downloadTask = new DownloadTask();
        downloadTask.execute(url);
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

        if (location == null) {
            Log.d(loggerTag, "LOCATION IS NULL IN CENTER IN LOCATION METHOD");
            return;
        }

        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
        LatLng myLaLn = new LatLng(this.latitude, this.longitude);
        this.searchCircle = this.googleMap.addCircle(new CircleOptions().center(myLaLn).radius(1000));
        this.searchCircle.setFillColor(Color.argb(66, 255, 0, 255));
        this.searchCircle.setStrokeColor(Color.argb(66, 0, 0, 0));
        CameraPosition camPos = new CameraPosition.Builder().target(myLaLn).zoom(15).bearing(45).tilt(70).build();
        CameraUpdate camUpd3 = CameraUpdateFactory.newCameraPosition(camPos);
        googleMap.animateCamera(camUpd3);
        BitmapDescriptor icon;
        if (userLocalStorage.isDriverMode()) {
            icon = BitmapDescriptorFactory.fromResource(R.drawable.car);
        } else {
            icon = BitmapDescriptorFactory.fromResource(R.drawable.person);
        }

        MarkerOptions markerOpts = new MarkerOptions().position(myLaLn).title("You're Here!").icon(icon);
        currentLocationMarker = googleMap.addMarker(markerOpts);
    }

    private void centerInLocation(LatLng latLng) {
        if (currentLocationMarker != null) {
            currentLocationMarker.remove();
        }

        this.searchCircle = this.googleMap.addCircle(new CircleOptions().center(latLng).radius(1000));
        this.searchCircle.setFillColor(Color.argb(66, 255, 0, 255));
        this.searchCircle.setStrokeColor(Color.argb(66, 0, 0, 0));
        CameraPosition camPos = new CameraPosition.Builder().target(latLng).zoom(15).bearing(45).tilt(70).build();
        CameraUpdate camUpd3 = CameraUpdateFactory.newCameraPosition(camPos);
        googleMap.animateCamera(camUpd3);
        BitmapDescriptor icon;
        if (userLocalStorage.isDriverMode()) {
            icon = BitmapDescriptorFactory.fromResource(R.drawable.car);
        } else {
            icon = BitmapDescriptorFactory.fromResource(R.drawable.person);
        }

        MarkerOptions markerOpts = new MarkerOptions().position(latLng).title("You're Here!").icon(icon);
        currentLocationMarker = googleMap.addMarker(markerOpts);
    }

    private synchronized void buildGoogleApiClient() {
        try {
            if (googleApiClient == null) {
                googleApiClient = new GoogleApiClient.Builder(this)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(LocationServices.API)
                        .addApi(Places.GEO_DATA_API)
                        .addApi(Places.PLACE_DETECTION_API)
                        .build();
            }
        } catch (Exception exc) {
            Log.e(loggerTag, exc.getMessage());
            Log.e(loggerTag, String.valueOf(exc.getCause()));
        }
    }

    private boolean isDriverMode() {
        return !userModeButton.isChecked();
    }

    private void addVehicleInformationToDB(final Vehicle vehicle, final VehicleRegistrationDialogFragment vehicleRegistrationDialogFragment) {
        if (vehicle == null) {
            Log.d(loggerTag, "Vehicle is NULL!");
            return;
        }


        vehicleMobileServiceTable.insert(vehicle, new TableOperationCallback<Vehicle>() {
            public void onCompleted(Vehicle entity, Exception exception, ServiceFilterResponse response) {
                if (exception == null) {
                    Log.i(loggerTag, "Service added the vehicle information successfully!");
                    Toast.makeText(getApplicationContext(), "Vehicle information was successfully added to system!", Toast.LENGTH_SHORT).show();
                    storeVehicleInformationToLocal(vehicle);
                    vehicleRegistrationDialogFragment.setCancelable(true);
                    vehicleRegistrationDialogFragment.dismiss();
                } else {
                    Log.e(loggerTag, exception.getMessage());
                    Toast.makeText(getApplicationContext(), "Vehicle information was not added to system!", Toast.LENGTH_SHORT).show();
                    vehicleRegistrationDialogFragment.setCancelable(true);
                    vehicleRegistrationDialogFragment.dismiss();
                }
            }
        });
    }

    private void initializeMobileService() {
        try {
            MobileServiceClient mobileServiceClient = new MobileServiceClient(
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

    private void clearVehicle() {
        vehicleLocalStorage.storeVehicleId(null);
        vehicleLocalStorage.storeVehicleModel(null);
        vehicleLocalStorage.storeVehicleColor(null);
        vehicleLocalStorage.storeVehicleYear(null);
        vehicleLocalStorage.storeVehicleLicensePlate(null);
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        String sensor = "sensor=false";
        String parameters = str_origin + "&" + str_dest + "&" + sensor;
        String output = "json";
        return "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
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
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d(loggerTag, e.getMessage());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }

        return data;
    }

    private void addRideToDB(final Ride ride, final CreateRideDialogFragment createRideDialogFragment) {
        if (ride == null) {
            Log.d(loggerTag, "Ride is NULL!");
            return;
        }

        rideMobileServiceTable.insert(ride, new TableOperationCallback<Ride>() {
            public void onCompleted(Ride result, Exception exception, ServiceFilterResponse response) {
                if (exception == null) {
                    Log.i(loggerTag, "Service added the ride successfully!");
                    if (result == null) {
                        Log.i(loggerTag, "Result is null from ride insertion!");
                    } else {
                        ride.setDriverId(result.getDriverId());
                    }

                    rideLocalStorage.storeRide(ride);
                    rideLocalStorage.storeRideOrigin(HomePageActivity.this.origin);
                    rideLocalStorage.storeRideDestination(HomePageActivity.this.destination);
                } else {
                    Log.e(loggerTag, exception.getMessage());
                    Toast.makeText(getApplicationContext(), "Ride was not created!", Toast.LENGTH_SHORT).show();
                }

                createRideDialogFragment.dismiss();
            }
        });
    }

    private void storeRegistrationInstanceId() {

        new AsyncTask() {
            @Override
            protected Object doInBackground(Object... params) {
                if (googleCloudMessaging == null) {
                    googleCloudMessaging = GoogleCloudMessaging.getInstance(HomePageActivity.this);
                }

                String regId = null;
                try {
                    regId = googleCloudMessaging.register(getString(R.string.gcmProjectNumber));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                rideLocalStorage.storeOwnInstanceId(regId);
                Log.d(loggerTag, regId);
                return null;
            }
        }.execute(null, null, null);
    }

    private void animateMarkerTo(final Marker marker, final double lat, final double lng) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final long DURATION_MS = 1000;
        final Interpolator interpolator = new AccelerateDecelerateInterpolator();
        final LatLng startPosition = marker.getPosition();
        handler.post(new Runnable() {
            @Override
            public void run() {
                float elapsed = SystemClock.uptimeMillis() - start;
                float t = elapsed / DURATION_MS;
                float v = interpolator.getInterpolation(t);

                double currentLat = (lat - startPosition.latitude) * v + startPosition.latitude;
                double currentLng = (lng - startPosition.longitude) * v + startPosition.longitude;
                marker.setPosition(new LatLng(currentLat, currentLng));

                // if animation is not finished yet, repeat
                if (t < 1) {
                    handler.postDelayed(this, 16);
                }
            }
        });
    }

    private double zoomLevelToRadius(double zoomLevel) {
        return 16384000 / Math.pow(2, zoomLevel);
    }

    private void getAllRides() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    rideMobileServiceTable.execute(new TableQueryCallback<Ride>() {
                        public void onCompleted(List<Ride> result, int count, Exception exception, ServiceFilterResponse response) {
                            if (exception == null) {
                                if (result.isEmpty() || result == null) {
                                    Log.i(loggerTag, "There is NO RIDE!");
                                    Toast.makeText(getApplicationContext(), "NO RIDE is Found", Toast.LENGTH_SHORT).show();
                                } else {
                                    HomePageActivity.this.ridesList = (ArrayList<Ride>) result;
                                }
                            } else {
                                Log.e(loggerTag, exception.getCause().toString());
                            }
                        }
                    });
                } catch (Exception exception) {
                    Log.e(loggerTag, exception.getMessage());
                }
                return null;
            }
        }.execute();
    }

    private MarkerOptions getDriverMarker(String key, GeoLocation location) {
        String snippet = "";
        if (ridesList == null || ridesList.size() == 0) {
            Log.d(loggerTag, "No Ride exists");
            return null;
        }

        for (Ride ride : ridesList) {
            if (ride.getDriverId().equals(key)) {
                snippet = getDriverMarkerSnippet(ride);
                break;
            }
        }

        BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.car);
        return new MarkerOptions()
                .position(new LatLng(location.latitude, location.longitude)).icon(icon).snippet(snippet);
    }

    private MarkerOptions getPedestrianMarker(String key, GeoLocation location) {
        BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.person);
        return new MarkerOptions()
                .position(new LatLng(location.latitude, location.longitude)).icon(icon).title("Pedestrian");
    }

    private String getDriverMarkerSnippet(Ride ride) {
        return "From: " + ride.getRideFrom() + " To: " + ride.getRideTo();
    }

    private String getRequestAlertMessage(Ride ride) {
        return ride.getDriverName() + " " + ride.getDriverSurName() + "\n"
                + "FROM: " + ride.getRideFrom() + "\n"
                + "TO: " + ride.getRideTo() + "\n"
                + "TIME: " + ride.getAppointmentTime() + "\n"
                + "AVAILABLE SEAT: " + ride.getAvailableSeat() + "\n"
                + "RIDE COMMENT: " + ride.getRideComment() + "\n";
    }

    private void buildRequestAlert(final Ride ride) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String message = getRequestAlertMessage(ride);
        builder.setMessage(message + "\n\n" + "Do you want to make request?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        ride.setPedestrianId(userLocalStorage.getUserId());
                        ride.setPedestrianInstanceId(rideLocalStorage.getOwnInstanceId());
                        ride.setPedestrianName(userLocalStorage.getUserName());
                        ride.setPedestrianSurName(userLocalStorage.getUserSurName());
                        rideLocalStorage.storeRide(ride);
                        HomePageActivity.this.makeRequestToRide(ride);
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

    private void makeRequestToRide(final Ride ride) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    rideMobileServiceTable.update(ride).get();
                    runOnUiThread(new Runnable() {
                        public void run() {

                        }
                    });
                } catch (Exception exception) {
                    Log.e(loggerTag, exception.getMessage());
                }
                return null;
            }
        }.execute();
    }

    private void initializeLocalStores() {
        userLocalStorage = new UserLocalStorage(getSharedPreferences(StoragePreferences.USER_PREFERENCES, Context.MODE_PRIVATE));
        vehicleLocalStorage = new VehicleLocalStorage(getSharedPreferences(StoragePreferences.VEHICLE_PREFERENCES, Context.MODE_PRIVATE));
        rideLocalStorage = new RideLocalStorage(getSharedPreferences(StoragePreferences.RIDE_PREFERENCES, Context.MODE_PRIVATE));
    }

    private void initializeLayoutEntities() {
        this.searchRideButton = (ImageButton) findViewById(R.id.searh_ride);
        this.searchRideButton.setOnClickListener(new SearchRideListener());
        ImageButton settingsButton = (ImageButton) findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(new SettingsListener());
        ImageButton chatButton = (ImageButton) findViewById(R.id.chat_button);
        chatButton.setOnClickListener(new ChatListener());
        this.driverRideButton = (ImageButton) findViewById(R.id.driver_ride);
        this.driverRideButton.setOnClickListener(new DriverRideListener());
        this.rideCancelButton = (ImageButton) findViewById(R.id.ride_cancel);
        this.rideCancelButton.setOnClickListener(new RideCancelListener());
        this.searchRideText = (TextView) findViewById(R.id.search_ride_text);
        this.userModeButton = (ToggleButton) findViewById(R.id.toggle_button);
        this.userModeButton.setOnCheckedChangeListener(new UserModeListener());
        if (this.userLocalStorage.isDriverMode()) {
            if (rideLocalStorage.getRideId() == null || rideLocalStorage.getRideId().isEmpty()) {
                driverRideButton.setVisibility(View.VISIBLE);
                rideCancelButton.setVisibility(View.GONE);
            } else {
                Log.d(loggerTag, "ID: " + rideLocalStorage.getRideId());
                driverRideButton.setVisibility(View.GONE);
                rideCancelButton.setVisibility(View.VISIBLE);
            }
            searchRideButton.setVisibility(View.GONE);
            searchRideText.setVisibility(View.GONE);
            this.userModeButton.setChecked(false);
        } else {
            this.userModeButton.setChecked(true);
        }
    }

    private void initializeFirebaseEntities() {
        Firebase.setAndroidContext(this);
        this.firebase = new Firebase(getString(R.string.firebaseUrl));
        this.firebase.child(this.userLocalStorage.getUserId()).setValue(String.valueOf(this.userLocalStorage.isDriverMode()));
        this.geoFire = new GeoFire(new Firebase(getString(R.string.firebaseCoordinates)));
        this.geoFire.setLocation(this.userLocalStorage.getUserId(), new GeoLocation(this.currentLocation.getLatitude(), this.currentLocation.getLongitude()));
        this.geoQuery = geoFire.queryAtLocation(new GeoLocation(this.currentLocation.getLatitude(), this.currentLocation.getLongitude()), 1);
        this.geoQuery.addGeoQueryEventListener(this);
    }

    private boolean isHasVehicle() {
        try {
            MobileServiceList<Vehicle> vehicleInfo = (MobileServiceList<Vehicle>) vehicleMobileServiceTable.where().field("id").eq(userLocalStorage.getUserId()).execute().get();
            if (vehicleInfo == null || vehicleInfo.size() == 0) {
                Log.d(loggerTag, "VEHICLE NULL");
                return false;
            }

            Log.d(loggerTag, "HAS VEHICLE");
            this.storeVehicleInformationToLocal(vehicleInfo.get(0));
            return true;
        } catch (InterruptedException e) {
            Log.e(loggerTag, e.getMessage());
        } catch (ExecutionException e) {
            Log.e(loggerTag, e.getMessage());
        }

        return false;
    }

    private boolean isHasRide() {
        try {
            MobileServiceList<Ride> rideInfo = (MobileServiceList<Ride>) rideMobileServiceTable.where().field("ride_driver_id").eq(userLocalStorage.getUserId())
                    .and().field("ride_driver_instance_id").eq(rideLocalStorage.getOwnInstanceId()).execute().get();
            if (rideInfo == null || rideInfo.size() == 0) {
                return false;
            }

            rideLocalStorage.storeRide(rideInfo.get(0));
            return true;
        } catch (InterruptedException e) {
            Log.e(loggerTag, e.getMessage());
        } catch (ExecutionException e) {
            Log.e(loggerTag, e.getMessage());
        }

        return false;
    }

    private void registerReceiver(){
        if(!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(registrationBroadcastReceiver,
                    new IntentFilter(StoragePreferences.REGISTRATION_COMPLETE));
            isReceiverRegistered = true;
        }
    }
    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(loggerTag, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
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

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            } catch (Exception e) {
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
            if (result.size() == 0) {
                Log.d(loggerTag, "Result Size is NULL!");
                return;
            }

            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

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
                HomePageActivity.this.googleMap.clear();
                if (isChecked) {
                    Log.d(loggerTag, "Pedestrian Mode");
                    driverRideButton.setVisibility(View.GONE);
                    rideCancelButton.setVisibility(View.GONE);
                    searchRideButton.setVisibility(View.VISIBLE);
                    searchRideText.setVisibility(View.VISIBLE);
                    userLocalStorage.storeIsDriver(false);
                    HomePageActivity.this.firebase.child(userLocalStorage.getUserId()).setValue(String.valueOf(false));
                    centerInLocation(HomePageActivity.this.currentLocation);
                    Toast.makeText(getApplicationContext(), "Pedestrian Mode", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Driver Mode", Toast.LENGTH_SHORT).show();
                    Log.d(loggerTag, "Driver Mode");
                    searchRideButton.setVisibility(View.GONE);
                    searchRideText.setVisibility(View.GONE);
                    userLocalStorage.storeIsDriver(true);
                    HomePageActivity.this.firebase.child(userLocalStorage.getUserId()).setValue(String.valueOf(true));
                    if (rideLocalStorage.getRideId() == null || rideLocalStorage.getRideId().isEmpty()) {
                        driverRideButton.setVisibility(View.VISIBLE);
                        rideCancelButton.setVisibility(View.GONE);
                        centerInLocation(HomePageActivity.this.currentLocation);
                    } else {
                        driverRideButton.setVisibility(View.GONE);
                        rideCancelButton.setVisibility(View.VISIBLE);
                        drawRoute(HomePageActivity.this.rideLocalStorage.getRideOrigin(), HomePageActivity.this.rideLocalStorage.getRideDestination());
                    }

                    if (vehicleLocalStorage.getVehicleId() == null) {
                        showVehicleRegistrationDialog();
                    }
                }


            } catch (Exception exc) {
                Log.e(loggerTag, exc.getMessage());
            }
        }
    }

    private class ChatListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Firebase.setAndroidContext(HomePageActivity.this);
            Firebase firebaseChat;
            if (userLocalStorage.isDriverMode()) {
                firebaseChat = new Firebase(getString(R.string.firebaseDriversChat));
            } else {
                firebaseChat = new Firebase(getString(R.string.firebaseUsersChat));
            }

            firebaseChat.addValueEventListener(new ChatValueEventListener());
        }
    }

    private class DriverRideListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            showCreateRideDialog();
        }
    }

    private class RideCancelListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Ride ride = rideLocalStorage.getRide();
            Log.d(loggerTag, ride.getRideId());
            rideMobileServiceTable.delete(ride);
            rideLocalStorage.clearRideLocalStorage();
            HomePageActivity.this.googleMap.clear();
            HomePageActivity.this.centerInLocation(currentLocation);
            HomePageActivity.this.origin = null;
            HomePageActivity.this.destination = null;
            driverRideButton.setVisibility(View.VISIBLE);
            rideCancelButton.setVisibility(View.GONE);
        }
    }

    private class UserMarkerClickListener implements GoogleMap.OnMarkerClickListener {

        @Override
        public boolean onMarkerClick(Marker marker) {
            String markerId = markersIdKeyPair.get(marker.getId());
            LatLng currentPosition = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            if (marker.getPosition().equals(currentPosition)) {
                Log.d(loggerTag, "My Marker");
                marker.showInfoWindow();
                return true;
            }

            if (userLocalStorage.isDriverMode()) {
                Log.d(loggerTag, "Pedestrian clicked!");
                marker.showInfoWindow();
                return true;
            } else if (ridesList == null || ridesList.size() == 0) {
                Log.d(loggerTag, "No ride when marker clicked!");
                return false;
            }

            for (Ride ride : ridesList) {
                if (ride.getDriverId().equals(markerId)) {
                    marker.hideInfoWindow();
                    buildRequestAlert(ride);
                    break;
                }
            }
            return true;
        }
    }

    private class ChatValueEventListener implements ValueEventListener {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (dataSnapshot.hasChildren()) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    hashTags.add("#" + child.getKey());
                }
            }

            if (userLocalStorage.isDriverMode()) {
                hashTags.add(getString(R.string.firebaseDriversChat));
            } else {
                hashTags.add(getString(R.string.firebaseUsersChat));
            }

            Log.d(loggerTag, "URL: " + hashTags.get(hashTags.size() - 1));
            Intent intent = new Intent(HomePageActivity.this, ChatHashTagsActivity.class);
            intent.putExtra("hashTagList", hashTags);
            startActivity(intent);
        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {
            Log.e(loggerTag, firebaseError.getMessage());
        }
    }
}