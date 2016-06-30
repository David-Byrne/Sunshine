package com.example.android.sunshine.app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.sunshine.app.data.WeatherContract;
import com.example.android.sunshine.app.data.WeatherContract.WeatherEntry;

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private final String LOG_TAG = DetailFragment.class.getSimpleName();
    static final String DETAIL_URI = "URI";
    private String forecastStr;
    private static final int DETAIL_LOADER = 0;
    private Uri uri;
    private static final String[] DETAIL_COLUMNS = {
            WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID,
            WeatherEntry.COLUMN_DATE,
            WeatherEntry.COLUMN_SHORT_DESC,
            WeatherEntry.COLUMN_MAX_TEMP,
            WeatherEntry.COLUMN_MIN_TEMP,
            WeatherEntry.COLUMN_HUMIDITY,
            WeatherEntry.COLUMN_PRESSURE,
            WeatherEntry.COLUMN_WIND_SPEED,
            WeatherEntry.COLUMN_DEGREES,
            WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING
    };
    // these constants correspond to the projection defined above, and must change if the projection changes
    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_WEATHER_HUMIDITY = 5;
    public static final int COL_WEATHER_PRESSURE = 6;
    public static final int COL_WEATHER_WIND_SPEED = 7;
    public static final int COL_WEATHER_DEGREES = 8;
    public static final int COL_WEATHER_CONDITION_ID = 9;

    private ShareActionProvider shareActionProvider;

    private ImageView iconView;
    private TextView friendlyDateView;
    private TextView dateView;
    private TextView descriptionView;
    private TextView highTempView;
    private TextView lowTempView;
    private TextView humidityView;
    private TextView windView;
    private TextView pressureView;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            uri = arguments.getParcelable(DetailFragment.DETAIL_URI);
        }
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        iconView = (ImageView) rootView.findViewById(R.id.detail_icon);
        dateView = (TextView) rootView.findViewById(R.id.detail_date_textview);
        friendlyDateView = (TextView) rootView.findViewById(R.id.detail_day_textview);
        descriptionView = (TextView) rootView.findViewById(R.id.detail_forecast_textview);
        highTempView = (TextView) rootView.findViewById(R.id.detail_high_textview);
        lowTempView = (TextView) rootView.findViewById(R.id.detail_low_textview);
        humidityView = (TextView) rootView.findViewById(R.id.detail_humidity_textview);
        windView = (TextView) rootView.findViewById(R.id.detail_wind_textview);
        pressureView = (TextView) rootView.findViewById(R.id.detail_pressure_textview);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detail_fragment, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        if (forecastStr != null) {
            shareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    private Intent createShareForecastIntent(){
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, forecastStr +" "+ getString(R.string.hashtag));
        return shareIntent;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if ( null != uri ) {
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    getActivity(),
                    uri,
                    DETAIL_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {

            int weatherId = data.getInt(COL_WEATHER_CONDITION_ID);
            iconView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));

            long date = data.getLong(COL_WEATHER_DATE);
            String friendlyDateText = Utility.getDayName(getActivity(), date);
            String dateText = Utility.getFormattedMonthDay(getActivity(), date);
            friendlyDateView.setText(friendlyDateText);
            dateView.setText(dateText);

            String description = data.getString(COL_WEATHER_DESC);
            descriptionView.setText(description);

            iconView.setContentDescription(description);

            boolean isMetric = Utility.isMetric(getActivity());

            double high = data.getDouble(COL_WEATHER_MAX_TEMP);
            String highString = Utility.formatTemperature(getActivity(), high);
            highTempView.setText(highString);

            double low = data.getDouble(COL_WEATHER_MIN_TEMP);
            String lowString = Utility.formatTemperature(getActivity(), low);
            lowTempView.setText(lowString);

            float humidity = data.getFloat(COL_WEATHER_HUMIDITY);
            humidityView.setText(getActivity().getString(R.string.format_humidity, humidity));

            float windSpeedStr = data.getFloat(COL_WEATHER_WIND_SPEED);
            float windDirStr = data.getFloat(COL_WEATHER_DEGREES);
            windView.setText(Utility.getFormattedWind(getActivity(), windSpeedStr, windDirStr));

            float pressure = data.getFloat(COL_WEATHER_PRESSURE);
            pressureView.setText(getActivity().getString(R.string.format_pressure, pressure));

            // We still need this for the share intent
            forecastStr = String.format("%s - %s - %s/%s", dateText, description, high, low);

            // If onCreateOptionsMenu has already happened, we need to update the share intent now.
            if (shareActionProvider != null) {
                shareActionProvider.setShareIntent(createShareForecastIntent());
            }
        }
    }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {/*do nothing*/}

    public void onLocationChanged(String location){
        //Uri newUri = uri;
        if (uri != null) {
            long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
            Uri updatedUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(location, date);
            uri = updatedUri;
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }
}