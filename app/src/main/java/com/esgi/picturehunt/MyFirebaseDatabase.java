package com.esgi.picturehunt;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MyFirebaseDatabase {
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    public MyFirebaseDatabase(){
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
    }

    public MyFirebaseDatabase(String reference){
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference(reference);
    }

    public MyFirebaseDatabase(DatabaseReference databaseReference, String reference){
        firebaseDatabase = FirebaseDatabase.getInstance();
        this.databaseReference = databaseReference.child(reference);
    }

    public FirebaseDatabase getFirebaseDatabase() {
        return firebaseDatabase;
    }

    public DatabaseReference getDatabaseReference() {
        return databaseReference;
    }
}
