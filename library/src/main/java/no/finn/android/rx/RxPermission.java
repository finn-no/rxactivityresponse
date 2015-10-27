package no.finn.android.rx;

import android.app.Activity;

import rx.Observable;
import rx.functions.Action0;

public class RxPermission {
    public static Observable<Boolean> getPermission(Activity activity, final RxState state, final String... permissions) {
        return getPermission(activity, state, null, permissions);
    }

    public static Observable<Boolean> getPermission(Activity activity, final RxState state, final RxPermissionRationale rationale, final String... permissions) {
        return Observable.create(new GetPermissionObservable(activity, state, rationale, permissions))
                .finallyDo(new Action0() {
                    @Override
                    public void call() {
                        state.reset();
                    }
                });
    }

    public static Observable<PermissionResult> getPermissionState(Activity activity, final String... permissions) {
        return Observable.create(new GetPermissionStatusObservable(activity, permissions));
    }
}
