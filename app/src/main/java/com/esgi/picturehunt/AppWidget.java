package com.esgi.picturehunt;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link AppWidgetConfigureActivity AppWidgetConfigureActivity}
 */
public class AppWidget extends AppWidgetProvider {
    private static Location userLocation;
    private static int radius;
    private static int nbPicturesInThisRadius;
    private static AppWidgetManager appWidgetManager;
    private static int appWidgetId;
    private static Context context;

    static void getNbPicturesInRadius(final Location userLocation) {
        nbPicturesInThisRadius = 0;
        FirebaseDatabase.getInstance().getReference("photosToHunt").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot photoSnapshot : dataSnapshot.getChildren()) {
                    Map photoValues = (HashMap)photoSnapshot.getValue();
                    PhotoToHunt photoToHunt = new PhotoToHunt((String)photoValues.get("userId"), (String)photoValues.get("image"), (double)photoValues.get("latitude"), (double)photoValues.get("longitude"));
                    Log.i("TUTU", photoToHunt.toString());
                    Location photoLocation = new Location("photoLocation");
                    photoLocation.setLatitude(photoToHunt.getLatitude());
                    photoLocation.setLongitude(photoToHunt.getLongitude());
                    float distanceM = userLocation.distanceTo(photoLocation);
                    if ( distanceM <= radius*1000 ) {
                        nbPicturesInThisRadius++;
                        Log.i("TUTU", "distanceM=" + distanceM + "< radius=" + radius*1000);
                    }
                }
                String toDisp = "Dans un rayon de " + radius + "km : " + nbPicturesInThisRadius + " photos";

                // Construct the RemoteViews object
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_widget);
                views.setTextViewText(R.id.appwidget_text, toDisp);

                // Instruct the widget manager to update the widget
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    static void updateAppWidget(Context context_, AppWidgetManager appWidgetManager_,
                                int appWidgetId_) {
        appWidgetManager = appWidgetManager_;
        context = context_;
        appWidgetId = appWidgetId_;
        String widgetValue = AppWidgetConfigureActivity.loadRadiusPref(context, appWidgetId);

        radius = Integer.parseInt(widgetValue);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(context);
        if(ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return;
        }
        client.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                userLocation = location;
                getNbPicturesInRadius(userLocation);

            }
        });


    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            AppWidgetConfigureActivity.deleteRadiusPref(context, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

