package no.finntech.rxactivityresponse.sample;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import no.finntech.android.rx.RxActivityResponseDelegate;
import no.finntech.android.rx.RxPermission;
import no.finntech.android.rx.RxPermissionRationale;

import rx.functions.Action1;

public class RxButtonExampleWithRationale extends Button implements View.OnClickListener {
    private ResponseHandler locationResponseHandler = new ResponseHandler();

    public RxButtonExampleWithRationale(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnClickListener(this);
    }

    public void getLocation() {
        final String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
        RxPermissionRationale rationaleOperator = new SnackbarRationaleOperator(this, "I need access to ...");
        RxPermission.getPermission((Activity) getContext(), locationResponseHandler, rationaleOperator, permissions)
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean permission) {
                        if (permission) {
                            Toast.makeText(getContext(), "Permission is granted and we can do something with it", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    @Override
    public void onClick(View v) {
        getLocation();
    }

    // This class handles restarting the permission request when a permission is retrieved. Since this class can be serialized
    // we could also pass extra arguments through here and back to the getLocation function.
    private static class ResponseHandler extends RxActivityResponseDelegate.RxResponseHandler implements Parcelable {
        @Override
        public void onRequestPermissionsResult(Activity activity, String[] permissions, int[] grantResults) {
            if (RxPermission.allPermissionsGranted(grantResults)) {
                ((RxButtonExampleWithRationale) activity.findViewById(R.id.getlocationbuttonwithrationale)).getLocation();
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
}
