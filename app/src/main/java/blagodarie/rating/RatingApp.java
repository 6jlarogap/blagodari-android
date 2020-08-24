package blagodarie.rating;

import android.app.Application;
import android.util.Log;

import blagodarie.rating.ui.user.UserActivity;

public final class RatingApp
        extends Application {

    private static final String TAG = UserActivity.class.getSimpleName();

    @Override
    public final void onCreate () {
        Log.d(TAG, "start RatingApp");
        super.onCreate();
    }
}
