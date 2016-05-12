package com.iride.ayride;

import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

public class RideLocalStorage {

    private final static String loggerTag = RideLocalStorage.class.getSimpleName();
    private final static String rideIdKey = "RIDEIDKEY";
    private final static String rideFromKey = "RIDEFROMKEY";
    private final static String rideToKey = "RIDETOKEY";
    private final static String rideAppointmentTimeKey = "RIDEAPPOINTMENTTIME";
    private final static String rideAvailableSeatKey = "RIDEAVAILABLESEAT";
    private final static String rideCommentKey = "RIDECOMMENTKEY";
    private final static String rideDriverIdKey = "RIDEDRIVERIDKEY";
    private final static String rideDriverInstanceIdKey = "RIDEDRIVERINSTANCEIDKEY";
    private final static String rideDriverNameKey = "RIDEDRIVERNAMEKEY";
    private final static String rideDriverSurnameKey = "RIDEDRIVERSURNAMEKEY";
    private final static String ridePedestrianIdKey = "RIDEPEDESTRIANIDKEY";
    private final static String ridePedestrianInstanceIdKey = "RIDEPEDESTRIANINSTANCEIDKEY";
    private final static String ridePedestrianNameKey = "RIDEPEDESTRIANNAMEKEY";
    private final static String ridePedestrianSurnameKey = "RIDEPEDESTRIANSURNAMEKEY";
    private final static String rideIsAcceptedKey = "RIDEISACCEPTED";
    private final static String rideIsRejectedKey = "RIDEISREJECTED";
    private final static String rideIsCompleteKey = "RIDEISCOMPLETE";
    private final static String rideIsCanceledKey = "RIDEISCANCELED";
    private final static String rideOriginLatitudeKey = "RIDEORIGINLATITUDE";
    private final static String rideOriginLongitudeKey = "RIDEORIGINLONGITUDE";
    private final static String rideDestinationLatitudeKey = "RIDEDESTINATIONLATITUDE";
    private final static String rideDestinationLongitudeKey = "RIDEDESTINATIONLONGITUDE";
    private final static String ownInstanceIdKey = "OWNINSTANCEIDKEY";

    private SharedPreferences rideSharedPreferences;
    private SharedPreferences.Editor rideSharedPreferencesEditor;

    public RideLocalStorage(SharedPreferences rideSharedPreferences) {
        this.rideSharedPreferences = rideSharedPreferences;
        this.rideSharedPreferencesEditor = this.rideSharedPreferences.edit();
    }

    public void storeOwnInstanceId(String ownInstanceId) {
        if (ownInstanceId.isEmpty()) {
            Log.d(loggerTag, "Own Instance Id is null or empty!");
            return;
        }

        rideSharedPreferencesEditor.putString(ownInstanceIdKey, ownInstanceId);
        rideSharedPreferencesEditor.apply();
    }

    public void storeRideId(String rideId) {
        if (rideId == null) {
            Log.d(loggerTag, "Ride Id is null or empty!");
            rideSharedPreferencesEditor.putString(rideIdKey, "");
            rideSharedPreferencesEditor.apply();
            return;
        }

        rideSharedPreferencesEditor.putString(rideIdKey, rideId);
        rideSharedPreferencesEditor.apply();
    }

    public void storeRideFrom(String rideFrom) {
        if (rideFrom == null) {
            Log.d(loggerTag, "Ride From is null or empty!");
            rideSharedPreferencesEditor.putString(rideFromKey, "");
            rideSharedPreferencesEditor.apply();
            return;
        }

        rideSharedPreferencesEditor.putString(rideFromKey, rideFrom);
        rideSharedPreferencesEditor.apply();
    }

    public void storeRideTo(String rideTo) {
        if (rideTo == null) {
            Log.d(loggerTag, "Ride To is null or empty!");
            rideSharedPreferencesEditor.putString(rideToKey, "");
            rideSharedPreferencesEditor.apply();
            return;
        }

        rideSharedPreferencesEditor.putString(rideToKey, rideTo);
        rideSharedPreferencesEditor.apply();
    }

    public void storeRideAvailableSeat(String rideAvailableSeat) {
        if (rideAvailableSeat == null) {
            Log.d(loggerTag, "Ride Available Seat is null or empty!");
            rideSharedPreferencesEditor.putString(rideAvailableSeatKey, "");
            rideSharedPreferencesEditor.apply();
            return;
        }

        rideSharedPreferencesEditor.putString(rideAvailableSeatKey, rideAvailableSeat);
        rideSharedPreferencesEditor.apply();
    }

    public void storeRideAppointmentTime(String rideAppointmentTime) {
        if (rideAppointmentTime == null) {
            Log.d(loggerTag, "Ride Appointment Time is null or empty!");
            rideSharedPreferencesEditor.putString(rideAppointmentTimeKey, "");
            rideSharedPreferencesEditor.apply();
            return;
        }

        rideSharedPreferencesEditor.putString(rideAppointmentTimeKey, rideAppointmentTime);
        rideSharedPreferencesEditor.apply();
    }

    public void storeRideComment(String rideComment) {
        if (rideComment == null) {
            Log.d(loggerTag, "Ride Comment is null or empty!");
            rideSharedPreferencesEditor.putString(rideCommentKey, rideComment);
            rideSharedPreferencesEditor.apply();
            return;
        }

        rideSharedPreferencesEditor.putString(rideCommentKey, rideComment);
        rideSharedPreferencesEditor.apply();
    }

    public void storeRideDriverId(String rideDriverId) {
        if (rideDriverId == null) {
            Log.d(loggerTag, "Ride Driver Id is null or empty!");
            rideSharedPreferencesEditor.putString(rideDriverIdKey, "");
            rideSharedPreferencesEditor.apply();
            return;
        }

        rideSharedPreferencesEditor.putString(rideDriverIdKey, rideDriverId);
        rideSharedPreferencesEditor.apply();
    }

    public void storeRideDriverInstanceId(String rideDriverInstanceId) {
        if (rideDriverInstanceId == null) {
            Log.d(loggerTag, "Ride Driver Instance Id is null or empty!");
            rideSharedPreferencesEditor.putString(rideDriverInstanceIdKey, "");
            rideSharedPreferencesEditor.apply();
            return;
        }

        rideSharedPreferencesEditor.putString(rideDriverInstanceIdKey, rideDriverInstanceId);
        rideSharedPreferencesEditor.apply();
    }

    public void storeRideDriverName(String rideDriverName) {
        if (rideDriverName == null) {
            Log.d(loggerTag, "Ride Driver Name is null or empty!");
            rideSharedPreferencesEditor.putString(rideDriverNameKey, "");
            rideSharedPreferencesEditor.apply();
            return;
        }

        rideSharedPreferencesEditor.putString(rideDriverNameKey, rideDriverName);
        rideSharedPreferencesEditor.apply();
    }

    public void storeRideDriverSurname(String rideDriverSurname) {
        if (rideDriverSurname == null) {
            Log.d(loggerTag, "Ride Driver Surname is null or empty!");
            rideSharedPreferencesEditor.putString(rideDriverSurnameKey, "");
            rideSharedPreferencesEditor.apply();
            return;
        }

        rideSharedPreferencesEditor.putString(rideDriverSurnameKey, rideDriverSurname);
        rideSharedPreferencesEditor.apply();
    }

    public void storeRidePedestrianId(String ridePedestrianId) {
        if (ridePedestrianId == null) {
            Log.d(loggerTag, "Ride Pedestrian Id is null or empty!");
            rideSharedPreferencesEditor.putString(ridePedestrianIdKey, "");
            rideSharedPreferencesEditor.apply();
            return;
        }

        rideSharedPreferencesEditor.putString(ridePedestrianIdKey, ridePedestrianId);
        rideSharedPreferencesEditor.apply();
    }

    public void storeRidePedestrianInstanceId(String ridePedestrianInstanceId) {
        if (ridePedestrianInstanceId == null) {
            Log.d(loggerTag, "Ride Pedestrian Instance Id is null or empty!");
            rideSharedPreferencesEditor.putString(ridePedestrianInstanceIdKey, "");
            rideSharedPreferencesEditor.apply();
            return;
        }

        rideSharedPreferencesEditor.putString(ridePedestrianInstanceIdKey, ridePedestrianInstanceId);
        rideSharedPreferencesEditor.apply();
    }

    public void storeRidePedestrianName(String ridePedestrianName) {
        if (ridePedestrianName == null) {
            Log.d(loggerTag, "Ride Pedestrian Name is null or empty!");
            rideSharedPreferencesEditor.putString(ridePedestrianNameKey, "");
            rideSharedPreferencesEditor.apply();
            return;
        }

        rideSharedPreferencesEditor.putString(ridePedestrianNameKey, ridePedestrianName);
        rideSharedPreferencesEditor.apply();
    }

    public void storeRidePedestrianSurname(String ridePedestrianSurname) {
        if (ridePedestrianSurname == null) {
            Log.d(loggerTag, "Ride Pedestrian Surname is null or empty!");
            rideSharedPreferencesEditor.putString(ridePedestrianSurnameKey, "");
            rideSharedPreferencesEditor.apply();
            return;
        }

        rideSharedPreferencesEditor.putString(ridePedestrianSurnameKey, ridePedestrianSurname);
        rideSharedPreferencesEditor.apply();
    }

    public void storeRideOrigin(LatLng origin) {
        if (origin == null) {
            Log.d(loggerTag, "Ride Origin is null or empty!");
            rideSharedPreferencesEditor.putString(rideOriginLatitudeKey, "");
            rideSharedPreferencesEditor.putString(rideOriginLongitudeKey, "");
            rideSharedPreferencesEditor.apply();
            return;
        }

        rideSharedPreferencesEditor.putFloat(rideOriginLatitudeKey, (float) origin.latitude);
        rideSharedPreferencesEditor.putFloat(rideOriginLongitudeKey, (float) origin.longitude);
        rideSharedPreferencesEditor.apply();
    }

    public void storeRideDestination(LatLng destination) {
        if (destination == null) {
            Log.d(loggerTag, "Ride Origin is null or empty!");
            rideSharedPreferencesEditor.putString(rideDestinationLatitudeKey, "");
            rideSharedPreferencesEditor.putString(rideDestinationLongitudeKey, "");
            rideSharedPreferencesEditor.apply();
            return;
        }

        rideSharedPreferencesEditor.putFloat(rideDestinationLatitudeKey, (float) destination.latitude);
        rideSharedPreferencesEditor.putFloat(rideDestinationLongitudeKey, (float) destination.longitude);
        rideSharedPreferencesEditor.apply();

    }

    public void storeIsAccepted(boolean isAccepted) {
        rideSharedPreferencesEditor.putBoolean(rideIsAcceptedKey, isAccepted);
        rideSharedPreferencesEditor.apply();
    }

    public void storeIsRejected(boolean isRejected) {
        rideSharedPreferencesEditor.putBoolean(rideIsRejectedKey, isRejected);
        rideSharedPreferencesEditor.apply();
    }

    public void storeIsComplete(boolean isComplete) {
        rideSharedPreferencesEditor.putBoolean(rideIsCompleteKey, isComplete);
        rideSharedPreferencesEditor.apply();
    }

    public void storeIsCanceled(boolean isCanceled) {
        rideSharedPreferencesEditor.putBoolean(rideIsCanceledKey, isCanceled);
        rideSharedPreferencesEditor.apply();
    }

    public String getOwnInstanceId() {
        return rideSharedPreferences.getString(ownInstanceIdKey, null);
    }

    public String getRideId() {
        return rideSharedPreferences.getString(rideIdKey, null);
    }

    public String getRideFrom() {
        return rideSharedPreferences.getString(rideFromKey, null);
    }

    public String getRideTo() {
        return rideSharedPreferences.getString(rideToKey, null);
    }

    public String getRideAppointmentTime() {
        return rideSharedPreferences.getString(rideAppointmentTimeKey, null);
    }

    public String getRideAvailableSeat() {
        return rideSharedPreferences.getString(rideAvailableSeatKey, null);
    }

    public String getRideComment() {
        return rideSharedPreferences.getString(rideCommentKey, null);
    }

    public String getRideDriverId() {
        return rideSharedPreferences.getString(rideDriverIdKey, null);
    }

    public String getRideDriverInstanceId() {
        return rideSharedPreferences.getString(rideDriverInstanceIdKey, null);
    }

    public String getRideDriverName() {
        return rideSharedPreferences.getString(rideDriverNameKey, null);
    }

    public String getRideDriverSurname() {
        return rideSharedPreferences.getString(rideDriverSurnameKey, null);
    }

    public String getRidePedestrianId() {
        return rideSharedPreferences.getString(ridePedestrianIdKey, null);
    }

    public String getRidePedestrianInstanceId() {
        return rideSharedPreferences.getString(ridePedestrianInstanceIdKey, null);
    }

    public String getRidePedestrianName() {
        return rideSharedPreferences.getString(ridePedestrianNameKey, null);
    }

    public String getRidePedestrianSurname() {
        return rideSharedPreferences.getString(ridePedestrianSurnameKey, null);
    }

    public LatLng getRideOrigin() {
        return new LatLng(rideSharedPreferences.getFloat(rideOriginLatitudeKey, 0),
                rideSharedPreferences.getFloat(rideOriginLongitudeKey, 0));
    }

    public LatLng getRideDestination() {
        return new LatLng(rideSharedPreferences.getFloat(rideDestinationLatitudeKey, 0),
                rideSharedPreferences.getFloat(rideDestinationLongitudeKey, 0));

    }

    public boolean getRideIsAccepted() {
        return rideSharedPreferences.getBoolean(rideIsAcceptedKey, true);
    }

    public boolean getRideIsRejected() {
        return rideSharedPreferences.getBoolean(rideIsRejectedKey, true);
    }

    public boolean getRideIsComplete() {
        return rideSharedPreferences.getBoolean(rideIsCompleteKey, true);
    }

    public boolean getRideIsCanceled() {
        return rideSharedPreferences.getBoolean(rideIsCanceledKey, true);
    }

    public Ride getRide() {
        Ride ride = new Ride();
        ride.setRideFrom(this.getRideFrom());
        ride.setRideTo(this.getRideTo());
        ride.setAvailableSeat(this.getRideAvailableSeat());
        ride.setAppointmentTime(this.getRideAppointmentTime());
        ride.setRideComment(this.getRideComment());
        ride.setRideId(this.getRideId());
        ride.setDriverId(this.getRideDriverId());
        ride.setDriverInstanceId(this.getRideDriverInstanceId());
        ride.setDriverName(this.getRideDriverName());
        ride.setDriverSurName(this.getRideDriverSurname());
        ride.setPedestrianId(this.getRidePedestrianId());
        ride.setPedestrianInstanceId(this.getRidePedestrianInstanceId());
        ride.setPedestrianName(this.getRidePedestrianName());
        ride.setPedestrianSurName(this.getRidePedestrianSurname());
        ride.setIsAccepted(this.getRideIsAccepted());
        ride.setIsRejected(this.getRideIsRejected());
        ride.setIsComplete(this.getRideIsComplete());
        ride.setIsCanceled(this.getRideIsCanceled());
        return ride;
    }

    public void storeRide(Ride ride) {
        storeRideId(ride.getRideId());
        storeRideFrom(ride.getRideFrom());
        storeRideTo(ride.getRideTo());
        storeRideAppointmentTime(ride.getAppointmentTime());
        storeRideAvailableSeat(ride.getAvailableSeat());
        storeRideComment(ride.getRideComment());
        storeRideDriverId(ride.getDriverId());
        storeRideDriverInstanceId(ride.getDriverInstanceId());
        storeRideDriverName(ride.getDriverName());
        storeRideDriverSurname(ride.getDriverSurName());
        storeRidePedestrianId(ride.getPedestrianId());
        storeRidePedestrianInstanceId(ride.getPedestrianInstanceId());
        storeRidePedestrianName(ride.getPedestrianName());
        storeRidePedestrianSurname(ride.getPedestrianSurName());
        storeIsAccepted(ride.isAccepted());
        storeIsRejected(ride.isRejected());
        storeIsCanceled(ride.isCanceled());
        storeIsComplete(ride.isComplete());
    }

    public void clearRideLocalStorage() {
        storeRideId(null);
        storeRideFrom(null);
        storeRideTo(null);
        storeRideAppointmentTime(null);
        storeRideAvailableSeat(null);
        storeRideComment(null);
        storeRideDriverId(null);
        storeRideDriverInstanceId(null);
        storeRideDriverName(null);
        storeRideDriverSurname(null);
        storeRidePedestrianId(null);
        storeRidePedestrianInstanceId(null);
        storeRidePedestrianName(null);
        storeRidePedestrianSurname(null);
        storeRideOrigin(null);
        storeRideDestination(null);
        storeIsAccepted(false);
        storeIsRejected(false);
        storeIsCanceled(false);
        storeIsComplete(false);
    }
}
