package com.iride.ayride;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;

import com.facebook.AccessToken;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.table.TableOperationCallback;

import java.net.MalformedURLException;

/**
 * Created by user on 6.7.2015.
 */
public class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener {

    private final static String loggerTag = SettingsActivity.class.getSimpleName();
    private final static String passwordUnVisible = "********";
    private final static String mobileServiceUrl = "https://useraccount.azure-mobile.net/";
    private final static String mobileServiceAppKey = "BCGeAFQbjUEOGanLwVXslBzVMykgEM16";
    private boolean isFacebookUser;
    private boolean isDriver;
    private Preference preferenceName;
    private Preference preferenceSurName;
    private Preference preferenceBirthday;
    private Preference preferenceGender;
    private Preference preferencePhoneNumber;
    private Preference preferenceEmail;
    private Preference preferenceNewPassword;
    private CheckBoxPreference preferencePassword;
    private UserLocalStorage userLocalStorage;
    private User user;
    private MobileServiceTable mobileServiceTable;
    private MobileServiceClient mobileServiceClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            this.isDriver = getIntent().getExtras().getBoolean("isDriver");
            userLocalStorage = new UserLocalStorage(getSharedPreferences(String.valueOf(StoragePreferences.PREFERENCES), Context.MODE_PRIVATE));
            initializeLayoutAndPreferences();
        } catch (Exception exc) {
            Log.e(loggerTag, exc.getMessage());
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

    @Override
    public void onBackPressed() {
        if (user == null) {
            startActivity(new Intent(SettingsActivity.this, HomePageActivity.class));
            finish();
        } else {
            initializeMobileService();
            updateUserInformation(user);
            startActivity(new Intent(SettingsActivity.this, HomePageActivity.class));
            finish();
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String preferenceKey = preference.getKey();
        user = new User();
        user.setId(userLocalStorage.getUserId());
        user.setName(userLocalStorage.getUserName());
        user.setSurName(userLocalStorage.getUserSurName());
        if (preferenceKey.contains("Email")) {
            user.setEmail((String) newValue);
            userLocalStorage.storeEmail((String) newValue);
            preference.setSummary((String) newValue);
        } else if (preferenceKey.contains("Birthday")) {
            user.setBirthday((String) newValue);
            userLocalStorage.storeBirthday((String) newValue);
            preference.setSummary((String) newValue);
        } else if (preferenceKey.contains("Phone")) {
            user.setPhoneNumber((String) newValue);
            userLocalStorage.storePhoneNumber((String) newValue);
            preference.setSummary((String) newValue);
        } else if (preferenceKey.contains("Gender")) {
            user.setGender((String) newValue);
            userLocalStorage.storeGender((String) newValue);
            preference.setSummary((String) newValue);
        } else if (preferenceKey.contains("NewPasword")) {
            user.setPassword((String) newValue);
            userLocalStorage.storePassword((String) newValue);
            if (preferencePassword.isChecked()) {
                preferencePassword.setSummary((String) newValue);
            } else {
                preferencePassword.setSummary(passwordUnVisible);
            }

            preference.setSummary("");
        }

        return true;
    }

    private void initializeUserPreferences() {
        preferenceName = getPreferenceManager().findPreference(PreferenceKeys.userName.toString());
        if (userLocalStorage.getUserName() == null || userLocalStorage.getUserName().isEmpty()) {
            Log.d(loggerTag, "User Name is NULL!");
            preferenceName.setSummary("NULL!");
        } else {
            preferenceName.setSummary(userLocalStorage.getUserName());
        }

        preferenceName.setEnabled(false);
        preferenceSurName = getPreferenceManager().findPreference(PreferenceKeys.userSurname.toString());
        if (userLocalStorage.getUserSurName() == null || userLocalStorage.getUserSurName().isEmpty()) {
            Log.d(loggerTag, "User Surname is NULL!");
            preferenceSurName.setSummary("NULL!");
        } else {
            preferenceSurName.setSummary(userLocalStorage.getUserSurName());
        }

        preferenceSurName.setEnabled(false);
        preferenceBirthday = getPreferenceManager().findPreference(PreferenceKeys.userBirthday.toString());
        if (userLocalStorage.getUserBirthday() == null || userLocalStorage.getUserBirthday().isEmpty()) {
            preferenceBirthday.setSummary("");
        } else {
            preferenceBirthday.setSummary(userLocalStorage.getUserBirthday());
        }

        preferenceEmail = getPreferenceManager().findPreference(PreferenceKeys.userEmail.toString());
        if (userLocalStorage.getUserEmail() == null || userLocalStorage.getUserEmail().isEmpty()) {
            preferenceEmail.setSummary("");
        } else {
            preferenceEmail.setSummary(userLocalStorage.getUserEmail());
        }

        preferenceGender = getPreferenceManager().findPreference(PreferenceKeys.userGender.toString());
        if (userLocalStorage.getUserGender() == null || userLocalStorage.getUserGender().isEmpty()) {
            preferenceGender.setSummary("");
        } else {
            preferenceGender.setSummary(userLocalStorage.getUserGender());
        }

        preferenceGender.setEnabled(false);
        preferencePhoneNumber = getPreferenceManager().findPreference(PreferenceKeys.userPhone.toString());
        if (userLocalStorage.getUserPhoneNumber() == null || userLocalStorage.getUserPhoneNumber().isEmpty()) {
            preferencePhoneNumber.setSummary("");
        } else {
            preferencePhoneNumber.setSummary(userLocalStorage.getUserPhoneNumber());
        }

        preferencePassword = (CheckBoxPreference) getPreferenceManager().findPreference(PreferenceKeys.userPassword.toString());
        if (userLocalStorage.getUserPassword() == null || userLocalStorage.getUserPassword().isEmpty()) {
            preferencePassword.setSummary("");
        } else {
            if (preferencePassword.isChecked()) {
                preferencePassword.setSummary(userLocalStorage.getUserPassword());
            } else {
                preferencePassword.setSummary(passwordUnVisible);
            }
        }

        preferenceNewPassword = getPreferenceManager().findPreference(PreferenceKeys.userNewPassword.toString());
        preferencePassword.setSummary("");
    }

    private void initializeFacebookUserPreferences() {
        preferenceName = getPreferenceManager().findPreference(PreferenceKeys.facebookUserName.toString());
        if (userLocalStorage.getUserName() == null || userLocalStorage.getUserName().isEmpty()) {
            Log.d(loggerTag, "Facebook User Name Is NULL!");
            preferenceName.setSummary("NULL!");
        } else {
            preferenceName.setSummary(userLocalStorage.getUserName());
        }

        preferenceName.setEnabled(false);
        preferenceSurName = getPreferenceManager().findPreference(PreferenceKeys.facebookUserSurname.toString());
        if (userLocalStorage.getUserSurName() == null || userLocalStorage.getUserSurName().isEmpty()) {
            Log.d(loggerTag, "Facebook User Surname Is NULL!");
            preferenceSurName.setSummary("NULL!");
        } else {
            preferenceSurName.setSummary(userLocalStorage.getUserSurName());
        }

        preferenceSurName.setEnabled(false);
        preferenceBirthday = getPreferenceManager().findPreference(PreferenceKeys.facebookUserBirthday.toString());
        if (userLocalStorage.getUserBirthday() == null || userLocalStorage.getUserBirthday().isEmpty()) {
            preferenceBirthday.setSummary("");
        } else {
            preferenceBirthday.setSummary(userLocalStorage.getUserBirthday());
        }

        preferenceEmail = getPreferenceManager().findPreference(PreferenceKeys.facebookUserEmail.toString());
        if (userLocalStorage.getUserEmail() == null || userLocalStorage.getUserEmail().isEmpty()) {
            preferenceEmail.setSummary("");
        } else {
            preferenceEmail.setSummary(userLocalStorage.getUserEmail());
        }

        preferenceGender = getPreferenceManager().findPreference(PreferenceKeys.facebookUserGender.toString());
        if (userLocalStorage.getUserGender() == null || userLocalStorage.getUserGender().isEmpty()) {
            preferenceGender.setSummary("");
        } else {
            preferenceGender.setSummary(userLocalStorage.getUserGender());
        }

        preferencePhoneNumber = getPreferenceManager().findPreference(PreferenceKeys.facebookUserPhone.toString());
        if (userLocalStorage.getUserPhoneNumber() == null || userLocalStorage.getUserPhoneNumber().isEmpty()) {
            preferencePhoneNumber.setSummary("");
        } else {
            preferencePhoneNumber.setSummary(userLocalStorage.getUserPhoneNumber());
        }
    }

    private void initializeDriverPreferences() {
        preferenceName = getPreferenceManager().findPreference(PreferenceKeys.driverName.toString());
        if (userLocalStorage.getUserName() == null || userLocalStorage.getUserName().isEmpty()) {
            Log.d(loggerTag, "Driver Name is NULL!");
            preferenceName.setSummary("NULL!");
        } else {
            preferenceName.setSummary(userLocalStorage.getUserName());
        }

        preferenceName.setEnabled(false);
        preferenceSurName = getPreferenceManager().findPreference(PreferenceKeys.driverSurname.toString());
        if (userLocalStorage.getUserSurName() == null || userLocalStorage.getUserSurName().isEmpty()) {
            Log.d(loggerTag, "Driver Surname is NULL!");
            preferenceSurName.setSummary("NULL!");
        } else {
            preferenceSurName.setSummary(userLocalStorage.getUserSurName());
        }

        preferenceSurName.setEnabled(false);
        preferenceBirthday = getPreferenceManager().findPreference(PreferenceKeys.driverBirthday.toString());
        if (userLocalStorage.getUserBirthday() == null || userLocalStorage.getUserBirthday().isEmpty()) {
            preferenceBirthday.setSummary("");
        } else {
            preferenceBirthday.setSummary(userLocalStorage.getUserBirthday());
        }

        preferenceEmail = getPreferenceManager().findPreference(PreferenceKeys.driverEmail.toString());
        if (userLocalStorage.getUserEmail() == null || userLocalStorage.getUserEmail().isEmpty()) {
            preferenceEmail.setSummary("");
        } else {
            preferenceEmail.setSummary(userLocalStorage.getUserEmail());
        }

        preferenceGender = getPreferenceManager().findPreference(PreferenceKeys.driverGender.toString());
        if (userLocalStorage.getUserGender() == null || userLocalStorage.getUserGender().isEmpty()) {
            preferenceGender.setSummary("");
        } else {
            preferenceGender.setSummary(userLocalStorage.getUserGender());
        }

        preferenceGender.setEnabled(false);
        preferencePhoneNumber = getPreferenceManager().findPreference(PreferenceKeys.driverPhone.toString());
        if (userLocalStorage.getUserPhoneNumber() == null || userLocalStorage.getUserPhoneNumber().isEmpty()) {
            preferencePhoneNumber.setSummary("");
        } else {
            preferencePhoneNumber.setSummary(userLocalStorage.getUserPhoneNumber());
        }

        preferencePassword = (CheckBoxPreference) getPreferenceManager().findPreference(PreferenceKeys.driverPassword.toString());
        if (userLocalStorage.getUserPassword() == null || userLocalStorage.getUserPassword().isEmpty()) {
            preferencePassword.setSummary("");
        } else {
            if (preferencePassword.isChecked()) {
                preferencePassword.setSummary(userLocalStorage.getUserPassword());
            } else {
                preferencePassword.setSummary(passwordUnVisible);
            }
        }

        preferenceNewPassword = getPreferenceManager().findPreference(PreferenceKeys.driverNewPassword.toString());
        preferenceNewPassword.setSummary("");
    }

    private void initializeFacebookDriverPreferences() {
        preferenceName = getPreferenceManager().findPreference(PreferenceKeys.facebookDriverName.toString());
        if (userLocalStorage.getUserName() == null || userLocalStorage.getUserName().isEmpty()) {
            Log.d(loggerTag, "Facebook Driver Name is NULL!");
            preferenceName.setSummary("NULL!");
        } else {
            preferenceName.setSummary(userLocalStorage.getUserName());
        }

        preferenceName.setEnabled(false);
        preferenceSurName = getPreferenceManager().findPreference(PreferenceKeys.facebookDriverSurname.toString());
        if (userLocalStorage.getUserSurName() == null || userLocalStorage.getUserSurName().isEmpty()) {
            Log.d(loggerTag, "Facebook Driver Surname is NULL!");
            preferenceSurName.setSummary("NULL!");
        } else {
            preferenceSurName.setSummary(userLocalStorage.getUserSurName());
        }

        preferenceSurName.setEnabled(false);
        preferenceBirthday = getPreferenceManager().findPreference(PreferenceKeys.facebookDriverBirthday.toString());
        if (userLocalStorage.getUserBirthday() == null || userLocalStorage.getUserBirthday().isEmpty()) {
            preferenceBirthday.setSummary("");
        } else {
            preferenceBirthday.setSummary(userLocalStorage.getUserBirthday());
        }

        preferenceEmail = getPreferenceManager().findPreference(PreferenceKeys.facebookDriverEmail.toString());
        if (userLocalStorage.getUserEmail() == null || userLocalStorage.getUserEmail().isEmpty()) {
            preferenceEmail.setSummary("");
        } else {
            preferenceEmail.setSummary(userLocalStorage.getUserEmail());
        }

        preferenceGender = getPreferenceManager().findPreference(PreferenceKeys.facebookDriverGender.toString());
        if (userLocalStorage.getUserGender() == null || userLocalStorage.getUserGender().isEmpty()) {
            preferenceGender.setSummary("");
        } else {
            preferenceGender.setSummary(userLocalStorage.getUserGender());
        }

        preferencePhoneNumber = getPreferenceManager().findPreference(PreferenceKeys.facebookDriverPhone.toString());
        if (userLocalStorage.getUserPhoneNumber() == null || userLocalStorage.getUserPhoneNumber().isEmpty()) {
            preferencePhoneNumber.setSummary("");
        } else {
            preferencePhoneNumber.setSummary(userLocalStorage.getUserPhoneNumber());
        }
    }

    private void initializeLayoutAndPreferences() {
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
            this.mobileServiceTable = mobileServiceClient.getTable("user_info", User.class);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.e(loggerTag, e.getCause().toString());
        }
    }

    private void updateUserInformation(User user) {
        mobileServiceTable.update(user, new TableOperationCallback<User>() {
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
}