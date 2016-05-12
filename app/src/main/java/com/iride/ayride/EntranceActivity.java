package com.iride.ayride;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.table.TableOperationCallback;
import com.microsoft.windowsazure.mobileservices.table.TableQueryCallback;

import org.json.JSONObject;

import java.net.MalformedURLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

public class EntranceActivity extends AppCompatActivity {

    private final static String signIn = "LOGIN";
    private final static String signUp = "SIGNUP";
    private final static String homePage = "HOMEPAGE";
    private final static String loggerTag = EntranceActivity.class.getSimpleName();
    private UserLocalStorage userLocalStorage;
    private CallbackManager callbackManager;
    private AccessToken accessToken;
    private AccessTokenTracker accessTokenTracker;
    private MobileServiceClient mobileServiceClient = null;
    private MobileServiceTable mobileServiceTable = null;
    private RelativeLayout loadingPanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            FacebookSdk.sdkInitialize(getApplicationContext());
            this.callbackManager = CallbackManager.Factory.create();
            setContentView(R.layout.activity_enterance);
            this.generateKeyHash();
            this.loadingPanel = (RelativeLayout) findViewById(R.id.loadingPanel);
            if (this.loadingPanel != null) {
                this.loadingPanel.setVisibility(View.GONE);
            }

            this.initializeMobileService();
            Button signInButton = (Button) findViewById(R.id.login_button);
            if (signInButton != null) {
                signInButton.setOnClickListener(new LoginListener());
            }

            Button signUpButton = (Button) findViewById(R.id.sign_up_button);
            if (signUpButton != null) {
                signUpButton.setOnClickListener(new SignUpListener());
            }

            while (!isConnectedToInternet()) {
                Toast.makeText(getApplicationContext(), "Internet connection is necessary!", Toast.LENGTH_LONG).show();
            }

            userLocalStorage = new UserLocalStorage(getSharedPreferences(StoragePreferences.USER_PREFERENCES, Context.MODE_PRIVATE));
            if (!isNullOrWhiteSpace(userLocalStorage.getUserName()) && !isNullOrWhiteSpace(userLocalStorage.getUserSurName())) {
                changeActivity(homePage);
                finish();
            }

            if (isFacebookUserLoggedIn()) {
                Log.d(loggerTag, "User Already Logged In!");
                changeActivity(homePage);
                finish();
            }

            LoginButton facebookLoginButton = (LoginButton) findViewById(R.id.facebook_login_button);
            List<String> permissionList = Arrays.asList("public_profile", "email", "user_birthday");
            if (facebookLoginButton != null) {
                facebookLoginButton.setReadPermissions(permissionList);
                facebookLoginButton.registerCallback(this.callbackManager, new FacebookRegisterCallback());
            }
        } catch (Exception exc) {
            Log.e(loggerTag, exc.getCause().toString());
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        this.callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (accessTokenTracker != null) {
            accessTokenTracker.stopTracking();
        }
    }

    private void generateKeyHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.iride.ayride",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(loggerTag, e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            Log.e(loggerTag, e.getMessage());
        }
    }

    private void trackAccessToken() {
        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken,
                    AccessToken currentAccessToken) {

            }
        };

        accessToken = AccessToken.getCurrentAccessToken();
    }

    private void changeActivity(String activityName) {
        activityName = activityName.toUpperCase();
        Intent intent = null;
        switch (activityName) {
            case signUp:
                intent = new Intent(EntranceActivity.this, RegisterActivity.class);
                break;
            case signIn:
                intent = new Intent(EntranceActivity.this, LoginActivity.class);
                break;
            case homePage:
                intent = new Intent(EntranceActivity.this, HomePageActivity.class);
                break;
        }

        if (intent != null) {
            startActivity(intent);
            finish();
        }
    }

    private void addFacebookUser(final User user) {
        if (mobileServiceClient == null || mobileServiceTable == null) {
            Log.d(loggerTag, "Mobile Service or Table is Null");
            return;
        }

        if (user == null) {
            Log.d(loggerTag, "User is Null");
            return;
        }

        Log.d(loggerTag, "Adds User");
        mobileServiceTable.insert(user, new TableOperationCallback<User>() {
            @Override
            public void onCompleted(User result, Exception exception, ServiceFilterResponse response) {
                if (exception == null) {
                    userLocalStorage.storeUser(user);
                    changeActivity(homePage);
                    Log.d(loggerTag, "User ADDED!");
                } else {
                    Log.e(loggerTag, "ERROR WHEN ADDING USER!");
                    LoginManager.getInstance().logOut();
                }
            }
        });

    }

    private String splitName(String name) {
        String[] splited = name.split(" ");
        if (splited.length > 2) {
            for (int i = 0; i < splited.length - 1; i++) {
                name += splited[i] + " ";
            }
            name = name.substring(0, name.length() - 1);
        } else {
            name = splited[0];
        }

        return name;
    }

    private void initializeMobileService() {
        try {
            this.mobileServiceClient = new MobileServiceClient(
                    getString(R.string.azureApiUrl),
                    getString(R.string.azureApiKey),
                    this
            );
            this.mobileServiceTable = mobileServiceClient.getTable("user_info", User.class);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.e(loggerTag, e.getCause().toString());
        }
    }

    private void checkFacebookUserExistence(final User user) {
        if (mobileServiceTable == null) {
            Log.d(loggerTag, "Mobile Service Table is Null!");
            return;
        }

        Log.d(loggerTag, "Checking User!");
        mobileServiceTable.where().field("id").eq(user.getId()).execute(new TableQueryCallback<User>() {
            @Override
            public void onCompleted(List<User> result, int count, Exception exception, ServiceFilterResponse response) {
                if (exception == null) {
                    if (result == null || result.size() == 0) {
                        Log.i(loggerTag, "No Existence!");
                        addFacebookUser(user);
                    } else {
                        Log.d(loggerTag, "USER ALREADY EXIST IN DB");
                        userLocalStorage.storeUser(user);
                        changeActivity(homePage);
                    }
                } else {
                    Log.e(loggerTag, "ERROR IN is Facebook user!");
                }
            }
        });
    }

    private boolean isFacebookUserLoggedIn() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null;
    }

    private boolean isConnectedToInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;

    }

    private class LoginListener implements View.OnClickListener {
        public void onClick(View v) {
            // Perform action on click
            changeActivity(signIn);
        }
    }

    private class SignUpListener implements View.OnClickListener {
        public void onClick(View v) {
            // Perform action on click
            changeActivity(signUp);
        }
    }

    private class FacebookRegisterCallback implements FacebookCallback<LoginResult> {
        @Override
        public void onSuccess(final LoginResult loginResult) {
            EntranceActivity.this.loadingPanel.setVisibility(View.VISIBLE);
            if (loginResult == null) {
                Log.d(loggerTag, "Login Result Is Null!");
                return;
            }

            final User user = new User();
            EntranceActivity.this.accessToken = loginResult.getAccessToken();
            accessTokenTracker = new AccessTokenTracker() {
                @Override
                protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                    if (currentAccessToken != null) {
                        Log.d(loggerTag, "New Access Token Enabled!");
                        accessToken = AccessToken.getCurrentAccessToken();
                    } else {
                        Log.d(loggerTag, "No Changed Access Token!");
                    }
                }
            };

            accessTokenTracker.startTracking();
            GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
                @Override
                public void onCompleted(JSONObject facebookUser, GraphResponse response) {
                    Log.d(loggerTag, facebookUser.toString());
                    if (response.getError() != null) {
                        // handle error
                        Log.e(loggerTag, "Response Error: " + response.getError().getErrorMessage());
                        return;
                    }

                    try {
                        String id = accessToken.getUserId();
                        if (isNullOrWhiteSpace(id)) {
                            id = facebookUser.getString("id");
                        }

                        String name = facebookUser.getString("first_name");
                        String surName = facebookUser.getString("last_name");
                        String gender = facebookUser.getString("gender");
                        String birthday = facebookUser.getString("birthday");
                        if (accessToken.getPermissions().contains("email")) {
                            String email = facebookUser.optString("email");
                            if (isNullOrWhiteSpace(email)){
                                email = facebookUser.getString("email");
                            }

                            user.setEmail(email);
                        }
                        user.setId(id);
                        user.setName(name);
                        user.setSurName(surName);
                        user.setGender(gender);
                        Log.d(loggerTag, user.toString());
                        checkFacebookUserExistence(user);
                        EntranceActivity.this.loadingPanel.setVisibility(View.GONE);
                    } catch (Exception exc) {
                        Log.e(loggerTag, "Exception Message: "+exc.getMessage());
                    }
                }
            });
            Bundle parameters = new Bundle();
            parameters.putString("fields", "id,name,link,first_name,last_name,gender,birthday,email");
            request.setParameters(parameters);
            request.executeAsync();
        }

        @Override
        public void onCancel() {
            EntranceActivity.this.loadingPanel.setVisibility(View.GONE);
            Log.d(loggerTag, "Facebook Login Is Canceled!");
        }

        @Override
        public void onError(FacebookException error) {
            EntranceActivity.this.loadingPanel.setVisibility(View.GONE);
            Log.e(loggerTag, error.getMessage());
            Log.e(loggerTag, error.getCause().toString());
        }
    }

    private boolean isNullOrWhiteSpace(String string){
        return (string == null || string.trim().equals(""));
    }
}
