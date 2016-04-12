package com.iride.ayride;

public class Vehicle {

    @com.google.gson.annotations.SerializedName("id")
    private String vehicleId;

    @com.google.gson.annotations.SerializedName("vehicle_model")
    private String vehicleModel;

    @com.google.gson.annotations.SerializedName("vehicle_color")
    private String vehicleColor;

    @com.google.gson.annotations.SerializedName("vehicle_license_plate")
    private String vehicleLicensePlate;

    @com.google.gson.annotations.SerializedName("vehicle_year")
    private String vehicleYear;

    public Vehicle(String vehicleModel, String vehicleColor, String vehicleLicensePlate, String vehicleYear){
        this.vehicleModel = vehicleModel;
        this.vehicleColor = vehicleColor;
        this.vehicleLicensePlate = vehicleLicensePlate;
        this.vehicleYear = vehicleYear;
    }

    public Vehicle(){
        this.vehicleModel = null;
        this.vehicleColor = null;
        this.vehicleLicensePlate = null;
        this.vehicleYear = null;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getVehicleModel() {
        return vehicleModel;
    }

    public void setVehicleModel(String vehicleModel) {
        this.vehicleModel = vehicleModel;
    }

    public String getVehicleColor() {
        return vehicleColor;
    }

    public void setVehicleColor(String vehicleColor) {
        this.vehicleColor = vehicleColor;
    }

    public String getVehicleLicensePlate() {
        return vehicleLicensePlate;
    }

    public void setVehicleLicensePlate(String vehicleLicensePlate) {
        this.vehicleLicensePlate = vehicleLicensePlate;
    }

    public String getVehicleYear() {
        return vehicleYear;
    }

    public void setVehicleYear(String vehicleYear) {
        this.vehicleYear = vehicleYear;
    }

}
