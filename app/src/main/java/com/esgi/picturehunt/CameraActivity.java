package com.esgi.picturehunt;

import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class CameraActivity extends AppCompatActivity {

    public static final String LIFE_CYCLE_CAMERA = "LIFE_CYCLE_CAMERA";
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        Menu menu = bottomNav.getMenu();
        MenuItem menuItem = menu.getItem(1);
        menuItem.setChecked(true);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(LIFE_CYCLE_CAMERA, "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(LIFE_CYCLE_CAMERA, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(LIFE_CYCLE_CAMERA, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(LIFE_CYCLE_CAMERA, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(LIFE_CYCLE_CAMERA, "onDestroy");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(LIFE_CYCLE_CAMERA, "onRestart");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == -1) {
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(bitmap);
        }
        else{
            Intent intent = new Intent(this,MainActivity.class);
            startActivity(intent);
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Intent intentSelected = null;

                    switch (item.getItemId()) {
                        case R.id.nav_home:
                            intentSelected = new Intent(CameraActivity.this, MainActivity.class);
                            break;
                        case R.id.nav_camera:
                            intentSelected = new Intent(CameraActivity.this, CameraActivity.class);
                            break;
                        case R.id.nav_settings:
                            intentSelected = new Intent(CameraActivity.this, SettingsActivity.class);
                            break;
                    }
                    startActivity(intentSelected);
                    finish();
                    return true;
                }
            };
}