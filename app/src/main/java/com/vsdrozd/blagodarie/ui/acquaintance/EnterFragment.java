package com.vsdrozd.blagodarie.ui.acquaintance;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ex.diagnosticlib.Diagnostic;
import com.vsdrozd.blagodarie.R;
import com.vsdrozd.blagodarie.databinding.EnterFragmentBinding;
import com.vsdrozd.blagodarie.ui.GoogleSignInListener;

public class EnterFragment extends Fragment {

    private EnterFragmentBinding enterFragmentBinding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Diagnostic.i();
        enterFragmentBinding = EnterFragmentBinding.inflate(inflater, container, false);
        View view = enterFragmentBinding.getRoot();
        initViews(view);
        return view;
    }

    private void initViews (View view) {
        Diagnostic.i();
        view.findViewById(R.id.btnSignIn).setOnClickListener(v -> GoogleSignInListener.getInstance().signIn(getActivity()));
    }

    @Override
    public void onActivityCreated (@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Diagnostic.i();
        enterFragmentBinding.setAcquaintanceNavigator((AcquaintanceNavigator) getActivity());
    }

    @Override
    public void onResume () {
        super.onResume();
    }

    @Override
    public void onStart () {
        super.onStart();
        GoogleSignInListener.getInstance().signIn(getActivity());
    }
}
