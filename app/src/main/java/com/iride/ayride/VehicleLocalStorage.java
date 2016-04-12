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
        if (vehicleId == null || vehicleId.isEmpty()) {
            Log.d(loggerTag, "Vehicle Id is null or empty!");
            return;
        }

        vehicleSharedPreferencesEditor.putString(vehicleIdKey, vehicleId);
        vehicleSharedPreferencesEditor.apply();
    }

    public void storeVehicleModel(String vehicleModel) {
        if (vehicleModel == null || vehicleModel.isEmpty()) {
            Log.d(loggerTag, "Vehicle Model is null or empty!");
            return;
        }

        vehicleSharedPreferencesEditor.putString(vehicleModelKey, vehicleModel);
        vehicleSharedPreferencesEditor.apply();
    }

    public void storeVehicleColor(String vehicleColor) {
        if (vehicleColor == null || vehicleColor.isEmpty()) {
            Log.d(loggerTag, "Vehicle Color is null or empty!");
            return;
        }

        vehicleSharedPreferencesEditor.putString(vehicleColorKey, vehicleColor);
        vehicleSharedPreferencesEditor.apply();
    }

    public void storeVehicleLicensePlate(String vehicleLicensePlate) {
        if (vehicleLicensePlate == null || vehicleLicensePlate.isEmpty()) {
            Log.d(loggerTag, "Vehicle License Plate is null or empty!");
            return;
        }

        vehicleSharedPreferencesEditor.putString(vehicleLicensePlateKey, vehicleLicensePlate);
        vehicleSharedPreferencesEditor.apply();
    }

    public void storeVehicleYear(String vehicleYear) {
        if (vehicleYear == null) {
            Log.d(loggerTag, "Vehicle Year is null or empty!");
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
}
