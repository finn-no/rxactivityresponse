package no.finn.android.rx;

import java.io.IOException;

import android.Manifest;
import android.app.Activity;
import android.content.IntentSender;
import android.location.Location;

import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

public class RxPlayServices {
    @SafeVarargs
    public static Observable<GoogleApiClient> getPlayServices(final Activity activity, final RxState state, final String[] permissions, final RxPermissionRationale rationale, final Scope[] scopes, final Api<? extends Api.ApiOptions.NotRequiredOptions>... services) {
        return Observable.create(new GetPermissionStatusObservable(activity, permissions))
                .flatMap(new Function<PermissionResult, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> apply(PermissionResult permissionResult) {
                        return Observable.create(new GetPermissionObservable(activity, state, rationale, permissionResult));
                    }
                })
                .flatMap(new Function<Boolean, Observable<GoogleApiClient>>() {
                    @Override
                    public Observable<GoogleApiClient> apply(Boolean granted) {
                        if (granted) {
                            return Observable.create(new PlayServicesObservable(activity, state, scopes, services));
                        }
                        return Observable.error(new UserAbortedException());
                    }
                }).compose(new BaseStateObservable.EndStateTransformer<GoogleApiClient>(state));
    }

    public static Observable<Location> getLocation(final Activity activity, final RxPermissionRationale rationale, final LocationRequest locationRequest, final RxState state) {
        String[] permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
        return Observable.create(new GetPermissionStatusObservable(activity, permissions))
                .flatMap(new Function<PermissionResult, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> apply(PermissionResult permissionResult) {
                        return Observable.create(new GetPermissionObservable(activity, state, rationale, permissionResult));
                    }
                })
                .flatMap(new Function<Boolean, Observable<Location>>() {
                    @Override
                    public Observable<Location> apply(Boolean granted) {
                        if (granted) {
                            return Observable.create(new LocationObservable(activity, state, locationRequest, LocationServices.API));
                        }
                        return Observable.error(new UserAbortedException());
                    }
                }).compose(new BaseStateObservable.EndStateTransformer<Location>(state));
    }

    public static class RxLocationError extends IOException {
        public final int settingsChangeUnavailable;

        public RxLocationError(int settingsChangeUnavailable) {
            super("RxLocationError " + settingsChangeUnavailable);
            this.settingsChangeUnavailable = settingsChangeUnavailable;
        }

        public RxLocationError(IntentSender.SendIntentException e) {
            super(e);
            settingsChangeUnavailable = LocationSettingsStatusCodes.RESOLUTION_REQUIRED;
        }
    }

}
