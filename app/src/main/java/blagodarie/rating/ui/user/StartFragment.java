package blagodarie.rating.ui.user;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public final class StartFragment
        extends Fragment {

    private static final String TAG = blagodarie.rating.auth.StartFragment.class.getSimpleName();

    @Override
    public View onCreateView (
            @NonNull final LayoutInflater inflater,
            final ViewGroup container,
            final Bundle savedInstanceState
    ) {
        Log.d(TAG, "onCreateView");
        return inflater.inflate(blagodarie.rating.auth.R.layout.start_fragment, container, false);
    }
}
