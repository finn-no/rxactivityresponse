package no.finn.android.rx.tmpnew;

import android.app.Activity;
import android.support.v4.app.ActivityCompat;

import no.finn.android.rx.PermissionResult;

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
        boolean allPermissionsGranted = true;
        boolean showRationale = false;
        for (String permission : permissions) {
            boolean permissionGranted = ActivityCompat.checkSelfPermission(activity, permission) == PERMISSION_GRANTED;
            allPermissionsGranted = allPermissionsGranted && permissionGranted;
            if (!permissionGranted) {
                showRationale = showRationale || ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
            }
        }
        subscriber.onNext(new PermissionResult(allPermissionsGranted, showRationale));
    }
}
