package no.finn.android.rx;

public class RxPermissionResult {
    public final boolean granted;
    public final boolean showRationale;

    public RxPermissionResult(boolean granted, boolean showRationale) {
        this.granted = granted;
        this.showRationale = showRationale;
    }
}
