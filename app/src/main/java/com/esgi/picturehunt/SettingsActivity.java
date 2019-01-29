package com.esgi.picturehunt;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class SettingsActivity extends AppCompatActivity {

    public static final String LIFE_CYCLE_SETTINGS = "LIFE_CYCLE_SETTINGS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        Menu menu = bottomNav.getMenu();
        MenuItem menuItem = menu.getItem(2);
        menuItem.setChecked(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(LIFE_CYCLE_SETTINGS, "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(LIFE_CYCLE_SETTINGS, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(LIFE_CYCLE_SETTINGS, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(LIFE_CYCLE_SETTINGS, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(LIFE_CYCLE_SETTINGS, "onDestroy");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(LIFE_CYCLE_SETTINGS, "onRestart");
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Intent intentSelected = null;

                    switch (item.getItemId()) {
                        case R.id.nav_home:
                            intentSelected = new Intent(SettingsActivity.this, MainActivity.class);
                            break;
                        case R.id.nav_camera:
                            intentSelected = new Intent(SettingsActivity.this, CameraActivity.class);
                            break;
                        case R.id.nav_settings:
                            intentSelected = new Intent(SettingsActivity.this, SettingsActivity.class);
                            break;
                    }
                    startActivity(intentSelected);
                    finish();
                    return true;
                }
            };
}
