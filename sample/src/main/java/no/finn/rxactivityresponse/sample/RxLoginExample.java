package no.finn.rxactivityresponse.sample;

import java.lang.ref.WeakReference;

import android.Manifest;
import android.accounts.Account;
import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import no.finn.android.rx.ActivityResultState;
import no.finn.android.rx.GetPermissionObservable;
import no.finn.android.rx.PlayServicesBaseObservable;
import no.finn.android.rx.RxState;
import no.finn.android.rx.RxStateRestart;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;
import junit.framework.Assert;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class RxLoginExample extends Button implements View.OnClickListener, RxStateRestart {
    private static final String GOOGLE_PLUS_SCOPES = Scopes.PLUS_LOGIN + " " + "email";
    private final RxState state;

    public RxLoginExample(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnClickListener(this);
        state = RxState.get(context, ActivityResponses.GET_LOGINTOKEN, new WeakReference<RxStateRestart>(this));
    }

    private static class GoogleLoginObservable extends PlayServicesBaseObservable<String> {
        private static final String STATE_NAME = "GoogleLoginToken";

        @SafeVarargs
        public GoogleLoginObservable(Activity activity, RxState state, Scope[] scopes, Api<? extends Api.ApiOptions.NotRequiredOptions>... services) {
            super(activity, state, scopes, services);
        }

        @Override
        public void onGoogleApiClientReady(final Subscriber<? super String> subscriber, final GoogleApiClient client) {
            if (activityResultCanceled(STATE_NAME)) {
                subscriber.onError(new GoogleLoginCanceledException());
                return;
            }
            final Subscription s = Observable.create(new Observable.OnSubscribe<String>() {
                @Override
                public void call(Subscriber<? super String> subscriber) {
                    String accountName = Plus.AccountApi.getAccountName(client);
                    Account account = new Account(accountName, GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
                    String scopes = "oauth2:" + GOOGLE_PLUS_SCOPES;
                    try {
                        subscriber.onNext(GoogleAuthUtil.getToken(activity.getApplicationContext(), account, scopes));
                        subscriber.onCompleted();
                    } catch (Exception e) {
                        subscriber.onError(e);
                    }
                }
            }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<String>() {
                        @Override
                        public void call(String s) {
                            subscriber.onNext(s);
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            if (throwable instanceof UserRecoverableAuthException) {
                                recieveStateResponse(STATE_NAME);
                                activity.startActivityForResult(((UserRecoverableAuthException) throwable).getIntent(), getRequestCode());
                                subscriber.onCompleted();
                            } else {
                                subscriber.onError(throwable);
                            }
                        }
                    });
            subscriber.add(s);
        }

        public static class GoogleLoginCanceledException extends ActivityResultState.ActivityResultCanceledException {

        }
    }

    @Override
    public void rxAction(int requestCode) {
        Assert.assertEquals(requestCode, ActivityResponses.GET_LOGINTOKEN);
        final Activity activity = (Activity) getContext();
        String[] permissions = new String[]{Manifest.permission.GET_ACCOUNTS};

        final Scope[] scopes = {new Scope(Scopes.PROFILE), new Scope(Scopes.EMAIL)};
        SnackbarRationaleOperator rationaleOperator = new SnackbarRationaleOperator(this, "Need permission for ...");

        // Example code to revoke play services to reset the flow (play services doesn't ask again once an app has been given access. You dont want this in production, but it's handy for testing
        /*RxPlayServices.getPlayServices((Activity) getContext(), responseHandler, permissions, scopes, Plus.API).subscribe(new Action1<GoogleApiClient>() {
            @Override
            public void call(GoogleApiClient client) {
                //probably dont want to do this in "production", but it makes testing easier.
                Plus.AccountApi.revokeAccessAndDisconnect(client);
                client.disconnect();
            }
        });*/
        getLoginToken(activity, permissions, scopes, rationaleOperator);
    }

    private void getLoginToken(final Activity activity, String[] permissions, final Scope[] scopes, SnackbarRationaleOperator rationaleOperator) {
        Observable.create(new GetPermissionObservable(activity, state, rationaleOperator, permissions))
                .flatMap(new Func1<Boolean, Observable<String>>() {
                    @Override
                    public Observable<String> call(Boolean granted) {
                        if (granted) {
                            return Observable.create(new GoogleLoginObservable(activity, state, scopes, Plus.API))
                                    .subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread());
                        }
                        return Observable.empty();
                    }
                })
                .finallyDo(new Action0() {
                    @Override
                    public void call() {
                        state.reset();
                    }
                })
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        Toast.makeText(getContext(), "Token is : " + s, Toast.LENGTH_SHORT).show();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Toast.makeText(getContext(), "Exception : " + throwable, Toast.LENGTH_SHORT).show();
                    }
                });
    }


    @Override
    public void onClick(View v) {
        rxAction(ActivityResponses.GET_LOGINTOKEN);
    }
}
