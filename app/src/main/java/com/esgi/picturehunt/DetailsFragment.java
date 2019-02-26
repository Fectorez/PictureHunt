package com.esgi.picturehunt;


import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;
import static android.os.Environment.getExternalStoragePublicDirectory;

/**
 * A simple {@link Fragment} subclass.
 */
public class DetailsFragment extends Fragment {
    public static final String REFERENCE_PHOTOS_TO_HUNT = "photosToHunt";
    public static final String CAMERA_LOG = "CAMERA_LOG";
    public static int PHOTO_PIXELS_X = 300;
    public static int PHOTO_PIXELS_Y = 200;

    private List<PhotoAttributes> attributesPhotoHunted = new ArrayList<>();
    private List<PhotoAttributes> attributesPhotoToHunt = new ArrayList<>();
    private ImageView imageToHunt, imageHunted;
    private MyFirebaseDatabase myFirebaseDatabase;
    private MyFirebaseStorage myFirebaseStorage;
    private Button play, validate, cancel;
    private FusedLocationProviderClient client;
    private double longitude, latitude;
    private Uri photoURI;
    private String ID, image, result;

    public DetailsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_details, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = getArguments();
        final PhotoToHunt photoToHunt = (PhotoToHunt)bundle.getSerializable(REFERENCE_PHOTOS_TO_HUNT);

        myFirebaseDatabase = new MyFirebaseDatabase(REFERENCE_PHOTOS_TO_HUNT);
        myFirebaseStorage = new MyFirebaseStorage();
        imageToHunt = view.findViewById(R.id.imageToHunt);
        imageHunted = view.findViewById(R.id.imageHunted);
        play = view.findViewById(R.id.play);
        validate = view.findViewById(R.id.validatePhoto);
        cancel = view.findViewById(R.id.cancelPhoto);
        client = LocationServices.getFusedLocationProviderClient(getActivity());

        attributesPhotoToHunt = photoToHunt.getAttributes();

        Picasso.get().load(photoToHunt.getImage()).resize(300, 200).into(imageToHunt);

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchPictureTakerAction();
            }
        });

        validate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ID = myFirebaseDatabase.getDatabaseReference().push().getKey();
                uploadPhoto();

                //TODO: Vérifier la géolocalisation et comparer les listes attributesPhotoToHunt et attributesPhotoHunted pour voir si ça correspond
            }
        });
    }

    private void dispatchPictureTakerAction() {
        Intent takePic = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePic.resolveActivity(getActivity().getPackageManager()) != null) {
            File photoFile = null;
            photoFile = createPhotoFile();

            if(photoFile != null){
                photoURI = FileProvider.getUriForFile(getContext(), "com.esgi.picturehunt.fileprovider", photoFile);
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

    private void uploadPhoto() {
        if(photoURI != null){
            final ProgressDialog progressDialog = new ProgressDialog(getContext());

            progressDialog.setTitle("Uploading ...");
            progressDialog.show();

            final StorageReference ref = myFirebaseStorage.getStorageReference().child("images/photosHunted/" + ID);
            ref.putFile(photoURI)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    image = uri.toString();
                                    CloudVisionManager cvm = new CloudVisionManager(getContext());
                                    cvm.execute(image);

                                    try{
                                        Thread.sleep(5000);
                                    } catch(InterruptedException e){
                                        e.printStackTrace();
                                    }

                                    result = cvm.getResult();
                                    Toast.makeText(getContext(), "Uploaded", Toast.LENGTH_SHORT).show();
                                    getAttributes();
                                }
                            });
                            progressDialog.dismiss();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(getContext(), "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            progressDialog.setMessage("Uploaded " + (int)progress + "%");
                        }
                    });
        }
    }

    private void getAttributes(){
        String id, desc;
        double score, topicality;

        try {
            JSONObject jsonObject = new JSONObject(result);
            JSONArray jsonArray = jsonObject.getJSONArray("responses");

            String response = jsonArray.toString().substring(1, jsonArray.toString().length() - 1);

            JSONObject myJsonObject = new JSONObject(response);
            JSONArray myJsonArray = myJsonObject.getJSONArray("labelAnnotations");

            for (int i = 0; i < myJsonArray.length(); i++) {
                JSONObject attribute = myJsonArray.getJSONObject(i);

                id = attribute.getString("mid");
                desc = attribute.getString("description");
                score = attribute.getDouble("score");
                topicality = attribute.getDouble("topicality");

                PhotoAttributes attr = new PhotoAttributes(id, desc, score, topicality);

                attributesPhotoHunted.add(attr);
            }
        } catch (JSONException e) {
            Log.e("My App", "Could not parse malformed JSON");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            if(requestCode == 1) {
                if(ActivityCompat.checkSelfPermission(
                        getContext(),
                        android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                    return;
                }

                client.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if(location != null){
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            Geocoder gcd = new Geocoder(getContext(), Locale.getDefault());
                        }
                    }
                });

                Picasso.get().load(photoURI).resize(PHOTO_PIXELS_X,PHOTO_PIXELS_Y).into(imageHunted);

                validate.setVisibility(View.VISIBLE);
                cancel.setVisibility(View.VISIBLE);
                play.setVisibility(View.INVISIBLE);
            }
        }
    }
}
