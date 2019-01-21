package com.esgi.picturehunt;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
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
import com.google.android.gms.signin.SignIn;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

public class LoginActivity extends AppCompatActivity {
    public static final int RC_SIGN_IN = 1;
    public static final String AUTH_TAG = "AUTH_TAG";
    private GoogleSignInOptions mGso;
    private GoogleSignInClient mGoogleSignInClient;
    private Button mSignOutButton;
    private SignInButton mSignInButton;
    private ImageView mImageView;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private TextView mScore;
    private TextView mPseudo;
    private SeekBar mSeekBar;
    private TextView mTextViewRadius;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mSignInButton = findViewById(R.id.sign_in_button);
        mSignOutButton = findViewById(R.id.sign_out_buton);
        mImageView = findViewById(R.id.imageView);
        mScore = findViewById(R.id.score);
        mPseudo = findViewById(R.id.pseudo);
        mSeekBar = findViewById(R.id.seekBar);
        mTextViewRadius = findViewById(R.id.textViewRadius);
        mGso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, mGso);
        mAuth = FirebaseAuth.getInstance();

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
                mUser = mAuth.getCurrentUser();
                updateUI();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mUser = mAuth.getCurrentUser();
        updateUI();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if ( account != null ) {
            Toast.makeText(this, "Ravi de vous revoir " + account.getDisplayName(), Toast.LENGTH_SHORT).show();
            firebaseAuthWithGoogle(account);
        }
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
                            updateUI();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(AUTH_TAG, "signInWithCredential:failure", task.getException());
                            //Snackbar.make(findViewById(R.id.main_layout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            updateUI();
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
            mTextViewRadius.setVisibility(View.GONE);
            mSeekBar.setVisibility(View.GONE);
        }
        else {
            Uri photoUrl = mUser.getPhotoUrl();
            if ( photoUrl != null ) {
                Picasso.get().load(photoUrl).into(mImageView);
            }
            else {
                mImageView.setImageResource(R.drawable.avatar_drawable);
            }
            mPseudo.setText(mUser.getDisplayName());
            mSignInButton.setVisibility(View.GONE);
            mSignOutButton.setVisibility(View.VISIBLE);
            mScore.setVisibility(View.VISIBLE);
            mPseudo.setVisibility(View.VISIBLE);
            mTextViewRadius.setVisibility(View.VISIBLE);
            mSeekBar.setVisibility(View.VISIBLE);
        }
    }
}