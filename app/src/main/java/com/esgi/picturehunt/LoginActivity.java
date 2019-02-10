package com.esgi.picturehunt;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {
    private static final String SERVER_CLIENT_ID = "210033024094-mv75596k5dfg2gde1516enoadn9n7f6u.apps.googleusercontent.com";
    public static final int RC_SIGN_IN = 1;
    public static final String AUTH_TAG = "AUTH_TAG";

    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        init();
    }

    private void init() {
        GoogleSignInOptions mGso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(SERVER_CLIENT_ID)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, mGso);

        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_WIDE);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ( requestCode == RC_SIGN_IN ) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            GoogleSignInAccount account = task.getResult();
            if ( account == null )
                Toast.makeText(this, "Echec lors de l'authentification avec Google", Toast.LENGTH_LONG).show();
            else
                firebaseAuthWithGoogle(account);
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        Log.d(AUTH_TAG, "firebaseAuthWithGoogle:" + account.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        MyFirebaseAuth.getAuth().signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if ( task.isSuccessful() ) {
                            Log.d(AUTH_TAG, "signInWithCredential:success");
                            Toast.makeText(LoginActivity.this, "Vous êtes maintenant connecté", Toast.LENGTH_SHORT).show();
                            goToSettings();
                        }
                        else {
                            Log.w(AUTH_TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Erreur lors de la connexion à l'application", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void goToSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        /*mUser = mAuth.getCurrentUser();
        if ( mUser == null ) {
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
            if ( account != null ) {
                Toast.makeText(this, "Ravi de vous revoir " + account.getDisplayName(), Toast.LENGTH_SHORT).show();
                firebaseAuthWithGoogle(account);
            }
        }
        else {
            //initRadiusAndScore();
        }*/
    }


}