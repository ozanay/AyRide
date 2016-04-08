package com.iride.ayride;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class SearchRideActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_ride);
        findViewById(R.id.search_ride_loading_panel).setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(SearchRideActivity.this,HomePageActivity.class));
        finish();
    }
}
