package no.finn.android.rx;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

public class RxState implements Parcelable {
    private WeakReference<RxStateRestart> rxContinueRef;

    public final int requestCode;
    private String currentRequest = null;
    private final HashMap<String, RequestPermissionState> permissionResults;
    private final HashMap<String, ActivityResultState> activityResults;

    RxState(int requestCode) {
        this.requestCode = requestCode;
        permissionResults = new HashMap<>();
        activityResults = new HashMap<>();
    }

    public static RxState get(Context context, int requestCode, WeakReference<RxStateRestart> rxContinueRef) {
        final RxActivityStateDelegate delegate = RxActivityStateDelegate.get(context);

        //@fixme : dont access delegate.resultTracking directly.
        RxState result = delegate.resultTracking.get(requestCode);
        if (result == null) {
            result = new RxState(requestCode);
            delegate.resultTracking.put(requestCode, result);
        }
        return result.withContinue(rxContinueRef);
    }


    public RxState withContinue(WeakReference<RxStateRestart> rxContinueRef) {
        this.rxContinueRef = rxContinueRef;
        return this;
    }

    private RxState(int requestCode, String currentRequest, HashMap permissionResults, HashMap activityResults) {
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
        RxStateRestart rxStateRestart = rxContinueRef.get();
        if (rxStateRestart == null) {
            throw new IllegalStateException("rxContinue is null - did you make the weak reference an inner class instead of your request object?");
        }
        rxStateRestart.rxAction(requestCode);
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
        public RxState createFromParcel(Parcel in) {
            return new RxState(in.readInt(), in.readString(),
                    in.readHashMap(RequestPermissionState.class.getClassLoader()), in.readHashMap(ActivityResultState.class.getClassLoader()));
        }

        public RxState[] newArray(int size) {
            return new RxState[size];
        }
    };
}
