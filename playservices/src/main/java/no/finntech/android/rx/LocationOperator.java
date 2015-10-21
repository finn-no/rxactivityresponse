package no.finntech.android.rx;

import android.location.Location;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

public class LocationOperator implements Observable.Operator<Location, GoogleApiClient> {
    private final LocationRequest locationRequest;

    public LocationOperator(LocationRequest locationRequest) {
        this.locationRequest = locationRequest;
    }

    @Override
    public Subscriber<? super GoogleApiClient> call(final Subscriber<? super Location> subscriber) {
        Subscriber<GoogleApiClient> s = new Subscriber<GoogleApiClient>() {
            @Override
            public void onCompleted() {
                subscriber.onCompleted();
            }

            @Override
            public void onError(Throwable throwable) {
                subscriber.onError(throwable);
            }

            @Override
            public void onNext(final GoogleApiClient client) {
                final LocationListener locationListener = new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        subscriber.onNext(location);
                        if (locationRequest.getNumUpdates() == 1) {
                            subscriber.onCompleted();
                        }
                    }
                };

                subscriber.add(Subscriptions.create(new Action0() {
                    @Override
                    public void call() {
                        LocationServices.FusedLocationApi.removeLocationUpdates(client, locationListener);
                    }
                }));
                LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, locationListener);

            }
        };
        subscriber.add(s);
        return s;
    }
}
