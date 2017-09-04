package no.finn.android.rx;


import android.app.Activity;

import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;

import io.reactivex.observers.ResourceObserver;

public class PlayServicesObservable extends PlayServicesBaseObservable<GoogleApiClient> {
    @SafeVarargs
    public PlayServicesObservable(Activity activity, RxState state, Api<? extends Api.ApiOptions.NotRequiredOptions>... services) {
        super(activity, state, null, services);
    }

    @SafeVarargs
    public PlayServicesObservable(Activity activity, RxState state, Scope[] scopes, Api<? extends Api.ApiOptions.NotRequiredOptions>... services) {
        super(activity, state, scopes, services);
    }

    @Override
    public void onGoogleApiClientReady(ResourceObserver<GoogleApiClient> emitter, GoogleApiClient client) {
        emitter.onNext(client);
    }
}
