package no.finn.android.rx;

import android.content.pm.PackageManager;
import android.os.Parcel;
import android.os.Parcelable;

public class RequestPermissionState implements Parcelable {
    public final String[] permissions;
    public final int[] grantResults;

    public RequestPermissionState(String[] permissions, int[] grantResults) {
        this.permissions = permissions;
        this.grantResults = grantResults;
    }

    public boolean permissionsGranted() {
        for (int grantResult : grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(permissions);
        dest.writeIntArray(grantResults);
    }

    public static final Creator<RequestPermissionState> CREATOR = new Creator<RequestPermissionState>() {
        public RequestPermissionState createFromParcel(Parcel in) {
            final String[] permissions = in.createStringArray();
            final int[] grantResults = in.createIntArray();
            return new RequestPermissionState(permissions, grantResults);
        }

        public RequestPermissionState[] newArray(int size) {
            return new RequestPermissionState[size];
        }
    };
}
