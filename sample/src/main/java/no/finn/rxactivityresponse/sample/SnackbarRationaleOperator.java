package no.finn.rxactivityresponse.sample;

import android.support.design.widget.Snackbar;
import android.view.View;

import no.finn.android.rx.RxPermissionRationale;

public class SnackbarRationaleOperator implements RxPermissionRationale {
    private final View activity;
    private final String explanation;

    public SnackbarRationaleOperator(View view, String explanation) {
        this.activity = view;
        this.explanation = explanation;
    }

    @Override
    public void showRationale(final RequestPermission requestPermission) {
        Snackbar.make(activity, explanation, Snackbar.LENGTH_INDEFINITE)
                .setAction(android.R.string.ok, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        requestPermission.execute();
                    }
                }).show();
    }
}
