package matsko.mykola.mycurrentweather;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.johnhiott.darkskyandroidlib.ForecastApi;
import com.johnhiott.darkskyandroidlib.RequestBuilder;
import com.johnhiott.darkskyandroidlib.models.Request;
import com.johnhiott.darkskyandroidlib.models.WeatherResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static com.google.android.gms.common.api.GoogleApiClient.Builder;
import static com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import static com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;

public class MainActivity extends AppCompatActivity implements ConnectionCallbacks, OnConnectionFailedListener {

    protected String mAddressOutput = "";
    protected boolean mLoadingRequested;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private double mLatitude;
    private double mLongitude;
    private AddressResultReceiver mResultReceiver;
    private ProgressBar mProgressBar;
    private View mFrameLayout;
    private Toolbar mToolbar;
    private TextView mTitleTextView;
    private AutocompleteFilter mAutocompleteFilter;
    private boolean isFirstTime = true;
    private SharedPreferences sPref;
    private boolean isEnabledInListCity = true;
    private ArrayAdapterSearchView mSearchView;
    private MenuItem mSearchMenuItem;

    private SearchView.OnQueryTextListener mOnQueryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            buildGoogleApiClient();
            if (mGoogleApiClient != null) {
                mGoogleApiClient.connect();
            }

            PendingResult<AutocompletePredictionBuffer> result =
                    Places.GeoDataApi.getAutocompletePredictions(mGoogleApiClient, newText, null, null);

            result.setResultCallback(mAutocompleteResultCallback);
            return true;
        }
    };

    private ResultCallback<AutocompletePredictionBuffer> mAutocompleteResultCallback = new ResultCallback<AutocompletePredictionBuffer>() {
        @Override
        public void onResult(final AutocompletePredictionBuffer autocompletePredictions) {
            if (autocompletePredictions.getStatus().isSuccess()) {
                String[] citiesArray = new String[autocompletePredictions.getCount()];// ????
                for (int i = 0; i < autocompletePredictions.getCount(); i++) {
                    citiesArray[i] = autocompletePredictions.get(i).getDescription();
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, citiesArray);
                mSearchView.setAdapter(adapter);
                mSearchView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                        Places.GeoDataApi.getPlaceById(mGoogleApiClient, autocompletePredictions.get(position).getPlaceId())
                                .setResultCallback(mPlaceResultCallback);
                    }
                });
            }

        }
    };

    private ResultCallback<PlaceBuffer> mPlaceResultCallback = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer placeBuffer) {
            if (placeBuffer.getStatus().isSuccess()) {
                LatLng latLng = placeBuffer.get(0).getLatLng();
                isFirstTime = false;
                goWeather(latLng.latitude, latLng.longitude);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    mSearchMenuItem.collapseActionView();
                }
            }
            placeBuffer.release();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mProgressBar = (ProgressBar) findViewById(R.id.main_progress_bar);
        mFrameLayout = findViewById(R.id.single_fragment);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mTitleTextView = (TextView) mToolbar.findViewById(R.id.toolbar_title);
        setSupportActionBar(mToolbar);


        if (savedInstanceState == null) {
            mLoadingRequested = true;
            mResultReceiver = new AddressResultReceiver(new Handler());
            updateUIWidgets();
            buildGoogleApiClient();
            ForecastApi.create("80cafdc845d979b3e365db8bfd0c73b9");
        } else {
            mLoadingRequested = true;
            updateUIWidgets();
            showData();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        mSearchMenuItem = menu.findItem(R.id.action_search);
        mSearchView = (ArrayAdapterSearchView) MenuItemCompat.getActionView(mSearchMenuItem);
        mSearchView.setCustomColor(new ColorDrawable(getResources().getColor(R.color.backgroundAutocomlete)));
        List<Integer> filters = new ArrayList<Integer>();
        filters.add(Place.TYPE_LOCALITY);
        mAutocompleteFilter = AutocompleteFilter.create(filters);

        mSearchView.setOnQueryTextListener(mOnQueryTextListener);


        return super.onCreateOptionsMenu(menu);
    }

    private void updateUIWidgets() {
        if (mLoadingRequested) {
            mProgressBar.setVisibility(View.VISIBLE);
            mFrameLayout.setVisibility(View.GONE);

        } else {
            mProgressBar.setVisibility(View.GONE);
            mFrameLayout.setVisibility(View.VISIBLE);

        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
            Log.d("qwerty", "onStop OK");
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("qwerty", "onConnected OK");
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        if (mLastLocation == null) {
            Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show();
            return;
        }

        if (isFirstTime) {
            goWeather(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        }

//        mLatitude = 49.8268070;
//        mLongitude = 23.9592920;


    }

    private void goWeather(double latitude, double longitude) {
        if (!Double.isNaN(latitude) && !Double.isNaN(longitude)) {
            startIntentService(latitude, longitude);
        }

        RequestBuilder weather = new RequestBuilder();

        Request request = new Request();
        request.setLat(String.valueOf(latitude));
        request.setLng(String.valueOf(longitude));
        request.setUnits(Request.Units.SI);
        request.setLanguage(Request.Language.ENGLISH);

        weather.getWeather(request, new Callback<WeatherResponse>() {
            @Override
            public void success(WeatherResponse weatherResponse, Response response) {
                //1. Get our weather

                WeatherData.getsInstance().setCurrentTemperature((int) weatherResponse.getCurrently().getTemperature());

                WeatherData.getsInstance().setSunrise(Long.valueOf(weatherResponse.getDaily().getData().get(0).getSunriseTime()) * 1000);
                WeatherData.getsInstance().setSunset(Long.valueOf(weatherResponse.getDaily().getData().get(0).getSunsetTime()) * 1000);
                WeatherData.getsInstance().setCurrentIcon(weatherResponse.getCurrently().getIcon());
                WeatherData.getsInstance().setCurrentDescription(weatherResponse.getHourly().getSummary());

                List<WeatherForecast> items = new ArrayList<>();
                for (int i = 0; i < weatherResponse.getDaily().getData().size(); i++) {
                    WeatherForecast myItem = new WeatherForecast();
                    myItem.date = weatherResponse.getDaily().getData().get(i).getTime();
                    myItem.apparentTemperatureMin = (int) weatherResponse.getDaily().getData().get(i).getApparentTemperatureMin();
                    myItem.apparentTemperatureMax = (int) weatherResponse.getDaily().getData().get(i).getApparentTemperatureMax();
                    myItem.dailyIcon = weatherResponse.getDaily().getData().get(i).getIcon();
                    myItem.dailySummary = weatherResponse.getDaily().getData().get(i).getSummary();
                    items.add(myItem);
                }

                WeatherData.getsInstance().mWeatherForecastList = items;

                Log.d("qwerty", "success callback OK");
                showData();


            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.d("qwerty", "failure FAIL");
                Toast.makeText(MainActivity.this, "Error to get weather. Try again later.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void startIntentService(double latitude, double longitude) {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LATITUDE_DATA, latitude);
        intent.putExtra(Constants.LONGITUDE_DATA, longitude);
        startService(intent);

    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, Constants.REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                mGoogleApiClient.connect();
            }
        } else {
            showErrorDialog(connectionResult.getErrorCode());
        }

    }

    private void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        MyErrorDialogFragment dialogFragment = new MyErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(Constants.DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getSupportFragmentManager(), "errordialog");
    }

    private void showData() {
        displayTitleBar(WeatherData.getsInstance().getCityTitle());
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            getSupportFragmentManager().beginTransaction().replace(R.id.current_fragment, new CurrentWeatherFragment()).commit();
        }
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE
                && ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) ==
                Configuration.SCREENLAYOUT_SIZE_NORMAL)) {
//            if(getFragmentManager().findFragmentByTag(WeatherData.FRAGMENT_TAG) != null){
//                return;
//            }
            getSupportFragmentManager().beginTransaction().replace(R.id.current_fragment, new CurrentWeatherFragment()).commit();
        }
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE
                && !((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) ==
                Configuration.SCREENLAYOUT_SIZE_NORMAL)) {

            getSupportFragmentManager().beginTransaction().replace(R.id.current_fragment, new CurrentWeatherFragment()).commit();
            getSupportFragmentManager().beginTransaction().replace(R.id.forecast_fragment, new WeatherForecastFragment()).commit();
        }
        mLoadingRequested = false;
        updateUIWidgets();
    }

    private void displayTitleBar(String name) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(null);
            mTitleTextView.setText(name);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_RESOLVE_ERROR) {
            if (resultCode == RESULT_OK) {
                if (!mGoogleApiClient.isConnecting() &&
                        !mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.connect();
                }
            }
        }
    }

    public static class MyErrorDialogFragment extends DialogFragment {
        public MyErrorDialogFragment() {
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt(Constants.DIALOG_ERROR);
            return GoogleApiAvailability.getInstance().getErrorDialog(
                    this.getActivity(), errorCode, Constants.REQUEST_RESOLVE_ERROR);
        }
    }

    @SuppressLint("ParcelCreator")
    class AddressResultReceiver extends ResultReceiver {

        /**
         * Create a new ResultReceive to receive results.  Your
         * {@link #onReceiveResult} method will be called from the thread running
         * <var>handler</var> if given, or from an arbitrary thread if null.
         *
         * @param handler
         */
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);

            if (resultCode == Constants.SUCCESS_RESULT) {
                WeatherData.getsInstance().setCityTitle(mAddressOutput);
                displayTitleBar(mAddressOutput);
            } else if (resultCode == Constants.FAILURE_RESULT) {
                displayTitleBar("");
                Toast.makeText(MainActivity.this, mAddressOutput, Toast.LENGTH_LONG).show();
            }
        }
    }
}