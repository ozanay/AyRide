package com.iride.ayride;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.widget.DatePicker;

import com.facebook.AccessToken;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.table.TableOperationCallback;

import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class SettingsActivity extends PreferenceActivity {

    private final static String loggerTag = SettingsActivity.class.getSimpleName();
    private final static String passwordUnVisible = "********";
    private final static String mobileServiceUrl = "https://useraccount.azure-mobile.net/";
    private final static String mobileServiceAppKey = "BCGeAFQbjUEOGanLwVXslBzVMykgEM16";
    private boolean isFacebookUser;
    private boolean isDriver;
    private boolean isUserInformationChange;
    private boolean isVehicleInformationChange;
    private DatePickerDialog birthdayPicker;
    private SimpleDateFormat dateFormatter;
    private Preference preferenceVehicleModel;
    private Preference preferenceVehicleColor;
    private Preference preferenceVehicleLicensePlate;
    private Preference preferenceVehicleYear;
    private Preference preferenceName;
    private Preference preferenceSurName;
    private Preference preferenceBirthday;
    private Preference preferenceGender;
    private Preference preferencePhoneNumber;
    private Preference preferenceEmail;
    private Preference preferenceNewPassword;
    private CheckBoxPreference preferencePassword;
    private UserLocalStorage userLocalStorage;
    private VehicleLocalStorage vehicleLocalStorage;
    private User user;
    private Vehicle vehicle;
    private MobileServiceTable userMobileServiceTable;
    private MobileServiceTable vehicleMobileServiceTable;
    private MobileServiceClient mobileServiceClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            this.isDriver = getIntent().getExtras().getBoolean("isDriver");
            this.isVehicleInformationChange = false;
            this.isUserInformationChange = false;
            user = new User();
            userLocalStorage = new UserLocalStorage(getSharedPreferences(String.valueOf(StoragePreferences.PREFERENCES), Context.MODE_PRIVATE));
            if (this.isDriver) {
                vehicle = new Vehicle();
                vehicleLocalStorage = new VehicleLocalStorage(getSharedPreferences(String.valueOf(StoragePreferences.VEHICLEPREFERENCES), Context.MODE_PRIVATE));
            }

            initializeLayoutAndPreferences();
        } catch (Exception exc) {
            Log.e(loggerTag, exc.getMessage());
        }
    }

    @Override
    public void onBackPressed() {
        if (!isUserInformationChange) {
            startActivity(new Intent(SettingsActivity.this, HomePageActivity.class));
            finish();
        } else {
            initializeMobileService();
            updateUserInformation(user);
            startActivity(new Intent(SettingsActivity.this, HomePageActivity.class));
        }
    }

    private void initializeUserPreferences() {
        initializeNamePreference(PreferenceKeys.userName.toString());
        initializeSurNamePreference(PreferenceKeys.userSurname.toString());
        initializeGenderPreference(PreferenceKeys.userGender.toString());
        initializeBirthdayPreference(PreferenceKeys.userBirthday.toString());
        initializeEmailPreference(PreferenceKeys.userEmail.toString());
        initializePhoneNumberPreference(PreferenceKeys.userPhone.toString());
        initializePasswordPreference(PreferenceKeys.userPassword.toString());
        initializeNewPasswordPreference(PreferenceKeys.userNewPassword.toString());
    }

    private void initializeFacebookUserPreferences() {
        initializeNamePreference(PreferenceKeys.facebookUserName.toString());
        initializeSurNamePreference(PreferenceKeys.facebookUserSurname.toString());
        initializeGenderPreference(PreferenceKeys.facebookUserGender.toString());
        initializeBirthdayPreference(PreferenceKeys.facebookUserBirthday.toString());
        initializeEmailPreference(PreferenceKeys.facebookUserEmail.toString());
        initializePhoneNumberPreference(PreferenceKeys.facebookUserPhone.toString());
    }

    private void initializeDriverPreferences() {
        initializeNamePreference(PreferenceKeys.driverName.toString());
        initializeSurNamePreference(PreferenceKeys.driverSurname.toString());
        initializeGenderPreference(PreferenceKeys.driverGender.toString());
        initializeBirthdayPreference(PreferenceKeys.driverBirthday.toString());
        initializeEmailPreference(PreferenceKeys.driverEmail.toString());
        initializePhoneNumberPreference(PreferenceKeys.driverPhone.toString());
        initializePasswordPreference(PreferenceKeys.driverPassword.toString());
        initializeNewPasswordPreference(PreferenceKeys.driverNewPassword.toString());
        initializeVehicleModelPreference(PreferenceKeys.vehicleModel.toString());
        initializeVehicleColorPreference(PreferenceKeys.vehicleColor.toString());
        initializeVehicleLicensePlatePreference(PreferenceKeys.vehicleLicensePlate.toString());
        initializeVehicleYearPreference(PreferenceKeys.vehicleYear.toString());
    }

    private void initializeFacebookDriverPreferences() {
        initializeNamePreference(PreferenceKeys.facebookDriverName.toString());
        initializeSurNamePreference(PreferenceKeys.facebookDriverSurname.toString());
        initializeGenderPreference(PreferenceKeys.facebookDriverGender.toString());
        initializeBirthdayPreference(PreferenceKeys.facebookDriverBirthday.toString());
        initializeEmailPreference(PreferenceKeys.facebookDriverEmail.toString());
        initializePhoneNumberPreference(PreferenceKeys.facebookDriverPhone.toString());
        initializeVehicleModelPreference(PreferenceKeys.facebookDriverVehicleModel.toString());
        initializeVehicleColorPreference(PreferenceKeys.facebookDriverVehicleColor.toString());
        initializeVehicleLicensePlatePreference(PreferenceKeys.facebookDriverVehicleLicensePlate.toString());
        initializeVehicleYearPreference(PreferenceKeys.facebookDriverVehicleYear.toString());
    }

    private void initializeLayoutAndPreferences() {
        user.setId(userLocalStorage.getUserId());
        if (isFacebookUser()) {
            if (!this.isDriver) {
                addPreferencesFromResource(R.xml.facebook_user_preferences);
                initializeFacebookUserPreferences();
            } else {
                addPreferencesFromResource(R.xml.facebook_driver_preferences);
                initializeFacebookDriverPreferences();
            }
        } else if (!this.isDriver) {
            addPreferencesFromResource(R.xml.preferences);
            initializeUserPreferences();
        } else {
            addPreferencesFromResource(R.xml.driver_preferences);
            initializeDriverPreferences();
        }
    }

    private void initializeMobileService() {
        try {
            this.mobileServiceClient = new MobileServiceClient(
                    mobileServiceUrl,
                    mobileServiceAppKey,
                    this
            );
            this.userMobileServiceTable = mobileServiceClient.getTable("user_info", User.class);
            this.vehicleMobileServiceTable = mobileServiceClient.getTable("vehicle_info", Vehicle.class);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.e(loggerTag, e.getCause().toString());
        }
    }

    private void updateVehicleInformation(Vehicle vehicle) {
        if (vehicle == null) {
            Log.d(loggerTag, "Vehicle in update is NULL!");
            return;
        }

        vehicleMobileServiceTable.update(vehicle, new TableOperationCallback<User>() {
            public void onCompleted(User entity, Exception exception, ServiceFilterResponse response) {
                if (exception == null) {
                    Log.d(loggerTag, "Vehicle information was updated Successfully!");
                } else {
                    Log.e(loggerTag, exception.getMessage());
                }
            }
        });
    }

    private void updateUserInformation(User user) {
        if (user == null) {
            Log.d(loggerTag, "User in update is NULL!");
            return;
        }

        userMobileServiceTable.update(user, new TableOperationCallback<User>() {
            public void onCompleted(User entity, Exception exception, ServiceFilterResponse response) {
                if (exception == null) {
                    Log.d(loggerTag, "User information was updated Successfully!");
                } else {
                    Log.e(loggerTag, exception.getMessage());
                }
            }
        });
    }

    private boolean isFacebookUser() {
        this.isFacebookUser = AccessToken.getCurrentAccessToken() != null;
        return this.isFacebookUser;
    }

    private void initializeNamePreference(String namePreferenceKey){
        preferenceName = getPreferenceManager().findPreference(namePreferenceKey);
        if (userLocalStorage.getUserName() == null) {
            Log.d(loggerTag, "User Name is NULL!");
            preferenceName.setSummary("NULL!");
        } else {
            preferenceName.setSummary(userLocalStorage.getUserName());
            user.setName(userLocalStorage.getUserName());
        }

        preferenceName.setEnabled(false);
    }

    private void initializeSurNamePreference(String surNamePreferenceKey){
        preferenceSurName = getPreferenceManager().findPreference(surNamePreferenceKey);
        if (userLocalStorage.getUserSurName() == null) {
            Log.d(loggerTag, "User Surname is NULL!");
            preferenceSurName.setSummary("NULL!");
        } else {
            preferenceSurName.setSummary(userLocalStorage.getUserSurName());
            user.setSurName(userLocalStorage.getUserSurName());
        }

        preferenceSurName.setEnabled(false);
    }

    private void initializeGenderPreference(String genderPreferenceKey){
        preferenceGender = getPreferenceManager().findPreference(genderPreferenceKey);
        preferenceGender.setOnPreferenceChangeListener(new GenderChangeListener());
        if (userLocalStorage.getUserGender() != null) {
            preferenceGender.setSummary(userLocalStorage.getUserGender());
            user.setGender(userLocalStorage.getUserGender());
        } else {
            preferenceGender.setSummary("");
        }
    }

    private void initializeBirthdayPreference(String birthdayPreferenceKey){
        preferenceBirthday = getPreferenceManager().findPreference(birthdayPreferenceKey);
        if (userLocalStorage.getUserBirthday() == null) {
            preferenceBirthday.setSummary("");
        } else {
            preferenceBirthday.setSummary(userLocalStorage.getUserBirthday());
            user.setBirthday(userLocalStorage.getUserBirthday());
        }

        preferenceBirthday.setOnPreferenceChangeListener(new BirthdayChangeListener());
    }

    private void initializeEmailPreference(String emailPreferenceKey){
        preferenceEmail = getPreferenceManager().findPreference(emailPreferenceKey);
        preferenceEmail.setOnPreferenceChangeListener(new EmailChangeListener());
        if (userLocalStorage.getUserEmail() == null) {
            preferenceEmail.setSummary("");
        } else {
            preferenceEmail.setSummary(userLocalStorage.getUserEmail());
            user.setEmail(userLocalStorage.getUserEmail());
        }
    }

    private void initializePhoneNumberPreference(String phoneNumberPreferenceKey){
        preferencePhoneNumber = getPreferenceManager().findPreference(phoneNumberPreferenceKey);
        preferencePhoneNumber.setOnPreferenceChangeListener(new PhoneNumberChangeListener());
        if (userLocalStorage.getUserPhoneNumber() == null) {
            preferencePhoneNumber.setSummary("");
        } else {
            preferencePhoneNumber.setSummary(userLocalStorage.getUserPhoneNumber());
            user.setPhoneNumber(userLocalStorage.getUserPhoneNumber());
        }
    }

    private void initializePasswordPreference(String passwordPreferenceKey){
        preferencePassword = (CheckBoxPreference) getPreferenceManager().findPreference(passwordPreferenceKey);
        preferencePassword.setOnPreferenceChangeListener(new PasswordChangeListener());
        if (userLocalStorage.getUserPassword() == null) {
            preferencePassword.setSummary("");
        } else {
            user.setPassword(userLocalStorage.getUserPassword());
            if (preferencePassword.isChecked()) {
                preferencePassword.setSummary(userLocalStorage.getUserPassword());
            } else {
                preferencePassword.setSummary(passwordUnVisible);
            }
        }
    }

    private void initializeNewPasswordPreference(String newPasswordPreferenceKey){
        preferenceNewPassword = getPreferenceManager().findPreference(newPasswordPreferenceKey);
        preferenceNewPassword.setSummary(passwordUnVisible);
        preferenceNewPassword.setOnPreferenceChangeListener(new NewPasswordChangeListener());
    }

    private void initializeVehicleModelPreference(String vehicleModelPreferenceKey){
        preferenceVehicleModel = getPreferenceManager().findPreference(vehicleModelPreferenceKey);
        preferenceVehicleModel.setOnPreferenceChangeListener(new VehicleModelChangeListener());
        if (vehicleLocalStorage.getVehicleModel() == null){
            preferenceVehicleModel.setSummary("");
        } else {
            preferenceVehicleModel.setSummary(vehicleLocalStorage.getVehicleModel());
            vehicle.setVehicleModel(vehicleLocalStorage.getVehicleModel());
        }
    }

    private void initializeVehicleColorPreference(String vehicleColorPreferenceKey){
        preferenceVehicleColor = getPreferenceManager().findPreference(vehicleColorPreferenceKey);
        preferenceVehicleColor.setOnPreferenceChangeListener(new VehicleColorChangeListener());
        if (vehicleLocalStorage.getVehicleColor() == null){
            preferenceVehicleColor.setSummary("");
        } else {
            preferenceVehicleColor.setSummary(vehicleLocalStorage.getVehicleColor());
            vehicle.setVehicleColor(vehicleLocalStorage.getVehicleColor());
        }
    }

    private void initializeVehicleLicensePlatePreference(String vehicleLicensePlatePreferenceKey){
        preferenceVehicleLicensePlate = getPreferenceManager().findPreference(vehicleLicensePlatePreferenceKey);
        preferenceVehicleLicensePlate.setOnPreferenceChangeListener(new VehicleLicensePlateChangeListener());
        if (vehicleLocalStorage.getVehicleLicensePlate() == null){
            preferenceVehicleLicensePlate.setSummary("");
        } else {
            preferenceVehicleLicensePlate.setSummary(vehicleLocalStorage.getVehicleLicensePlate());
            vehicle.setVehicleLicensePlate(vehicleLocalStorage.getVehicleLicensePlate());
        }
    }

    private void initializeVehicleYearPreference(String vehicleYearPreferenceKey){
        preferenceVehicleYear = getPreferenceManager().findPreference(vehicleYearPreferenceKey);
        preferenceVehicleYear.setOnPreferenceChangeListener(new VehicleYearChangeListener());
        if (vehicleLocalStorage.getVehicleYear() == null){
            preferenceVehicleYear.setSummary("");
        } else {
            preferenceVehicleYear.setSummary(vehicleLocalStorage.getVehicleYear());
            vehicle.setVehicleYear(vehicleLocalStorage.getVehicleYear());
        }
    }

    private class BirthdayChangeListener implements Preference.OnPreferenceChangeListener{
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            user.setBirthday((String) newValue);
            userLocalStorage.storeBirthday((String) newValue);
            preference.setSummary((String) newValue);
            if (!isUserInformationChange) {
                isUserInformationChange = true;
            }

            return true;
        }
    }

    private class GenderChangeListener implements Preference.OnPreferenceChangeListener {

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            user.setGender((String) newValue);
            userLocalStorage.storeGender((String) newValue);
            preference.setSummary((String) newValue);
            if (!isUserInformationChange) {
                isUserInformationChange = true;
            }

            return true;
        }
    }

    private class EmailChangeListener implements Preference.OnPreferenceChangeListener {

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            user.setEmail((String) newValue);
            userLocalStorage.storeEmail((String) newValue);
            preference.setSummary((String) newValue);
            if (!isUserInformationChange) {
                isUserInformationChange = true;
            }

            return true;
        }
    }

    private class PhoneNumberChangeListener implements Preference.OnPreferenceChangeListener {

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            user.setPhoneNumber((String) newValue);
            userLocalStorage.storePhoneNumber((String) newValue);
            preference.setSummary((String) newValue);
            if (!isUserInformationChange) {
                isUserInformationChange = true;
            }

            return true;
        }
    }

    private class PasswordChangeListener implements Preference.OnPreferenceChangeListener {

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (((CheckBoxPreference) preference).isChecked()) {
                    preference.setSummary(userLocalStorage.getUserPassword());
            } else {
                preference.setSummary(passwordUnVisible);
            }

            return true;
        }
    }

    private class NewPasswordChangeListener implements Preference.OnPreferenceChangeListener {

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            user.setPassword((String) newValue);
            userLocalStorage.storePassword((String) newValue);
            preference.setSummary(passwordUnVisible);
            if (preferencePassword.isChecked()) {
                preferencePassword.setSummary((String) newValue);
            } else {
                preferencePassword.setSummary(passwordUnVisible);
            }

            if (!isUserInformationChange) {
                isUserInformationChange = true;
            }

            return true;
        }
    }

    private class VehicleModelChangeListener implements Preference.OnPreferenceChangeListener {

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            vehicle.setVehicleModel((String) newValue);
            vehicleLocalStorage.storeVehicleModel((String) newValue);
            preference.setSummary((String) newValue);
            if (!isVehicleInformationChange) {
                isVehicleInformationChange = true;
            }

            return true;
        }
    }

    private class VehicleColorChangeListener implements Preference.OnPreferenceChangeListener {

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            vehicle.setVehicleColor((String) newValue);
            vehicleLocalStorage.storeVehicleColor((String) newValue);
            preference.setSummary((String) newValue);
            if (!isVehicleInformationChange) {
                isVehicleInformationChange = true;
            }

            return true;
        }
    }

    private class VehicleLicensePlateChangeListener implements Preference.OnPreferenceChangeListener {

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            vehicle.setVehicleLicensePlate((String) newValue);
            vehicleLocalStorage.storeVehicleLicensePlate((String) newValue);
            preference.setSummary((String) newValue);
            if (!isVehicleInformationChange) {
                isVehicleInformationChange = true;
            }

            return true;
        }
    }

    private class VehicleYearChangeListener implements Preference.OnPreferenceChangeListener {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            vehicle.setVehicleYear((String) newValue);
            vehicleLocalStorage.storeVehicleYear((String) newValue);
            preference.setSummary((String) newValue);
            if (!isVehicleInformationChange) {
                isVehicleInformationChange = true;
            }

            return true;
        }
    }

    private class BirthdayDateListener implements DatePickerDialog.OnDateSetListener {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            Calendar date = Calendar.getInstance();
            date.set(year, monthOfYear, dayOfMonth);
            preferenceBirthday.setSummary(dateFormatter.format(date.getTime()));
            userLocalStorage.storeBirthday(dateFormatter.format(date.getTime()));
            if (!isUserInformationChange) {
                isUserInformationChange = true;
            }

        }
    }
}