package no.finntech.android.rx;

import java.io.IOException;

final class GoogleApiConnectionSuspended extends IOException {
    public GoogleApiConnectionSuspended(int reason) {
        super("Reason : " + reason);
    }
}
