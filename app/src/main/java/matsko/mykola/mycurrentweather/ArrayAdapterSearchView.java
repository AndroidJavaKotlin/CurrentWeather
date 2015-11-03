package matsko.mykola.mycurrentweather;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.SearchView;
import android.util.AttributeSet;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

/**
 * Created by akrup on 28.10.2015.
 */
public class ArrayAdapterSearchView extends SearchView {

    private SearchView.SearchAutoComplete mSearchAutoComplete;

    public ArrayAdapterSearchView(Context context) {
        super(context);
        initialize();
    }

    public ArrayAdapterSearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public void initialize() {
        mSearchAutoComplete = (SearchAutoComplete) findViewById(android.support.v7.appcompat.R.id.search_src_text);
        mSearchAutoComplete.setTextColor(Color.WHITE);
        mSearchAutoComplete.setHintTextColor(Color.WHITE);
//        this.setAdapter(null);
//        this.setOnItemClickListener(null);
    }

    @Override
    public void setSuggestionsAdapter(CursorAdapter adapter) {
        // don't let anyone touch this
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        mSearchAutoComplete.setOnItemClickListener(listener);
    }

    public void setAdapter(ArrayAdapter<?> adapter) {
        mSearchAutoComplete.setAdapter(adapter);
        mSearchAutoComplete.setDropDownHeight(400);
    }

    public void setText(String text) {
        mSearchAutoComplete.setText(text);
    }

    public void setCustomColor(ColorDrawable color){
        mSearchAutoComplete.setDropDownBackgroundDrawable(color);
    }

}