package blagodarie.rating.auth;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public final class AuthenticatorService
        extends Service {

    private static final String TAG = AuthenticatorService.class.getSimpleName();

    @Override
    public IBinder onBind (final Intent intent) {
        Log.d(TAG, "onBind");
        return new Authenticator(this).getIBinder();
    }

}