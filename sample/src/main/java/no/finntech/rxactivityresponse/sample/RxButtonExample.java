package no.finntech.rxactivityresponse.sample;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import no.finntech.android.rx.RxActivityResponseDelegate;
import no.finntech.android.rx.RxPermission;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;

public class RxButtonExample extends Button implements View.OnClickListener {
    private ResponseHandler locationResponseHandler = new ResponseHandler();
    private final RxActivityResponseDelegate rxActivityResponseDelegate;

    public RxButtonExample(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnClickListener(this);
        rxActivityResponseDelegate = RxActivityResponseDelegate.get(context);
    }

    public void getLocation() {
        RxPermission.getPermission((Activity) getContext(), locationResponseHandler, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION).subscribe(new Action1<Boolean>() {
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

    public void getLocationHandleRationale() {
        final String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
        RxPermission.getPermissionStatus((Activity) getContext(), permissions)
                .lift(permissionCheckerWithRationale(permissions))
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean permission) {
                        if (permission) {
                            Toast.makeText(getContext(), "Permission is granted and we can do something with it", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private Observable.Operator<Boolean, RxPermission.RxPermissionResult> permissionCheckerWithRationale(final String[] permissions) {
        return new Observable.Operator<Boolean, RxPermission.RxPermissionResult>() {
            @Override
            public Subscriber<? super RxPermission.RxPermissionResult> call(final Subscriber<? super Boolean> subscriber) {
                return new Subscriber<RxPermission.RxPermissionResult>() {
                    private void getPermission() {
                        rxActivityResponseDelegate.setResponse(locationResponseHandler);
                        ActivityCompat.requestPermissions((Activity) getContext(), permissions, rxActivityResponseDelegate.getRequestCode());
                    }

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable throwable) {

                    }

                    @Override
                    public void onNext(RxPermission.RxPermissionResult rxPermissionResult) {
                        if (rxPermissionResult.granted) {
                            subscriber.onNext(true);
                        } else {
                            if (rxPermissionResult.showRationale) {
                                Snackbar.make(findViewById(R.id.getlocationbutton), "I need access to location..", Snackbar.LENGTH_INDEFINITE)
                                        .setAction(android.R.string.ok, new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                getPermission();
                                            }
                                        }).show();
                            } else {
                                getPermission();
                            }
                        }
                    }
                };
            }
        };
    }
}
