package com.esgi.picturehunt;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class ViewHolder extends RecyclerView.ViewHolder {
    public static final String REFERENCE_PHOTOS_TO_HUNT = "photosToHunt";

    public static int PHOTO_PIXELS_X = 300;
    public static int PHOTO_PIXELS_Y = 200;

    View mView;
    TextView mUserID;
    TextView mDistance;
    ImageView mImage, mImageView;

    public ViewHolder(View itemView){
        super(itemView);

        mView = itemView;

        mUserID = mView.findViewById(R.id.userID);
        mDistance = mView.findViewById(R.id.distance);
        mImage = mView.findViewById(R.id.image);
        mImageView = mView.findViewById(R.id.loadingImageView);
    }

    private static String getDistance(Location userLocation, PhotoToHunt photoToHunt) {
        Location photoLocation = new Location("photoLocation");
        photoLocation.setLatitude(photoToHunt.getLatitude());
        photoLocation.setLongitude(photoToHunt.getLongitude());

        float distanceM = userLocation.distanceTo(photoLocation);

        if ( distanceM < 1000 )
            return "Moins d'1 km";

        int distanceKm = Math.round(distanceM/1000);

        return distanceKm + "km";
    }

    private void setUserName(String userId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUserID.setText((String)((HashMap)(dataSnapshot.getValue())).get("displayName"));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void setDetails(final Context context, final PhotoToHunt photoToHunt, Location userLocation) {
        final Bundle bundle = new Bundle();
        bundle.putSerializable(REFERENCE_PHOTOS_TO_HUNT, photoToHunt);
        final String distance = getDistance(userLocation, photoToHunt);
        setUserName(photoToHunt.getUserId());
        mDistance.setText(distance);
        Glide.with(context).load(R.drawable.loading).into(mImageView);
        Picasso.get().load(photoToHunt.getImage()).resize(PHOTO_PIXELS_X,PHOTO_PIXELS_Y).into(mImage);

        mImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppCompatActivity activity = (AppCompatActivity) v.getContext();
                Fragment fragment = new DetailsFragment();
                fragment.setArguments(bundle);
                activity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }
}
