package com.example.lisamwatkins.sunshine;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.lisamwatkins.sunshine.data.WeatherContract;
import com.example.lisamwatkins.sunshine.utilities.SunshineDateUtils;
import com.example.lisamwatkins.sunshine.utilities.SunshineWeatherUtils;

/**
 * Created by lisamwatkins on 8/29/17.
 */

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder> {
    private ForecastAdapterOnClickHandler mClickHandler;
    private Context mContext;
    private Cursor mCursor;
    // Defines listener
    public interface ForecastAdapterOnClickHandler{
        public void onClick(Long weatherForDay);
    }

    public ForecastAdapter(ForecastAdapterOnClickHandler clickHandler, @NonNull Context context){
        mClickHandler = clickHandler;
        mContext = context;
    }

    @Override
    public ForecastViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.weather_list_item, viewGroup, false);
        view.setFocusable(true);

        return new ForecastViewHolder(view);
    }

    // populates each item with data
    @Override
    public void onBindViewHolder(ForecastViewHolder holder, int position) {
        mCursor.moveToPosition(position);

        long dateInMillis = mCursor.getLong(MainActivity.INDEX_WEATHER_DATE);
        String dateString = SunshineDateUtils.getFriendlyDateString(mContext, dateInMillis, false);
        holder.dateView.setText(dateString);

        int weatherId = mCursor.getInt(MainActivity.INDEX_WEATHER_CONDITION_ID);
        int weatherImageId;
        weatherImageId = SunshineWeatherUtils.getSmallArtResourceIdForWeatherCondition(weatherId);
        holder.iconView.setImageResource(weatherImageId);

        String description = SunshineWeatherUtils.getStringForWeatherCondition(mContext, weatherId);
        holder.descriptionView.setText(description);

        double highInCelsius = mCursor.getDouble(MainActivity.INDEX_WEATHER_MAX_TEMP);
        double lowInCelsius = mCursor.getDouble(MainActivity.INDEX_WEATHER_MIN_TEMP);

        String highString = SunshineWeatherUtils.formatTemperature(mContext, highInCelsius);
        String lowString = SunshineWeatherUtils.formatTemperature(mContext, lowInCelsius);

        holder.highTempView.setText(highString);
        holder.lowTempView.setText(lowString);
    }

    @Override
    public int getItemCount() {
        if(mCursor == null) return 0;
        return mCursor.getCount();
    }

    public void swapCursors(Cursor cursor){
        mCursor = cursor;
        notifyDataSetChanged();
    }

    class ForecastViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        final TextView dateView;
        final TextView descriptionView;
        final TextView highTempView;
        final TextView lowTempView;
        final ImageView iconView;

        public ForecastViewHolder(View view){
            super(view);
            dateView = (TextView) view.findViewById(R.id.date);
            iconView = (ImageView) view.findViewById(R.id.weather_icon);
            descriptionView = (TextView) view.findViewById(R.id.weather_description);
            lowTempView = (TextView) view.findViewById(R.id.low_temperature);
            highTempView = (TextView) view.findViewById(R.id.high_temperature);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);

            long dateInMillis = mCursor.getLong(MainActivity.INDEX_WEATHER_DATE);
            mClickHandler.onClick(dateInMillis);
        }
    }
}
