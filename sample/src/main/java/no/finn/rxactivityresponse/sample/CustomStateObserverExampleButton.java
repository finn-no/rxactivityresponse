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

import no.finn.android.rx.BaseStateObservable;
import no.finn.android.rx.GetPermissionObservable;
import no.finn.android.rx.GetPermissionStatusObservable;
import no.finn.android.rx.PermissionResult;
import no.finn.android.rx.PlayServicesBaseObservable;
import no.finn.android.rx.RxState;
import no.finn.android.rx.RxStateRestart;
import no.finn.android.rx.UserAbortedException;

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

public class CustomStateObserverExampleButton extends Button implements View.OnClickListener, RxStateRestart {
    private static final String GOOGLE_PLUS_SCOPES = Scopes.PLUS_LOGIN + " " + "email";
    private final RxState state;

    public CustomStateObserverExampleButton(Context context, AttributeSet attrs) {
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


        private void resetClientForDemoPurposes(GoogleApiClient client) {
            Plus.AccountApi.revokeAccessAndDisconnect(client);
        }

        @Override
        public void onGoogleApiClientReady(final Subscriber<? super String> subscriber, final GoogleApiClient client) {
            if (activityResultCanceled(STATE_NAME)) {
                // always check and handle responses after recieveStateResponse
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
                        resetClientForDemoPurposes(client);
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
                                // always trigger recieveStateResponse before startActivityForResult (or permission...)
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


        public static class GoogleLoginCanceledException extends UserAbortedException {

        }
    }

    @Override
    public void rxAction(int requestCode) {
        Assert.assertEquals(requestCode, ActivityResponses.GET_LOGINTOKEN);
        final Activity activity = (Activity) getContext();
        String[] permissions = new String[]{Manifest.permission.GET_ACCOUNTS};

        final Scope[] scopes = {new Scope(Scopes.PROFILE), new Scope(Scopes.EMAIL)};
        SnackbarRationaleOperator rationaleOperator = new SnackbarRationaleOperator(this, "Need permission for ...");

        getLoginToken(activity, permissions, scopes, rationaleOperator)
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

    private Observable<String> getLoginToken(final Activity activity, String[] permissions, final Scope[] scopes, final SnackbarRationaleOperator rationale) {
        // we cant use RxPermission.getPermission directly, as it triggers state.reset() mid flow.
        return Observable.create(new GetPermissionStatusObservable(activity, permissions))
                .flatMap(new Func1<PermissionResult, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(PermissionResult permissionResult) {
                        return Observable.create(new GetPermissionObservable(activity, state, rationale, permissionResult));
                    }
                })
                .flatMap(new Func1<Boolean, Observable<String>>() {
                    @Override
                    public Observable<String> call(Boolean granted) {
                        if (granted) {
                            return Observable.create(new GoogleLoginObservable(activity, state, scopes, Plus.API))
                                    .subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread());
                        }
                        return Observable.error(new UserAbortedException());
                    }
                })
                .compose(new BaseStateObservable.EndStateTransformer<String>(state));
    }


    @Override
    public void onClick(View v) {
        rxAction(ActivityResponses.GET_LOGINTOKEN);
    }
}
