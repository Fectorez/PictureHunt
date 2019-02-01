package com.esgi.picturehunt;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.support.v4.content.FileProvider;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.os.Environment.getExternalStoragePublicDirectory;
import static android.view.View.VISIBLE;

public class CameraActivity extends AppCompatActivity {

    public static final String LIFE_CYCLE_CAMERA = "LIFE_CYCLE_CAMERA";
    public static final String CAMERA_LOG = "CAMERA_LOG";
    public static final String REFERENCE_PHOTOS_TO_HUNT = "photosToHunt";

    private String ID, image;
    private double latitude, longitude;

    private TextView userLatitude, userLongitude, noGeoloc;
    private Button btnTakePicture, btnValidatePicture, btnCancel;
    private ImageView myPicture;
    private FusedLocationProviderClient client;
    private MyFirebaseDatabase myFirebaseDatabase;
    private MyFirebaseStorage myFirebaseStorage;
    private MyFirebaseAuth myFirebaseAuth;
    private Uri photoURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        myFirebaseAuth = new MyFirebaseAuth();
        if ( myFirebaseAuth.getUser() == null ){
            goToLogin();
        }

        myFirebaseDatabase = new MyFirebaseDatabase(REFERENCE_PHOTOS_TO_HUNT);
        myFirebaseStorage = new MyFirebaseStorage();
        client = LocationServices.getFusedLocationProviderClient(CameraActivity.this);

        userLatitude = findViewById(R.id.userLatitude);
        userLongitude = findViewById(R.id.userLongitude);
        noGeoloc = findViewById(R.id.noGeoloc);
        btnTakePicture = findViewById(R.id.takePicture);
        btnValidatePicture = findViewById(R.id.validatePicture);
        btnCancel = findViewById(R.id.cancel);
        myPicture = findViewById(R.id.myPicture);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        Menu menu = bottomNav.getMenu();
        MenuItem menuItem = menu.getItem(1);
        menuItem.setChecked(true);

        if(Build.VERSION.SDK_INT >= 24){
            requestPermissions(new String[] {Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.ACCESS_FINE_LOCATION}, 2);
        }

        btnTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchPictureTakerAction();
            }
        });

        btnValidatePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ID = myFirebaseDatabase.getDatabaseReference().push().getKey();
                uploadPhoto();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CameraActivity.this, CameraActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(LIFE_CYCLE_CAMERA, "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(LIFE_CYCLE_CAMERA, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(LIFE_CYCLE_CAMERA, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(LIFE_CYCLE_CAMERA, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(LIFE_CYCLE_CAMERA, "onDestroy");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(LIFE_CYCLE_CAMERA, "onRestart");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            if(requestCode == 1) {
                if(ActivityCompat.checkSelfPermission(
                        CameraActivity.this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                    return;
                }

                client.getLastLocation().addOnSuccessListener(CameraActivity.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if(location != null){
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            userLatitude.setText("Latitude : " + latitude);
                            userLongitude.setText("Longitude : " + longitude);
                        }
                    }
                });

                try{
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), photoURI);
                    myPicture.setImageBitmap(bitmap);
                }catch(IOException e){
                    Log.d(CAMERA_LOG, "Exception : " + e.toString());
                }

                btnTakePicture.setVisibility(View.INVISIBLE);
                btnCancel.setVisibility(VISIBLE);
                btnValidatePicture.setVisibility(VISIBLE);
                userLatitude.setVisibility(VISIBLE);
                userLongitude.setVisibility(VISIBLE);

                //TODO : Afficher ce TextView si pas de géolocalisation (pour Maxence)
                //noGeoloc.setVisibility(VISIBLE);
            }
        }
    }

    private void dispatchPictureTakerAction() {
        Intent takePic = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePic.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            photoFile = createPhotoFile();

            if(photoFile != null){
                photoURI = FileProvider.getUriForFile(CameraActivity.this, "com.esgi.picturehunt.fileprovider", photoFile);
                takePic.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePic, 1);
            }
        }
    }

    private File createPhotoFile() {
        String name = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File storageDir = getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = null;

        try {
            image = File.createTempFile(name, ".jpg", storageDir);
        } catch (IOException e) {
            Log.d(CAMERA_LOG, "Exception : " + e.toString());
        }
        return image;
    }

    private void addPhotoToHunt(){
        PhotoToHunt photoToHunt = new PhotoToHunt(myFirebaseAuth.getUser().getUid(), image, latitude, longitude);

        myFirebaseDatabase.getDatabaseReference().child(ID).setValue(photoToHunt);

        Toast.makeText(CameraActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
    }

    private void uploadPhoto() {
        if(photoURI != null){
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading ...");
            progressDialog.show();

            final StorageReference ref = myFirebaseStorage.getStorageReference().child("images/photosToHunt/" + ID);
            ref.putFile(photoURI)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    image = uri.toString();
                                    addPhotoToHunt();
                                }
                            });
                            progressDialog.dismiss();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(CameraActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                            progressDialog.setMessage("Uploaded " + (int)progress + "%");
                        }
                    });
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Intent intentSelected = null;

                    switch (item.getItemId()) {
                        case R.id.nav_home:
                            intentSelected = new Intent(CameraActivity.this, MainActivity.class);
                            break;
                        case R.id.nav_camera:
                            return true;
                        case R.id.nav_settings:
                            intentSelected = new Intent(CameraActivity.this, SettingsActivity.class);
                            break;
                    }
                    startActivity(intentSelected);
                    finish();
                    return true;
                }
            };
}