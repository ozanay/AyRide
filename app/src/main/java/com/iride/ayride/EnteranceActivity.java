package com.iride.ayride;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.facebook.FacebookSdk;

public class EnteranceActivity extends AppCompatActivity {

    private static String login = "LOGIN";
    private static String signup = "SIGNUP";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_enterance);
        final Button loginButton = (Button) findViewById(R.id.login_button);
        loginButton.setOnClickListener(new LoginListener());

        final Button signUpButton = (Button) findViewById(R.id.sign_up_button);
        signUpButton.setOnClickListener(new SignUpListener());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_enterance, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return super.onOptionsItemSelected(item);
    }

    private void changeActivity(String activityName){
        activityName = activityName.toUpperCase();
        Intent intent;
        if(activityName == signup) {
             intent = new Intent(this, RegisterActivity.class);
        } else if (activityName == login) {
            intent = new Intent(this, LoginActivity.class);
        } else {
            intent = new Intent(this, EnteranceActivity.class);
        }
        startActivity(intent);
    }

    private class LoginListener implements View.OnClickListener {
        public void onClick(View v) {
            // Perform action on click
            changeActivity(login);
        }
    }

    private class SignUpListener implements View.OnClickListener {
        public void onClick(View v) {
            // Perform action on click
            changeActivity(signup);
        }
    }
}
