package no.finntech.android.rx;

import android.app.Activity;
import android.support.v4.app.ActivityCompat;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

public abstract class PermissionRationaleOperator implements Observable.Operator<Boolean, RxPermission.RxPermissionResult> {
    private final Activity activity;
    private final RxActivityResponseDelegate.RxResponseHandler handler;
    private final String[] permissions;

    public PermissionRationaleOperator(Activity activity, RxActivityResponseDelegate.RxResponseHandler handler, final String... permissions) {
        this.activity = activity;
        this.handler = handler;
        this.permissions = permissions;
    }

    public abstract void showRationale();

    public void onUnsubscribe() {

    }

    public void requestPermission() {
        RxActivityResponseDelegate rxActivityResponseDelegate = RxActivityResponseDelegate.get(activity);
        rxActivityResponseDelegate.setResponse(handler);
        ActivityCompat.requestPermissions(activity, permissions, rxActivityResponseDelegate.getRequestCode());
    }

    @Override
    public Subscriber<? super RxPermission.RxPermissionResult> call(final Subscriber<? super Boolean> subscriber) {
        subscriber.add(Subscriptions.create(new Action0() {
            @Override
            public void call() {
                onUnsubscribe();
            }
        }));
        Subscriber<RxPermission.RxPermissionResult> s = new Subscriber<RxPermission.RxPermissionResult>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable throwable) {
                subscriber.onError(throwable);
            }

            @Override
            public void onNext(RxPermission.RxPermissionResult rxPermissionResult) {
                if (rxPermissionResult.granted) {
                    subscriber.onNext(true);
                } else {
                    try {
                        if (rxPermissionResult.showRationale) {
                            showRationale();
                        } else {
                            requestPermission();
                        }
                    } catch (Exception e) {
                        subscriber.onError(e);
                    }
                }
            }
        };
        subscriber.add(s);
        return s;
    }
}
