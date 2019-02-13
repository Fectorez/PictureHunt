package com.esgi.picturehunt;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MyFirebaseDatabase {
    private static FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference;

    public MyFirebaseDatabase(){
        databaseReference = firebaseDatabase.getReference();
    }

    public MyFirebaseDatabase(String reference){
        databaseReference = firebaseDatabase.getReference(reference);
    }

    public MyFirebaseDatabase(DatabaseReference databaseReference, String reference){
        this.databaseReference = databaseReference.child(reference);
    }

    public FirebaseDatabase getFirebaseDatabase() {
        return firebaseDatabase;
    }

    public DatabaseReference getDatabaseReference() {
        return databaseReference;
    }
}
