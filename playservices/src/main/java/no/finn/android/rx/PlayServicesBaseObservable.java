package no.finn.android.rx;

import android.app.Activity;
import android.content.IntentSender;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.functions.Cancellable;
import io.reactivex.observers.ResourceObserver;

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

    public abstract void onGoogleApiClientReady(ResourceObserver<T> emitter, GoogleApiClient client);

    public void onUnsubscribe() {
        disconnect();
    }

    public void disconnect() {
        if (client.isConnected() || client.isConnecting()) {
            client.disconnect();
        }
    }

    @Override
    public void subscribe(final ObservableEmitter<T> emitter) {
        if (activityResultCanceled(STATE_NAME)) {
            emitter.onError(new PlayServicesConnectionCanceledException());
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
                ResourceObserver<T> resourceObserver = new ResourceObserver<T>() {
                    @Override
                    public void onNext(T t) {
                        emitter.onNext(t);
                    }

                    @Override
                    public void onError(Throwable e) {
                        emitter.onError(e);
                    }

                    @Override
                    public void onComplete() {
                        emitter.onComplete();
                    }
                };


                onGoogleApiClientReady(resourceObserver, client);
            }

            @Override
            public void onConnectionSuspended(int reason) {
                emitter.onError(new GoogleApiConnectionSuspended(reason));
            }
        });
        builder.addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(final ConnectionResult connectionResult) {
                if (connectionResult.hasResolution()) {
                    resolveConnectionFailed(connectionResult, emitter);
                } else {
                    emitter.onError(new GoogleApiConnectionFailed(connectionResult));
                }
            }
        });
        client = builder.build();
        client.connect();
        emitter.setCancellable(new Cancellable() {
            @Override
            public void cancel() throws Exception {
                onUnsubscribe();
            }
        });
    }

    protected void resolveConnectionFailed(ConnectionResult connectionResult, ObservableEmitter<?> emitter) {
        try {
            recieveStateResponse(STATE_NAME);
            connectionResult.startResolutionForResult(activity, getRequestCode());
        } catch (IntentSender.SendIntentException e) {
            emitter.onError(new GoogleApiConnectionFailed(e));
        }
    }

    public static class PlayServicesConnectionCanceledException extends UserAbortedException {

    }
}
