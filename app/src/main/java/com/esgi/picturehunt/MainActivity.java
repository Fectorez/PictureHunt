package com.esgi.picturehunt;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    public static final String LIFE_CYCLE_MAIN = "LIFE_CYCLE_MAIN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(LIFE_CYCLE_MAIN, "onCreate");
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        Menu menu = bottomNav.getMenu();
        MenuItem menuItem = menu.getItem(0);
        menuItem.setChecked(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(LIFE_CYCLE_MAIN, "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(LIFE_CYCLE_MAIN, "onResume");

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(LIFE_CYCLE_MAIN, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(LIFE_CYCLE_MAIN, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(LIFE_CYCLE_MAIN, "onDestroy");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(LIFE_CYCLE_MAIN, "onRestart");
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Intent intentSelected = null;

                    switch (item.getItemId()) {
                        case R.id.nav_home:
                            intentSelected = new Intent(MainActivity.this, MainActivity.class);
                            break;
                        case R.id.nav_camera:
                            intentSelected = new Intent(MainActivity.this, CameraActivity.class);
                            break;
                        case R.id.nav_settings:
                            intentSelected = new Intent(MainActivity.this, SettingsActivity.class);
                            break;
                    }
                    startActivity(intentSelected);
                    finish();
                    return true;
                }
            };
}
