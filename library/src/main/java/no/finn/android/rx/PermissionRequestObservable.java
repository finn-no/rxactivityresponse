package no.finn.android.rx;

import android.app.Activity;

import rx.Subscriber;

public class PermissionRequestObservable extends PermissionRequestBaseObservable<Boolean> {
    public PermissionRequestObservable(Activity activity, RxResponseHandler handler, RxPermissionRationale permissionRationale, String... permissions) {
        super(activity, handler, permissionRationale, permissions);
    }

    @Override
    public void onPermissionsGranted(Subscriber<? super Boolean> subscriber) {
        subscriber.onNext(true);
        subscriber.onCompleted();
    }
}