package com.esgi.picturehunt;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MyFirebaseAuth {
    private FirebaseAuth auth;
    private FirebaseUser user;

    public MyFirebaseAuth(){
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
    }

    public FirebaseAuth getAuth() {
        return auth;
    }

    public FirebaseUser getUser() {
        return user;
    }
}