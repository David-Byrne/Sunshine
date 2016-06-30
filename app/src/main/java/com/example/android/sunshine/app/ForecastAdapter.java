package com.example.android.sunshine.app;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class ForecastAdapter extends CursorAdapter {

    private final int VIEW_TYPE_TODAY = 0;
    private final int VIEW_TYPE_FUTURE_DAY = 1;
    private boolean useTodayLayout = false;

    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public int getViewTypeCount(){
        return 2;
    }

    public void setUseTodayLayout(boolean use){
        useTodayLayout = use;
    }

    @Override
    public int getItemViewType(int position){
        return ((position == 0) && (useTodayLayout)) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;
        if(viewType == VIEW_TYPE_TODAY) {
            layoutId = R.layout.list_item_forecast_today;
        }
        else {
            layoutId = R.layout.list_item_forecast;
        }

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder holder = (ViewHolder) view.getTag();

        int weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID);
        switch(getItemViewType(cursor.getPosition())){
            case VIEW_TYPE_TODAY: {
                holder.iconView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));
                break;
            }
            case VIEW_TYPE_FUTURE_DAY:{
                holder.iconView.setImageResource(Utility.getIconResourceForWeatherCondition(weatherId));
            }
        }


        Long unixTime = cursor.getLong(ForecastFragment.COL_WEATHER_DATE);
        holder.dateView.setText( Utility.getFriendlyDayString(context, unixTime) );

        String weather = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
        holder.descriptionView.setText(weather);

        holder.iconView.setContentDescription(weather);//for accessibility

        double high = cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
        holder.highTempView.setText(Utility.formatTemperature(context, high));

        double low = cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
        holder.lowTempView.setText(Utility.formatTemperature(context, low));

    }

    public static class ViewHolder {
        public final ImageView iconView;
        public final TextView dateView;
        public final TextView descriptionView;
        public final TextView highTempView;
        public final TextView lowTempView;

        public ViewHolder(View view){
            iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            descriptionView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            highTempView = (TextView) view.findViewById(R.id.list_item_high_textview);
            lowTempView = (TextView) view.findViewById(R.id.list_item_low_textview);
        }

    }
}