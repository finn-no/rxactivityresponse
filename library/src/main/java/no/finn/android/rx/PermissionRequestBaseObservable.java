package no.finn.android.rx;

import android.app.Activity;
import android.support.v4.app.ActivityCompat;

import rx.Subscriber;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

public abstract class PermissionRequestBaseObservable<T> extends PermissionStatusBaseObservable<T> implements RxPermissionRationale.RequestPermission {
    private final Activity activity;
    private final RxResponseHandler handler;
    private final RxPermissionRationale permissionRationale;
    private final String[] permissions;
    private boolean rationaleDialogActive = false;

    public PermissionRequestBaseObservable(Activity activity, RxResponseHandler handler, RxPermissionRationale permissionRationale, final String... permissions) {
        super(activity, permissions);
        this.activity = activity;
        this.handler = handler;
        this.permissionRationale = permissionRationale;
        this.permissions = permissions;
    }

    @Override
    public void onPermissionResult(Subscriber<? super T> subscriber, boolean allPermissionsGranted, boolean showRationale) {
        if (allPermissionsGranted) {
            onPermissionsGranted(subscriber);
        } else {
            try {
                if (permissionRationale != null && (showRationale || permissionRationale.alwaysShowRationale)) {
                    subscriber.add(Subscriptions.create(new Action0() {
                        @Override
                        public void call() {
                            if (rationaleDialogActive) {
                                rationaleDialogActive = false;
                                permissionRationale.onUnsubscribe();
                            }
                        }
                    }));
                    rationaleDialogActive = true;
                    permissionRationale.showRationale(PermissionRequestBaseObservable.this);
                } else {
                    showRationale();
                }
            } catch (Exception e) {
                subscriber.onError(e);
            }
        }
    }

    public abstract void onPermissionsGranted(Subscriber<? super T> subscriber);

    @Override
    public void showRationale() {
        rationaleDialogActive = false;
        RxActivityResponseDelegate rxActivityResponseDelegate = RxActivityResponseDelegate.get(activity);
        rxActivityResponseDelegate.setResponse(handler);
        ActivityCompat.requestPermissions(activity, permissions, rxActivityResponseDelegate.getRequestCode());
    }
}
