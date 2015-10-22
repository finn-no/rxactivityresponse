package no.finn.rxactivityresponse.sample;

import android.os.Bundle;
import android.os.Parcelable;

import no.finn.android.rx.RxActivityResponseDelegate;

class Serializer implements RxActivityResponseDelegate.BundleSerializer {

    @Override
    public void serialize(Bundle bundle, RxActivityResponseDelegate.RxResponseHandler rxResponseHandler) {
        bundle.putParcelable("RxActivityResponseDelegateObject", ((Parcelable) rxResponseHandler));
    }

    @Override
    public RxActivityResponseDelegate.RxResponseHandler deserialize(Bundle bundle) {
        if (bundle != null && bundle.containsKey("RxActivityResponseDelegateObject")) {
            return bundle.getParcelable("RxActivityResponseDelegateObject");
        } return null;
    }
}
