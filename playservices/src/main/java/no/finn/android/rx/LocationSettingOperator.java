package no.finn.android.rx;

import android.app.Activity;
import android.content.IntentSender;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import rx.Observable;
import rx.Subscriber;

public class LocationSettingOperator implements Observable.Operator<GoogleApiClient, GoogleApiClient> {
    private final Activity activity;
    private final LocationRequest locationRequest;
    private final RxActivityResponseDelegate.RxResponseHandler responseHandler;

    public LocationSettingOperator(Activity activity, LocationRequest locationRequest, RxActivityResponseDelegate.RxResponseHandler responseHandler) {
        this.activity = activity;
        this.locationRequest = locationRequest;
        this.responseHandler = responseHandler;
    }

    @Override
    public Subscriber<? super GoogleApiClient> call(final Subscriber<? super GoogleApiClient> subscriber) {
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
                                subscriber.onNext(client);
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
        };
        subscriber.add(s);
        return s;
    }
}
