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
import rx.Observable;

public class RxPlayServices {

    @SafeVarargs
    public static Observable<GoogleApiClient> getPlayServices(final Activity activity, final RxResponseHandler responseHandler, final Api<? extends Api.ApiOptions.NotRequiredOptions>... services) {
        return getPlayServices(activity, responseHandler, null, null, services);
    }

    @SafeVarargs
    public static Observable<GoogleApiClient> getPlayServices(final Activity activity, final RxResponseHandler responseHandler, String[] permissions, final Api<? extends Api.ApiOptions.NotRequiredOptions>... services) {
        return getPlayServices(activity, responseHandler, permissions, null, services);
    }

    @SafeVarargs
    public static Observable<GoogleApiClient> getPlayServices(final Activity activity, final RxResponseHandler responseHandler, String[] permissions, Scope[] scopes, final Api<? extends Api.ApiOptions.NotRequiredOptions>... services) {
        return RxPermission.getPermission(activity, responseHandler, permissions)
                .lift(new PlayServicesPermissionsConnectionOperator(activity, responseHandler, scopes, services));
    }

    @SafeVarargs
    public static Observable<GoogleApiClient> getPlayServices(final Activity activity, final RxResponseHandler responseHandler, Scope[] scopes, final Api<? extends Api.ApiOptions.NotRequiredOptions>... services) {
        return Observable.just(true)
                .lift(new PlayServicesPermissionsConnectionOperator(activity, responseHandler, scopes, services));
    }


    public static Observable<Location> getLocation(final Activity activity, final LocationRequest locationRequest, final RxResponseHandler responseHandler) {
        return getLocation(activity, null, locationRequest, responseHandler);
    }

    public static Observable<Location> getLocation(final Activity activity, RxPermissionRationale rationaleOperator, final LocationRequest locationRequest, final RxResponseHandler responseHandler) {
        String[] permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
        return RxPermission.getPermission(activity, responseHandler, rationaleOperator, permissions)
                .lift(new PlayServicesPermissionsConnectionOperator(activity, responseHandler, LocationServices.API))
                .lift(new LocationSettingOperator(activity, locationRequest, responseHandler))
                .lift(new LocationOperator(locationRequest));
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
