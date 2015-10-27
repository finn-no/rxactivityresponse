package no.finn.android.rx;

import android.app.Activity;
import android.content.IntentSender;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import rx.Subscriber;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

//NB : all connection responses will come back on the main thread!
public abstract class PlayServicesBaseObservable<T> extends BaseStateObservable<T> {
    private static final String STATE_NAME = "BasePlayServices";
    public final Activity activity;
    private final Scope[] scopes;
    private final Api<? extends Api.ApiOptions.NotRequiredOptions>[] services;
    private GoogleApiClient client;

    @SafeVarargs
    public PlayServicesBaseObservable(Activity activity, RxState state, Api<? extends Api.ApiOptions.NotRequiredOptions>... services) {
        this(activity, state, null, services);
    }

    @SafeVarargs
    public PlayServicesBaseObservable(Activity activity, RxState state, Scope[] scopes, Api<? extends Api.ApiOptions.NotRequiredOptions>... services) {
        super(state);
        this.activity = activity;
        this.scopes = scopes;
        this.services = services;
    }

    public abstract void onGoogleApiClientReady(Subscriber<? super T> subscriber, GoogleApiClient client);

    public void onUnsubscribe() {
        disconnect();
    }

    public void disconnect() {
        if (client.isConnected() || client.isConnecting()) {
            client.disconnect();
        }
    }

    @Override
    public void call(final Subscriber<? super T> subscriber) {
        if (activityResultCanceled(STATE_NAME)) {
            subscriber.onError(new PlayServicesConnectionCanceledException());
            return;
        }
        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(activity);
        for (Api<? extends Api.ApiOptions.NotRequiredOptions> service : services) {
            builder.addApi(service);
        }
        if (scopes != null) {
            for (Scope scope : scopes) {
                builder.addScope(scope);
            }
        }
        builder.addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(Bundle bundle) {
                onGoogleApiClientReady(subscriber, client);
            }

            @Override
            public void onConnectionSuspended(int reason) {
                subscriber.onError(new GoogleApiConnectionSuspended(reason));
            }
        });
        builder.addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(final ConnectionResult connectionResult) {
                if (connectionResult.hasResolution()) {
                    resolveConnectionFailed(connectionResult, subscriber);
                } else {
                    subscriber.onError(new GoogleApiConnectionFailed(connectionResult));
                }
            }
        });
        client = builder.build();
        client.connect();
        subscriber.add(Subscriptions.create(new Action0() {
            @Override
            public void call() {
                onUnsubscribe();
            }
        }));
    }

    protected void resolveConnectionFailed(ConnectionResult connectionResult, Subscriber<?> subscriber) {
        try {
            recieveStateResponse(STATE_NAME);
            connectionResult.startResolutionForResult(activity, getRequestCode());
        } catch (IntentSender.SendIntentException e) {
            subscriber.onError(new GoogleApiConnectionFailed(e));
        }
    }

    public static class PlayServicesConnectionCanceledException extends ActivityResultState.ActivityResultCanceledException {

    }
}
