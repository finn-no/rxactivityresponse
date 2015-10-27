package no.finn.android.rx.tmpnew;

import android.app.Activity;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import no.finn.android.rx.PermissionStatusBaseObservable;
import no.finn.android.rx.RxPermissionRationale;

import rx.Subscriber;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

public class GetPermissionObservable extends PermissionStatusBaseObservable<Boolean> implements RxPermissionRationale.RequestPermission {
    private final Activity activity;
    private final RxState result;
    private final RxPermissionRationale rationale;
    private final String[] permissions;
    private boolean rationaleActive = false;

    public GetPermissionObservable(Activity activity, RxState result, RxPermissionRationale rationale, String... permissions) {
        super(activity, permissions);
        this.activity = activity;
        this.result = result;
        this.rationale = rationale;
        this.permissions = permissions;
    }

    @Override
    public void onPermissionResult(Subscriber<? super Boolean> subscriber, boolean allPermissionsGranted, boolean showRationale) {
        Log.d("DBG", "RXCALL : NewRequestPermissionObservable.onPermissionResult " + allPermissionsGranted + " Time:" + System.currentTimeMillis());
        if (allPermissionsGranted) {
            subscriber.onNext(true);
            subscriber.onCompleted();
        } else {
            if (result.permissionRequestDenied(getRequestName())) {
                subscriber.onNext(false);
                subscriber.onCompleted();
            } else {
                if (rationale != null && (showRationale || rationale.alwaysShowRationale)) {
                    subscriber.add(Subscriptions.create(new Action0() {
                        @Override
                        public void call() {
                            if (rationaleActive) {
                                rationale.onUnsubscribe();
                                rationaleActive = false;
                            }
                        }
                    }));
                    rationaleActive = true;
                    rationale.showRationale(this);
                } else {
                    requestPermission();
                }
            }
        }
    }

    @Override
    public void requestPermission() {
        rationaleActive = false;
        Log.d("DBG", "NewRequestPermissionObservable.requestPermission  Time:" + System.currentTimeMillis());
        result.recieve(getRequestName());
        ActivityCompat.requestPermissions(activity, permissions, result.requestCode);
    }

    protected String getRequestName() {
        return "PermissionRequest";
    }
}
