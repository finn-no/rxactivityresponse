package no.finn.android.rx;

public interface RxPermissionRationale {
    void showRationale(RequestPermission requestPermission);

    interface RequestPermission {
        void execute();
    }
}
