package no.finn.rxactivityresponse.sample;

import android.os.Bundle;
import android.os.Parcelable;

import no.finn.android.rx.RxActivityResponseDelegate;
import no.finn.android.rx.RxResponseHandler;

class Serializer implements RxActivityResponseDelegate.BundleSerializer {

    @Override
    public void serialize(Bundle bundle, RxResponseHandler rxResponseHandler) {
        bundle.putParcelable("RxActivityResponseDelegateObject", ((Parcelable) rxResponseHandler));
    }

    @Override
    public RxResponseHandler deserialize(Bundle bundle) {
        if (bundle != null && bundle.containsKey("RxActivityResponseDelegateObject")) {
            return bundle.getParcelable("RxActivityResponseDelegateObject");
        } return null;
    }
}
