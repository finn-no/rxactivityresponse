package no.finn.android.rx;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.location.Location;

import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import io.reactivex.observers.ResourceObserver;

public class LocationObservable extends LocationSettingsObservable<Location> {
    private LocationListener locationListener = null;
    private GoogleApiClient client;

    @SafeVarargs
    public LocationObservable(Activity activity, RxState state, LocationRequest locationRequest, Api<? extends Api.ApiOptions.NotRequiredOptions>... services) {
        super(activity, state, locationRequest, services);
    }

    @Override
    public void onUnsubscribe() {
        if (locationListener != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(client, locationListener);
        }
        super.onUnsubscribe();
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void locationSettingSuccess(final ResourceObserver<Location> emitter, GoogleApiClient client) {
        this.client = client;
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                emitter.onNext(location);
                if (locationRequest.getNumUpdates() == 1) {
                    emitter.onComplete();
                }
            }
        };

        //noinspection MissingPermission
        LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, locationListener);
    }
}
