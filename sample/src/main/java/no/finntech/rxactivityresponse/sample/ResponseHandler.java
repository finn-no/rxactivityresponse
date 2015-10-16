package no.finntech.rxactivityresponse.sample;

import android.app.Activity;
import android.os.Parcel;
import android.os.Parcelable;

import no.finntech.android.rx.RxActivityResponseDelegate;
import no.finntech.android.rx.RxPermission;

class ResponseHandler extends RxActivityResponseDelegate.RxResponseHandler implements Parcelable {
    @Override
    public void onRequestPermissionsResult(Activity activity, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(activity, permissions, grantResults);
        if (RxPermission.allPermissionsGranted(grantResults)) {
            ((RxButtonExample) activity.findViewById(R.id.getlocationbutton)).getLocation();
        }
        // optionally you can handle a "permission denied" scenario here.
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }

    public static final Creator CREATOR = new Creator() {
        public ResponseHandler createFromParcel(Parcel in) {
            return new ResponseHandler();
        }

        public ResponseHandler[] newArray(int size) {
            return new ResponseHandler[size];
        }
    };
}
