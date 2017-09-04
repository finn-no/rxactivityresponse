package no.finn.rxactivityresponse.sample;

import android.Manifest;
import android.accounts.Account;
import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;

import junit.framework.Assert;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.observers.ResourceObserver;
import io.reactivex.schedulers.Schedulers;
import no.finn.android.rx.BaseStateObservable;
import no.finn.android.rx.GetPermissionObservable;
import no.finn.android.rx.GetPermissionStatusObservable;
import no.finn.android.rx.PermissionResult;
import no.finn.android.rx.PlayServicesBaseObservable;
import no.finn.android.rx.RxState;
import no.finn.android.rx.RxStateRestart;
import no.finn.android.rx.UserAbortedException;

public class CustomStateObserverExampleButton extends AppCompatButton implements View.OnClickListener, RxStateRestart {
    private static final String GOOGLE_PLUS_SCOPES = Scopes.PLUS_LOGIN + " " + "email";
    private final RxState state;

    public CustomStateObserverExampleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnClickListener(this);
        state = RxState.get(context, ActivityResponses.GET_LOGINTOKEN, this);
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
        public void onGoogleApiClientReady(final ResourceObserver<String> emitter, final GoogleApiClient client) {
            if (activityResultCanceled(STATE_NAME)) {
                // always check and handle responses after recieveStateResponse
                emitter.onError(new GoogleLoginCanceledException());
                return;
            }
            final Disposable disposable = Observable.create(new ObservableOnSubscribe<String>() {
                @Override
                public void subscribe(ObservableEmitter<String> subscriber) {
                    //noinspection MissingPermission
                    final String accountName = Plus.AccountApi.getAccountName(client);
                    final Account account = new Account(accountName, GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
                    final String scopes = "oauth2:" + GOOGLE_PLUS_SCOPES;
                    try {
                        subscriber.onNext(GoogleAuthUtil.getToken(activity.getApplicationContext(), account, scopes));
                        resetClientForDemoPurposes(client);
                        subscriber.onComplete();
                    } catch (Exception e) {
                        subscriber.onError(e);
                    }
                }
            }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<String>() {
                        @Override
                        public void accept(String s) {
                            emitter.onNext(s);
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) {
                            if (throwable instanceof UserRecoverableAuthException) {
                                // always trigger recieveStateResponse before startActivityForResult (or permission...)
                                recieveStateResponse(STATE_NAME);
                                activity.startActivityForResult(((UserRecoverableAuthException) throwable).getIntent(), getRequestCode());
                                emitter.onComplete();
                            } else {
                                emitter.onError(throwable);
                            }
                        }
                    });
            emitter.add(disposable);
        }


        public static class GoogleLoginCanceledException extends UserAbortedException {

        }
    }

    @Override
    public void rxAction(int requestCode) {
        Assert.assertEquals(requestCode, ActivityResponses.GET_LOGINTOKEN);
        final Activity activity = (Activity) getContext();
        final String[] permissions = new String[]{Manifest.permission.GET_ACCOUNTS};

        final Scope[] scopes = {new Scope(Scopes.PROFILE), new Scope(Scopes.EMAIL)};

        // NB : Rationale is optional and can be null
        final SnackbarRationaleOperator rationaleOperator = new SnackbarRationaleOperator(this, "Need permission for ...");

        getLoginToken(activity, permissions, scopes, rationaleOperator)
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) {
                        Toast.makeText(getContext(), "Token is : " + s, Toast.LENGTH_SHORT).show();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        Toast.makeText(getContext(), "Exception : " + throwable, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private Observable<String> getLoginToken(final Activity activity, final String[] permissions, final Scope[] scopes, final SnackbarRationaleOperator rationale) {
        // we cant use RxPermission.getPermission directly, as it triggers state.reset() mid flow.
        return Observable.create(new GetPermissionStatusObservable(activity, permissions))
                .flatMap(new Function<PermissionResult, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> apply(PermissionResult permissionResult) {
                        return Observable.create(new GetPermissionObservable(activity, state, rationale, permissionResult));
                    }
                })
                .flatMap(new Function<Boolean, Observable<String>>() {
                    @Override
                    public Observable<String> apply(Boolean granted) {
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
