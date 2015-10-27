package no.finn.android.rx.tmpnew;

import android.app.Activity;

import no.finn.android.rx.RxPermissionRationale;

import rx.Observable;
import rx.functions.Action0;

public class RxPermissionB {
    public static Observable<Boolean> getPermission(Activity activity, final RxState state, final String... permissions) {
        return getPermission(activity, state, null, permissions);
    }

    public static Observable<Boolean> getPermission(Activity activity, final RxState state, final RxPermissionRationale rationale, final String... permissions) {
        return Observable.create(new GetPermissionObservable(activity, state, rationale, permissions))
                .doOnCompleted(new Action0() {
                    @Override
                    public void call() {
                        state.reset();
                    }
                });
    }
}
