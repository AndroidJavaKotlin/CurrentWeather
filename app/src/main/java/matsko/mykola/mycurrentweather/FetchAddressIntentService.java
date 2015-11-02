package matsko.mykola.mycurrentweather;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Asme on 23.10.2015.
 */
public class FetchAddressIntentService extends IntentService {

    private static final String TAG = "FetchAddressIS";

    private ResultReceiver mReceiver;
    private double mLatitude;
    private double mLongitude;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public FetchAddressIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        String errorMessage = "";

        mReceiver = intent.getParcelableExtra(Constants.RECEIVER);
        mLatitude = intent.getDoubleExtra(Constants.LATITUDE_DATA, 0.0);
        mLongitude = intent.getDoubleExtra(Constants.LONGITUDE_DATA, 0.0);

        if (mReceiver == null) {
            return;
        }

        if (Double.isNaN(mLatitude) && Double.isNaN(mLongitude)) {
            errorMessage = getString(R.string.no_location_data_provided);
            deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage);
            return;
        }

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocation(mLatitude, mLongitude, 1);
        } catch (IOException e) {
            errorMessage = "Sorry, the service is not available";
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            errorMessage = "Invalid latitude or longitude used";
        }

        if (addresses == null || addresses.size()  == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = getString(R.string.no_address_found);
            }
            deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage);
        } else {
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<>();
                addressFragments.add(address.getLocality());
                addressFragments.add(address.getCountryName());
            deliverResultToReceiver(Constants.SUCCESS_RESULT, TextUtils.join("    ", addressFragments));
        }
    }

    void deliverResultToReceiver(int requestCode, String message) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.RESULT_DATA_KEY, message);
        mReceiver.send(requestCode, bundle);
    }
}
