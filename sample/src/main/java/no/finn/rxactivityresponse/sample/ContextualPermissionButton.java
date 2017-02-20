package no.finn.rxactivityresponse.sample;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import no.finn.android.rx.RxPermission;
import no.finn.android.rx.RxState;
import rx.Subscription;
import rx.functions.Action1;

public class ContextualPermissionButton extends AppCompatButton implements View.OnClickListener {

    private Subscription subscription;

    public ContextualPermissionButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        doSomething(0x1337, 0xdeadbeef, 0xbaadc0de);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        unsubscribe();
    }

    private void unsubscribe() {
        if (subscription != null) {
            subscription.unsubscribe();
            subscription = null;
        }
    }

    private void doSomething(int a, int b, int c) {
        // Function that requires some permission but has arguments. Handle this async, but we don't care about configuration changes.

        unsubscribe();
        subscription = RxPermission
                .getPermission((Activity) getContext(), RxState.get(getContext(), 0x1337), Manifest.permission.WRITE_EXTERNAL_STORAGE)
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
