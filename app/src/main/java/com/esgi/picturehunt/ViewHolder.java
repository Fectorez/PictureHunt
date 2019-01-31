package com.esgi.picturehunt;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class ViewHolder extends RecyclerView.ViewHolder {

    View mView;

    public ViewHolder(View itemView){
        super(itemView);

        mView = itemView;
    }

    public void setDetails(Context context, String userID, double latitude, double longitude, String image) {
        TextView mUserID = mView.findViewById(R.id.userID);
        TextView mLatitude = mView.findViewById(R.id.latitude);
        TextView mLongitude = mView.findViewById(R.id.longitude);
        ImageView mImage = mView.findViewById(R.id.image);

        mUserID.setText(userID);
        mLatitude.setText("Latitude : " + Double.toString(latitude));
        mLongitude.setText("Longitude : " + Double.toString(longitude));
        Picasso.get().load(image).into(mImage);
    }
}
