package no.finn.android.rx;

import android.app.Activity;

import io.reactivex.Observable;
import io.reactivex.functions.Function;


public class RxPermission {
    public static Observable<Boolean> getPermission(final Activity activity, final RxState state, final String... permissions) {
        return getPermission(activity, state, null, permissions);
    }

    public static Observable<Boolean> getPermission(final Activity activity, final RxState state, final RxPermissionRationale rationale, final String... permissions) {
        return Observable.create(new GetPermissionStatusObservable(activity, permissions))
                .flatMap(new Function<PermissionResult, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> apply(PermissionResult permissionResult) {
                        return Observable.create(new GetPermissionObservable(activity, state, rationale, permissionResult));
                    }
                }).compose(new BaseStateObservable.EndStateTransformer<Boolean>(state));
    }

    public static Observable<PermissionResult> getPermissionState(final Activity activity, final String... permissions) {
        return Observable.create(new GetPermissionStatusObservable(activity, permissions));
    }
}
