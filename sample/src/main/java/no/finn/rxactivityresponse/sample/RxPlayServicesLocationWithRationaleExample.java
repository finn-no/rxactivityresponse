package no.finn.rxactivityresponse.sample;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import no.finn.android.rx.RxPermissionRationale;
import no.finn.android.rx.RxPlayServices;
import no.finn.android.rx.RxResponseHandler;

import com.google.android.gms.location.LocationRequest;
import rx.functions.Action1;

public class RxPlayServicesLocationWithRationaleExample extends Button implements View.OnClickListener {
    private ResponseHandler locationResponseHandler = new ResponseHandler();

    public RxPlayServicesLocationWithRationaleExample(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnClickListener(this);
    }

    public void getLocation() {
        final LocationRequest locationRequest = new LocationRequest();
        locationRequest.setNumUpdates(1);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        RxPermissionRationale rationaleOperator = new SnackbarRationaleOperator(this, "I need access to ...");
        RxPlayServices.getLocation((Activity) getContext(), rationaleOperator, locationRequest, locationResponseHandler)
                .subscribe(new Action1<Location>() {
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
    private static class ResponseHandler extends RxResponseHandler implements Parcelable {
        @Override
        public void onResponse(Activity activity, boolean success, Response response) {
            if (success) {
                ((RxPlayServicesLocationWithRationaleExample) activity.findViewById(R.id.getlocationwithrationale)).getLocation();
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
