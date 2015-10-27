package no.finn.rxactivityresponse.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import no.finn.android.rx.tmpnew.PersistedActivityResultActivityDelegate;
import no.finn.android.rx.RxActivityResponseDelegate;

public class SerializedRxSampleActivity extends AppCompatActivity {
    private Serializer serializer = new Serializer();
    private static final int RXACTIVITYREQUESTCODE = 123;
    RxActivityResponseDelegate rxActivityResponseDelegate = new RxActivityResponseDelegate(RXACTIVITYREQUESTCODE, serializer); // NB : should be a singleton
    PersistedActivityResultActivityDelegate persistedActivityResultActivityDelegate = new PersistedActivityResultActivityDelegate();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        rxActivityResponseDelegate.onCreate(savedInstanceState);
        persistedActivityResultActivityDelegate.onCreate(savedInstanceState);
    }

    @Override
    public Object getSystemService(@NonNull String name) {
        {
            Object o = persistedActivityResultActivityDelegate.getSystemService(name);
            if (o != null) {
                return o;
            }
        }
        Object o = rxActivityResponseDelegate.getSystemService(name);
        return o != null ? o : super.getSystemService(name);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        persistedActivityResultActivityDelegate.onSaveInstanceState(outState);
        rxActivityResponseDelegate.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        rxActivityResponseDelegate.onActivityResult(this, requestCode, resultCode, data);
        persistedActivityResultActivityDelegate.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        rxActivityResponseDelegate.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
        persistedActivityResultActivityDelegate.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


}
