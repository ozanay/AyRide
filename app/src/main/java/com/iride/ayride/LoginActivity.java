package com.iride.ayride;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.appevents.AppEventsLogger;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.table.TableQueryCallback;

import java.net.MalformedURLException;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private final static int passwordLengthRestriction = 8;
    private final static String containsDigit = ".*\\d+.*";
    private final static String mobileServiceUrl = "https://useraccount.azure-mobile.net/";
    private final static String mobileServiceAppKey = "BCGeAFQbjUEOGanLwVXslBzVMykgEM16";
    private EditText emailText;
    private EditText passwordText;
    private Button signInButton;
    private MobileServiceClient mobileServiceClient;
    private MobileServiceTable<User> mobileServiceTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initializeUIFields();
        initializeMobileService();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }

    private void initializeUIFields() {
        emailText = (EditText) findViewById(R.id.email_field);
        passwordText = (EditText) findViewById(R.id.password_field);
        signInButton = (Button) findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new SignInListener());
    }

    private void initializeMobileService() {
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
    }

    private boolean isValidEmail(EditText emailText) {
        String email = emailText.getText().toString().trim();
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(getApplicationContext(), "Invalid email address", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean isValidPassword(String password) {
        if (password.length() < passwordLengthRestriction) {
            Toast.makeText(getApplicationContext(), "Password cannot be less than 8 characters!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!password.matches(containsDigit)) {
            Toast.makeText(getApplicationContext(), "Password must contain at least one number!", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void loginToApp(final String email, final String password) {
        mobileServiceTable.where().field("user_email").eq(email).and().field("user_password").eq(password).execute(new TableQueryCallback<User>() {
            public void onCompleted(List<User> result, int count, Exception exception, ServiceFilterResponse response) {
                if (exception == null) {
                    if (!result.isEmpty()) {
                        if (result.size() == 1){
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        }
                    }
                } else {
                    ShowMessage(exception.getCause().toString());
                    emailText.requestFocus();
                    emailText.setSelectAllOnFocus(true);
                }
            }
        });
    }

    private void ShowMessage(String msg) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(LoginActivity.this);
        builder1.setMessage(msg);
        builder1.setCancelable(true);
        builder1.setNegativeButton("Close",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    private class SignInListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (!isValidEmail(emailText)) {
                emailText.requestFocus();
                emailText.setSelectAllOnFocus(true);
                return;
            }

            if (!isValidPassword(passwordText.getText().toString())) {
                passwordText.requestFocus();
                passwordText.setSelectAllOnFocus(true);
                return;
            }

            loginToApp(emailText.getText().toString(), passwordText.getText().toString());
        }
    }
}
