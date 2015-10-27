package no.finn.android.rx;

import rx.Observable;

public abstract class BaseStateObservable<T> implements Observable.OnSubscribe<T> {
    protected final RxState state;

    public BaseStateObservable(RxState state) {
        this.state = state;
    }

    public void recieveStateResponse(String stateName) {
        state.recieve(stateName);
    }

    public int getRequestCode() {
        return state.requestCode;
    }

    public RequestPermissionState getPermissionResult(String stateName) {
        return state.getPermissionResult(stateName);
    }

    public boolean permissionRequestDenied(String stateName) {
        final RequestPermissionState result = getPermissionResult(stateName);
        return result != null && !result.permissionsGranted();
    }

    public ActivityResultState getActivityResult(String stateName) {
        return state.getActivityResult(stateName);
    }

    public boolean activityResultCanceled(String stateName) {
        final ActivityResultState activityResult = getActivityResult(stateName);
        return activityResult != null && activityResult.resultCanceled();
    }
}
