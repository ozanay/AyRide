package com.iride.ayride;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.notifications.NotificationsHandler;

import java.util.ArrayList;

public class RideRequestHandler extends NotificationsHandler {

    private final static String loggerTag = RideRequestHandler.class.getSimpleName();
    private final static String rideRequestMessage = "RIDE REQUEST MESSAGE";
    private final static String rideRequestAcceptedMessage = "REQUEST ACCEPTED";
    private final static String rideRequestRejectedMessage = "REQUEST REJECTED";
    private final static String rideCanceled = "RIDE CANCELED";
    private static final int NOTIFICATION_ID = 1;
    private MobileServiceClient mobileServiceClient;
    private RideLocalStorage rideLocalStorage;

    @Override
    public void onRegistered(final Context context,  final String gcmRegistrationId) {
        super.onRegistered(context, gcmRegistrationId);

        new AsyncTask<Void, Void, Void>() {

            protected Void doInBackground(Void... params) {
                try {
                    rideLocalStorage = new RideLocalStorage(context.getSharedPreferences(String.valueOf(StoragePreferences.RIDEPREFERENCES),Context.MODE_PRIVATE));
                    mobileServiceClient.getPush().register(gcmRegistrationId, null);
                    return null;
                }
                catch(Exception e) {
                    // handle error
                    Log.e(loggerTag, e.getMessage());
                }
                return null;
            }
        }.execute();
    }

    @Override
    public void onReceive(Context context, Bundle bundle) {
        String message = bundle.getString("message");
        PendingIntent contentIntent = null;
        Ride ride = rideLocalStorage.getRide();
        if (ride.getPedestrianId().isEmpty()){
            ride.setPedestrianId(bundle.getString("pedestrianId"));
            ride.setPedestrianInstanceId(bundle.getString("pedestrianInstanceId"));
            ride.setPedestrianName(bundle.getString("pedestrianName"));
            ride.setPedestrianSurName("pedestrianSurname");
            Log.d(loggerTag, ride.getPedestrianId() + "\n" + ride.getPedestrianInstanceId() + "\n"
                    + ride.getPedestrianName() + "\n" + ride.getPedestrianSurName());
            rideLocalStorage.storeRide(ride);
        }

        switch (message){
            case(rideRequestMessage):
                Intent intent = new Intent(context, IncomingRequestActivity.class);
                ArrayList<Ride> rides = new ArrayList<>();
                rides.add(ride);
                intent.putExtra("Ride",rides);
                contentIntent = PendingIntent.getActivity(context, 0, new Intent(context, IncomingRequestActivity.class), 0);
                break;
            case(rideRequestAcceptedMessage):
                // Chat Application Will Open
                break;
            case(rideRequestRejectedMessage):
                contentIntent = PendingIntent.getActivity(context, 0, new Intent(context, SearchRideActivity.class), 0);
                break;
            case(rideCanceled):
                contentIntent = PendingIntent.getActivity(context, 0, new Intent(context, SearchRideActivity.class), 0);
                break;
        }

        Notification notification = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("RideRequestNotification")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setContentText(message)
                .setContentIntent(contentIntent)
                .build();

        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(NOTIFICATION_ID, notification);
    }
}
