package matsko.mykola.mycurrentweather;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.joda.time.DateTime;

/**
 * Created by Asme on 13.10.2015.
 */
public class CurrentWeatherFragment extends Fragment {

    private TextView temperatureText;
    private TextView sunriseText;
    private TextView sunsetText;
    private ImageView currentIcon;
    private TextView currentDescription;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_current_weather, container, false);
        findIDs(v);

        if (WeatherData.getsInstance().getCurrentTemperature() > 0) {
            temperatureText.setText("+" + String.valueOf(WeatherData.getsInstance().getCurrentTemperature()) + "\u00b0C");
        } else {
            temperatureText.setText(String.valueOf(WeatherData.getsInstance().getCurrentTemperature()) + "\u00b0C");
        }
        sunriseText.setText(new DateTime(WeatherData.getsInstance().getSunrise()).toString("HH:mm"));
        sunsetText.setText(new DateTime(WeatherData.getsInstance().getSunset()).toString("HH:mm"));
        currentIcon.setImageResource(WeatherData.getsInstance().getIconResourceID(WeatherData.getsInstance().getCurrentIcon()));
        currentDescription.setText(WeatherData.getsInstance().getCurrentDescription());
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        if ((getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) ||
                (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE
                        && ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) ==
                        Configuration.SCREENLAYOUT_SIZE_NORMAL))) {
            FloatingActionButton forecastButton = (FloatingActionButton) view.findViewById(R.id.fab);
            if (forecastButton != null) {
                forecastButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().getSupportFragmentManager().beginTransaction()
//                                .replace(R.id.single_fragment, new WeatherForecastFragment(), WeatherData.FRAGMENT_TAG)
                                .replace(R.id.current_fragment, new WeatherForecastFragment())
                                .addToBackStack(null).commit();
                    }
                });
            }
        }
    }

    private void findIDs(View v) {
        temperatureText = (TextView) v.findViewById(R.id.temperature);
        sunriseText = (TextView) v.findViewById(R.id.sunrise);
        sunsetText = (TextView) v.findViewById(R.id.sunset);
        currentIcon = (ImageView) v.findViewById(R.id.current_icon);
        currentDescription = (TextView) v.findViewById(R.id.current_description);
    }
}
