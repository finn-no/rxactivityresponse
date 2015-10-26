package no.finn.android.rx;


import android.app.Activity;

import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import rx.Subscriber;

public class PlayServicesObservable extends PlayServicesBaseObservable<GoogleApiClient> {
    @SafeVarargs
    public PlayServicesObservable(Activity activity, RxResponseHandler responseHandler, Api<? extends Api.ApiOptions.NotRequiredOptions>... services) {
        super(activity, responseHandler, services);
    }

    @SafeVarargs
    public PlayServicesObservable(Activity activity, RxResponseHandler responseHandler, Scope[] scopes, Api<? extends Api.ApiOptions.NotRequiredOptions>... services) {
        super(activity, responseHandler, scopes, services);
    }

    @Override
    public void onGoogleApiClientReady(Subscriber<? super GoogleApiClient> subscriber, GoogleApiClient client) {
        subscriber.onNext(client);
    }
}
