package no.finn.android.rx;

import android.content.pm.PackageManager;
import android.os.Parcel;
import android.os.Parcelable;

public class OnRequestPermissionResult implements Parcelable {
    public final String[] permissions;
    public final int[] grantResults;

    public OnRequestPermissionResult(String[] permissions, int[] grantResults) {
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

    public static final Creator<OnRequestPermissionResult> CREATOR = new Creator<OnRequestPermissionResult>() {
        public OnRequestPermissionResult createFromParcel(Parcel in) {
            return new OnRequestPermissionResult(in.createStringArray(), in.createIntArray());
        }

        public OnRequestPermissionResult[] newArray(int size) {
            return new OnRequestPermissionResult[size];
        }
    };
}
