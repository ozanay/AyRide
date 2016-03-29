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
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.table.TableOperationCallback;

import java.net.MalformedURLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class EnteranceActivity extends AppCompatActivity {

    private final static String signIn = "LOGIN";
    private final static String signup = "SIGNUP";
    private final static String facebookLogin = "FACEBOOKLOGIN";
    private final static String mobileServiceUrl = "https://useraccount.azure-mobile.net/";
    private final static String mobileServiceAppKey = "BCGeAFQbjUEOGanLwVXslBzVMykgEM16";
    private Button loginButton;
    private Button signUpButton;
    private LoginButton facebookLoginButton;
    private CallbackManager callbackManager;
    private AccessToken accessToken;
    private Profile profile;
    private AccessTokenTracker accessTokenTracker;
    private ProfileTracker profileTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        this.callbackManager = CallbackManager.Factory.create();
        checkKeyHash();
        setContentView(R.layout.activity_enterance);

        this.loginButton = (Button) findViewById(R.id.login_button);
        this.loginButton.setOnClickListener(new LoginListener());

        this.signUpButton = (Button) findViewById(R.id.sign_up_button);
        this.signUpButton.setOnClickListener(new SignUpListener());

        this.TrackAccessToken();
        if (accessToken != null) {
            changeActivity(facebookLogin);
        }

        this.facebookLoginButton = (LoginButton) findViewById(R.id.facebook_login_button);
        this.facebookLoginButton.registerCallback(this.callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                changeActivity(facebookLogin);
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });
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

        if (profileTracker != null) {
            profileTracker.stopTracking();
        }
    }

    private void checkKeyHash() {
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
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
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

    private void TrackProfile() {
        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(
                    Profile oldProfile,
                    Profile currentProfile) {
                // App code
                if (!oldProfile.equals(currentProfile)) {
                    Profile.setCurrentProfile(currentProfile);
                }
            }
        };

        profile = Profile.getCurrentProfile();
    }

    private void changeActivity(String activityName) {
        activityName = activityName.toUpperCase();
        Intent intent = null;
        switch (activityName) {
            case signup:
                intent = new Intent(EnteranceActivity.this, RegisterActivity.class);
                break;
            case signIn:
                intent = new Intent(EnteranceActivity.this, LoginActivity.class);
                break;
            case facebookLogin:
                intent = new Intent(EnteranceActivity.this, MainActivity.class);
                break;
        }

        if (intent != null){
            startActivity(intent);
        }
    }

    private boolean isUserCreated() {
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
        }

        if (mobileServiceClient == null || mobileServiceTable == null) {
            return false;
        }

        final boolean[] isUserCreated = new boolean[1];
        User user = convertFromFacebookToUser();
        if (user != null) {
            mobileServiceTable.insert(convertFromFacebookToUser(), new TableOperationCallback<User>() {
                public void onCompleted(User entity, Exception exception, ServiceFilterResponse response) {
                    if (exception == null) {
                        isUserCreated[0] = true;
                    } else {
                        Toast.makeText(getApplicationContext(), "Service Fail To Add User!", Toast.LENGTH_SHORT).show();
                        isUserCreated[0] = false;
                    }
                }
            });
        }

        return isUserCreated[0];
    }

    private User convertFromFacebookToUser() {
        User user = new User();
        user.setId(Profile.getCurrentProfile().getId());
        user.setName(Profile.getCurrentProfile().getFirstName());
        user.setSurName(Profile.getCurrentProfile().getLastName());
        return user;
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
            changeActivity(signup);
        }
    }

    private class FacebookRegisterCallback implements FacebookCallback {
        @Override
        public void onSuccess(Object o) {
            changeActivity(facebookLogin);
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
