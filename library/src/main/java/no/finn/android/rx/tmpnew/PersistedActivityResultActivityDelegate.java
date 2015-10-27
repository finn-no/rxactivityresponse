package no.finn.android.rx.tmpnew;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;

import no.finn.android.rx.RxActivityResponseDelegate;

public class PersistedActivityResultActivityDelegate {
    private static final String SYSTEMSERVICE_NAME = "PersistedActivityResult";

    private SparseArray<RxState> resultTracking = new SparseArray<>();

    //@fixme : document this...
    public static RxState getOrCreate(Context context, int requestCode, WeakReference<RxActivityResponseDelegate.RxContinue> rxContinueRef) {
        final PersistedActivityResultActivityDelegate delegate = get(context);
        RxState result = delegate.resultTracking.get(requestCode);
        if (result == null) {
            result = new RxState(requestCode);
            delegate.resultTracking.put(requestCode, result);
        }
        return result.withContinue(rxContinueRef);
    }

    @SuppressWarnings("ResourceType")
    public static PersistedActivityResultActivityDelegate get(Context context) {
        return (PersistedActivityResultActivityDelegate) context.getSystemService(SYSTEMSERVICE_NAME);
    }


    public PersistedActivityResultActivityDelegate getSystemService(String name) {
        if (SYSTEMSERVICE_NAME.equals(name)) {
            return this;
        }
        return null;
    }


    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            resultTracking = savedInstanceState.getSparseParcelableArray("RESULTS");
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putSparseParcelableArray("RESULTS", resultTracking);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("DBG", "PersistedActivityResultActivityDelegate.onActivityResult " + requestCode + " " + resultCode + " Time:" + System.currentTimeMillis());
        final RxState activityResult = resultTracking.get(requestCode);
        if (activityResult != null) {
            activityResult.onActivityResult(resultCode, data);
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.d("DBG", "PersistedActivityResultActivityDelegate.onRequestPermissionsResult onRequestPermissionsResult " + requestCode + " Time:" + System.currentTimeMillis());
        final RxState activityResult = resultTracking.get(requestCode);
        if (activityResult != null) {
            activityResult.onRequestPermissionsResult(permissions, grantResults);
        }
    }
}
