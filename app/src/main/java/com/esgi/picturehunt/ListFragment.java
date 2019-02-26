package com.esgi.picturehunt;


import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class ListFragment extends Fragment {

    public static final String REFERENCE_PHOTOS_TO_HUNT = "photosToHunt";

    private RecyclerView recyclerView;
    private MyFirebaseDatabase myFirebaseDatabase;
    private FusedLocationProviderClient client;
    private Location userLocation;
    private FirebaseUser user;
    private TextView noPhoto;

    public ListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        user = FirebaseAuth.getInstance().getCurrentUser();
        noPhoto = view.findViewById(R.id.noPhoto);

        client = LocationServices.getFusedLocationProviderClient(getActivity());
        myFirebaseDatabase = new MyFirebaseDatabase(REFERENCE_PHOTOS_TO_HUNT);

        if(ActivityCompat.checkSelfPermission(
                getContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return;
        }

        client.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                userLocation = location;
                FirebaseRecyclerAdapter<PhotoToHunt, ViewHolder> firebaseRecyclerAdapter =
                        new FirebaseRecyclerAdapter<PhotoToHunt, ViewHolder>(
                                PhotoToHunt.class,
                                R.layout.row_photo_to_hunt,
                                ViewHolder.class,
                                myFirebaseDatabase.getDatabaseReference()
                        ) {

                            public void populateViewHolder(final ViewHolder viewHolder, final PhotoToHunt photoToHunt, int position) {
                                if ( userLocation == null ) {
                                    Toast.makeText(getContext(), "Veuillez activer la géolocalisation", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    final float distanceM = getDistanceM(userLocation, photoToHunt);
                                    FirebaseDatabase.getInstance().getReference("users").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            Map userData = (Map)dataSnapshot.getValue();
                                            long userRadius = (long)(userData.get("radius"));
                                            if ( distanceM < userRadius*1000 ) {
                                                viewHolder.setDetails(getContext(), photoToHunt, distanceM);
                                                noPhoto.setVisibility(View.GONE);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                            }
                        };
                recyclerView.setAdapter(firebaseRecyclerAdapter);
            }
        });
    }

    private static float getDistanceM(Location userLocation, PhotoToHunt photoToHunt) {
        Location photoLocation = new Location("photoLocation");
        photoLocation.setLatitude(photoToHunt.getLatitude());
        photoLocation.setLongitude(photoToHunt.getLongitude());

        return userLocation.distanceTo(photoLocation);
    }
}