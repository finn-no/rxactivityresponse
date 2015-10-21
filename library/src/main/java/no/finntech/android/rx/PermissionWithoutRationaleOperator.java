package no.finntech.android.rx;

import android.app.Activity;

public class PermissionWithoutRationaleOperator extends PermissionRationaleOperator {
    public PermissionWithoutRationaleOperator(Activity activity, RxActivityResponseDelegate.RxResponseHandler handler, String... permissions) {
        super(activity, handler, permissions);
    }

    @Override
    public void showRationale() {
        requestPermission();
    }
}
