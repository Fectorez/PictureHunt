package com.esgi.picturehunt;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class MyFirebaseStorage {
    private FirebaseStorage storage;
    private StorageReference storageReference;

    public MyFirebaseStorage(){
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
    }

    public FirebaseStorage getStorage() {
        return storage;
    }

    public StorageReference getStorageReference() {
        return storageReference;
    }
}