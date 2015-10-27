package no.finn.android.rx;

import android.app.Activity;
import android.support.v4.app.ActivityCompat;

import rx.Subscriber;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

public class GetPermissionObservable extends BaseStateObservable<Boolean> implements RxPermissionRationale.RequestPermission {
    private static final String STATE_NAME = "GetPermission";
    private final Activity activity;
    private final RxPermissionRationale rationale;
    private final PermissionResult permissionResult;
    private boolean rationaleActive = false;

    public GetPermissionObservable(Activity activity, RxState state, RxPermissionRationale rationale, PermissionResult permissionResult) {
        super(state);
        this.activity = activity;
        this.rationale = rationale;
        this.permissionResult = permissionResult;
    }

    @Override
    public void call(Subscriber<? super Boolean> subscriber) {
        if (permissionResult.granted) {
            subscriber.onNext(true);
            subscriber.onCompleted();
        } else {
            if (permissionRequestDenied(STATE_NAME)) {
                subscriber.onNext(false);
                subscriber.onCompleted();
            } else {
                if (rationale != null && (permissionResult.showRationale || rationale.alwaysShowRationale)) {
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
        recieveStateResponse(STATE_NAME);
        ActivityCompat.requestPermissions(activity, permissionResult.permissions, getRequestCode());
    }
}
