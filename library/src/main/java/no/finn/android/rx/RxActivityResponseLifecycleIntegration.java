package no.finn.android.rx;

import android.app.Activity;
import android.app.Application;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

public final class RxActivityResponseLifecycleIntegration extends Fragment {
    private static final String TAG = RxActivityResponseLifecycleIntegration.class.getSimpleName();
    private static final String RESULTS_KEY = TAG + ".ResultsKey";

    static RxActivityResponseLifecycleIntegration find(Activity activity) {
        return (RxActivityResponseLifecycleIntegration) activity.getFragmentManager().findFragmentByTag(TAG);
    }

    static void install(final Application app, final Activity activity) {
        app.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity a, Bundle savedInstanceState) {
                if (a == activity) {
                    RxActivityResponseLifecycleIntegration fragment = find(activity);
                    if (fragment == null) {
                        fragment = new RxActivityResponseLifecycleIntegration();
                        activity.getFragmentManager()
                            .beginTransaction()
                            .add(fragment, TAG)
                            .commit();
                        activity.getFragmentManager().executePendingTransactions();
                    }
                    app.unregisterActivityLifecycleCallbacks(this);
                }
            }

            @Override
            public void onActivityStarted(Activity activity) {
            }

            @Override
            public void onActivityResumed(Activity activity) {
            }

            @Override
            public void onActivityPaused(Activity activity) {
            }

            @Override
            public void onActivityStopped(Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(Activity a) {
            }
        });
    }

    RxActivityResponse rxActivityResponse;

    public RxActivityResponseLifecycleIntegration() {
        super();
        this.rxActivityResponse = new RxActivityResponse();
        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            rxActivityResponse.state = savedInstanceState.getSparseParcelableArray(RESULTS_KEY);
        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final RxState activityResult = rxActivityResponse.state.get(requestCode);
        if (activityResult != null) {
            activityResult.onActivityResult(resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        final RxState activityResult = rxActivityResponse.state.get(requestCode);
        if (activityResult != null) {
            activityResult.onRequestPermissionsResult(permissions, grantResults);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSparseParcelableArray(RESULTS_KEY, rxActivityResponse.state);
    }
}