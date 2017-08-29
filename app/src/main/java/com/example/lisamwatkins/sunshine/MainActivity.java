package com.example.lisamwatkins.sunshine;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.nfc.Tag;
import android.os.AsyncTask;
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

public class MainActivity extends AppCompatActivity implements ForecastAdapter.ForecastAdapterOnClickHandler {
    private TextView mErrorMessageTextView;
    private ProgressBar mLoadingProgressBar;
    private RecyclerView mForecastRecyclerView;
    private ForecastAdapter mForecastAdapter;
    private String TAG = MainActivity.class.getSimpleName();
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

        loadWeatherData();
    }

    @Override
    public void onClick(String weatherForDay) {
        Class targetActivity = DetailActivity.class;
        Context context = this;
        Intent intentToStartDetailActivity = new Intent(context, targetActivity);

        intentToStartDetailActivity.putExtra(Intent.EXTRA_TEXT, weatherForDay);

        startActivity(intentToStartDetailActivity);
    }

    private void loadWeatherData(){
        String location = SunshinePreferences.getPreferredWeatherLocation(this);
        new FetchWeatherTask().execute(location);
    }

    private void showWeatherDataView(){
        mErrorMessageTextView.setVisibility(View.INVISIBLE);
        mForecastRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showErrorView(){
        mErrorMessageTextView.setVisibility(View.VISIBLE);
        mForecastRecyclerView.setVisibility(View.INVISIBLE);
    }

    public void showMap(){
        String addressString = "1600 Ampitheatre Parkway, CA";
        Uri geoLocation = Uri.parse("geo:0,0?q=" + addressString);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Log.d(TAG, "Couldn't call " + geoLocation.toString()
                    + ", no receiving apps installed!");
        }
    }

    public class FetchWeatherTask extends AsyncTask<String, Void, String[]>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String[] doInBackground(String... params) {
            if (params.length == 0){
                return null;
            }

            String location = params[0];

            URL weatherRequest = NetworkUtils.buildUrl(location);

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
        protected void onPostExecute(String[] weatherData) {
            mLoadingProgressBar.setVisibility(View.INVISIBLE);

            if(weatherData != null){
                showWeatherDataView();
                mForecastAdapter.setWeatherData(weatherData);
            }
            else{
                showErrorView();
            }
        }
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
            mForecastAdapter = null;
            loadWeatherData();
            return true;
        }

        if(id == R.id.action_map){
            showMap();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
