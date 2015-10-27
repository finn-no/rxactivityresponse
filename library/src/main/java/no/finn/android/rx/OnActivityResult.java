package no.finn.android.rx;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

public class OnActivityResult implements Parcelable {
    public final int resultCode;
    public final Intent data;

    public OnActivityResult(int resultCode, Intent data) {
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

    public static final Creator<OnActivityResult> CREATOR = new Creator<OnActivityResult>() {
        public OnActivityResult createFromParcel(Parcel in) {
            final Intent parcelable = in.readParcelable(Intent.class.getClassLoader());
            return new OnActivityResult(in.readInt(), parcelable);
        }

        public OnActivityResult[] newArray(int size) {
            return new OnActivityResult[size];
        }
    };
}
