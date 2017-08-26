package com.example.lisamwatkins.sunshine;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.lisamwatkins.sunshine.data.SunshinePreferences;
import com.example.lisamwatkins.sunshine.utilities.NetworkUtils;
import com.example.lisamwatkins.sunshine.utilities.OpenWeatherJsonUtils;
import com.example.lisamwatkins.sunshine.utilities.SunshineWeatherUtils;

import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private TextView mWeatherView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);

        mWeatherView = (TextView) findViewById(R.id.tv_weather_data);
    }

    private void loadWeatherData(){
        String location = SunshinePreferences.getPreferredWeatherLocation(this);
        new FetchWeatherTask().execute(location);
    }

    public class FetchWeatherTask extends AsyncTask<String, Void, String[]>{
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
            if(weatherData != null){
                for (String weather : weatherData){
                    mWeatherView.append(weather + "\n\n\n");
                }
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
        if(item.getItemId() == R.id.action_refresh){
            mWeatherView.setText("");
            loadWeatherData();
        }
        return super.onOptionsItemSelected(item);
    }
}
