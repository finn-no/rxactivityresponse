package no.finn.android.rx;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.support.annotation.Nullable;

public class RxActivityResponseContextWrapper extends ContextWrapper {
    private static final String SYSTEMSERVICE_NAME = RxActivityResponseContextWrapper.class.getSimpleName() + ".Service";
    private final Activity activity;
    private RxActivityResponse rxActivityResponse;

    @Nullable
    @SuppressWarnings("WrongConstant")
    static RxActivityResponse getRxActivityResponse(Context context) {
        return (RxActivityResponse) context.getSystemService(SYSTEMSERVICE_NAME);
    }

    public RxActivityResponseContextWrapper(Context base, Activity activity) {
        super(base);
        this.activity = activity;
    }

    @Override
    public Object getSystemService(String name) {
        if (SYSTEMSERVICE_NAME.equals(name)) {
            if (rxActivityResponse == null) {
                rxActivityResponse = RxActivityResponseLifecycleIntegration.find(activity).rxActivityResponse;
            }
            return rxActivityResponse;
        }
        return super.getSystemService(name);
    }
}
