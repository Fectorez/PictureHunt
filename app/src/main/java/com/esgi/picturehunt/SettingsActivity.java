package com.esgi.picturehunt;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
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
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Locale;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

import static java.lang.Math.toIntExact;

public class SettingsActivity extends AppCompatActivity {

    public static final String LIFE_CYCLE_SETTINGS = "LIFE_CYCLE_SETTINGS";

    private static final String SERVER_CLIENT_ID = "210033024094-mv75596k5dfg2gde1516enoadn9n7f6u.apps.googleusercontent.com";
    private static final int DEFAULT_PROGRESS = 2;
    private static final int DEFAULT_RADIUS = 3;

    private Button mSignOutButton;
    private TextView mScore;
    private SeekBar mSeekBar;
    private TextView mRadius;

    private MyFirebaseDatabase myFirebaseDatabase, mUsersListRef, mUserRef, mRadiusRef, mScoreRef;

    private void setAttributes() {
        mSignOutButton = findViewById(R.id.sign_out_buton);
        mScore = findViewById(R.id.score);
        mSeekBar = findViewById(R.id.seekBar);
        mRadius = findViewById(R.id.radius);

        myFirebaseDatabase = new MyFirebaseDatabase();
        mUsersListRef = new MyFirebaseDatabase(myFirebaseDatabase.getDatabaseReference(), "users");
        mUserRef = new MyFirebaseDatabase(mUsersListRef.getDatabaseReference(), MyFirebaseAuth.getUser().getUid());
        mRadiusRef = new MyFirebaseDatabase(mUserRef.getDatabaseReference(), "radius");
        mScoreRef = new MyFirebaseDatabase(mUserRef.getDatabaseReference(), "score");
    }

    private void initSignOutButton() {
        mSignOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyFirebaseAuth.getAuth().signOut();
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(SERVER_CLIENT_ID)
                        .requestEmail()
                        .build();
                GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(SettingsActivity.this, gso);
                googleSignInClient.signOut();
                goToLogin();
            }
        });
    }

    private void initSeekBar() {
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mRadius.setText(String.format(Locale.FRENCH,"%d km",progress+1));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mRadiusRef.getDatabaseReference().setValue(seekBar.getProgress()+1);
            }
        });
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if(Build.VERSION.SDK_INT >= 24){
            requestPermissions(new String[] {Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_FINE_LOCATION}, 2);
        }

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        Menu menu = bottomNav.getMenu();
        MenuItem menuItem = menu.getItem(2);
        menuItem.setChecked(true);

        setAttributes();
        initSignOutButton();
        initSeekBar();

        if ( MyFirebaseAuth.getUser() == null ){
            goToLogin();
        }
        else {
            initRadiusAndScore();
            initPhoto();
            ((TextView)findViewById(R.id.pseudo)).setText(MyFirebaseAuth.getUser().getDisplayName());
        }
    }

    private void initPhoto() {
        Uri photoUrl = MyFirebaseAuth.getUser().getPhotoUrl();
        ImageView imageView = findViewById(R.id.profilePicture);
        if ( photoUrl != null ) {
            Picasso.get()
                    .load(photoUrl)
                    .transform(new CropCircleTransformation())
                    .into(imageView);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(LIFE_CYCLE_SETTINGS, "onStart");
    }

    private void initRadiusAndScore() {
        mUsersListRef.getDatabaseReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(MyFirebaseAuth.getUser().getUid())) {
                    // user pas connu donc init ses attributs
                    mRadiusRef.getDatabaseReference().setValue(DEFAULT_RADIUS);
                    mSeekBar.setProgress(DEFAULT_PROGRESS);
                    mScoreRef.getDatabaseReference().setValue(0);
                    mUserRef.getDatabaseReference().child("displayName").setValue(MyFirebaseAuth.getUser().getDisplayName());
                } else {
                    // user connu donc on va chercher ses attributs
                    mRadiusRef.getDatabaseReference().addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            mSeekBar.setProgress(toIntExact((long)dataSnapshot.getValue()-1), false);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {}
                    });
                    mScoreRef.getDatabaseReference().addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            mScore.setText(String.valueOf((long)dataSnapshot.getValue()));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {}
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
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
                            return true;
                    }
                    startActivity(intentSelected);
                    finish();
                    return true;
                }
            };
}