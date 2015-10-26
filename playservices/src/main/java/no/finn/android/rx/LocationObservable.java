package no.finn.android.rx;

import android.app.Activity;
import android.location.Location;

import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import rx.Subscriber;

public class LocationObservable extends LocationSettingsObservable<Location> {
    private LocationListener locationListener = null;
    private GoogleApiClient client;

    @SafeVarargs
    public LocationObservable(Activity activity, RxResponseHandler responseHandler, LocationRequest locationRequest, Api<? extends Api.ApiOptions.NotRequiredOptions>... services) {
        super(activity, responseHandler, locationRequest, services);
    }

    @Override
    public void onUnsubscribe() {
        if (locationListener != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(client, locationListener);
        }
        super.onUnsubscribe();
    }

    @Override
    protected void locationSettingSuccess(final Subscriber<? super Location> subscriber, GoogleApiClient client) {
        this.client = client;
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                subscriber.onNext(location);
                if (locationRequest.getNumUpdates() == 1) {
                    subscriber.onCompleted();
                }
            }
        };

        LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, locationListener);
    }
}
