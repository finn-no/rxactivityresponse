package no.finn.android.rx;

import android.app.Activity;
import android.support.v4.app.ActivityCompat;

import rx.Observable;
import rx.Subscriber;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public abstract class PermissionStatusBaseObservable<T> implements Observable.OnSubscribe<T> {
    private final Activity activity;
    private final String[] permissions;

    public PermissionStatusBaseObservable(final Activity activity, final String... permissions) {
        this.activity = activity;
        this.permissions = permissions;
    }

    @Override
    public void call(Subscriber<? super T> subscriber) {
        boolean allPermissionGranted = true;
        boolean showRationale = false;
        for (String permission : permissions) {
            boolean permissionGranted = ActivityCompat.checkSelfPermission(activity, permission) == PERMISSION_GRANTED;
            allPermissionGranted = allPermissionGranted && permissionGranted;
            if (!permissionGranted) {
                showRationale = showRationale || ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
            }
        }
        onPermissionResult(subscriber, allPermissionGranted, showRationale);
    }

    public abstract void onPermissionResult(Subscriber<? super T> subscriber, boolean allPermissionsGranted, boolean showRationale);
}
