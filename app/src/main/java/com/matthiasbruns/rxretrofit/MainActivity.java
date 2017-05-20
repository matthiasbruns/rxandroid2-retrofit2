package com.matthiasbruns.rxretrofit;

import com.matthiasbruns.rxretrofit.network.CityResponse;
import com.matthiasbruns.rxretrofit.network.CityService;
import com.matthiasbruns.rxretrofit.network.Geoname;
import com.matthiasbruns.rxretrofit.network.RetrofitHelper;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * This activity demonstrates how retrofit and rx work together.
 */
public class MainActivity extends AppCompatActivity {

    /**
     * We will query geonames with this service
     */
    @NonNull
    private CityService mCityService;

    /**
     * Collects all subscriptions to unsubscribe later
     */
    @NonNull
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    private TextView mOutputTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mOutputTextView = (TextView) findViewById(R.id.output);

        // Initialize the city endpoint
        mCityService = new RetrofitHelper().getCityService();

        // Trigger our request and display afterwards
        requestGeonames();
    }

    @Override
    protected void onDestroy() {
        // DO NOT CALL .dispose()
        mCompositeDisposable.clear();
        super.onDestroy();
    }

    private void displayGeonames(@NonNull final List<Geoname> geonames) {
        // Cheap way to display a list of Strings - I was too lazy to implement a RecyclerView
        final StringBuilder output = new StringBuilder();
        for (final Geoname geoname : geonames) {
            output.append(geoname.name).append("\n");
        }

        mOutputTextView.setText(output.toString());
    }

    private void requestGeonames() {
        mCompositeDisposable.add(mCityService.queryGeonames(44.1, -9.9, -22.4, 55.2, "de")
                .subscribeOn(Schedulers.io()) // "work" on io thread
                .observeOn(AndroidSchedulers.mainThread()) // "listen" on UIThread
                .map(new Function<CityResponse, List<Geoname>>() {
                    @Override
                    public List<Geoname> apply(
                            @io.reactivex.annotations.NonNull final CityResponse cityResponse)
                            throws Exception {
                        // we want to have the geonames and not the wrapper object
                        return cityResponse.geonames;
                    }
                })
                .subscribe(new Consumer<List<Geoname>>() {
                    @Override
                    public void accept(
                            @io.reactivex.annotations.NonNull final List<Geoname> geonames)
                            throws Exception {
                        displayGeonames(geonames);
                    }
                })
        );
    }
}
