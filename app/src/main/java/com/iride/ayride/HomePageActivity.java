package com.iride.ayride;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.facebook.AccessToken;

public class HomePageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);
    }

    @Override
    public void onBackPressed(){
        exitFromTheApp();
    }

    private boolean isFaccebookUser(){
        return AccessToken.getCurrentAccessToken() != null;
    }

    private void exitFromTheApp() {
        String exitMessage = "Do you want to exit?";
        AlertDialog.Builder builder = new AlertDialog.Builder(HomePageActivity.this);
        builder.setMessage(exitMessage);
        builder.setCancelable(true);
        builder.setNegativeButton("Close",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        builder.setPositiveButton("Exit",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });

        AlertDialog alert11 = builder.create();
        alert11.show();
    }
}
