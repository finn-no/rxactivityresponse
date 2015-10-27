package no.finn.android.rx;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseArray;

public class RxActivityStateDelegate {
    private static final String SYSTEMSERVICE_NAME = "PersistedActivityResult";
    private SparseArray<RxState> state = new SparseArray<>();

    @SuppressWarnings("ResourceType")
    public static RxActivityStateDelegate get(Context context) {
        return (RxActivityStateDelegate) context.getSystemService(SYSTEMSERVICE_NAME);
    }

    public RxActivityStateDelegate getSystemService(String name) {
        if (SYSTEMSERVICE_NAME.equals(name)) {
            return this;
        }
        return null;
    }

    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            state = savedInstanceState.getSparseParcelableArray("RESULTS");
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putSparseParcelableArray("RESULTS", state);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        final RxState activityResult = state.get(requestCode);
        if (activityResult != null) {
            activityResult.onActivityResult(resultCode, data);
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        final RxState activityResult = state.get(requestCode);
        if (activityResult != null) {
            activityResult.onRequestPermissionsResult(permissions, grantResults);
        }
    }

    RxState getState(int requestCode) {
        return state.get(requestCode);
    }

    void putState(int requestCode, RxState result) {
        state.put(requestCode, result);
    }
}
