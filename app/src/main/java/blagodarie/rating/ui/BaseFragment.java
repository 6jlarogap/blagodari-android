package blagodarie.rating.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;

import org.jetbrains.annotations.NotNull;

public abstract class BaseFragment<ViewModelType extends ViewModel, BindingType extends ViewDataBinding>
        extends Fragment {

    private static final String TAG = BaseFragment.class.getSimpleName();

    private ViewModelType mViewModel;

    private BindingType mBinding;

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

    abstract void initBinding (
            @NonNull final LayoutInflater inflater,
            @Nullable final ViewGroup container
    );

    abstract void initViewModel ();

    protected ViewModelType getViewModel () {
        return mViewModel;
    }

    protected BindingType getBinding () {
        return mBinding;
    }
}
