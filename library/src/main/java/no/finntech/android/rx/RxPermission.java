package no.finntech.android.rx;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

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
        return getPermissionStatus(activity, permissions)
                .first()
                .lift(new Observable.Operator<Boolean, RxPermissionResult>() {
                    @Override
                    public Subscriber<? super RxPermissionResult> call(final Subscriber<? super Boolean> subscriber) {
                        return new Subscriber<RxPermissionResult>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable throwable) {

                            }

                            @Override
                            public void onNext(RxPermissionResult rxPermissionResult) {
                                if (rxPermissionResult.granted) {
                                    subscriber.onNext(true);
                                } else {
                                    RxActivityResponseDelegate rxActivityResponseDelegate = RxActivityResponseDelegate.get(activity);
                                    if (!rxActivityResponseDelegate.hasActiveResponse()) {
                                        rxActivityResponseDelegate.setResponse(responseHandler);
                                        ActivityCompat.requestPermissions(activity, permissions, rxActivityResponseDelegate.getRequestCode());
                                    }
                                }
                            }
                        };
                    }
                });
    }


    public abstract static class PermissionRationaleOperator implements Observable.Operator<Boolean, RxPermission.RxPermissionResult> {
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
            return new Subscriber<RxPermission.RxPermissionResult>() {
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
        }
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
