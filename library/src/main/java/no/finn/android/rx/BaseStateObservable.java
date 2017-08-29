package no.finn.android.rx;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableTransformer;
import io.reactivex.functions.Action;

public abstract class BaseStateObservable<T> implements ObservableOnSubscribe<T> {
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

    public static class EndStateTransformer<T> implements ObservableTransformer<T, T> {
        private final RxState state;

        public EndStateTransformer(RxState state) {
            this.state = state;
        }

        @Override
        public Observable<T> apply(Observable<T> observable) {
            return observable.doAfterTerminate(new Action() {
                @Override
                public void run() {
                    state.reset();
                }
            });
        }
    }
}
