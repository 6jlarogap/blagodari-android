package blagodarie.rating.ui.graph;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import blagodarie.rating.R;
import blagodarie.rating.databinding.GraphFragmentBinding;

public final class GraphFragment
        extends Fragment {

    private static final String TAG = GraphFragment.class.getSimpleName();

    private GraphFragmentBinding mBinding;

    @NotNull
    @Override
    public View onCreateView (
            @NonNull final LayoutInflater inflater,
            @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState
    ) {
        Log.d(TAG, "onCreateView");
        initBinding(inflater, container);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated (
            @NonNull final View view,
            @Nullable final Bundle savedInstanceState
    ) {
        Log.d(TAG, "onViewCreated");
        super.onViewCreated(view, savedInstanceState);

        final GraphFragmentArgs args = GraphFragmentArgs.fromBundle(requireArguments());

        final UUID userId = args.getUserId();

        mBinding.wbGraph.setWebViewClient(new WebViewClient());
        mBinding.wbGraph.getSettings().setJavaScriptEnabled(true);
        mBinding.wbGraph.getSettings().setLoadWithOverviewMode(true);
        mBinding.wbGraph.getSettings().setUseWideViewPort(true);
        mBinding.wbGraph.getSettings().setBuiltInZoomControls(true);
        mBinding.wbGraph.getSettings().setSupportZoom(true);
        if (userId == null) {
            mBinding.wbGraph.loadUrl(getString(R.string.url_social_graph) + "?from_app=1");
        } else {
            mBinding.wbGraph.loadUrl(getString(R.string.url_profile, userId.toString()) + "&from_app=1");
        }
    }

    private void initBinding (
            @NonNull final LayoutInflater inflater,
            @Nullable final ViewGroup container
    ) {
        Log.d(TAG, "initBinding");
        mBinding = GraphFragmentBinding.inflate(inflater, container, false);
    }

}
