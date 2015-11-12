package matsko.mykola.mycurrentweather;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
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
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
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
import com.google.android.gms.location.places.AutocompletePrediction;
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

    private static final String TAG = "tag";
    private static final String CITI_KEY = "CITI_KEY";
    protected String mAddressOutput = "";
    protected boolean mLoadingRequested;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private AddressResultReceiver mResultReceiver;
    private ProgressBar mProgressBar;
    private View mFrameLayout;
    private Toolbar mToolbar;
    private TextView mTitleTextView;
    private boolean isFirstTime = true;
    private ArrayAdapterSearchView mSearchView;
    private MenuItem mSearchMenuItem;
    private PlaceAutocompleteAdapter mAutocompleteAdapter;
    private ImageButton imageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mProgressBar = (ProgressBar) findViewById(R.id.main_progress_bar);
        mFrameLayout = findViewById(R.id.single_fragment);
        imageButton = (ImageButton) findViewById(R.id.my_position);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectedWeather();
                Log.d("qwerty", "onConnected OK");
                mResultReceiver = new AddressResultReceiver(new Handler());
                goWeather(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            }
        });
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mTitleTextView = (TextView) mToolbar.findViewById(R.id.toolbar_title);
        setSupportActionBar(mToolbar);

        if (savedInstanceState == null) {
            isFirstTime = true;
            mLoadingRequested = true;
            mResultReceiver = new AddressResultReceiver(new Handler());
//            updateUIWidgets();
            buildGoogleApiClient();
            ForecastApi.create("80cafdc845d979b3e365db8bfd0c73b9");
        } else {
            buildGoogleApiClient();
            mLoadingRequested = true;
            isFirstTime = false;
            updateUIWidgets();
            showData();
            displayTitleBar(WeatherData.getsInstance().getCityTitle());
        }

        List<Integer> filters = new ArrayList<Integer>();
        filters.add(Place.TYPE_GEOCODE);
        AutocompleteFilter filter = AutocompleteFilter.create(filters);
        mAutocompleteAdapter = new PlaceAutocompleteAdapter(this, mGoogleApiClient, null, filter);


    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        mSearchMenuItem = menu.findItem(R.id.action_search);
        mSearchView = (ArrayAdapterSearchView) MenuItemCompat.getActionView(mSearchMenuItem);
        mSearchView.setCustomColor(new ColorDrawable(getResources().getColor(R.color.backgroundAutocomlete)));
        mSearchView.setAdapter(mAutocompleteAdapter);
        mSearchView.setOnItemClickListener(mOnItemClickListener);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("qwerty", "onStart()");
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
            Log.d("qwerty", "mGoogleApiClient.connect();");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("qwerty", "onStop()");
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
            Log.d("qwerty", "mGoogleApiClient.disconnect()");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(CITI_KEY, WeatherData.getsInstance().getCityTitle());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        displayTitleBar(savedInstanceState.getString(CITI_KEY));
    }

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                // Request did not complete successfully
                Log.e(TAG, "Place query did not complete. Error: " + places.getStatus().toString());
                places.release();
                return;
            }
            // Get the Place object from the buffer.
            LatLng latLng = places.get(0).getLatLng();
            isFirstTime = false;
            mResultReceiver = new AddressResultReceiver(new Handler());
            goWeather(latLng.latitude, latLng.longitude);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                mSearchMenuItem.collapseActionView();
            }

//            // Display the third party attributions if set.
//            final CharSequence thirdPartyAttribution = places.getAttributions();
//            if (thirdPartyAttribution == null) {
//                mPlaceDetailsAttribution.setVisibility(View.GONE);
//            } else {
//                mPlaceDetailsAttribution.setVisibility(View.VISIBLE);
//                mPlaceDetailsAttribution.setText(Html.fromHtml(thirdPartyAttribution.toString()));
//            }
            places.release();
        }
    };
    private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final AutocompletePrediction item = mAutocompleteAdapter.getItem(position);
            final String placeId = item.getPlaceId();

            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);

            Log.i(TAG, "Called getPlaceById to get Place details for " + placeId);
        }
    };




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
        Log.d("qwerty", "buildGoogleApiClient()");
        mGoogleApiClient = new Builder(this)
                .enableAutoManage(this, 0 /* clientId */, this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }


    @Override
    public void onConnected(Bundle bundle) {

        connectedWeather();

//        mLatitude = 49.8268070;
//        mLongitude = 23.9592920;

        if (isFirstTime) {
            Log.d("qwerty", "onConnected OK");
            if (mLastLocation == null) {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show();
            } else {
                goWeather(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            }
        }

    }

    private void connectedWeather() {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);


    }

    private void goWeather(double latitude, double longitude) {

        startIntentService(latitude, longitude);


        RequestBuilder weather = new RequestBuilder();

        Request request = new Request();
        request.setLat(String.valueOf(latitude));
        request.setLng(String.valueOf(longitude));
        request.setUnits(Request.Units.SI);
        request.setLanguage(Request.Language.ENGLISH);
        Log.d("qwerty", "goWeather");
        weather.getWeather(request, new Callback<WeatherResponse>() {
            @Override
            public void success(WeatherResponse weatherResponse, Response response) {
                //1. Get our weather
                Log.d("qwerty", "success");
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
            mTitleTextView.setGravity(Gravity.CENTER);
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
                Log.d("qwerty", "onReceiveResult() OK");
                WeatherData.getsInstance().setCityTitle(mAddressOutput);
                displayTitleBar(WeatherData.getsInstance().getCityTitle());
            } else if (resultCode == Constants.FAILURE_RESULT) {
                Log.d("qwerty", "onReceiveResult() fail");
                displayTitleBar("ERROR");
                Toast.makeText(MainActivity.this, mAddressOutput, Toast.LENGTH_LONG).show();
            }
        }
    }
}