package no.finn.android.rx;

public class PermissionResult {
    public final boolean granted;
    public final boolean showRationale;

    public PermissionResult(boolean granted, boolean showRationale) {
        this.granted = granted;
        this.showRationale = showRationale;
    }
}
