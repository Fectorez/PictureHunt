package com.esgi.picturehunt;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MyFirebaseAuth {
    private FirebaseAuth auth;
    private FirebaseUser user;
    private static MyFirebaseAuth instance;

    private MyFirebaseAuth(){
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
    }

    public static FirebaseAuth getAuth() {
        if ( instance == null )
            instance = new MyFirebaseAuth();
        return instance.auth;
    }

    public static FirebaseUser getUser() {
        if ( instance == null )
            instance = new MyFirebaseAuth();
        return instance.user;
    }
}