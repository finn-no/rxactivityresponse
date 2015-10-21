package no.finntech.rxactivityresponse.sample;

import android.support.design.widget.Snackbar;
import android.view.View;

import no.finntech.android.rx.RxPermissionRationale;

public class SnackbarRationaleOperator implements RxPermissionRationale {
    private final View activity;
    private final String explanation;

    public SnackbarRationaleOperator(View view, String explanation) {
        this.activity = view;
        this.explanation = explanation;
    }

    @Override
    public void showRationale(final Runnable requestPermission) {
        Snackbar.make(activity, explanation, Snackbar.LENGTH_INDEFINITE)
                .setAction(android.R.string.ok, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        requestPermission.run();
                    }
                }).show();
    }
}
