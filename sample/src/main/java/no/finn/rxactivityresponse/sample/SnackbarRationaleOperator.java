package no.finn.rxactivityresponse.sample;

import android.support.design.widget.Snackbar;
import android.view.View;

import no.finn.android.rx.RxPermissionRationale;

public class SnackbarRationaleOperator extends RxPermissionRationale {
    private final View activity;
    private final String explanation;
    private Snackbar snackbar;

    public SnackbarRationaleOperator(View view, String explanation) {
        this.activity = view;
        this.explanation = explanation;
    }

    @Override
    public void showRationale(final RequestPermission requestPermission) {
        snackbar = Snackbar.make(activity, explanation, Snackbar.LENGTH_INDEFINITE).setAction(android.R.string.ok, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermission.showRationale();
            }
        });
        snackbar.show();
    }

    @Override
    public void onUnsubscribe() {
        snackbar.dismiss();
        snackbar = null;
    }
}
