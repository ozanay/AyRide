package com.iride.ayride;

import android.content.SharedPreferences;
import android.util.Log;

public class UserLocalStorage {

    private final String loggerTag = UserLocalStorage.class.getSimpleName();
    private final static String idKey = "IDKEY";
    private final static String surNameKey = "SURNAMEKEY";
    private final static String nameKey = "NAMEKEY";
    private final static String birthdayKey = "BIRTHDAYKEY";
    private final static String genderKey = "GENDERKEY";
    private final static String phoneKey = "PHONEKEY";
    private final static String emailKey = "EMAILKEY";
    private final static String passwordKey = "PASSWORDKEY";
    private final static String userModeKey = "USERMODEKEY";
    private final static String noInformation = "";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor sharedPreferencesEditor;

    public UserLocalStorage(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
        sharedPreferencesEditor = this.sharedPreferences.edit();
    }

    public void storeId(String id) {
        if (isNullOrWhiteSpace(id)) {
            Log.d(loggerTag, "Id is null or empty!");
            sharedPreferencesEditor.putString(idKey, "");
            sharedPreferencesEditor.apply();
            return;
        }

        sharedPreferencesEditor.putString(idKey, id);
        sharedPreferencesEditor.apply();
    }

    public void storeName(String name) {
        if (isNullOrWhiteSpace(name)) {
            Log.d(loggerTag, "Name is null or empty!");
            sharedPreferencesEditor.putString(nameKey, "");
            sharedPreferencesEditor.apply();
            return;
        }

        sharedPreferencesEditor.putString(nameKey, name);
        sharedPreferencesEditor.apply();
    }

    public void storeSurName(String surName) {
        if (isNullOrWhiteSpace(surName)) {
            Log.d(loggerTag, "Surname is null or empty!");
            sharedPreferencesEditor.putString(surNameKey, "");
            sharedPreferencesEditor.apply();
            return;
        }

        sharedPreferencesEditor.putString(surNameKey, surName);
        sharedPreferencesEditor.apply();
    }

    public void storeBirthday(String birthday) {
        if (isNullOrWhiteSpace(birthday)) {
            Log.d(loggerTag, "Birthday is null or empty!");
            sharedPreferencesEditor.putString(birthdayKey, "");
            sharedPreferencesEditor.apply();
            return;
        }

        sharedPreferencesEditor.putString(birthdayKey, birthday);
        sharedPreferencesEditor.apply();
    }

    public void storeGender(String gender) {
        if (isNullOrWhiteSpace(gender)) {
            Log.d(loggerTag, "Gender is null or empty!");
            sharedPreferencesEditor.putString(genderKey, "");
            sharedPreferencesEditor.apply();
            return;
        }

        sharedPreferencesEditor.putString(genderKey, gender);
        sharedPreferencesEditor.apply();
    }

    public void storePhoneNumber(String phoneNumber) {
        if (isNullOrWhiteSpace(phoneNumber)) {
            Log.d(loggerTag, "Phone number is null or empty!");
            sharedPreferencesEditor.putString(phoneKey, "");
            sharedPreferencesEditor.apply();
            return;
        }

        sharedPreferencesEditor.putString(phoneKey, phoneNumber);
        sharedPreferencesEditor.apply();
    }

    public void storeEmail(String email) {
        if (isNullOrWhiteSpace(email)) {
            Log.d(loggerTag, "Email is null or empty!");
            sharedPreferencesEditor.putString(emailKey, "");
            sharedPreferencesEditor.apply();
            return;
        }

        sharedPreferencesEditor.putString(emailKey, email);
        sharedPreferencesEditor.apply();
    }

    public void storePassword(String password) {
        if (isNullOrWhiteSpace(password)) {
            Log.d(loggerTag, "Password is null or empty!");
            sharedPreferencesEditor.putString(passwordKey, "");
            sharedPreferencesEditor.apply();
            return;
        }

        sharedPreferencesEditor.putString(passwordKey, password);
        sharedPreferencesEditor.apply();
    }

    public void storeUser(User user) {
        if (user == null) {
            Log.d(loggerTag, "User Is NULL!");
            return;
        }

        if (!isNullOrWhiteSpace(user.getId())) {
            storeId(user.getId());
        }

        storeName(user.getName());
        storeSurName(user.getSurName());
        storeBirthday(user.getBirthday());
        storeGender(user.getGender());
        storePhoneNumber(user.getPhoneNumber());
        storeEmail(user.getEmail());
        storePassword(user.getPassword());
        storeIsDriver(false);
    }

    public void storeIsDriver(boolean isDriverMode) {
        sharedPreferencesEditor.putBoolean(userModeKey, isDriverMode);
        sharedPreferencesEditor.apply();
    }

    public String getUserId() {
        return sharedPreferences.getString(idKey, null);
    }

    public String getUserName() {
        return sharedPreferences.getString(nameKey, null);
    }

    public String getUserSurName() {
        return sharedPreferences.getString(surNameKey, null);
    }

    public String getUserBirthday() {
        return sharedPreferences.getString(birthdayKey, null);
    }

    public String getUserGender() {
        return sharedPreferences.getString(genderKey, null);
    }

    public String getUserPhoneNumber() {
        return sharedPreferences.getString(phoneKey, null);
    }

    public String getUserEmail() {
        return sharedPreferences.getString(emailKey, null);
    }

    public String getUserPassword() {
        return sharedPreferences.getString(passwordKey, null);
    }

    public boolean isDriverMode() {
        return sharedPreferences.getBoolean(userModeKey, false);
    }

    public void clearStorage(){
        storeId(null);
        storeName(null);
        storeSurName(null);
        storeBirthday(null);
        storeEmail(null);
        storeGender(null);
        storePhoneNumber(null);
        storePassword(null);
        storeIsDriver(false);
    }

    private boolean isNullOrWhiteSpace(String string){
        return (string == null || string.trim().equals(""));
    }
}
