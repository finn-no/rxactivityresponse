package no.finn.android.rx;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class PermissionResult {
    public final String[] permissions;
    public final int[] grantResults;
    public final boolean showRationale;
    public final boolean granted;

    public PermissionResult(String[] permissions, int[] grantResults, boolean showRationale) {
        this.permissions = permissions;
        this.grantResults = grantResults;
        this.showRationale = showRationale;
        boolean allPermissionsGranted = true;
        for (int i = 0; i < permissions.length; i++) {
            boolean permissionGranted = grantResults[i] == PERMISSION_GRANTED;
            allPermissionsGranted = allPermissionsGranted && permissionGranted;
        }
        this.granted = allPermissionsGranted;
    }
}
