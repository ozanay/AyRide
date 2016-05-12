package com.iride.ayride;

import android.content.SharedPreferences;
import android.util.Log;

public class VehicleLocalStorage {

    private final static String loggerTag = VehicleLocalStorage.class.getSimpleName();
    private final static String vehicleIdKey = "VEHICLEIDKEY";
    private final static String vehicleModelKey = "VEHICLEMODELKEY";
    private final static String vehicleColorKey = "VEHICLECOLORKEY";
    private final static String vehicleLicensePlateKey = "VEHICLELICENSEPLATEKEY";
    private final static String vehicleYearKey = "VEHICLEYEARKEY";
    private SharedPreferences vehicleSharedPreferences;
    private SharedPreferences.Editor vehicleSharedPreferencesEditor;

    public VehicleLocalStorage(SharedPreferences vehicleSharedPreferences) {
        this.vehicleSharedPreferences = vehicleSharedPreferences;
        this.vehicleSharedPreferencesEditor = this.vehicleSharedPreferences.edit();
    }

    public void storeVehicleId(String vehicleId) {
        if (isNullOrWhiteSpace(vehicleId)) {
            Log.d(loggerTag, "Vehicle Id is null or empty!");
            vehicleSharedPreferencesEditor.putString(vehicleIdKey, "");
            vehicleSharedPreferencesEditor.apply();
            return;
        }

        vehicleSharedPreferencesEditor.putString(vehicleIdKey, vehicleId);
        vehicleSharedPreferencesEditor.apply();
    }

    public void storeVehicleModel(String vehicleModel) {
        if (isNullOrWhiteSpace(vehicleModel)) {
            Log.d(loggerTag, "Vehicle Model is null or empty!");
            vehicleSharedPreferencesEditor.putString(vehicleModelKey, "");
            vehicleSharedPreferencesEditor.apply();
            return;
        }

        vehicleSharedPreferencesEditor.putString(vehicleModelKey, vehicleModel);
        vehicleSharedPreferencesEditor.apply();
    }

    public void storeVehicleColor(String vehicleColor) {
        if (isNullOrWhiteSpace(vehicleColor)) {
            Log.d(loggerTag, "Vehicle Color is null or empty!");
            vehicleSharedPreferencesEditor.putString(vehicleColorKey, "");
            vehicleSharedPreferencesEditor.apply();
            return;
        }

        vehicleSharedPreferencesEditor.putString(vehicleColorKey, vehicleColor);
        vehicleSharedPreferencesEditor.apply();
    }

    public void storeVehicleLicensePlate(String vehicleLicensePlate) {
        if (isNullOrWhiteSpace(vehicleLicensePlate)) {
            Log.d(loggerTag, "Vehicle License Plate is null or empty!");
            vehicleSharedPreferencesEditor.putString(vehicleLicensePlateKey, "");
            vehicleSharedPreferencesEditor.apply();
            return;
        }

        vehicleSharedPreferencesEditor.putString(vehicleLicensePlateKey, vehicleLicensePlate);
        vehicleSharedPreferencesEditor.apply();
    }

    public void storeVehicleYear(String vehicleYear) {
        if (isNullOrWhiteSpace(vehicleYear)) {
            Log.d(loggerTag, "Vehicle Year is null or empty!");
            vehicleSharedPreferencesEditor.putString(vehicleYearKey, "");
            vehicleSharedPreferencesEditor.apply();
            return;
        }

        vehicleSharedPreferencesEditor.putString(vehicleYearKey, vehicleYear);
        vehicleSharedPreferencesEditor.apply();
    }

    public String getVehicleId() {
        return vehicleSharedPreferences.getString(vehicleIdKey, null);
    }

    public String getVehicleModel() {
        return vehicleSharedPreferences.getString(vehicleModelKey, null);
    }

    public String getVehicleColor() {
        return vehicleSharedPreferences.getString(vehicleColorKey, null);
    }

    public String getVehicleLicensePlate() {
        return vehicleSharedPreferences.getString(vehicleLicensePlateKey, null);
    }

    public String getVehicleYear() {
        return vehicleSharedPreferences.getString(vehicleYearKey, null);
    }

    public void storeVehicle(Vehicle vehicle) {
        storeVehicleId(vehicle.getVehicleId());
        storeVehicleModel(vehicle.getVehicleModel());
        storeVehicleColor(vehicle.getVehicleColor());
        storeVehicleYear(vehicle.getVehicleYear());
        storeVehicleLicensePlate(vehicle.getVehicleLicensePlate());
    }

    public void clearStorage() {
        storeVehicleId(null);
        storeVehicleModel(null);
        storeVehicleColor(null);
        storeVehicleYear(null);
        storeVehicleLicensePlate(null);
    }

    private boolean isNullOrWhiteSpace(String string){
        return (string == null || string.trim().equals(""));
    }
}
