package matsko.mykola.mycurrentweather;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Asme on 13.10.2015.
 */
public class WeatherData {

    final static String FRAGMENT_TAG = "FORECAST";

    private static WeatherData sInstance = new WeatherData();
    private int currentTemperature;
    private long sunrise;
    private long sunset;
    private String currentIcon;
    private String currentDescription;
    private String CityTitle;
    private List<String> cities = new ArrayList<>();

    public List<String> getCities() {
        return cities;
    }

    public void setCities(String city) {
        this.cities.add(city);
    }



    List<WeatherForecast> mWeatherForecastList = new ArrayList<>();

    private WeatherData() {
    }



    public static WeatherData getsInstance() {
        return sInstance;
    }

    public int getCurrentTemperature() {
        return currentTemperature;
    }

    public String getCityTitle() {
        return CityTitle;
    }

    public void setCityTitle(String cityTitle) {
        CityTitle = cityTitle;
    }

    public void setCurrentTemperature(int currentTemperature) {
        this.currentTemperature = currentTemperature;
    }

    public long getSunrise() {
        return sunrise;
    }

    public void setSunrise(long sunrise) {
        this.sunrise = sunrise;
    }

    public long getSunset() {
        return sunset;
    }

    public void setSunset(long sunset) {
        this.sunset = sunset;
    }

    public String getCurrentIcon() {
        return currentIcon;
    }

    public void setCurrentIcon(String currentIcon) {
        this.currentIcon = currentIcon;
    }

    public String getCurrentDescription() {
        return currentDescription;
    }

    public void setCurrentDescription(String currentDescription) {
        this.currentDescription = currentDescription;
    }

    public int getIconResourceID(String icon) {
        switch (icon) {
            case "clear-day":
                return R.drawable.clear_day;
            case "clear-night":
                return R.drawable.clear_night;
            case "rain":
                return R.drawable.rain;
            case "snow":
                return R.drawable.snow;
            case "sleet":
                return R.drawable.sleet;
            case "wind":
                return R.drawable.wind;
            case "fog":
                return R.drawable.fog;
            case "cloudy":
                return R.drawable.cloudy;
            case "partly-cloudy-day":
                return R.drawable.partly_cloudy_day;
            case "partly-cloudy-night":
                return R.drawable.partly_cloudy_night;
            default:
                return 0;
        }
    }
}
