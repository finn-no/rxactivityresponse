package no.finn.android.rx;

import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

public class RxState implements Parcelable {
    public final int requestCode;

    private final Map<String, RequestPermissionState> permissionResults;
    private final Map<String, ActivityResultState> activityResults;
    private WeakReference<RxStateRestart> rxContinueRef;
    private String currentRequest = null;

    public static RxState get(Context context, int requestCode) {
        return get(context, requestCode, (WeakReference<RxStateRestart>) null);
    }

    public static RxState get(Context context, int requestCode, RxStateRestart rxContinue) {
        return get(context, requestCode, new WeakReference<>(rxContinue));
    }

    public static RxState get(Context context, int requestCode, WeakReference<RxStateRestart> rxContinueRef) {
        RxState result = RxActivityResponse.get(context).getState(requestCode);
        if (result == null) {
            result = new RxState(requestCode);
            RxActivityResponse.get(context).putState(requestCode, result);
        }
        result.rxContinueRef = rxContinueRef;
        return result;
    }

    private RxState(int requestCode) {
        this.requestCode = requestCode;
        this.permissionResults = new HashMap<>();
        this.activityResults = new HashMap<>();
    }

    private RxState(int requestCode, String currentRequest, Map<String, RequestPermissionState> permissionResults, Map<String, ActivityResultState> activityResults) {
        this.requestCode = requestCode;
        this.currentRequest = currentRequest;
        this.permissionResults = permissionResults;
        this.activityResults = activityResults;
    }

    public RequestPermissionState getPermissionResult(String name) {
        return permissionResults.get(name);
    }

    public ActivityResultState getActivityResult(String name) {
        return activityResults.get(name);
    }

    public void onRequestPermissionsResult(String[] permissions, int[] grantResults) {
        permissionResults.put(currentRequest, new RequestPermissionState(permissions, grantResults));
        rxContinueAction(requestCode);
        currentRequest = null;
    }

    public void onActivityResult(int resultCode, Intent data) {
        activityResults.put(currentRequest, new ActivityResultState(resultCode, data));
        rxContinueAction(requestCode);
        currentRequest = null;
    }

    private void rxContinueAction(int requestCode) {
        if (rxContinueRef != null && rxContinueRef.get() != null) {
            RxStateRestart rxStateRestart = rxContinueRef.get();
            if (rxStateRestart == null) {
                throw new IllegalStateException("rxContinue is null - did you make the weak reference an inner class instead of your request object?");
            }
            rxStateRestart.rxAction(requestCode);
        }
    }

    public void recieve(String requestName) {
        currentRequest = requestName;
    }

    public void reset() {
        activityResults.clear();
        permissionResults.clear();
        currentRequest = null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(requestCode);
        dest.writeString(currentRequest);
        dest.writeMap(permissionResults);
        dest.writeMap(activityResults);
    }

    public static final Creator<RxState> CREATOR = new Creator<RxState>() {
        @SuppressWarnings("unchecked")
        public RxState createFromParcel(Parcel in) {
            return new RxState(in.readInt(), in.readString(),
                in.readHashMap(RequestPermissionState.class.getClassLoader()), in.readHashMap(ActivityResultState.class.getClassLoader()));
        }

        public RxState[] newArray(int size) {
            return new RxState[size];
        }
    };
}
