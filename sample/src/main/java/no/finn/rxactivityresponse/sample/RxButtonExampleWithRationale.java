package no.finn.rxactivityresponse.sample;

import java.lang.ref.WeakReference;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import no.finn.android.rx.RxPermission;
import no.finn.android.rx.RxPermissionRationale;
import no.finn.android.rx.RxState;
import no.finn.android.rx.RxStateRestart;

import junit.framework.Assert;
import rx.functions.Action1;

public class RxButtonExampleWithRationale extends Button implements View.OnClickListener, RxStateRestart {
    private final RxState rxState;

    public RxButtonExampleWithRationale(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.d("DBG", "RxButtonExampleWithRationale.RxButtonExampleWithRationale CONSTRUCTION TIME Time:" + System.currentTimeMillis());
        rxState = RxState.get(context, ActivityResponses.GET_LOCATIONPERMISSION, new WeakReference<RxStateRestart>(this));
        setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        rxAction(ActivityResponses.GET_LOCATIONPERMISSION);
    }

    @Override
    public void rxAction(int requestCode) {
        Assert.assertEquals(requestCode, ActivityResponses.GET_LOCATIONPERMISSION);
        RxPermissionRationale rationaleOperator = new SnackbarRationaleOperator(this, "I need access to ...");
        Log.d("DBG", "RxButtonExampleWithRationale.startRequest " + rxState + " Time:" + System.currentTimeMillis());

        final String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
        RxPermission.getPermission((Activity) getContext(), rxState, rationaleOperator, permissions)
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean granted) {
                        Toast.makeText(getContext(), "Permission : " + granted, Toast.LENGTH_SHORT).show();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Toast.makeText(getContext(), "Exception : " + throwable, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
