package no.finntech.rxactivityresponse.sample;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.support.design.widget.Snackbar;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import no.finntech.android.rx.RxPermission;

import rx.functions.Action1;

public class RxButtonExample extends Button implements View.OnClickListener {
    private ResponseHandler locationResponseHandler = new ResponseHandler();

    public RxButtonExample(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnClickListener(this);
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
                .lift(new RxPermission.PermissionRationaleOperator((Activity) getContext(), locationResponseHandler, permissions) {
                    @Override
                    public void showRationale() {
                        Snackbar.make(findViewById(R.id.getlocationbutton), "I need access to location..", Snackbar.LENGTH_INDEFINITE)
                                .setAction(android.R.string.ok, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        requestPermission();
                                    }
                                }).show();
                    }
                })
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean permission) {
                        if (permission) {
                            Toast.makeText(getContext(), "Permission is granted and we can do something with it", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
