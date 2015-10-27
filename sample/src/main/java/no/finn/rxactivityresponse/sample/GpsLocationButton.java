package no.finn.rxactivityresponse.sample;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import no.finn.android.rx.RxPermissionRationale;
import no.finn.android.rx.RxPlayServices;
import no.finn.android.rx.RxState;
import no.finn.android.rx.RxStateRestart;

import com.google.android.gms.location.LocationRequest;
import junit.framework.Assert;
import rx.functions.Action1;

public class GpsLocationButton extends Button implements View.OnClickListener, RxStateRestart {
    private final RxState rxState;

    public GpsLocationButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnClickListener(this);
        rxState = RxState.get(context, ActivityResponses.GET_LOCATION, new WeakReference<RxStateRestart>(this));
    }

    @Override
    public void onClick(View v) {
        rxAction(ActivityResponses.GET_LOCATION);
    }

    @Override
    public void rxAction(int requestCode) {
        Assert.assertEquals(ActivityResponses.GET_LOCATION, requestCode);
        final LocationRequest locationRequest = new LocationRequest();
        locationRequest.setNumUpdates(1);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        RxPermissionRationale rationaleOperator = new SnackbarRationaleOperator(this, "I need access to ...");

        // GetLocation fetches the required permission, turns on location stuff with the locationsetting api, then recieves a location
        RxPlayServices.getLocation((Activity) getContext(), rationaleOperator, locationRequest, rxState)
                .subscribe(new Action1<Location>() {
                    @Override
                    public void call(Location location) {
                        //NB : if setNumUpdates != 0 you need to make sure you unsubscribe from the subscription!
                        Toast.makeText(getContext(), "Got a location " + location.toString(), Toast.LENGTH_LONG).show();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Toast.makeText(getContext(), "Exception : " + throwable, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
