package com.example.benjaminlize.sunshine.app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.benjaminlize.sunshine.app.data.WeatherContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    public static String DETAIL_URI;
    private final String LOG_TAG = "DetailFragment";
    private final String HASHTAG_STRING = "#SunshineApp";
    private String mForecast;
    private Uri mUri;

    private static final int DETAIL_LOADER = 1002;

    private static final String[] DETAIL_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID
    };


    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_SHORT_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_WEATHER_HUMIDITY = 5;
    static final int COL_WEATHER_WIND_SPEED = 6;
    static final int COL_WEATHER_PRESSURE = 7;
    static final int COL_WEATHER_CONDITION_ID = 8;

    ShareActionProvider myShareActionProvider;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    public static DetailFragment newInstance(int index){
        DetailFragment f = new DetailFragment();

        Bundle args = new Bundle();
        args.putInt("index",index);
        f.setArguments(args);

        return f;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle args = getArguments();
        if (args != null){
            mUri = args.getParcelable(DetailFragment.DETAIL_URI);
        }
        return inflater.inflate(R.layout.fragment_detail,container,false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        MenuItem menuItem = menu.findItem(R.id.action_item_share);
        myShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        if(mForecast != null){
            myShareActionProvider.setShareIntent(createShareForecastIntent());
        } else {
            Log.d(LOG_TAG, "Share action provider is null");
        }
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                mForecast + HASHTAG_STRING);
        return shareIntent;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        if (mUri != null){
            return new android.support.v4.content.CursorLoader(getActivity(),
                    mUri,
                    DETAIL_COLUMNS,
                    null,
                    null,
                    null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (data.moveToFirst()){

            boolean isMetric = Utility.isMetric(getContext());

            String dateStr = Utility.formatDate(data.getLong(COL_WEATHER_DATE));
            String high = Utility.formatTemperature(getContext(), data.getDouble(COL_WEATHER_MAX_TEMP), isMetric);
            String low = Utility.formatTemperature(getContext(), data.getDouble(COL_WEATHER_MIN_TEMP), isMetric);
            String humidity = data.getString(COL_WEATHER_HUMIDITY);
            String windSpeed = data.getString(COL_WEATHER_WIND_SPEED);
            String pressure = data.getString(COL_WEATHER_PRESSURE);

            String weatherDescriptionStr = data.getString(COL_WEATHER_SHORT_DESC);

            mForecast = String.format("%s - %s - %s/%s", dateStr, weatherDescriptionStr, high, low);

            int weatherId = data.getInt(COL_WEATHER_CONDITION_ID);
            int resourceId = Utility.getArtResourceForWeatherCondition(weatherId);


            ImageView imageView = (ImageView)getView().findViewById(R.id.list_item_icon);
            imageView.setImageResource(resourceId);
            TextView dateStrTv = (TextView)getView().findViewById(R.id.list_item_date_textview);
            dateStrTv.setText(dateStr);
            TextView highTv = (TextView)getView().findViewById(R.id.list_item_high_textview);
            highTv.setText(high);
            TextView minTv = (TextView)getView().findViewById(R.id.list_item_low_textview);
            minTv.setText(low);
            TextView humidityTv = (TextView)getView().findViewById(R.id.list_item_humidity_textview);
            humidityTv.setText("HUMIDITY: " + humidity + "%");
            TextView windSpeedTv = (TextView)getView().findViewById(R.id.list_item_wind_textview);
            windSpeedTv.setText("WIND: " + windSpeed + "km/H NW");
            TextView pressureTv = (TextView)getView().findViewById(R.id.list_item_pressure_textview);
            pressureTv.setText("PERSSURE: " + pressure + "hPa");

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    public void onLocationChanged(String newlocation) {

        Uri uri = mUri;

        if(uri != null){
            long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
            Uri updateUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newlocation,date);
            mUri = updateUri;
            getLoaderManager().restartLoader(DETAIL_LOADER,null,this);
        }
    }


}