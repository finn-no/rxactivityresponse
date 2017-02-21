package no.finn.android.rx;

import android.app.Activity;

import rx.Observable;
import rx.functions.Func1;

public class RxPermission {
    public static Observable<Boolean> getPermission(final Activity activity, final RxState state, final String... permissions) {
        return getPermission(activity, state, null, permissions);
    }

    public static Observable<Boolean> getPermission(final Activity activity, final RxState state, final RxPermissionRationale rationale, final String... permissions) {
        return Observable.create(new GetPermissionStatusObservable(activity, permissions))
                .flatMap(new Func1<PermissionResult, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(PermissionResult permissionResult) {
                        return Observable.create(new GetPermissionObservable(activity, state, rationale, permissionResult));
                    }
                }).compose(new BaseStateObservable.EndStateTransformer<Boolean>(state));
    }

    public static Observable<PermissionResult> getPermissionState(final Activity activity, final String... permissions) {
        return Observable.create(new GetPermissionStatusObservable(activity, permissions));
    }
}
