package com.esgi.picturehunt;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;
import static android.os.Environment.getExternalStoragePublicDirectory;
import static android.view.View.VISIBLE;

/**
 * A simple {@link Fragment} subclass.
 */
public class DetailsFragment extends Fragment {
    public static final String REFERENCE_PHOTOS_TO_HUNT = "photosToHunt";
    public static final String CAMERA_LOG = "CAMERA_LOG";
    private ImageView imageToHunt, imageHunted;
    private Button play;
    private FusedLocationProviderClient client;
    private double longitude, latitude;

    private Uri photoURI;

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
        PhotoToHunt photoToHunt = (PhotoToHunt)bundle.getSerializable(REFERENCE_PHOTOS_TO_HUNT);

        imageToHunt = view.findViewById(R.id.imageToHunt);
        imageHunted = view.findViewById(R.id.imageHunted);
        play = view.findViewById(R.id.play);
        client = LocationServices.getFusedLocationProviderClient(getActivity());


        Picasso.get().load(photoToHunt.getImage()).resize(300, 200).into(imageToHunt);

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchPictureTakerAction();
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

                try{
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), photoURI);
                    imageHunted.setImageBitmap(bitmap);
                }catch(IOException e){
                    Log.d(CAMERA_LOG, "Exception : " + e.toString());
                }
            }
        }
    }
}