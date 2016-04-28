package com.iride.ayride;

public class Ride {

    @com.google.gson.annotations.SerializedName("id")
    private String rideId;

    @com.google.gson.annotations.SerializedName("ride_from")
    private String rideFrom;

    @com.google.gson.annotations.SerializedName("ride_to")
    private String rideTo;

    @com.google.gson.annotations.SerializedName("ride_appointment_time")
    private String appointmentTime;

    @com.google.gson.annotations.SerializedName("ride_available_seat")
    private String availableSeat;

    @com.google.gson.annotations.SerializedName("ride_comment")
    private String rideComment;

    @com.google.gson.annotations.SerializedName("ride_driver_id")
    private String driverId;

    @com.google.gson.annotations.SerializedName("ride_pedestrian_id")
    private String pedestrianId;

    @com.google.gson.annotations.SerializedName("ride_is_complete")
    private boolean isComplete;

    @com.google.gson.annotations.SerializedName("ride_is_canceled")
    private boolean isCanceled;

    public Ride(){
        this.rideFrom = null;
        this.rideTo = null;
        this.appointmentTime = null;
        this.availableSeat = null;
        this.rideComment = null;
        this.driverId = null;
        this.pedestrianId = null;
        this.isCanceled = false;
        this.isComplete = false;
    }

    public boolean isCanceled() {
        return isCanceled;
    }

    public void setIsCanceled(boolean isCanceled) {
        this.isCanceled = isCanceled;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public void setIsComplete(boolean isComplete) {
        this.isComplete = isComplete;
    }

    public String getPedestrianId() {
        return pedestrianId;
    }

    public void setPedestrianId(String pedestrianId) {
        this.pedestrianId = pedestrianId;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getRideComment() {
        return rideComment;
    }

    public void setRideComment(String rideComment) {
        this.rideComment = rideComment;
    }

    public String getAvailableSeat() {
        return availableSeat;
    }

    public void setAvailableSeat(String availableSeat) {
        this.availableSeat = availableSeat;
    }

    public String getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(String appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public String getRideTo() {
        return rideTo;
    }

    public void setRideTo(String rideTo) {
        this.rideTo = rideTo;
    }

    public String getRideFrom() {
        return rideFrom;
    }

    public void setRideFrom(String rideFrom) {
        this.rideFrom = rideFrom;
    }

    public String getRideId() {
        return rideId;
    }

    public void setRideId(String rideId) {
        this.rideId = rideId;
    }

    public static boolean isEmptyRide(Ride ride){
        return ride.getAppointmentTime() == null && ride.getAvailableSeat() == null
                && ride.getRideFrom() == null && ride.getRideTo() == null;
    }
}
