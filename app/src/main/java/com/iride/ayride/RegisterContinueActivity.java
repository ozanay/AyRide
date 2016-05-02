package com.iride.ayride;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.table.TableOperationCallback;
import com.microsoft.windowsazure.mobileservices.table.TableQueryCallback;

import java.net.MalformedURLException;
import java.util.List;

public class RegisterContinueActivity extends AppCompatActivity {

    private final static int passwordLengthRestriction = 8;
    private final static int phoneNumberLengthRestriction = 10;
    private final static String containsDigit = ".*\\d+.*";
    private final static String letterPattern = "[a-zA-Z]+";
    private final static String numberPattern = "[0-9]+";
    private final static String loggerTag = RegisterContinueActivity.class.getSimpleName();
    private String name;
    private String surName;
    private String gender;
    private String birthday;
    private User user;
    private EditText phoneText;
    private EditText emailText;
    private EditText passwordText;
    private EditText rePasswordText;
    private Button createAccount;
    private MobileServiceClient mobileServiceClient;
    private MobileServiceTable<User> mobileServiceTable;
    private UserLocalStorage userLocalStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_continue);
        user = new User();
        userLocalStorage = new UserLocalStorage(getSharedPreferences(String.valueOf(StoragePreferences.PREFERENCES), Context.MODE_PRIVATE));
        findViewById(R.id.sign_up_loading_panel).setVisibility(View.GONE);
        setFields(getIntent());
        initializeEditTexts();
        initializeMobileService();
    }

    private void initializeEditTexts() {
        phoneText = (EditText) findViewById(R.id.input_phone_number);
        phoneText.addTextChangedListener(new PhoneNumberFormatter());
        emailText = (EditText) findViewById(R.id.input_email);
        passwordText = (EditText) findViewById(R.id.input_password);
        rePasswordText = (EditText) findViewById(R.id.input_repassword);
        createAccount = (Button) findViewById(R.id.create_account_button);
        createAccount.setOnClickListener(new CreateAccountListener());
    }

    private void initializeMobileService(){
        try {
            mobileServiceClient = new MobileServiceClient(
                    getString(R.string.azureApiUrl),
                    getString(R.string.azureApiKey),
                    this
            );
            mobileServiceTable = mobileServiceClient.getTable("user_info",User.class);
        } catch (MalformedURLException e) {
            Log.e(loggerTag,e.getMessage());
        }
    }

    private void setFields(Intent intent){
        name = intent.getStringExtra("name");
        surName = intent.getStringExtra("surname");
        gender = intent.getStringExtra("gender");
        birthday = intent.getStringExtra("birthday");

    }

    private void ShowMessage(String msg) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(RegisterContinueActivity.this);
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

    private String ExtractPhoneNumber(String phoneNumber) {
        return phoneNumber.substring(1, 4) + phoneNumber.substring(6, 9) + phoneNumber.substring(10);
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber.contains(letterPattern)) {
            Toast.makeText(getApplicationContext(), "Invalid characters in phone number", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (phoneNumber.length() != phoneNumberLengthRestriction) {
            Toast.makeText(getApplicationContext(), "Invalid phone number length", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!phoneNumber.matches(numberPattern)) {
            Toast.makeText(getApplicationContext(), "Invalid characters in phone number", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private boolean isValidEmail(EditText emailText) {
        String email = emailText.getText().toString().trim();
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(getApplicationContext(), "Invalid email address", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean isValidPassword(String password, String repassword) {
        if (!password.equals(repassword)) {
            Toast.makeText(getApplicationContext(), "Re-password does not match!", Toast.LENGTH_SHORT).show();
            return false;
        }

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

    private void addUserToDB(final User user) {
        if (mobileServiceClient == null) {
            Log.e(loggerTag, "Service Is Null");
            return;
        }

        mobileServiceTable.insert(user, new TableOperationCallback<User>() {
            public void onCompleted(User entity, Exception exception, ServiceFilterResponse response) {
                if (exception == null) {
                    userLocalStorage.storeUser(entity);
                    findViewById(R.id.sign_up_loading_panel).setVisibility(View.GONE);
                    startActivity(new Intent(RegisterContinueActivity.this, HomePageActivity.class));
                } else {
                    Toast.makeText(getApplicationContext(), "Fail Registration!", Toast.LENGTH_SHORT).show();
                    Log.d(loggerTag, exception.getMessage());
                    startActivity(new Intent(RegisterContinueActivity.this, RegisterActivity.class));
                    finish();
                }
            }
        });
    }

    private void checkExistenceOfUser(String email) {
        mobileServiceTable.where().field("user_email").eq(email).execute(new TableQueryCallback<User>() {
            public void onCompleted(List<User> result, int count, Exception exception, ServiceFilterResponse response) {
                if (exception == null) {
                    if (result.isEmpty() || result.size() == 0) {
                        Log.d(loggerTag, "No Existence!");
                    } else {
                        Log.d(loggerTag, "Already Exist!");
                        Toast.makeText(getApplicationContext(), "User Has Already Existed", Toast.LENGTH_SHORT).show();
                        findViewById(R.id.sign_up_loading_panel).setVisibility(View.GONE);
                        startActivity(new Intent(RegisterContinueActivity.this, HomePageActivity.class));
                        finish();
                    }
                } else {
                    findViewById(R.id.sign_up_loading_panel).setVisibility(View.GONE);
                    Log.e(loggerTag, exception.getMessage());
                }
            }
        });
    }

    private class CreateAccountListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            String phone = ExtractPhoneNumber(phoneText.getText().toString());
            if (!isValidPhoneNumber(phone)) {
                phoneText.requestFocus();
                phoneText.setSelectAllOnFocus(true);
                ShowMessage(phone);
                return;
            }

            if (!isValidEmail(emailText)) {
                emailText.requestFocus();
                emailText.setSelectAllOnFocus(true);
                return;
            }

            if (!isValidPassword(passwordText.getText().toString(), rePasswordText.getText().toString())) {
                passwordText.requestFocus();
                passwordText.setSelectAllOnFocus(true);
                return;
            }
            user.setName(name);
            user.setSurName(surName);
            user.setGender(gender);
            user.setBirthday(birthday);
            user.setPhoneNumber(phone);
            user.setEmail(emailText.getText().toString());
            user.setPassword(passwordText.getText().toString());
            findViewById(R.id.sign_up_loading_panel).setVisibility(View.VISIBLE);
            checkExistenceOfUser(emailText.getText().toString());
            addUserToDB(user);
        }
    }

    private class PhoneNumberFormatter implements TextWatcher {

        private boolean backspacingFlag = false;
        private boolean editedFlag = false;
        private int cursorComplement;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            cursorComplement = s.length() - phoneText.getSelectionStart();
            if (count > after) {
                backspacingFlag = true;
            } else {
                backspacingFlag = false;
            }
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            String string = s.toString();
            String phone = string.replaceAll("[^\\d]", "");
            if (!editedFlag) {

                if (phone.length() >= 6 && !backspacingFlag) {
                    editedFlag = true;
                    String ans = "(" + phone.substring(0, 3) + ") " + phone.substring(3, 6) + "-" + phone.substring(6);
                    phoneText.setText(ans);
                    phoneText.setSelection(phoneText.getText().length() - cursorComplement);

                } else if (phone.length() >= 3 && !backspacingFlag) {
                    editedFlag = true;
                    String ans = "(" + phone.substring(0, 3) + ") " + phone.substring(3);
                    phoneText.setText(ans);
                    phoneText.setSelection(phoneText.getText().length() - cursorComplement);
                }
            } else {
                editedFlag = false;
            }
        }
    }
}
