package com.iride.ayride;

import java.io.Serializable;

public class Ride implements Serializable {

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

    @com.google.gson.annotations.SerializedName("ride_driver_instance_id")
    private String driverInstanceId;

    @com.google.gson.annotations.SerializedName("ride_driver_name")
    private String driverName;

    @com.google.gson.annotations.SerializedName("ride_driver_surname")
    private String driverSurName;

    @com.google.gson.annotations.SerializedName("ride_pedestrian_id")
    private String pedestrianId;

    @com.google.gson.annotations.SerializedName("ride_pedestrian_instance_id")
    private String pedestrianInstanceId;

    @com.google.gson.annotations.SerializedName("ride_pedestrian_name")
    private String pedestrianName;

    @com.google.gson.annotations.SerializedName("ride_pedestrian_surname")
    private String pedestrianSurName;

    @com.google.gson.annotations.SerializedName("ride_is_complete")
    private boolean isComplete;

    @com.google.gson.annotations.SerializedName("ride_is_canceled")
    private boolean isCanceled;

    @com.google.gson.annotations.SerializedName("ride_is_accepted")
    private boolean isAccepted;

    @com.google.gson.annotations.SerializedName("ride_is_rejected")
    private boolean isRejected;

    public Ride(){
        this.rideFrom = null;
        this.rideTo = null;
        this.appointmentTime = null;
        this.availableSeat = null;
        this.rideComment = null;
        this.driverId = null;
        this.driverInstanceId = null;
        this.driverName = null;
        this.driverSurName = null;
        this.pedestrianId = null;
        this.pedestrianInstanceId = null;
        this.pedestrianName = null;
        this.pedestrianSurName = null;
        this.isCanceled = false;
        this.isComplete = false;
        this.isAccepted = false;
        this.isRejected = false;
    }

    public boolean isAccepted() {
        return isAccepted;
    }

    public void setIsAccepted(boolean isAccepted) {
        this.isAccepted = isAccepted;
    }

    public boolean isRejected() {
        return isRejected;
    }

    public void setIsRejected(boolean isRejected) {
        this.isRejected = isRejected;
    }

    public String getDriverInstanceId() {
        return driverInstanceId;
    }

    public void setDriverInstanceId(String driverInstanceId) {
        this.driverInstanceId = driverInstanceId;
    }

    public String getPedestrianInstanceId() {
        return pedestrianInstanceId;
    }

    public void setPedestrianInstanceId(String pedestrianInstanceId) {
        this.pedestrianInstanceId = pedestrianInstanceId;
    }

    public String getPedestrianName() {
        return pedestrianName;
    }

    public void setPedestrianName(String pedestrianName) {
        this.pedestrianName = pedestrianName;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getDriverSurName() {
        return driverSurName;
    }

    public void setDriverSurName(String driverSurName) {
        this.driverSurName = driverSurName;
    }

    public String getPedestrianSurName() {
        return pedestrianSurName;
    }

    public void setPedestrianSurName(String pedestrianSurName) {
        this.pedestrianSurName = pedestrianSurName;
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
