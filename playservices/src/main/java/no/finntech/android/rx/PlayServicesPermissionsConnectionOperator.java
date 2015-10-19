package no.finntech.android.rx;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

// Used for use with Permissions api. Only opens connection if the boolean is true. - NB : same as above, make sure you unsubscribe!
public class PlayServicesPermissionsConnectionOperator implements Observable.Operator<GoogleApiClient, Boolean> {
    // Exists to allow consumers of play services to unregister from events before we disconnect.
    private static Handler unsubscribeHandler = new Handler();

    private final Context context;
    private final Api<? extends Api.ApiOptions.NotRequiredOptions>[] services;
    private GoogleApiClient client;

    public PlayServicesPermissionsConnectionOperator(Context context, Api<? extends Api.ApiOptions.NotRequiredOptions>... services) {
        this.context = context;
        this.services = services;
    }

    @Override
    public Subscriber<? super Boolean> call(final Subscriber<? super GoogleApiClient> subscriber) {
        Subscriber<Boolean> s = new Subscriber<Boolean>() {
            @Override
            public void onCompleted() {
                subscriber.onCompleted();
            }

            @Override
            public void onError(Throwable throwable) {
                subscriber.onError(throwable);
            }

            @Override
            public void onNext(Boolean permissionGranted) {
                if (permissionGranted) {
                    GoogleApiClient.Builder builder = new GoogleApiClient.Builder(context);
                    for (Api<? extends Api.ApiOptions.NotRequiredOptions> service : services) {
                        builder.addApi(service);
                    }
                    builder.addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle bundle) {
                            if (!subscriber.isUnsubscribed()) {
                                subscriber.onNext(client);
                                // we cant trigger completed, because that'll trigger a unsubscribe which invalidates this client.
                                // also this isn't technically "completed" as the result has an open connection until disconnected.
                            } else {
                                subscriber.unsubscribe();
                            }
                        }

                        @Override
                        public void onConnectionSuspended(int reason) {
                            subscriber.onError(new GoogleApiConnectionSuspended(reason));
                        }
                    });
                    builder.addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult connectionResult) {
                            subscriber.onError(new GoogleApiConnectionFailed(connectionResult));
                        }
                    });
                    client = builder.build();
                    client.connect();
                    subscriber.add(Subscriptions.create(new Action0() {
                        @Override
                        public void call() {
                            unsubscribeHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (client.isConnected() || client.isConnecting()) {
                                        client.disconnect();
                                    }
                                }
                            });
                        }
                    }));
                }
            }
        };
        subscriber.add(s);
        return s;
    }
}
