package com.example.lisamwatkins.sunshine;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.AsyncTaskLoader;
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

import org.w3c.dom.Text;

import java.net.URL;

public class MainActivity extends AppCompatActivity implements
        ForecastAdapter.ForecastAdapterOnClickHandler,
        LoaderManager.LoaderCallbacks<String[]>{

    private TextView mErrorMessageTextView;
    private ProgressBar mLoadingProgressBar;
    private RecyclerView mForecastRecyclerView;
    private ForecastAdapter mForecastAdapter;
    private String TAG = MainActivity.class.getSimpleName();
    private static final int LOADER_ID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);

        mErrorMessageTextView = (TextView) findViewById(R.id.error_message);
        mLoadingProgressBar = (ProgressBar) findViewById(R.id.loading_progress);
        mForecastRecyclerView = (RecyclerView) findViewById(R.id.rv_forecast);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mForecastRecyclerView.setLayoutManager(layoutManager);
        mForecastRecyclerView.setHasFixedSize(true);

        mForecastAdapter = new ForecastAdapter(this);
        mForecastRecyclerView.setAdapter(mForecastAdapter);

        int loaderId = LOADER_ID;

        LoaderManager.LoaderCallbacks<String[]> callbacks = MainActivity.this;

        Bundle bundleForLoader = null;

        getSupportLoaderManager().initLoader(loaderId, bundleForLoader, callbacks);
    }

    @Override
    public Loader<String[]> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<String[]>(this) {
            String[] mWeatherData;

            @Override
            protected void onStartLoading() {
                if(mWeatherData != null){
                    deliverResult(mWeatherData);
                }
                else{
                    mLoadingProgressBar.setVisibility(View.VISIBLE);
                    forceLoad();
                }
            }

            @Override
            public String[] loadInBackground() {
                String locationQuery = SunshinePreferences
                        .getPreferredWeatherLocation(MainActivity.this);

                URL weatherRequest = NetworkUtils.buildUrl(locationQuery);

                try{
                    String jsonWeatherResponse = NetworkUtils.getResponseFromHttpUrl(weatherRequest);
                    String[] simpleJsonWeatherData = OpenWeatherJsonUtils
                            .getSimpleWeatherStringsFromJson(MainActivity.this, jsonWeatherResponse);

                    return simpleJsonWeatherData;
                }catch (Exception e){
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public void deliverResult(String[] data) {
                mWeatherData = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<String[]> loader, String[] data) {
        mLoadingProgressBar.setVisibility(View.INVISIBLE);
        mForecastAdapter.setWeatherData(data);
        if(data == null){
            showErrorView();
        }
        else{
            showWeatherDataView();
        }
    }

    @Override
    public void onLoaderReset(Loader<String[]> loader) {
        // not implementing yet
    }

    private void invalidatedata(){
        mForecastAdapter.setWeatherData(null);
    }

    private void openLocationInMap(){
        String addressString = "1600 Amiptheatre Parkway, CA";
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
        mErrorMessageTextView.setVisibility(View.INVISIBLE);
        mForecastRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showErrorView(){
        mErrorMessageTextView.setVisibility(View.VISIBLE);
        mForecastRecyclerView.setVisibility(View.INVISIBLE);
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
        if(id == R.id.action_refresh){
            invalidatedata();
            getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
            return true;
        }

        if(id == R.id.action_map){
            openLocationInMap();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
