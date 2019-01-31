package com.esgi.picturehunt;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Locale;

/**
 * The configuration screen for the {@link AppWidget AppWidget} AppWidget.
 */
public class AppWidgetConfigureActivity extends Activity {

    private static final String PREFS_NAME = "com.esgi.picturehunt.AppWidget";
    private static final String PREF_PREFIX_KEY = "appwidget_";
    public static final String DEFAULT_RADIUS = "3";
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    SeekBar mAppWidgetSeekBar;
    TextView mSeekBarValue;
    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            final Context context = AppWidgetConfigureActivity.this;

            // When the button is clicked, store the string locally
            String widgetRadius = String.valueOf(mAppWidgetSeekBar.getProgress()+1);
            saveRadiusPref(context, mAppWidgetId, widgetRadius);

            // It is the responsibility of the configuration activity to update the app widget
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            AppWidget.updateAppWidget(context, appWidgetManager, mAppWidgetId);

            // Make sure we pass back the original appWidgetId
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }
    };
    SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if ( mSeekBarValue != null )
                mSeekBarValue.setText(String.valueOf(progress+1));
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {}
    };

    public AppWidgetConfigureActivity() {
        super();
    }

    // Write the prefix to the SharedPreferences object for this widget
    static void saveRadiusPref(Context context, int appWidgetId, String radius) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(PREF_PREFIX_KEY + appWidgetId, radius);
        prefs.apply();
    }

    // Read the prefix from the SharedPreferences object for this widget.
    // If there is no preference saved, get the default from a resource
    static String loadRadiusPref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String radiusPref = prefs.getString(PREF_PREFIX_KEY + appWidgetId, DEFAULT_RADIUS);
        return radiusPref;
    }

    static void deleteRadiusPref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId);
        prefs.apply();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        setContentView(R.layout.app_widget_configure);
        mSeekBarValue = findViewById(R.id.seekBarValue);
        mAppWidgetSeekBar = findViewById(R.id.appwidget_seekBar);
        mAppWidgetSeekBar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
        findViewById(R.id.add_button).setOnClickListener(mOnClickListener);

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        String radiusPref = loadRadiusPref(AppWidgetConfigureActivity.this, mAppWidgetId);
        int radius = Integer.parseInt(radiusPref);
        mAppWidgetSeekBar.setProgress(radius-1);
    }
}

