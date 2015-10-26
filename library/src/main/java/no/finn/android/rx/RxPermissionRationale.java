package no.finn.android.rx;

public abstract class RxPermissionRationale {
    final boolean alwaysShowRationale;

    public RxPermissionRationale() {
        this(false);
    }

    public RxPermissionRationale(boolean alwaysShowRationale) {
        this.alwaysShowRationale = alwaysShowRationale;
    }

    public abstract void showRationale(RequestPermission requestPermission);

    public void onUnsubscribe() {

    }

    public interface RequestPermission {
        void showRationale();
    }
}
