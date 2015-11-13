package matsko.mykola.mycurrentweather;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.TextView;

import org.joda.time.DateTime;

import java.util.Locale;

public class WeatherForecastFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private WeatherForecastAdapter mAdapter = new WeatherForecastAdapter();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weather_forecast, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.content_view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mAdapter);
    }

    private class WeatherForecastAdapter extends RecyclerView.Adapter<WeatherForecastViewHolder> {


        @Override
        public WeatherForecastViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.weather_forecast_list_item, parent, false);
            return new WeatherForecastViewHolder(view);
        }

        @Override
        public void onBindViewHolder(WeatherForecastViewHolder holder, int position) {
            WeatherForecast item = WeatherData.getsInstance().mWeatherForecastList.get(position);
            holder.dateTextView.setText(new DateTime(item.date * 1000).toString("EEE, d MMM", Locale.US));
            holder.apparentTemperatureMin.setText(String.valueOf(item.apparentTemperatureMin));
            holder.apparentTemperatureMax.setText(String.valueOf(item.apparentTemperatureMax));
            holder.dailyWeatherIcon.setImageResource(WeatherData.getsInstance().getIconResourceID(item.dailyIcon));
            holder.dailySummaryText.setText(item.dailySummary);
        }

        @Override
        public int getItemCount() {
            return WeatherData.getsInstance().mWeatherForecastList.size();
        }
    }

    private class WeatherForecastViewHolder extends RecyclerView.ViewHolder {

        public TextView dateTextView;
        public TextView apparentTemperatureMin;
        public TextView apparentTemperatureMax;
        public ImageView dailyWeatherIcon;
        public TextView dailySummaryText;

        public WeatherForecastViewHolder(View itemView) {
            super(itemView);
            dateTextView = (TextView) itemView.findViewById(R.id.date);
            apparentTemperatureMin = (TextView) itemView.findViewById(R.id.daily_min_temperature);
            apparentTemperatureMax = (TextView) itemView.findViewById(R.id.daily_max_temperature);
            dailyWeatherIcon = (ImageView) itemView.findViewById(R.id.weather_icon);
            dailySummaryText = (TextView) itemView.findViewById(R.id.daily_summary);
        }
    }
}
