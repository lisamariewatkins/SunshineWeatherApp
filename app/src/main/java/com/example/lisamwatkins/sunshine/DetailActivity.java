package com.example.lisamwatkins.sunshine;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.lisamwatkins.sunshine.data.WeatherContract;
import com.example.lisamwatkins.sunshine.utilities.SunshineDateUtils;
import com.example.lisamwatkins.sunshine.utilities.SunshineWeatherUtils;

public class DetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";

    private String mForecast;

    private TextView mDateTextView;
    private TextView mDescriptionTextView;
    private TextView mHighTextView;
    private TextView mLowTextView;
    private TextView mHumidityTextView;
    private TextView mWindTextView;
    private TextView mPressureTextView;

    private Uri mUri;

    public static final String[] DETAIL_FORECAST_PROJECTION = {
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES
    };

    public static final int INDEX_WEATHER_DATE = 0;
    public static final int INDEX_WEATHER_MAX_TEMP = 1;
    public static final int INDEX_WEATHER_MIN_TEMP = 2;
    public static final int INDEX_WEATHER_CONDITION_ID = 3;
    public static final int INDEX_WEATHER_HUMIDITY = 4;
    public static final int INDEX_WEATHER_PRESSURE = 5;
    public static final int INDEX_WEATHER_WIND_SPEED = 6;
    public static final int INDEX_WEATHER_DEGREES = 7;

    private static final int LOADER_ID = 38;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mDateTextView = (TextView) findViewById(R.id.tv_date);
        mDescriptionTextView = (TextView) findViewById(R.id.tv_description);
        mHighTextView = (TextView) findViewById(R.id.tv_max_temp);
        mLowTextView = (TextView) findViewById(R.id.tv_min_temp);
        mHumidityTextView = (TextView) findViewById(R.id.tv_humidity);
        mWindTextView = (TextView) findViewById(R.id.tv_wind_speed);
        mPressureTextView = (TextView) findViewById(R.id.tv_pressure);

        Intent intentThatStartedThisActivity = getIntent();

        mUri = intentThatStartedThisActivity.getData();

        if(mUri == null){
            throw new NullPointerException("Uri is null.");
        }

        getSupportLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail, menu);
        MenuItem menuItem = menu.findItem(R.id.action_share);
        menuItem.setIntent(createShareForecastIntent());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_settings){
            Intent intentToStartSettings = new Intent(this, SettingsActivity.class);
            startActivity(intentToStartSettings);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public Intent createShareForecastIntent(){
        Intent shareIntent = ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setText(mForecast + FORECAST_SHARE_HASHTAG)
                .getIntent();
        return shareIntent;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch(id){
            case LOADER_ID:
                return new CursorLoader(this,
                        mUri,
                        DETAIL_FORECAST_PROJECTION,
                        null,
                        null,
                        null);
            default:
                throw new RuntimeException("Loader Not Implemented: " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        boolean cursorHasValidData = false;

        if(data != null && data.moveToFirst()){
            cursorHasValidData = true;
        }

        if(!cursorHasValidData) return;

        long localDateMidnightGmt = data.getLong(INDEX_WEATHER_DATE);
        String dateText = SunshineDateUtils.getFriendlyDateString(this, localDateMidnightGmt, true);
        mDateTextView.setText(dateText);

        int weatherId = data.getInt(INDEX_WEATHER_CONDITION_ID);
        String description = SunshineWeatherUtils.getStringForWeatherCondition(this, weatherId);
        mDescriptionTextView.setText(description);

        double highInCelsius = data.getDouble(INDEX_WEATHER_MAX_TEMP);
        String highString = SunshineWeatherUtils.formatTemperature(this, highInCelsius);
        mHighTextView.setText(highString);

        double lowInCelsius = data.getDouble(INDEX_WEATHER_MIN_TEMP);
        String lowString = SunshineWeatherUtils.formatTemperature(this, lowInCelsius);
        mLowTextView.setText(lowString);

        float humidity = data.getFloat(INDEX_WEATHER_HUMIDITY);
        String humidityString = getString(R.string.format_humidity, humidity);
        mHumidityTextView.setText(humidityString);

        float windSpeed = data.getFloat(INDEX_WEATHER_WIND_SPEED);
        float windDirection = data.getFloat(INDEX_WEATHER_DEGREES);
        String windString = SunshineWeatherUtils.getFormattedWind(this, windSpeed, windDirection);
        mWindTextView.setText(windString);

        float pressure = data.getFloat(INDEX_WEATHER_PRESSURE);
        String pressureString = getString(R.string.format_pressure);
        mPressureTextView.setText(pressureString);

        mForecast = String.format("%s - %s - %s/%s", dateText, description, lowString, highString);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // not implemented yet
    }
}
