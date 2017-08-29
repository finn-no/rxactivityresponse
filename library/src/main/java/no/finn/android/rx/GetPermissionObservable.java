package no.finn.android.rx;

import android.app.Activity;
import android.support.v4.app.ActivityCompat;

import io.reactivex.ObservableEmitter;
import io.reactivex.functions.Cancellable;


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
    public void subscribe(ObservableEmitter<Boolean> emitter) {
        if (emitter.isDisposed()) {
            return;
        }

        if (permissionResult.granted) {
            emitter.onNext(true);
            emitter.onComplete();
        } else {
            if (permissionRequestDenied(STATE_NAME)) {
                emitter.onNext(false);
                emitter.onComplete();
            } else {
                if (rationale != null && (permissionResult.showRationale || rationale.alwaysShowRationale)) {
                    emitter.setCancellable(new Cancellable() {
                        @Override
                        public void cancel() throws Exception {
                            if (rationaleActive) {
                                rationale.onUnsubscribe();
                                rationaleActive = false;
                            }
                        }
                    });
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
