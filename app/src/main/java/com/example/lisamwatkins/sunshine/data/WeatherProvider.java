package com.example.lisamwatkins.sunshine.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.lisamwatkins.sunshine.utilities.SunshineDateUtils;

/**
 * Created by lisa.watkins on 9/26/2017.
 */

public class WeatherProvider extends ContentProvider{
    private WeatherDbHelper mOpenHelper;
    public static final int CODE_WEATHER = 100;
    public static final int CODE_WEATHER_WITH_DATE = 101;
    public static final String TABLE_NAME = WeatherContract.WeatherEntry.TABLE_NAME;
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    public static UriMatcher buildUriMatcher(){
        final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = WeatherContract.CONTENT_AUTHORITY;
        uriMatcher.addURI(authority, WeatherContract.PATH_WEATHER, CODE_WEATHER);
        uriMatcher.addURI(authority, WeatherContract.PATH_WEATHER + "/#", CODE_WEATHER_WITH_DATE);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        mOpenHelper = new WeatherDbHelper(context);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        int match = sUriMatcher.match(uri);
        Cursor retCursor;

        switch(match){
            case CODE_WEATHER:
                retCursor = db.query(TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case CODE_WEATHER_WITH_DATE:
                String normalizedUtcDateString = uri.getLastPathSegment();
                String[] selectionArguments = new String[]{normalizedUtcDateString};
                retCursor = db.query(TABLE_NAME,
                        projection,
                        WeatherContract.WeatherEntry.COLUMN_DATE + " = ? ",
                        selectionArguments,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);

        if(match == CODE_WEATHER){
            db.beginTransaction();
            int rowsInserted = 0;

            try{
                for(ContentValues value : values){
                    long weatherDate = value.getAsLong(WeatherContract.WeatherEntry.COLUMN_DATE);
                    if(!SunshineDateUtils.isDateNormalized(weatherDate)){
                        throw new IllegalArgumentException("Date must be normalized to start.");
                    }

                    long _id = db.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, value);
                    if(_id != -1){
                        rowsInserted++;
                    }
                }
                db.setTransactionSuccessful();
            }finally {
                db.endTransaction();
            }
            if(rowsInserted > 0){
                getContext().getContentResolver().notifyChange(uri, null);
            }
            return rowsInserted;
        }
        else {
            return super.bulkInsert(uri, values);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        int rowsDeleted = 0;
        if(null == selection) selection = "1";

        if(match == CODE_WEATHER){
            rowsDeleted = db.delete(WeatherContract.WeatherEntry.TABLE_NAME,
                    selection,
                    selectionArgs);
        }
        else{
            throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if(rowsDeleted != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
