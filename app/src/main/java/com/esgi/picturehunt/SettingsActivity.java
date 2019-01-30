package com.esgi.picturehunt;

import android.content.Intent;
import android.net.Uri;
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
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Locale;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

import static java.lang.Math.toIntExact;

public class SettingsActivity extends AppCompatActivity {

    public static final String LIFE_CYCLE_SETTINGS = "LIFE_CYCLE_SETTINGS";

    private static final String SERVER_CLIENT_ID = "210033024094-mv75596k5dfg2gde1516enoadn9n7f6u.apps.googleusercontent.com";
    public static final int RC_SIGN_IN = 1;
    public static final String AUTH_TAG = "AUTH_TAG";
    private static final int DEFAULT_PROGRESS = 2;
    private static final int DEFAULT_RADIUS = 3;
    private Button mSignOutButton;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser mUser;
    private TextView mScore;
    private SeekBar mSeekBar;
    private TextView mRadius;
    private DatabaseReference mUsersListRef;
    private DatabaseReference mUserRef;
    private DatabaseReference mRadiusRef;

    private void setAttributes() {
        mSignOutButton = findViewById(R.id.sign_out_buton);
        mScore = findViewById(R.id.score);
        mSeekBar = findViewById(R.id.seekBar);
        mRadius = findViewById(R.id.radius);
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        mUsersListRef = dbRef.child("users");
        mUser = mAuth.getCurrentUser();
    }

    private void initSignOutButton() {
        mSignOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(SERVER_CLIENT_ID)
                        .requestEmail()
                        .build();
                GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(SettingsActivity.this, gso);
                googleSignInClient.signOut();
                mUser = mAuth.getCurrentUser();
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
                mRadiusRef.setValue(seekBar.getProgress()+1);
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

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        Menu menu = bottomNav.getMenu();
        MenuItem menuItem = menu.getItem(2);
        menuItem.setChecked(true);

        setAttributes();
        initSignOutButton();
        initSeekBar();

        if ( mUser == null )
            goToLogin();
        else {
            initRadiusAndScore();
            initPhoto();
        }
    }

    private void initPhoto() {
        Uri photoUrl = mUser.getPhotoUrl();
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
        mUsersListRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUserRef = mUsersListRef.child(mUser.getUid());
                mRadiusRef = mUserRef.child("radius");
                DatabaseReference scoreRef = mUserRef.child("score");
                if (!dataSnapshot.hasChild(mUser.getUid())) {
                    // user pas connu donc init ses attributs
                    mRadiusRef.setValue(DEFAULT_RADIUS);
                    mSeekBar.setProgress(DEFAULT_PROGRESS);
                    scoreRef.setValue(0);
                } else {
                    // user connu donc on va chercher ses attributs
                    mRadiusRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            mSeekBar.setProgress(toIntExact((long)dataSnapshot.getValue()-1), false);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {}
                    });
                    scoreRef.addListenerForSingleValueEvent(new ValueEventListener() {
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
                            intentSelected = new Intent(SettingsActivity.this, SettingsActivity.class);
                            break;
                    }
                    startActivity(intentSelected);
                    finish();
                    return true;
                }
            };
}
