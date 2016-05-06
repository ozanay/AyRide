package com.iride.ayride;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.table.TableOperationCallback;
import com.microsoft.windowsazure.notifications.NotificationsManager;

import java.net.MalformedURLException;

public class IncomingRequestActivity extends AppCompatActivity {

    private final static String loggerTag = IncomingRequestActivity.class.getSimpleName();
    private Ride ride;
    private TextView textView;
    private Button acceptButton;
    private Button rejectButton;
    private RelativeLayout loadingPanel;
    private RideLocalStorage rideLocalStorage;
    private MobileServiceClient mobileServiceClient;
    private MobileServiceTable mobileServiceTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_request);
        try {
            rideLocalStorage = new RideLocalStorage(getSharedPreferences(String.valueOf(StoragePreferences.RIDEPREFERENCES), Context.MODE_PRIVATE));
            this.ride = rideLocalStorage.getRide();
            if(ride == null){
                Log.d(loggerTag, "RIDE IS NULL");
            }

            acceptButton = (Button) findViewById(R.id.accept_button);
            acceptButton.setOnClickListener(acceptButtonListener);
            rejectButton = (Button) findViewById(R.id.reject_button);
            rejectButton.setOnClickListener(rejectButtonListener);
            loadingPanel = (RelativeLayout) findViewById(R.id.request_loading_panel);
            loadingPanel.setVisibility(View.GONE);
            textView = (TextView) findViewById(R.id.request_message);
            textView.setText(convertRideToMessage(ride));
            NotificationsManager.handleNotifications(this, getString(R.string.gcmSenderId), RideRequestHandler.class);

        } catch(Exception exc){
            Log.e(loggerTag, exc.getMessage());
        }
    }

    private String convertRideToMessage(Ride ride){
        return ride.getPedestrianName()+" "+ride.getPedestrianSurName()+"\n"
                +"Wants To Share Your Ride!";
    }

    private void initializeMobileService(){
        try {
            this.mobileServiceClient = new MobileServiceClient(
                    getString(R.string.azureApiUrl),
                    getString(R.string.azureApiKey),
                    this
            );
            this.mobileServiceTable = mobileServiceClient.getTable("ride_info", Ride.class);
        } catch (MalformedURLException e) {
            Log.e(loggerTag,e.getMessage());
        }
    }

    private void updateRide(Ride ride){
       initializeMobileService();
        this.mobileServiceTable.update(ride, new TableOperationCallback<Ride>() {
            @Override
            public void onCompleted(Ride ride, Exception exception, ServiceFilterResponse response) {
                if (exception == null){
                    Log.i(loggerTag,"Ride Was Updated Successfuly");
                    Toast.makeText(getApplicationContext(),"Ride Was Updated Successfuly",Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(loggerTag, exception.getMessage());
                }
            }
        });
    }

    private Ride clearRejectedRide(Ride ride){
        Ride clearedRide = ride;
        ride.setIsRejected(false);
        ride.setPedestrianId("");
        ride.setPedestrianInstanceId("");
        ride.setPedestrianName("");
        ride.setPedestrianSurName("");
        return clearedRide;
    }


    private View.OnClickListener acceptButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ride.setIsAccepted(true);
            rideLocalStorage.storeIsAccepted(true);
            updateRide(ride);
            Intent intent = new Intent(IncomingRequestActivity.this, ChatActivity.class);
            intent.putExtra("chatUrl",getString(R.string.firebaseP2PChat)+rideLocalStorage.getRideId());
            startActivity(intent);
            finish();
        }
    };

    private View.OnClickListener rejectButtonListener = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            ride.setIsRejected(true);
            rideLocalStorage.storeIsRejected(true);
            updateRide(ride);
            ride = clearRejectedRide(ride);
            updateRide(ride);
            rideLocalStorage.storeRide(ride);
            startActivity(new Intent(IncomingRequestActivity.this, HomePageActivity.class));
            finish();
        }
    };
}
