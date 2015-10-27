package no.finn.android.rx;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

public class ActivityResultState implements Parcelable {
    public final int resultCode;
    public final Intent data;

    public ActivityResultState(int resultCode, Intent data) {
        this.resultCode = resultCode;
        this.data = data;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(resultCode);
        dest.writeParcelable(data, 0);
    }

    public static final Creator<ActivityResultState> CREATOR = new Creator<ActivityResultState>() {
        public ActivityResultState createFromParcel(Parcel in) {
            final Intent parcelable = in.readParcelable(Intent.class.getClassLoader());
            return new ActivityResultState(in.readInt(), parcelable);
        }

        public ActivityResultState[] newArray(int size) {
            return new ActivityResultState[size];
        }
    };

    public static class ActivityResultCanceledException extends IOException {

    }

    public boolean resultCanceled() {
        return resultCode == Activity.RESULT_CANCELED;
    }
}
