package no.finn.rxactivityresponse.sample;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import junit.framework.Assert;

import io.reactivex.functions.Consumer;
import no.finn.android.rx.RxPermission;
import no.finn.android.rx.RxPermissionRationale;
import no.finn.android.rx.RxState;
import no.finn.android.rx.RxStateRestart;

public class SimplePermissionButton extends AppCompatButton implements View.OnClickListener, RxStateRestart {
    private final RxState rxState;

    public SimplePermissionButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        // The State has to be created construction time (it's the only way we can get if we're serialized mid flow!)
        rxState = RxState.get(context, ActivityResponses.GET_LOCATIONPERMISSION, this);
        setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        rxAction(ActivityResponses.GET_LOCATIONPERMISSION);
    }

    @Override
    public void rxAction(int requestCode) {
        Assert.assertEquals(requestCode, ActivityResponses.GET_LOCATIONPERMISSION);

        // NB : Rationale is optional and can be null
        RxPermissionRationale rationaleOperator = new SnackbarRationaleOperator(this, "I need access to ...");

        final String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
        RxPermission.getPermission((Activity) getContext(), rxState, rationaleOperator, permissions)
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean granted) {
                        Toast.makeText(getContext(), "Permission : " + granted, Toast.LENGTH_SHORT).show();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        Toast.makeText(getContext(), "Exception : " + throwable, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
