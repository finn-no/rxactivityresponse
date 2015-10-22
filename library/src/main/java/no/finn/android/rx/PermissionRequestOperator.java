package no.finn.android.rx;

import android.app.Activity;
import android.support.v4.app.ActivityCompat;

import rx.Observable;
import rx.Subscriber;

public class PermissionRequestOperator implements Observable.Operator<Boolean, RxPermission.RxPermissionResult> {
    private final Activity activity;
    private final RxResponseHandler handler;
    private final RxPermissionRationale permissionRationale;
    private final String[] permissions;


    public PermissionRequestOperator(Activity activity, RxResponseHandler handler, RxPermissionRationale permissionRationale, final String... permissions) {
        this.activity = activity;
        this.handler = handler;
        this.permissionRationale = permissionRationale;
        this.permissions = permissions;
    }

    @Override
    public Subscriber<? super RxPermission.RxPermissionResult> call(final Subscriber<? super Boolean> subscriber) {
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
                        if (rxPermissionResult.showRationale && permissionRationale != null) {

                            permissionRationale.showRationale(requestPermissionHandler);
                        } else {
                            requestPermissionHandler.execute();
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

    private final RxPermissionRationale.RequestPermission requestPermissionHandler = new RxPermissionRationale.RequestPermission() {
        @Override
        public void execute() {
            RxActivityResponseDelegate rxActivityResponseDelegate = RxActivityResponseDelegate.get(activity);
            rxActivityResponseDelegate.setResponse(handler);
            ActivityCompat.requestPermissions(activity, permissions, rxActivityResponseDelegate.getRequestCode());
        }
    };

}
