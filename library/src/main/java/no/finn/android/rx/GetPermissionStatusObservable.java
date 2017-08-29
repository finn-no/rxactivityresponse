package no.finn.android.rx;

import android.app.Activity;
import android.support.v4.app.ActivityCompat;

import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;


public class GetPermissionStatusObservable implements ObservableOnSubscribe<PermissionResult> {
    private final Activity activity;
    private final String[] permissions;

    public GetPermissionStatusObservable(Activity activity, String... permissions) {
        this.activity = activity;
        this.permissions = permissions;
    }

    @Override
    public void subscribe(ObservableEmitter<PermissionResult> subscriber) {
        if (subscriber.isDisposed()) {
            return;
        }

        boolean showRationale = false;
        int[] grantResults = new int[permissions.length];
        for (int i = 0; i < permissions.length; i++) {
            grantResults[i] = ActivityCompat.checkSelfPermission(activity, permissions[i]);
            showRationale |= ActivityCompat.shouldShowRequestPermissionRationale(activity, permissions[i]);
        }
        subscriber.onNext(new PermissionResult(permissions, grantResults, showRationale));
        subscriber.onComplete();
    }
}
