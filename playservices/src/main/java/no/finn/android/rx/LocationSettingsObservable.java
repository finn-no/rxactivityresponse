package no.finn.android.rx;

import android.app.Activity;
import android.content.IntentSender;

import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import rx.Subscriber;

public abstract class LocationSettingsObservable<T> extends PlayServicesBaseObservable<T> {
    public final LocationRequest locationRequest;

    public LocationSettingsObservable(Activity activity, RxResponseHandler responseHandler, LocationRequest locationRequest, Api<? extends Api.ApiOptions.NotRequiredOptions>... services) {
        super(activity, responseHandler, services);
        this.locationRequest = locationRequest;
    }

    @Override
    public void onGoogleApiClientReady(Subscriber<? super T> subscriber, GoogleApiClient client) {
        handleLocationSettings(subscriber, client);
    }

    private void handleLocationSettings(final Subscriber<? super T> subscriber, final GoogleApiClient client) {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .setAlwaysShow(true)
                .addLocationRequest(locationRequest);

        PendingResult<LocationSettingsResult> pendingResult = LocationServices.SettingsApi.checkLocationSettings(client, builder.build());
        pendingResult.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        locationSettingSuccess(subscriber, client);
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            RxActivityResponseDelegate rxActivityResponseDelegate = RxActivityResponseDelegate.get(activity);
                            if (!rxActivityResponseDelegate.hasActiveResponse()) {
                                rxActivityResponseDelegate.setResponse(responseHandler);
                                status.startResolutionForResult(activity, rxActivityResponseDelegate.getRequestCode());
                            }
                            subscriber.unsubscribe(); // silently close our api connection, full restart through responsehandler required, so we can close this connection.
                        } catch (IntentSender.SendIntentException e) {
                            subscriber.onError(new RxPlayServices.RxLocationError(e));
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        subscriber.onError(new RxPlayServices.RxLocationError(LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE));
                        break;
                }
            }
        });
    }

    protected abstract void locationSettingSuccess(Subscriber<? super T> subscriber, GoogleApiClient client);
}