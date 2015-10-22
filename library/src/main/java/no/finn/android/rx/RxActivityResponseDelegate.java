package no.finn.android.rx;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import no.finn.rxactivityresponse.BuildConfig;

public class RxActivityResponseDelegate {
    private static final String TAG = "RxActivityResponse";
    private static final String SYSTEMSERVICE_NAME = "RxActivityResponseDelegate";

    private int requestCode;
    private BundleSerializer serializer;

    private RxResponseHandler currentResponseHandler;

    public RxActivityResponseDelegate(int requestCode, BundleSerializer serializer) {
        this.requestCode = requestCode;
        this.serializer = serializer;
    }

    public int getRequestCode() {
        return requestCode;
    }

    @SuppressWarnings("ResourceType")
    public static RxActivityResponseDelegate get(Context context) {
        return (RxActivityResponseDelegate) context.getSystemService(SYSTEMSERVICE_NAME);
    }

    public boolean hasActiveResponse() {
        return currentResponseHandler != null;
    }

    public void setResponse(RxResponseHandler rxResponseHandler) {
        if (BuildConfig.DEBUG) {
            try {
                Bundle bundle = new Bundle();
                serializer.serialize(bundle, currentResponseHandler);
            } catch (Exception e) {
                Log.e(TAG, "Failed to serialize currentResponseHandler");
                e.printStackTrace();
            }

            if (hasActiveResponse()) {
                throw new IllegalStateException("setResponse called with hasActiveResponse negative");
            }
        }
        this.currentResponseHandler = rxResponseHandler;
    }

    public void onSaveInstanceState(Bundle outState) {
        if (currentResponseHandler != null) {
            serializer.serialize(outState, currentResponseHandler);
        }
    }

    public RxActivityResponseDelegate getSystemService(String name) {
        if (SYSTEMSERVICE_NAME.equals(name)) {
            return this;
        }
        return null;
    }

    public void onRequestPermissionsResult(Activity activity, int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == RxActivityResponseDelegate.this.requestCode && currentResponseHandler != null) {
            currentResponseHandler.onRequestPermissionsResult(activity, permissions, grantResults);
            currentResponseHandler = null;
        }
    }

    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        if (requestCode == RxActivityResponseDelegate.this.requestCode && currentResponseHandler != null) {
            currentResponseHandler.onActivityResult(activity, resultCode, data);
            currentResponseHandler = null;
        }
    }


    public void onCreate(Bundle savedInstanceState) {
        currentResponseHandler = serializer.deserialize(savedInstanceState);
    }

    public static class RxResponseHandler {
        public void onActivityResult(Activity activity, int resultCode, Intent data) {

        }

        public void onRequestPermissionsResult(Activity activity, String[] permissions, int[] grantResults) {

        }
    }

    public interface BundleSerializer {
        void serialize(Bundle bundle, RxResponseHandler rxResponseHandler);

        RxResponseHandler deserialize(Bundle bundle);
    }
}
