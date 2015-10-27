package no.finn.android.rx;

import android.app.Activity;

import rx.Observable;

public class RxPermission {
    public static Observable<PermissionResult> getPermissionStatus(final Activity activity, final String... permissions) {
        return Observable.create(new PermissionStatusObservable(activity, permissions));
    }

    public static Observable<Boolean> getPermission(final Activity activity, final RxResponseHandler responseHandler, final String... permissions) {
        return getPermission(activity, responseHandler, null, permissions);
    }

    public static Observable<Boolean> getPermission(final Activity activity, final RxResponseHandler responseHandler, final RxPermissionRationale rationaleOperator, final String... permissions) {
        return Observable.create(new PermissionRequestObservable(activity, responseHandler, rationaleOperator, permissions));
    }
}
