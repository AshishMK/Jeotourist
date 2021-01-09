package com.x.jeotourist.scene.mapScene;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.x.jeotourist.R;

public class MapActivity extends AppCompatActivity {
    public static final String TOURID = "tourID";
    public static final String TITLE = "title";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        if(getResources().getBoolean(R.bool.has_two_panes)){
            finish();
            return;
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getIntent().getStringExtra(MapActivity.TITLE));
        MapFragment fragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        fragment.setTourIdFromActivity(getIntent().getLongExtra(MapActivity.TOURID, 0));
    }
}