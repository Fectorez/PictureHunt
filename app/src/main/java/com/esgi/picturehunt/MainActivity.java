package com.esgi.picturehunt;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ColorSpace;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    public static final String LIFE_CYCLE_MAIN = "LIFE_CYCLE_MAIN";
    public static final String REFERENCE_PHOTOS_TO_HUNT = "photosToHunt";

    private RecyclerView recyclerView;
    private MyFirebaseDatabase myFirebaseDatabase;

    private FusedLocationProviderClient client;

    private Location userLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(LIFE_CYCLE_MAIN, "onCreate");

        if ( MyFirebaseAuth.getUser() == null )
            goToLogin();

        client = LocationServices.getFusedLocationProviderClient(MainActivity.this);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        myFirebaseDatabase = new MyFirebaseDatabase(REFERENCE_PHOTOS_TO_HUNT);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        Menu menu = bottomNav.getMenu();
        MenuItem menuItem = menu.getItem(0);
        menuItem.setChecked(true);
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(LIFE_CYCLE_MAIN, "onStart");

        if(ActivityCompat.checkSelfPermission(
                MainActivity.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return;
        }
        client.getLastLocation().addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                userLocation = location;
            }
        });

        FirebaseRecyclerAdapter<PhotoToHunt, ViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<PhotoToHunt, ViewHolder>(
                        PhotoToHunt.class,
                        R.layout.row_photo_to_hunt,
                        ViewHolder.class,
                        myFirebaseDatabase.getDatabaseReference()
                ) {
                    @Override
                    protected void populateViewHolder(ViewHolder viewHolder, PhotoToHunt model, int position) {
                        viewHolder.setDetails(getApplicationContext(), model, userLocation);
                    }
                };

        recyclerView.setAdapter(firebaseRecyclerAdapter);
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
                            return true;
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
