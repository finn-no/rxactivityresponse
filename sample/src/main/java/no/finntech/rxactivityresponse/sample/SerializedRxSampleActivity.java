package no.finntech.rxactivityresponse.sample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import no.finntech.android.rx.RxActivityResponseDelegate;

public class SerializedRxSampleActivity extends Activity {
    private Serializer serializer = new Serializer();
    private static final int RXACTIVITYREQUESTCODE = 123;
    RxActivityResponseDelegate rxActivityResponseDelegate = new RxActivityResponseDelegate(RXACTIVITYREQUESTCODE, serializer); // NB : should be a singleton

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        rxActivityResponseDelegate.onCreate(savedInstanceState);
    }

    @Override
    public Object getSystemService(@NonNull String name) {
        Object o = rxActivityResponseDelegate.getSystemService(name);
        return o != null ? o : super.getSystemService(name);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        rxActivityResponseDelegate.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        rxActivityResponseDelegate.onActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        rxActivityResponseDelegate.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }


}
