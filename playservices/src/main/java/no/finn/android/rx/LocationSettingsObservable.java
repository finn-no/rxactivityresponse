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

import io.reactivex.ObservableEmitter;

public abstract class LocationSettingsObservable<T> extends PlayServicesBaseObservable<T> {
    private static final String STATE_NAME = "LocationSettings";
    public final LocationRequest locationRequest;

    public LocationSettingsObservable(Activity activity, RxState state, LocationRequest locationRequest, Api<? extends Api.ApiOptions.NotRequiredOptions>... services) {
        super(activity, state, services);
        this.locationRequest = locationRequest;
    }

    @Override
    public void onGoogleApiClientReady(ObservableEmitter<T> emitter, GoogleApiClient client) {
        handleLocationSettings(emitter, client);
    }

    private void handleLocationSettings(final ObservableEmitter<T> emitter, final GoogleApiClient client) {
        if (activityResultCanceled(STATE_NAME)) {
            emitter.onError(new LocationSettingDeniedException());
            return;
        }
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
                        locationSettingSuccess(emitter, client);
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        resolveResolutionRequired(emitter, status);
                        emitter.onComplete();  // silently close our api connection, full restart through responsehandler required, so we can close this connection.
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        emitter.onError(new RxPlayServices.RxLocationError(LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE));
                        break;
                }
            }
        });
    }

    protected void resolveResolutionRequired(ObservableEmitter<T> subscriber, Status status) {
        recieveStateResponse(STATE_NAME);
        try {
            status.startResolutionForResult(activity, getRequestCode());
        } catch (IntentSender.SendIntentException e) {
            subscriber.onError(new RxPlayServices.RxLocationError(e));
        }
    }

    protected abstract void locationSettingSuccess(ObservableEmitter<T> subscriber, GoogleApiClient client);

    public static class LocationSettingDeniedException extends UserAbortedException {

    }
}
