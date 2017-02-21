package no.finn.android.rx;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.util.SparseArray;

public class RxActivityResponse {
    SparseArray<RxState> state = new SparseArray<>();

    public static RxActivityResponse get(Context context) {
        return RxActivityResponseContextWrapper.getRxActivityResponse(context);
    }

    public static Context install(final Activity activity, final Context base) {
        if (RxActivityResponseLifecycleIntegration.find(activity) == null) {
            RxActivityResponseLifecycleIntegration.install((Application) base.getApplicationContext(), activity);
        }

        return new RxActivityResponseContextWrapper(base, activity);
    }

    public RxState getState(int requestCode) {
        return state.get(requestCode);
    }

    public void putState(int requestCode, RxState result) {
        state.put(requestCode, result);
    }
}
