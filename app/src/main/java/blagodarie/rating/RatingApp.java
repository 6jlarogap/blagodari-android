package blagodarie.rating;

import android.app.Application;
import android.util.Log;

public final class RatingApp
        extends Application {

    private static final String TAG = RatingApp.class.getSimpleName();

    @Override
    public final void onCreate () {
        Log.d(TAG, "start RatingApp");
        super.onCreate();
    }
}
