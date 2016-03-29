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

import org.json.JSONObject;

import java.net.MalformedURLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
    protected AccessToken accessToken;
    private AccessTokenTracker accessTokenTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            FacebookSdk.sdkInitialize(getApplicationContext());
            this.callbackManager = CallbackManager.Factory.create();
            setContentView(R.layout.activity_enterance);
            this.generateKeyHash();
            this.signInButton = (Button) findViewById(R.id.login_button);
            this.signInButton.setOnClickListener(new LoginListener());

            this.signUpButton = (Button) findViewById(R.id.sign_up_button);
            this.signUpButton.setOnClickListener(new SignUpListener());

            if (accessToken != null) {
                this.TrackAccessToken();
                changeActivity(facebookLogin);
            }

            this.facebookLoginButton = (LoginButton) findViewById(R.id.facebook_login_button);
            /*List<String> permissionList = Arrays.asList("public_profile", "email", "gender", "user_birthday");
            this.facebookLoginButton.setReadPermissions(permissionList);*/

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
            e.printStackTrace();
            Log.e(loggerTag, e.getCause().toString());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            Log.e(loggerTag, e.getCause().toString());
        }
    }

    private void TrackAccessToken() {
        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken,
                    AccessToken currentAccessToken) {

                if (!oldAccessToken.equals(currentAccessToken)) {
                    AccessToken.setCurrentAccessToken(currentAccessToken);
                }
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
                intent = new Intent(EntranceActivity.this, MainActivity.class);
                break;
        }

        if (intent != null) {
            startActivity(intent);
        }
    }

    private void addUser(User user) {
        MobileServiceClient mobileServiceClient = null;
        MobileServiceTable mobileServiceTable = null;
        try {
            mobileServiceClient = new MobileServiceClient(
                    mobileServiceUrl,
                    mobileServiceAppKey,
                    this
            );
            mobileServiceTable = mobileServiceClient.getTable("user_info", User.class);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.e(loggerTag, e.getCause().toString());
        }

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
                    changeActivity(facebookLogin);
                } else {
                    Log.e(loggerTag, exception.getCause().toString());
                    Toast.makeText(getApplicationContext(), "User was not added to system!", Toast.LENGTH_SHORT).show();
                }
            }
        });
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
            final User user = new User();
            accessToken = loginResult.getAccessToken();

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
                        String name = profile.getName();
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
                        addUser(user);
                    }
                }
            }).executeAsync();
        }

        @Override
        public void onCancel() {

        }

        @Override
        public void onError(FacebookException error) {
            Toast.makeText(getApplicationContext(), error.getCause().toString(), Toast.LENGTH_SHORT).show();
        }
    }
}
