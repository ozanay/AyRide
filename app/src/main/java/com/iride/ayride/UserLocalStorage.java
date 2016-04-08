package com.iride.ayride;

import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by user on 8.04.2016.
 */
public class UserLocalStorage {

    private final String loggerTag = UserLocalStorage.class.getSimpleName();
    private final static String surNameKey = "SURNAMEKEY";
    private final static String nameKey = "NAMEKEY";
    private final static String birthdayKey = "BIRTHDAYKEY";
    private final static String genderKey = "GENDERKEY";
    private final static String phoneKey = "PHONEKEY";
    private final static String emailKey = "EMAILKEY";
    private final static String passwordKey = "PASSWORDKEY";
    private final static String noInformation = "";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor sharedPreferencesEditor;

    public UserLocalStorage(SharedPreferences sharedPreferences){
        this.sharedPreferences = sharedPreferences;
        sharedPreferencesEditor = this.sharedPreferences.edit();
    }

    public void storeName(String name){
        if (name == null || name.isEmpty()) {
            Log.d(loggerTag, "Name is null or empty!");
            return;
        }

        sharedPreferencesEditor.putString(nameKey, name);
        sharedPreferencesEditor.apply();
    }

    public void storeSurName(String surName){
        if (surName == null || surName.isEmpty()) {
            Log.d(loggerTag, "Surname is null or empty!");
            return;
        }

        sharedPreferencesEditor.putString(surNameKey, surName);
        sharedPreferencesEditor.apply();
    }

    public void storeBirthday(String birthday){
        if (birthday == null || birthday.isEmpty()) {
            Log.d(loggerTag, "Birthday is null or empty!");
            sharedPreferencesEditor.putString(birthdayKey, noInformation);
            return;
        }

        sharedPreferencesEditor.putString(birthdayKey, birthday);
        sharedPreferencesEditor.apply();
    }

    public void storeGender(String gender){
        if (gender == null || gender.isEmpty()) {
            Log.d(loggerTag, "Gender is null or empty!");
            sharedPreferencesEditor.putString(genderKey, noInformation);
            return;
        }

        sharedPreferencesEditor.putString(genderKey, gender);
        sharedPreferencesEditor.apply();
    }

    public void storePhoneNumber(String phoneNumber){
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            Log.d(loggerTag, "Phone number is null or empty!");
            sharedPreferencesEditor.putString(phoneKey, noInformation);
            return;
        }

        sharedPreferencesEditor.putString(phoneKey, phoneNumber);
        sharedPreferencesEditor.apply();
    }

    public void storeEmail(String email){
        if (email == null || email.isEmpty()) {
            Log.d(loggerTag, "Email is null or empty!");
            sharedPreferencesEditor.putString(emailKey, email);
            return;
        }

        sharedPreferencesEditor.putString(emailKey, email);
        sharedPreferencesEditor.apply();
    }

    public void storePassword(String password){
        if (password == null || password.isEmpty()) {
            Log.d(loggerTag, "Password is null or empty!");
            sharedPreferencesEditor.putString(emailKey, noInformation);
        }

        sharedPreferencesEditor.putString(passwordKey, password);
        sharedPreferencesEditor.apply();
    }

    public void storeUser(User user){
        if (user == null){
            Log.d(loggerTag,"User Is NULL!");
            return;
        }

        storeName(user.getName());
        storeSurName(user.getSurName());
        storeBirthday(user.getBirthday());
        storeGender(user.getGender());
        storePhoneNumber(user.getPhoneNumber());
        storeEmail(user.getEmail());
        storePassword(user.getPassword());
    }

    public String getUserName(){
        return sharedPreferences.getString(nameKey,null);
    }

    public String getUserSurName(){
        return sharedPreferences.getString(surNameKey,null);
    }

    public String getUserBirthday(){
        return sharedPreferences.getString(birthdayKey,null);
    }

    public String getUserGender(){
        return sharedPreferences.getString(genderKey,null);
    }

    public String getUserPhoneNumber(){
        return sharedPreferences.getString(phoneKey,null);
    }

    public String getUserEmail(){
        return sharedPreferences.getString(emailKey,null);
    }

    public String getUserPassword(){
        return sharedPreferences.getString(passwordKey,null);
    }
}
