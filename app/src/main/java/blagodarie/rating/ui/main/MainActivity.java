package blagodarie.rating.ui.main;

import android.accounts.Account;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public final class MainActivity
        extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String EXTRA_ACCOUNT = "blagodarie.rating.ui.main.ACCOUNT";

    public static Intent createSelfIntent (
            @NonNull final Context context,
            @NonNull final Account account
    ) {
        Log.d(TAG, "createSelfIntent");
        final Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(EXTRA_ACCOUNT, account);
        return intent;
    }
}
