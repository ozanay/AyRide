package com.iride.ayride;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class RegisterActivity extends AppCompatActivity {

    private final static String containsDigit = ".*\\d+.*";
    private String gender = null;
    private final static int ageLimit = 17;
    private DatePickerDialog birthdayPicker;
    private SimpleDateFormat dateFormatter;
    private Spinner genders;
    private EditText nameText;
    private EditText surNameText;
    private EditText birthdayText;
    private Button nextButton;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initializeName();
        initializeSurName();
        initializeBirthdayPicker();
        initializeGender();
        initializeNext();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void initializeName() {
        nameText = (EditText) findViewById(R.id.input_name);
    }

    private void initializeSurName() {
        surNameText = (EditText) findViewById(R.id.input_surname);
    }

    private void initializeBirthdayPicker() {
        dateFormatter = new SimpleDateFormat("dd-MM-yyyy");
        birthdayText = (EditText) findViewById(R.id.birthday_text);
        birthdayText.setInputType(InputType.TYPE_NULL);
        birthdayText.setOnTouchListener(new BirthdayListener());
        birthdayText.setOnFocusChangeListener(birthdayTextFocusChangeListener);
        Calendar newCalendar = Calendar.getInstance();
        birthdayPicker = new DatePickerDialog(this, new BirthdayDateListener(),
                newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
    }

    private void initializeGender() {
        genders = (Spinner) findViewById(R.id.gender_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.genderArray, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genders.setAdapter(adapter);
        genders.setOnItemSelectedListener(new GenderListener());
    }

    private void initializeNext() {
        nextButton = (Button) findViewById(R.id.next_button);
        nextButton.setOnClickListener(new NextListener());
    }

    private boolean isValidName(EditText textName) {
        String name = textName.getText().toString();
        name = name.replaceAll(" ", "");
        if (name == null || name.isEmpty() || name.matches(containsDigit) || name.length() < 3) {
            Toast.makeText(getApplicationContext(), "Invalid Name", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private boolean isValidSurName(EditText textSurName) {
        String surName = textSurName.getText().toString();
        surName = surName.replaceAll(" ", "");
        if (surName == null || surName.isEmpty() || surName.matches(containsDigit) || surName.length() < 2) {
            Toast.makeText(getApplicationContext(), "Invalid Surname", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private boolean isValidBirthday(Date birthday) {
        if (Calendar.getInstance().getTime().getYear() - birthday.getYear() < ageLimit) {
            Toast.makeText(getApplicationContext(), "Age +17", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void continueToRegister(String nameField, String surNameField, String genderField, String birthdayField) {
        Intent intent = new Intent(this, RegisterContinueActivity.class);
        intent.putExtra("name", nameField);
        intent.putExtra("surname", surNameField);
        intent.putExtra("gender", genderField);
        intent.putExtra("birthday", birthdayField);
        startActivity(intent);
    }

    private static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Register Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.iride.ayride/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Register Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.iride.ayride/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    private View.OnFocusChangeListener birthdayTextFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus){
                hideSoftKeyboard(RegisterActivity.this);
                birthdayPicker.show();
            }
        }
    };

    private class GenderListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int pos, long id) {
            // An item was selected. You can retrieve the selected item using
            parent.getItemAtPosition(pos);
            String genderString = genders.getSelectedItem().toString();
            gender = genderString.toUpperCase();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }


    private class BirthdayListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            hideSoftKeyboard(RegisterActivity.this);
            birthdayPicker.show();
            return false;
        }
    }

    private class BirthdayDateListener implements DatePickerDialog.OnDateSetListener {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            Calendar date = Calendar.getInstance();
            date.set(year, monthOfYear, dayOfMonth);
            birthdayText.setText(dateFormatter.format(date.getTime()));
        }
    }

    private class NextListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (!isValidName(nameText)) {
                nameText.requestFocus();
                nameText.setSelectAllOnFocus(true);
                return;
            }

            if (!isValidSurName(surNameText)) {
                surNameText.requestFocus();
                surNameText.setSelectAllOnFocus(true);
                return;
            }

            Date birthday = null;
            String birthdayString = birthdayText.getText().toString();
            try {
                birthday = dateFormatter.parse(birthdayString);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (!isValidBirthday(birthday)) {
                birthdayText.requestFocus();
                birthdayText.setSelectAllOnFocus(true);
                return;
            }

            String name = nameText.getText().toString();
            String surName = surNameText.getText().toString();
            continueToRegister(name, surName, gender, birthdayString);
        }
    }
}

