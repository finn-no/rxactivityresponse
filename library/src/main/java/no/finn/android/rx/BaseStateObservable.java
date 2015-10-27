package no.finn.android.rx;

import android.app.Activity;

import rx.Observable;

public abstract class BaseStateObservable<T> implements Observable.OnSubscribe<T> {
    protected final RxState state;

    public BaseStateObservable(RxState state) {
        this.state = state;
    }

    public String getStateName() {
        return getClass().getName();
    }

    public void recieveStateResponse() {
        state.recieve(getStateName());
    }

    public int getRequestCode() {
        return state.requestCode;
    }

    public OnRequestPermissionResult getPermissionResult() {
        return state.getPermissionResult(getStateName());
    }

    public boolean permissionRequestDenied() {
        final OnRequestPermissionResult result = getPermissionResult();
        return result != null && !result.permissionsGranted();
    }

    public OnActivityResult getActivityResult() {
        return state.getActivityResult(getStateName());
    }

    public boolean activityResultCanceled() {
        final OnActivityResult activityResult = getActivityResult();
        return activityResult != null && activityResult.resultCode == Activity.RESULT_CANCELED;
    }
}
