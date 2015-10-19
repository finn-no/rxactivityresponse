package no.finntech.rxactivityresponse.sample;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.design.widget.Snackbar;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import no.finntech.android.rx.RxActivityResponseDelegate;
import no.finntech.android.rx.RxPermission;
import no.finntech.android.rx.RxPlayServices;

import com.google.android.gms.location.LocationRequest;
import rx.Observable;
import rx.functions.Action1;

public class RxPlayServicesLocationWithRationaleExample extends Button implements View.OnClickListener {
    private ResponseHandler locationResponseHandler = new ResponseHandler();

    public RxPlayServicesLocationWithRationaleExample(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnClickListener(this);
    }

    public void getLocation() {
        final LocationRequest locationRequest = new LocationRequest();
        locationRequest.setNumUpdates(10);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        String[] permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
        Observable<Boolean> getPermissionObservable = RxPermission.getPermissionStatus((Activity) getContext(), permissions)
                .lift(new RxPermission.PermissionRationaleOperator((Activity) getContext(), locationResponseHandler, permissions) {
                    @Override
                    public void showRationale() {
                        Snackbar.make(RxPlayServicesLocationWithRationaleExample.this, "I need access to location..", Snackbar.LENGTH_INDEFINITE)
                                .setAction(android.R.string.ok, new OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        requestPermission();
                                    }
                                }).show();
                    }
                });

        RxPlayServices.getLocation(getPermissionObservable, (Activity) getContext(), locationRequest, locationResponseHandler).subscribe(new Action1<Location>() {
            @Override
            public void call(Location location) {
                //NB : if setNumUpdates != 0 you need to make sure you unsubscribe from the subscription!
                Toast.makeText(getContext(), "Got a location " + location.toString(), Toast.LENGTH_LONG).show();
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
                ((RxPlayServicesLocationWithRationaleExample) activity.findViewById(R.id.getlocationwithrationale)).getLocation();
            } else {
                // permission denied
            }
        }


        @Override
        public void onActivityResult(Activity activity, int resultCode, Intent data) {
            if (resultCode == Activity.RESULT_OK) {
                // location enabled
                ((RxPlayServicesLocationWithRationaleExample) activity.findViewById(R.id.getlocationwithrationale)).getLocation();
            } else {
                // location not enabled
            }
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
