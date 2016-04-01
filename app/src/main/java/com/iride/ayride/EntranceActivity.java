package com.iride.ayride;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
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
    private final static String facebookLogin = "FACEBOOKLOGIN";
    private final static String mobileServiceUrl = "https://useraccount.azure-mobile.net/";
    private final static String mobileServiceAppKey = "BCGeAFQbjUEOGanLwVXslBzVMykgEM16";
    private final static String loggerTag = "EntranceActivity";
    private Button signInButton;
    private Button signUpButton;
    private LoginButton facebookLoginButton;
    private CallbackManager callbackManager;
    private AccessToken accessToken;
    private AccessTokenTracker accessTokenTracker;
    private MobileServiceClient mobileServiceClient = null;
    private MobileServiceTable mobileServiceTable = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            FacebookSdk.sdkInitialize(getApplicationContext());
            this.callbackManager = CallbackManager.Factory.create();
            setContentView(R.layout.activity_enterance);
            this.generateKeyHash();
            findViewById(R.id.loadingPanel).setVisibility(View.GONE);
            this.initializeMobileService();
            this.signInButton = (Button) findViewById(R.id.login_button);
            this.signInButton.setOnClickListener(new LoginListener());

            this.signUpButton = (Button) findViewById(R.id.sign_up_button);
            this.signUpButton.setOnClickListener(new SignUpListener());

            if (isFacebookUserLoggedIn()){
                Log.d(loggerTag,"User Already Logged In!");
                changeActivity(facebookLogin);
                finish();
            }

            this.facebookLoginButton = (LoginButton) findViewById(R.id.facebook_login_button);
            List<String> permissionList = Arrays.asList("public_profile", "email", "user_birthday");
            this.facebookLoginButton.setReadPermissions(permissionList);
            this.facebookLoginButton.registerCallback(this.callbackManager, new FacebookRegisterCallback());
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
            case facebookLogin:
                intent = new Intent(EntranceActivity.this, HomePageActivity.class);
                break;
        }

        if (intent != null) {
            startActivity(intent);
        }
    }

    private void addFacebookUser(User user) {
        if (mobileServiceClient == null || mobileServiceTable == null) {
            Log.d(loggerTag, "Mobile Service or Table is Null");
            return;
        }

        if (user == null) {
            Log.d(loggerTag, "User is Null");
            return;
        }

        mobileServiceTable.insert(user, new TableOperationCallback<User>() {
            public void onCompleted(User entity, Exception exception, ServiceFilterResponse response) {
                if (exception == null) {
                    Log.i(loggerTag, "Service added the user successfully!");
                    findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                    changeActivity(facebookLogin);
                    finish();
                } else {
                    Log.e(loggerTag, exception.getMessage());
                    Toast.makeText(getApplicationContext(), "User was not added to system!", Toast.LENGTH_SHORT).show();
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

    private void checkExistenceOfFacebookUser(String facebookUserId) {
        if (mobileServiceTable == null){
            Log.d(loggerTag,"Mobile Service Table is Null!");
            return;
        }

        mobileServiceTable.where().field("id").eq(facebookUserId).execute(new TableQueryCallback<User>() {
            public void onCompleted(List<User> result, int count, Exception exception, ServiceFilterResponse response) {
                if (exception == null) {
                    findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                    if (result.isEmpty() || result == null) {
                        Log.i(loggerTag, "No Existence!");
                    } else {
                        Log.i(loggerTag, "Already Exist!");
                        changeActivity(facebookLogin);
                        finish();
                    }
                } else {
                    findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                    Log.e(loggerTag, exception.getCause().toString());
                }
            }
        });
    }

    private boolean isFacebookUserLoggedIn() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null;
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
                        changeActivity(facebookLogin);
                        finish();
                    }
                }
            };
            accessTokenTracker.startTracking();
            GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
                @Override
                public void onCompleted(JSONObject facebookUser, GraphResponse response) {
                    if (response.getError() != null) {
                        // handle error
                        Log.e(loggerTag, response.getError().toString());
                    } else {
                        final Profile profile = Profile.getCurrentProfile();
                        if (profile == null) {
                            Log.d(loggerTag, "Profile Information Is Null");
                            return;
                        }

                        String message = null;
                        String id = accessToken.getUserId();
                        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
                        EntranceActivity.this.checkExistenceOfFacebookUser(id);
                        String name = EntranceActivity.this.splitName(profile.getName());
                        String surName = profile.getLastName();
                        message = "Id: " + id + "\n" + "Name: " + name + "\n" + "Surname: " + surName + "\n";
                        user.setId(id);
                        user.setName(name);
                        user.setSurName(surName);

                        if (accessToken.getPermissions().contains("email")) {
                            String email = facebookUser.optString("email");
                            message += "E-mail: " + email + "\n";
                            user.setEmail(email);
                        }

                        if (accessToken.getPermissions().contains("user_birthday")) {
                            String birthday = facebookUser.optString("user_birthday");
                            message += "Birthday: " + birthday + "\n";
                            user.setBirthday(birthday);
                        }

                        if (accessToken.getPermissions().contains("gender")) {
                            String gender = facebookUser.optString("gender");
                            message += "Gender: " + gender + "\n";
                            user.setGender(gender);
                        }

                        Log.i(loggerTag, message);
                        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
                        EntranceActivity.this.addFacebookUser(user);
                    }
                }
            }).executeAsync();
        }

        @Override
        public void onCancel() {
            findViewById(R.id.loadingPanel).setVisibility(View.GONE);
            Log.d(loggerTag, "Facebook Login Is Canceled!");
        }

        @Override
        public void onError(FacebookException error) {
            findViewById(R.id.loadingPanel).setVisibility(View.GONE);
            Log.e(loggerTag, error.getMessage());
            Log.e(loggerTag, error.getCause().toString());
        }
    }
}
