package com.iride.ayride;

/**
 * Created by user on 11.04.2016.
 */
public class Vehicle {


    private String vehicleId;
    private String vehicleModel;
    private String vehicleColor;
    private String vehicleLicensePlate;

    public Vehicle(String vehicleModel, String vehicleColor, String vehicleLicensePlate){
        this.vehicleModel = vehicleModel;
        this.vehicleColor = vehicleColor;
        this.vehicleLicensePlate = vehicleLicensePlate;
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
}
