package no.finn.android.rx;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;

public abstract class RxResponseHandler {
    public void onActivityResult(Activity activity, int resultCode, Intent data) {
        onResponse(activity, resultCode == Activity.RESULT_OK, new Response(new ActivityResponse(resultCode, data)));
    }

    public void onRequestPermissionsResult(Activity activity, String[] permissions, int[] grantResults) {
        onResponse(activity, allPermissionsGranted(grantResults), new Response(new PermissionResponse(permissions, grantResults)));
    }

    public abstract void onResponse(Activity activity, boolean success, Response response);

    public static class ActivityResponse {
        public final int resultCode;
        public final Intent data;

        public ActivityResponse(int resultCode, Intent data) {
            this.resultCode = resultCode;
            this.data = data;
        }
    }

    public static class PermissionResponse {
        public final String[] permissions;
        public final int[] grantResults;

        public PermissionResponse(String[] permissions, int[] grantResults) {
            this.permissions = permissions;
            this.grantResults = grantResults;
        }
    }

    public static class Response {
        public final PermissionResponse permissionResponse;
        public final ActivityResponse activityResponse;

        public Response(PermissionResponse permissionResponse) {
            this.permissionResponse = permissionResponse;
            activityResponse = null;
        }

        public Response(ActivityResponse activityResponse) {
            this.activityResponse = activityResponse;
            permissionResponse = null;
        }
    }

    private static boolean allPermissionsGranted(int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}
