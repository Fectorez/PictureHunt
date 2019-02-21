package com.esgi.picturehunt;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * A simple {@link Fragment} subclass.
 */
public class DetailsFragment extends Fragment {
    public static final String REFERENCE_PHOTOS_TO_HUNT = "photosToHunt";
    private TextView user;
    private ImageView image;
    private Button play;

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

        user = view.findViewById(R.id.user);
        image = view.findViewById(R.id.image);
        play = view.findViewById(R.id.play);

        user.setText(photoToHunt.getUserId());
        Picasso.get().load(photoToHunt.getImage()).resize(300, 200).into(image);
    }
}