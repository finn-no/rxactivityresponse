package no.finntech.android.rx;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

import rx.Observable;
import rx.Subscriber;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class RxPermission {
    public static Observable<RxPermissionResult> getPermissionStatus(final Activity activity, final String... permissions) {
        return Observable.create(new Observable.OnSubscribe<RxPermissionResult>() {
            @Override
            public void call(Subscriber<? super RxPermissionResult> subscriber) {
                boolean allPermissionGranted = true;
                boolean showRationale = false;
                for (String permission : permissions) {
                    boolean permissionGranted = ActivityCompat.checkSelfPermission(activity, permission) == PERMISSION_GRANTED;
                    allPermissionGranted = allPermissionGranted && permissionGranted;
                    if (!permissionGranted) {
                        showRationale = showRationale || ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
                    }
                }
                subscriber.onNext(new RxPermissionResult(allPermissionGranted, showRationale));
            }
        });
    }

    public static Observable<Boolean> getPermission(final Activity activity, final RxActivityResponseDelegate.RxResponseHandler responseHandler, final String... permissions) {
        return getPermission(activity, responseHandler, null, permissions);
    }

    public static Observable<Boolean> getPermission(final Activity activity, final RxActivityResponseDelegate.RxResponseHandler responseHandler, RxPermissionRationale rationaleOperator, final String... permissions) {
        return getPermissionStatus(activity, permissions)
                .lift(new PermissionRequestOperator(activity, responseHandler, rationaleOperator, permissions));
    }

    public static class RxPermissionResult {
        public final boolean granted;
        public final boolean showRationale;

        public RxPermissionResult(boolean granted, boolean showRationale) {

            this.granted = granted;
            this.showRationale = showRationale;
        }
    }

    public static boolean allPermissionsGranted(int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}
