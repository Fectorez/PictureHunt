package com.esgi.picturehunt;

import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class ViewHolder extends RecyclerView.ViewHolder {
    public static int PHOTO_PIXELS_X = 300;
    public static int PHOTO_PIXELS_Y = 200;

    View mView;
    TextView mUserID;
    TextView mDistance;
    ImageView mImage;

    public ViewHolder(View itemView){
        super(itemView);

        mView = itemView;

        mUserID = mView.findViewById(R.id.userID);
        mDistance = mView.findViewById(R.id.distance);
        mImage = mView.findViewById(R.id.image);
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

    public void setDetails(Context context, PhotoToHunt photoToHunt, Location userLocation) {
        String distance = getDistance(userLocation, photoToHunt);
        setUserName(photoToHunt.getUserId());
        mDistance.setText(distance);
        Picasso.get().load(photoToHunt.getImage()).resize(PHOTO_PIXELS_X,PHOTO_PIXELS_Y).into(mImage);
    }
}
