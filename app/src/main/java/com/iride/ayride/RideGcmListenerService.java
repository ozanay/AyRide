package com.iride.ayride;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

public class RideGcmListenerService extends GcmListenerService {

    private static final String TAG = RideGcmListenerService.class.getSimpleName();
    private static final int NOTIFICATION_ID = 1;

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param bundle Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle bundle) {
        String message = bundle.getString("message");
        RideLocalStorage rideLocalStorage = new RideLocalStorage(getSharedPreferences(StoragePreferences.RIDE_PREFERENCES, Context.MODE_PRIVATE));
        Ride ride = rideLocalStorage.getRide();
        if (ride.getPedestrianId().isEmpty()) {
            ride.setPedestrianId(bundle.getString("pedestrianId"));
            ride.setPedestrianInstanceId(bundle.getString("pedestrianInstanceId"));
            ride.setPedestrianName(bundle.getString("pedestrianName"));
            ride.setPedestrianSurName("pedestrianSurname");
            Log.d(TAG, ride.getPedestrianId() + "\n" + ride.getPedestrianInstanceId() + "\n"
                    + ride.getPedestrianName() + "\n" + ride.getPedestrianSurName());
            rideLocalStorage.storeRide(ride);
        }

        sendNotification(message, ride);
        // [END_EXCLUDE]
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     * @param ride Ride
     */
    private void sendNotification(String message, Ride ride) {
        PendingIntent pendingIntent = null;
        Intent intent;
        switch (message) {
            case (GcmRequestMessages.RIDE_REQUEST_MESSAGE):
                    intent = new Intent(this, IncomingRequestActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
                break;
            case (GcmRequestMessages.RIDE_REQUEST_ACCEPTED_MESSAGE):
                intent = new Intent(this, ChatActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("chatUrl", this.getString(R.string.firebaseP2PChat) + ride.getRideId());
                pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
                break;
            case (GcmRequestMessages.RIDE_REQUEST_REJECTED_MESSAGE):
                intent = new Intent(this, SearchRideActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
                break;
            case (GcmRequestMessages.RIDE_CANCELED):
                intent = new Intent(this, SearchRideActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
                break;
        }

        NotificationManager mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("Notification Hub Demo")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(message))
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentText(message)
                        .setContentIntent(pendingIntent);

        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
