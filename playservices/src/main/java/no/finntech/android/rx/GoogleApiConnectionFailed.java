package no.finntech.android.rx;

import java.io.IOException;

import com.google.android.gms.common.ConnectionResult;

final class GoogleApiConnectionFailed extends IOException {
    public GoogleApiConnectionFailed(ConnectionResult reason) {
        super("Reason : " + reason);
    }
}
