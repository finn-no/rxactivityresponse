package no.finn.rxactivityresponse.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import no.finn.android.rx.RxActivityStateDelegate;

public class SerializedRxSampleActivity extends AppCompatActivity {
    private final RxActivityStateDelegate rxActivityStateDelegate = new RxActivityStateDelegate();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        rxActivityStateDelegate.onCreate(savedInstanceState);
    }

    @Override
    public Object getSystemService(@NonNull String name) {
        Object o = rxActivityStateDelegate.getSystemService(name);
        return o != null ? o : super.getSystemService(name);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        rxActivityStateDelegate.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        rxActivityStateDelegate.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        rxActivityStateDelegate.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


}
