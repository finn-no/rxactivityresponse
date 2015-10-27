package no.finn.android.rx;

import android.app.Activity;
import android.support.v4.app.ActivityCompat;

import rx.Observable;
import rx.Subscriber;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class GetPermissionStatusObservable implements Observable.OnSubscribe<PermissionResult> {
    private final Activity activity;
    private final String[] permissions;

    public GetPermissionStatusObservable(Activity activity, String... permissions) {
        this.activity = activity;
        this.permissions = permissions;
    }

    @Override
    public void call(Subscriber<? super PermissionResult> subscriber) {
        boolean showRationale = false;
        int[] grantResults = new int[permissions.length];
        for (int i = 0; i < permissions.length; i++) {
            grantResults[i] = ActivityCompat.checkSelfPermission(activity, permissions[i]);
            showRationale = ActivityCompat.shouldShowRequestPermissionRationale(activity, permissions[i]);
        }
        subscriber.onNext(new PermissionResult(permissions, grantResults, showRationale));
    }
}
