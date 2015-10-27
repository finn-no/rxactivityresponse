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

public class RxPlayServicesLocationWithRationaleExample extends Button implements View.OnClickListener, RxStateRestart {
    private final int REQUEST_LOCATION = 42;
    private final RxState rxState;

    public RxPlayServicesLocationWithRationaleExample(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnClickListener(this);
        rxState = RxState.get(context, REQUEST_LOCATION, new WeakReference<RxStateRestart>(this));
    }

    @Override
    public void onClick(View v) {
        rxAction(REQUEST_LOCATION);
    }

    @Override
    public void rxAction(int requestCode) {
        Assert.assertEquals(REQUEST_LOCATION, requestCode);
        final LocationRequest locationRequest = new LocationRequest();
        locationRequest.setNumUpdates(1);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        RxPermissionRationale rationaleOperator = new SnackbarRationaleOperator(this, "I need access to ...");
        RxPlayServices.getLocation((Activity) getContext(), rationaleOperator, locationRequest, rxState)
                .subscribe(new Action1<Location>() {
                    @Override
                    public void call(Location location) {
                        //NB : if setNumUpdates != 0 you need to make sure you unsubscribe from the subscription!
                        Toast.makeText(getContext(), "Got a location " + location.toString(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
