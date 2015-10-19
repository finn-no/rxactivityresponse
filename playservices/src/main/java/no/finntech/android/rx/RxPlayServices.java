package no.finntech.android.rx;

import java.io.IOException;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.location.Location;

import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import rx.Observable;

public class RxPlayServices {
    public static Observable<GoogleApiClient> getPlayServices(final Context context, final Api<? extends Api.ApiOptions.NotRequiredOptions>... services) {
        return Observable.just(true).lift(new PlayServicesPermissionsConnectionOperator(context, services));
    }

    public static Observable<Location> getLocation(final Activity activity, final LocationRequest locationRequest, final RxActivityResponseDelegate.RxResponseHandler responseHandler) {
        String[] permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
        return getLocation(activity, new PermissionWithoutRationaleOperator(activity, responseHandler, permissions), locationRequest, responseHandler);
    }

    public static Observable<Location> getLocation(final Activity activity, PermissionRationaleOperator rationaleOperator, final LocationRequest locationRequest, final RxActivityResponseDelegate.RxResponseHandler responseHandler) {
        String[] permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
        return RxPermission.getPermissionStatus(activity, permissions)
                .lift(rationaleOperator)
                .lift(new PlayServicesPermissionsConnectionOperator(activity, LocationServices.API))
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
