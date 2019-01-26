package com.esgi.picturehunt;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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
import com.google.android.gms.common.SignInButton;
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
import static java.lang.Math.toIntExact;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class LoginActivity extends AppCompatActivity {
    private static final String SERVER_CLIENT_ID = "210033024094-mv75596k5dfg2gde1516enoadn9n7f6u.apps.googleusercontent.com";
    public static final int RC_SIGN_IN = 1;
    public static final String AUTH_TAG = "AUTH_TAG";
    private GoogleSignInClient mGoogleSignInClient;
    private Button mSignOutButton;
    private SignInButton mSignInButton;
    private ImageView mImageView;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private TextView mScore;
    private TextView mScoreTxt;
    private TextView mPseudo;
    private SeekBar mSeekBar;
    private TextView mRadiusTxt;
    private TextView mRadius;
    private FirebaseAnalytics mFirebaseAnalytics;
    private DatabaseReference mDatabase;
    private DatabaseReference mUsersListRef;
    private DatabaseReference mUserRef;
    private DatabaseReference mRadiusRef;

    private void setAttributes() {
        mSignInButton = findViewById(R.id.sign_in_button);
        mSignOutButton = findViewById(R.id.sign_out_buton);
        mImageView = findViewById(R.id.imageView);
        mScore = findViewById(R.id.score);
        mScoreTxt = findViewById(R.id.scoreTxt);
        mPseudo = findViewById(R.id.pseudo);
        mSeekBar = findViewById(R.id.seekBar);
        mRadiusTxt = findViewById(R.id.radiusTxt);
        mRadius = findViewById(R.id.radius);
        GoogleSignInOptions mGso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(SERVER_CLIENT_ID)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, mGso);
        mAuth = FirebaseAuth.getInstance();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mUsersListRef = mDatabase.child("users");
    }

    private void initAttributes() {
        mSignInButton.setSize(SignInButton.SIZE_WIDE);
        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });
        mSignOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                mGoogleSignInClient.signOut();
                mUser = mAuth.getCurrentUser();
                updateUI();
            }
        });
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setAttributes();
        initAttributes();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mUser = mAuth.getCurrentUser();
        updateUI();
        if ( mUser == null ) {
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
            if ( account != null ) {
                Toast.makeText(this, "Ravi de vous revoir " + account.getDisplayName(), Toast.LENGTH_SHORT).show();
                firebaseAuthWithGoogle(account);
            }
        }
        else {
            initRadiusAndScore();
        }
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
                    mRadiusRef.setValue(3);
                    mSeekBar.setProgress(2);
                    scoreRef.setValue(0);
                    updateUI();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ( requestCode == RC_SIGN_IN ) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            GoogleSignInAccount account = task.getResult();
            if ( account != null ) {
                firebaseAuthWithGoogle(account);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        Log.d(AUTH_TAG, "firebaseAuthWithGoogle:" + account.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(AUTH_TAG, "signInWithCredential:success");
                            Toast.makeText(LoginActivity.this, "Vous êtes maintenant connecté", Toast.LENGTH_SHORT).show();
                            mUser = mAuth.getCurrentUser();
                            initRadiusAndScore();
                            updateUI();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(AUTH_TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Erreur lors de la connexion", Toast.LENGTH_SHORT).show();
                            //Snackbar.make(findViewById(R.id.main_layout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void updateUI() {
        if ( mUser == null ) {
            mSignInButton.setVisibility(View.VISIBLE);
            mSignOutButton.setVisibility(View.GONE);
            mImageView.setImageResource(android.R.color.transparent);
            mScore.setVisibility(View.GONE);
            mPseudo.setVisibility(View.GONE);
            mRadiusTxt.setVisibility(View.GONE);
            mScoreTxt.setVisibility(View.GONE);
            mSeekBar.setVisibility(View.GONE);
            mRadius.setVisibility(View.GONE);
        }
        else {
            Uri photoUrl = mUser.getPhotoUrl();
            if ( photoUrl != null ) {
                Picasso.get()
                        .load(photoUrl)
                        .transform(new CropCircleTransformation())
                        .into(mImageView);
            }
            else {
                //mImageView.setImageResource(R.drawable.avatar_drawable);
            }
            mPseudo.setText(mUser.getDisplayName());
            mSignInButton.setVisibility(View.GONE);
            mSignOutButton.setVisibility(View.VISIBLE);
            mScore.setVisibility(View.VISIBLE);
            mScoreTxt.setVisibility(View.VISIBLE);
            mPseudo.setVisibility(View.VISIBLE);
            mRadiusTxt.setVisibility(View.VISIBLE);
            mSeekBar.setVisibility(View.VISIBLE);
            mRadius.setVisibility(View.VISIBLE);
        }
    }
}