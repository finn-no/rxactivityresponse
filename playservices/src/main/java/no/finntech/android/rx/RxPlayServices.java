package no.finntech.android.rx;

import java.io.IOException;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.subscriptions.Subscriptions;

public class RxPlayServices {
    // Exists to allow consumers of play services to unregister from events before we disconnect.
    private static Handler unsubscribeHandler = new Handler();

    /*
    NB : Make sure you unsubscribe from this to close the api connection!
     */
    public static Observable<GoogleApiClient> getPlayServices(final Context context, final Api<? extends Api.ApiOptions.NotRequiredOptions>... services) {
        return Observable.create(new Observable.OnSubscribe<GoogleApiClient>() {
            private GoogleApiClient client;

            @Override
            public void call(final Subscriber<? super GoogleApiClient> subscriber) {
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
        });
    }

    public static class ConnectionOperator implements Observable.Operator<GoogleApiClient, Boolean> {
        private final Context context;
        private final Api<? extends Api.ApiOptions.NotRequiredOptions>[] services;
        private GoogleApiClient client;

        public ConnectionOperator(Context context, Api<? extends Api.ApiOptions.NotRequiredOptions>... services) {
            this.context = context;
            this.services = services;
        }

        @Override
        public Subscriber<? super Boolean> call(final Subscriber<? super GoogleApiClient> subscriber) {
            return new Subscriber<Boolean>() {
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
        }
    }

    public static class LocationSettingOperator implements Observable.Operator<GoogleApiClient, GoogleApiClient> {
        private final Activity activity;
        private final LocationRequest locationRequest;
        private final RxActivityResponseDelegate.RxResponseHandler responseHandler;

        public LocationSettingOperator(Activity activity, LocationRequest locationRequest, RxActivityResponseDelegate.RxResponseHandler responseHandler) {
            this.activity = activity;
            this.locationRequest = locationRequest;
            this.responseHandler = responseHandler;
        }

        @Override
        public Subscriber<? super GoogleApiClient> call(final Subscriber<? super GoogleApiClient> subscriber) {
            return new Subscriber<GoogleApiClient>() {
                @Override
                public void onCompleted() {
                    subscriber.onCompleted();
                }

                @Override
                public void onError(Throwable throwable) {
                    subscriber.onError(throwable);
                }

                @Override
                public void onNext(final GoogleApiClient client) {
                    LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                            .setAlwaysShow(true)
                            .addLocationRequest(locationRequest);

                    PendingResult<LocationSettingsResult> pendingResult = LocationServices.SettingsApi.checkLocationSettings(client, builder.build());
                    pendingResult.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                        @Override
                        public void onResult(LocationSettingsResult result) {
                            final Status status = result.getStatus();
                            switch (status.getStatusCode()) {
                                case LocationSettingsStatusCodes.SUCCESS:
                                    subscriber.onNext(client);
                                    break;
                                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                    try {
                                        RxActivityResponseDelegate rxActivityResponseDelegate = RxActivityResponseDelegate.get(activity);
                                        if (!rxActivityResponseDelegate.hasActiveResponse()) {
                                            rxActivityResponseDelegate.setResponse(responseHandler);
                                            status.startResolutionForResult(activity, rxActivityResponseDelegate.getRequestCode());
                                        }
                                        subscriber.unsubscribe(); // silently close our api connection, full restart through responsehandler required, so we can close this connection.
                                    } catch (IntentSender.SendIntentException e) {
                                        subscriber.onError(new RxLocationError(e));
                                    }
                                    break;
                                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                    subscriber.onError(new RxLocationError(LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE));
                                    break;
                            }
                        }
                    });

                }
            };
        }
    }

    public static class LocationOperator implements Observable.Operator<Location, GoogleApiClient> {
        private final LocationRequest locationRequest;

        public LocationOperator(LocationRequest locationRequest) {
            this.locationRequest = locationRequest;
        }

        @Override
        public Subscriber<? super GoogleApiClient> call(final Subscriber<? super Location> subscriber) {
            return new Subscriber<GoogleApiClient>() {
                @Override
                public void onCompleted() {
                    subscriber.onCompleted();
                }

                @Override
                public void onError(Throwable throwable) {
                    subscriber.onError(throwable);
                }

                @Override
                public void onNext(final GoogleApiClient client) {
                    final LocationListener locationListener = new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            subscriber.onNext(location);
                            if (locationRequest.getNumUpdates() == 1) {
                                subscriber.onCompleted();
                            }
                        }
                    };

                    subscriber.add(Subscriptions.create(new Action0() {
                        @Override
                        public void call() {
                            LocationServices.FusedLocationApi.removeLocationUpdates(client, locationListener);
                        }
                    }));
                    LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, locationListener);

                }
            };
        }
    }


    public static Observable<GoogleApiClient> getLocationEnabled(final Activity activity, final LocationRequest locationRequest, final RxActivityResponseDelegate.RxResponseHandler responseHandler) {
        return getLocationEnabled(null, activity, locationRequest, responseHandler);
    }

    public static Observable<GoogleApiClient> getLocationEnabled(final Observable<Boolean> getPermissionObservable, final Activity activity, final LocationRequest locationRequest, final RxActivityResponseDelegate.RxResponseHandler responseHandler) {
        if (getPermissionObservable == null) {
            return getLocationEnabled(RxPermission.getPermission(activity, responseHandler, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), activity, locationRequest, responseHandler);
        }
        return Observable.create(new Observable.OnSubscribe<GoogleApiClient>() {
            @Override
            public void call(final Subscriber<? super GoogleApiClient> subscriber) {
                subscriber.add(getPermissionObservable
                        .subscribe(new Action1<Boolean>() {
                            @Override
                            public void call(Boolean granted) {
                                subscriber.add(RxPlayServices.getPlayServices(activity, LocationServices.API)
                                        .subscribe(new Action1<GoogleApiClient>() {
                                            @Override
                                            public void call(final GoogleApiClient client) {
                                                LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                                                        .setAlwaysShow(true)
                                                        .addLocationRequest(locationRequest);

                                                PendingResult<LocationSettingsResult> pendingResult = LocationServices.SettingsApi.checkLocationSettings(client, builder.build());
                                                pendingResult.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                                                    @Override
                                                    public void onResult(LocationSettingsResult result) {
                                                        final Status status = result.getStatus();
                                                        switch (status.getStatusCode()) {
                                                            case LocationSettingsStatusCodes.SUCCESS:
                                                                subscriber.onNext(client);
                                                                break;
                                                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                                                try {
                                                                    RxActivityResponseDelegate rxActivityResponseDelegate = RxActivityResponseDelegate.get(activity);
                                                                    if (!rxActivityResponseDelegate.hasActiveResponse()) {
                                                                        rxActivityResponseDelegate.setResponse(responseHandler);
                                                                        status.startResolutionForResult(activity, rxActivityResponseDelegate.getRequestCode());
                                                                    }
                                                                    subscriber.unsubscribe(); // silently close our api connection, full restart through responsehandler required, so we can close this connection.
                                                                } catch (IntentSender.SendIntentException e) {
                                                                    subscriber.onError(new RxLocationError(e));
                                                                }
                                                                break;
                                                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                                                subscriber.onError(new RxLocationError(LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE));
                                                                break;
                                                        }
                                                    }
                                                });
                                            }
                                        }));
                            }
                        }));
            }
        });
    }


    public static Observable<Location> getLocation(final Activity activity, final LocationRequest locationRequest, final RxActivityResponseDelegate.RxResponseHandler responseHandler) {
        return getLocation(null, activity, locationRequest, responseHandler);
    }

    public static Observable<Location> getLocation(final Observable<Boolean> getPermissionObservable, final Activity activity, final LocationRequest locationRequest, final RxActivityResponseDelegate.RxResponseHandler responseHandler) {
        return Observable.create(new Observable.OnSubscribe<Location>() {
                                     @Override
                                     public void call(final Subscriber<? super Location> subscriber) {
                                         subscriber.add(getLocationEnabled(getPermissionObservable, activity, locationRequest, responseHandler)
                                                 .subscribe(new Action1<GoogleApiClient>() {
                                                     @Override
                                                     public void call(final GoogleApiClient client) {
                                                         final LocationListener locationListener = new LocationListener() {
                                                             @Override
                                                             public void onLocationChanged(Location location) {
                                                                 subscriber.onNext(location);
                                                                 if (locationRequest.getNumUpdates() == 1) {
                                                                     subscriber.onCompleted();
                                                                 }
                                                             }
                                                         };

                                                         subscriber.add(Subscriptions.create(new Action0() {
                                                             @Override
                                                             public void call() {
                                                                 LocationServices.FusedLocationApi.removeLocationUpdates(client, locationListener);
                                                             }
                                                         }));
                                                         LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, locationListener);
                                                     }
                                                 }));
                                     }
                                 }

        );
    }


    public static class RxLocationError extends IOException {
        public final int settingsChangeUnavailable;

        public RxLocationError(int settingsChangeUnavailable) {
            super("RxLocationError " + settingsChangeUnavailable);
            this.settingsChangeUnavailable = settingsChangeUnavailable;
        }

        public RxLocationError(IntentSender.SendIntentException e) {
            super(e);
            settingsChangeUnavailable = LocationSettingsStatusCodes.RESOLUTION_REQUIRED;
        }
    }

    private static final class GoogleApiConnectionSuspended extends IOException {
        public GoogleApiConnectionSuspended(int reason) {
            super("Reason : " + reason);
        }
    }

    private static final class GoogleApiConnectionFailed extends IOException {
        public GoogleApiConnectionFailed(ConnectionResult reason) {
            super("Reason : " + reason);
        }
    }
}
