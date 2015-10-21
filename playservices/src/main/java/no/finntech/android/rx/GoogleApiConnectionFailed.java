package no.finntech.android.rx;

import java.io.IOException;

import android.content.IntentSender;

import com.google.android.gms.common.ConnectionResult;

final class GoogleApiConnectionFailed extends IOException {
    public GoogleApiConnectionFailed(ConnectionResult reason) {
        super("Reason : " + reason);
    }


    public GoogleApiConnectionFailed(IntentSender.SendIntentException e) {
        super(e);
    }
}
