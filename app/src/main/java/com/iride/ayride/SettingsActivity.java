package com.iride.ayride;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;

/**
 * Created by user on 6.7.2015.
 */
public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

    private final static String loggerTag = SettingsActivity.class.getSimpleName();
    private Preference preferenceName;
    private Preference preferenceSurName;
    private Preference preferenceBirthday;
    private Preference preferenceGender;
    private Preference preferencePhoneNumber;
    private Preference preferenceEmail;
    private Preference preferencePassword;
    private UserLocalStorage userLocalStorage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        try {
            userLocalStorage = new UserLocalStorage(getSharedPreferences(String.valueOf(StoragePreferences.PREFERENCES), Context.MODE_PRIVATE));
            initializeUserPreferences();
        } catch (Exception exc){
            Log.e(loggerTag,exc.getMessage());
        }
        /*final CheckBoxPreference showingPass = (CheckBoxPreference) getPreferenceScreen().findPreference("userPassword");
        showingPass.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                *//*Log.d("MyApp", "Pref " + preference.getKey() + " changed to " + newValue.toString());
                return true;*//*
                if (!showingPass.isChecked()) {
                    String passwordFromFirebase = "password";
                    //passwordFromFirebase should initialize to database value
                    showingPass.setSummary(passwordFromFirebase);
                } else {
                    showingPass.setSummary("");
                }
                return true;
            }
        });*/
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }


    @SuppressWarnings("deprecation")
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        updatePrefSummary(findPreference(key));
    }

    private void initializeUserPreferences() {
        preferenceName = getPreferenceManager().findPreference("userName");
        if (userLocalStorage.getUserName()==null || userLocalStorage.getUserName().isEmpty()){
            preferenceName.setSummary("");
        } else {
            preferenceName.setSummary(userLocalStorage.getUserName());
        }

        preferenceSurName = getPreferenceManager().findPreference("userSurname");
        if(userLocalStorage.getUserSurName() == null || userLocalStorage.getUserSurName().isEmpty()){
            preferenceSurName.setSummary("");
        } else {
            preferenceSurName.setSummary(userLocalStorage.getUserSurName());
        }

        preferenceBirthday = getPreferenceManager().findPreference("userBirthday");
        if(userLocalStorage.getUserBirthday() == null || userLocalStorage.getUserBirthday().isEmpty()){
            preferenceBirthday.setSummary("");
        } else {
            preferenceBirthday.setSummary(userLocalStorage.getUserBirthday());
        }

        preferenceEmail = getPreferenceManager().findPreference("userEmail");
        if(userLocalStorage.getUserEmail() == null || userLocalStorage.getUserEmail().isEmpty()) {
            preferenceEmail.setSummary("");
        } else {
            preferenceEmail.setSummary(userLocalStorage.getUserEmail());
        }

        preferenceGender = getPreferenceManager().findPreference("userGender");
        if(userLocalStorage.getUserGender() == null || userLocalStorage.getUserGender().isEmpty()) {
            preferenceGender.setSummary("");
        } else {
            preferenceGender.setSummary(userLocalStorage.getUserGender());
        }

        preferencePhoneNumber = getPreferenceManager().findPreference("userPhoneNumber");
        if(userLocalStorage.getUserPhoneNumber() == null || userLocalStorage.getUserPhoneNumber().isEmpty()) {
            preferencePhoneNumber.setSummary("");
        } else {
            preferencePhoneNumber.setSummary(userLocalStorage.getUserPhoneNumber());
        }

        preferencePassword = getPreferenceManager().findPreference("userPassword");
        if(userLocalStorage.getUserPassword() == null || userLocalStorage.getUserPassword().isEmpty()) {
            preferencePassword.setSummary("");
        } else {
            preferencePassword.setSummary(userLocalStorage.getUserPassword());
        }
    }

    private void changePassword(String pass) {
        //set paswoord in firebase -> pass
        CheckBoxPreference forPass = (CheckBoxPreference) getPreferenceScreen().findPreference("pass");
        if (forPass.isChecked()) {
            forPass.setSummary(pass);
        } else {
            forPass.setSummary("");
        }
    }

    private void updatePrefSummary(Preference p) {
        SharedPreferences.Editor editor = p.getEditor();
        if (p instanceof EditTextPreference) {
            EditTextPreference editTextPref = (EditTextPreference) p;
            if (p.getTitle().toString().contains("ifre")) {
                String password = editTextPref.getText();
                changePassword(password);
            } else {
                p.setSummary(editTextPref.getText());
                //firebase should be updated for necessary preference.
                String title = p.getTitle().toString();
                switch (title) {
                    case "NAME":
                        //set name in firebase
                        break;
                    case "SURNAME":
                        //set surname in firebase
                        break;
                    case "E-MAIL":
                        //set e-mail in firebase
                        break;
                    case "BIRTHDAY":
                        //set birthday in firebase
                        break;
                    case "PHONE NUMBER":
                        //set phone number in firebase
                        break;
                    case "CAR INFO":
                        //set car info in firebase
                        break;
                }
                editor.commit();
                editor.apply();

            }
        }
    }
}