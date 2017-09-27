package com.example.lisamwatkins.sunshine;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lisamwatkins.sunshine.data.SunshinePreferences;
import com.example.lisamwatkins.sunshine.utilities.NetworkUtils;
import com.example.lisamwatkins.sunshine.utilities.OpenWeatherJsonUtils;
import com.example.lisamwatkins.sunshine.utilities.SunshineWeatherUtils;
import com.example.lisamwatkins.sunshine.data.WeatherContract;

import org.w3c.dom.Text;

import java.net.URL;

public class MainActivity extends AppCompatActivity implements
        ForecastAdapter.ForecastAdapterOnClickHandler,
        LoaderManager.LoaderCallbacks<Cursor>
        {

    private int mPosition = RecyclerView.NO_POSITION;
    private ProgressBar mLoadingProgressBar;
    private RecyclerView mForecastRecyclerView;
    private ForecastAdapter mForecastAdapter;
    private String TAG = MainActivity.class.getSimpleName();
    private static final int LOADER_ID = 0;

    public static final String[] MAIN_FORECAST_PROJECTION = {
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
    };

    public static final int INDEX_WEATHER_DATE = 0;
    public static final int INDEX_WEATHER_MAX_TEMP = 1;
    public static final int INDEX_WEATHER_MIN_TEMP = 2;
    public static final int INDEX_WEATHER_CONDITION_ID = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);


        mLoadingProgressBar = (ProgressBar) findViewById(R.id.loading_progress);
        mForecastRecyclerView = (RecyclerView) findViewById(R.id.rv_forecast);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mForecastRecyclerView.setLayoutManager(layoutManager);
        mForecastRecyclerView.setHasFixedSize(true);

        mForecastAdapter = new ForecastAdapter(this, this);
        mForecastRecyclerView.setAdapter(mForecastAdapter);

        showLoading();

        int loaderId = LOADER_ID;

        LoaderManager.LoaderCallbacks<Cursor> callbacks = MainActivity.this;

        Bundle bundleForLoader = null;

        getSupportLoaderManager().initLoader(loaderId, bundleForLoader, callbacks);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, final Bundle args) {
        switch(id){
            case LOADER_ID:
                Uri forecastQueryUri = WeatherContract.WeatherEntry.CONTENT_URI;
                String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
                String selection = WeatherContract.WeatherEntry.getSqlSelectForTodayOnwards();

                return new CursorLoader(this,
                        forecastQueryUri,
                        MAIN_FORECAST_PROJECTION,
                        selection,
                        null,
                        sortOrder);
            default:
                throw new RuntimeException("Loader Not Implemented: " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mForecastAdapter.swapCursors(data);
        if(mPosition == RecyclerView.NO_POSITION){
            mPosition = 0;
        }
        mForecastRecyclerView.smoothScrollToPosition(mPosition);
        if(data.getCount() != 0){
            showWeatherDataView();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mForecastAdapter.swapCursors(null);
    }

    public void showLoading(){
        mLoadingProgressBar.setVisibility(View.VISIBLE);
        mForecastRecyclerView.setVisibility(View.INVISIBLE);
    }

    private void openLocationInMap(){
        String addressString = SunshinePreferences.getPreferredWeatherLocation(this);
        Uri geolocation = Uri.parse("geo:0,0?q=" + addressString);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geolocation);

        if(intent.resolveActivity(getPackageManager()) != null){
            startActivity(intent);
        }
    }

    @Override
    public void onClick(String weatherForDay) {
        Class targetActivity = DetailActivity.class;
        Context context = this;
        Intent intentToStartDetailActivity = new Intent(context, targetActivity);

        intentToStartDetailActivity.putExtra(Intent.EXTRA_TEXT, weatherForDay);

        startActivity(intentToStartDetailActivity);
    }

    private void showWeatherDataView(){
        mForecastRecyclerView.setVisibility(View.VISIBLE);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflator = getMenuInflater();
        inflator.inflate(R.menu.forecast, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_map){
            openLocationInMap();
            return true;
        }

        if(id == R.id.action_settings){
            Intent intentToStartSettings = new Intent(this, SettingsActivity.class);
            startActivity(intentToStartSettings);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
