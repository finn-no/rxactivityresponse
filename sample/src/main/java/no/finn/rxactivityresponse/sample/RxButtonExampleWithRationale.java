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

import no.finn.android.rx.RxActivityResponseDelegate;
import no.finn.android.rx.RxPermissionRationale;
import no.finn.android.rx.tmpnew.RxState;
import no.finn.android.rx.tmpnew.PersistedActivityResultActivityDelegate;
import no.finn.android.rx.tmpnew.RxPermissionB;

import junit.framework.Assert;
import rx.functions.Action1;

public class RxButtonExampleWithRationale extends Button implements View.OnClickListener, RxActivityResponseDelegate.RxContinue {
    private static final int GET_LOCATION = 42;
    private final RxState rxState;

    public RxButtonExampleWithRationale(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.d("DBG", "RxButtonExampleWithRationale.RxButtonExampleWithRationale CONSTRUCTION TIME Time:" + System.currentTimeMillis());
        rxState = PersistedActivityResultActivityDelegate.getOrCreate(context, GET_LOCATION, new WeakReference<RxActivityResponseDelegate.RxContinue>(this));
        setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        rxAction(GET_LOCATION);
    }

    @Override
    public void rxAction(int requestCode) {
        Assert.assertEquals(requestCode, GET_LOCATION);
        RxPermissionRationale rationaleOperator = new SnackbarRationaleOperator(this, "I need access to ...");
        Log.d("DBG", "RxButtonExampleWithRationale.startRequest " + rxState + " Time:" + System.currentTimeMillis());

        final String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
        RxPermissionB.getPermission((Activity) getContext(), rxState, rationaleOperator, permissions)
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean granted) {
                        Toast.makeText(getContext(), "Permission : " + granted, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
