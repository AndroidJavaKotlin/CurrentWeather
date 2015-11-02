package matsko.mykola.mycurrentweather;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.Objects;

/**
 * Created by Asme on 28.10.2015.
 */
public class PlacesAdapter extends ArrayAdapter<Objects> {
    public PlacesAdapter(Context context, int resource) {
        super(context, resource);
    }
}
