package no.finn.rxactivityresponse.sample;

import java.io.IOException;

import android.Manifest;
import android.accounts.Account;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import no.finn.android.rx.PlayServicesPermissionsConnectionOperator;
import no.finn.android.rx.RxActivityResponseDelegate;
import no.finn.android.rx.RxPermission;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class RxLoginExample extends Button implements View.OnClickListener {
    private static final String GOOGLE_PLUS_SCOPES = Scopes.PLUS_LOGIN + " " + "email";
    private ResponseHandler responseHandler = new ResponseHandler();

    public RxLoginExample(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnClickListener(this);
    }

    private static class GoogleLoginTransformer implements Observable.Transformer<GoogleApiClient, String> {
        private static Handler handler = new Handler(Looper.getMainLooper());
        private final Activity activity;
        private final ResponseHandler responseHandler;

        public GoogleLoginTransformer(Activity activity, ResponseHandler responseHandler) {
            this.activity = activity;
            this.responseHandler = responseHandler;
        }

        @Override
        public Observable<String> call(Observable<GoogleApiClient> o) {
            return o.observeOn(Schedulers.io())
                    .lift(new Observable.Operator<String, GoogleApiClient>() {
                              @Override
                              public Subscriber<? super GoogleApiClient> call(final Subscriber<? super String> subscriber) {
                                  final Subscriber<GoogleApiClient> s = new Subscriber<GoogleApiClient>() {
                                      @Override
                                      public void onCompleted() {
                                          subscriber.onCompleted();
                                      }

                                      @Override
                                      public void onError(Throwable throwable) {
                                          subscriber.onError(throwable);
                                      }

                                      @Override
                                      public void onNext(GoogleApiClient client) {
                                          String accountName = Plus.AccountApi.getAccountName(client);
                                          Account account = new Account(accountName, GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
                                          String scopes = "oauth2:" + GOOGLE_PLUS_SCOPES;
                                          try {
                                              subscriber.onNext(GoogleAuthUtil.getToken(activity.getApplicationContext(), account, scopes));
                                              subscriber.onCompleted();
                                          } catch (IOException e) {
                                              subscriber.onError(e);
                                          } catch (final UserRecoverableAuthException e) {
                                              handler.post(new Runnable() {
                                                  @Override
                                                  public void run() {
                                                      RxActivityResponseDelegate rxActivityResponseDelegate = RxActivityResponseDelegate.get(activity);
                                                      if (!rxActivityResponseDelegate.hasActiveResponse()) {
                                                          rxActivityResponseDelegate.setResponse(responseHandler);
                                                          activity.startActivityForResult(e.getIntent(), rxActivityResponseDelegate.getRequestCode());
                                                      }
                                                  }
                                              });
                                              subscriber.onCompleted();
                                          } catch (final GoogleAuthException e) {
                                              subscriber.onError(e);
                                          }
                                      }
                                  };
                                  subscriber.add(s);
                                  return s;
                              }
                          }
                    ).observeOn(AndroidSchedulers.mainThread());
        }
    }

    public void getLoginToken() {
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

        RxPermission.getPermission(activity, responseHandler, rationaleOperator, permissions)
                .lift(new PlayServicesPermissionsConnectionOperator(activity, responseHandler, scopes, Plus.API))
                .compose(new GoogleLoginTransformer((Activity) getContext(), responseHandler))
                .subscribe(new Action1<String>() {
                               @Override
                               public void call(String s) {
                                   Toast.makeText(getContext(), "Token is : " + s, Toast.LENGTH_SHORT).show();
                               }
                           }
                );
    }


    @Override
    public void onClick(View v) {
        getLoginToken();
    }

    // This class handles restarting the permission request when a permission is retrieved. Since this class can be serialized
    // we could also pass extra arguments through here and back to the getLocation function.
    private static class ResponseHandler extends RxActivityResponseDelegate.RxResponseHandler implements Parcelable {
        @Override
        public void onRequestPermissionsResult(Activity activity, String[] permissions, int[] grantResults) {
            if (RxPermission.allPermissionsGranted(grantResults)) {
                ((RxLoginExample) activity.findViewById(R.id.getlogintoken)).getLoginToken();
            }
            // optionally you can handle a "permission denied" scenario here.
        }

        @Override
        public void onActivityResult(Activity activity, int resultCode, Intent data) {
            if (resultCode == Activity.RESULT_OK) {
                ((RxLoginExample) activity.findViewById(R.id.getlogintoken)).getLoginToken();
            }
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {

        }

        public static final Creator CREATOR = new Creator() {
            public ResponseHandler createFromParcel(Parcel in) {
                return new ResponseHandler();
            }

            public ResponseHandler[] newArray(int size) {
                return new ResponseHandler[size];
            }
        };
    }
}
