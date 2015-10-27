package no.finn.android.rx;

import android.app.Activity;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import rx.Subscriber;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class GetPermissionObservable extends BaseStateObservable<Boolean> implements RxPermissionRationale.RequestPermission {
    private static final String STATE_NAME = "GetPermission";
    private final Activity activity;
    private final RxPermissionRationale rationale;
    private final String[] permissions;
    private boolean rationaleActive = false;

    public GetPermissionObservable(Activity activity, RxState state, RxPermissionRationale rationale, String... permissions) {
        super(state);
        this.activity = activity;
        this.rationale = rationale;
        this.permissions = permissions;
    }

    @Override
    public void call(Subscriber<? super Boolean> subscriber) {
        //@fixme : share code between this and GetPermissionStatus - or just flatMap this..
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

    public void onPermissionResult(Subscriber<? super Boolean> subscriber, boolean allPermissionsGranted, boolean showRationale) {
        Log.d("DBG", "RXCALL : NewRequestPermissionObservable.onPermissionResult " + allPermissionsGranted + " Time:" + System.currentTimeMillis());
        if (allPermissionsGranted) {
            subscriber.onNext(true);
            subscriber.onCompleted();
        } else {
            if (permissionRequestDenied(STATE_NAME)) {
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
        recieveStateResponse(STATE_NAME);
        ActivityCompat.requestPermissions(activity, permissions, getRequestCode());
    }
}
