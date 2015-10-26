package no.finn.android.rx;

import android.app.Activity;

import rx.Subscriber;


public class PermissionStatusObservable extends PermissionStatusBaseObservable<RxPermissionResult> {
    public PermissionStatusObservable(Activity activity, String... permissions) {
        super(activity, permissions);
    }

    @Override
    public void onPermissionResult(Subscriber<? super RxPermissionResult> subscriber, boolean allPermissionsGranted, boolean showRationale) {
        subscriber.onNext(new RxPermissionResult(allPermissionsGranted, showRationale));
    }

}
