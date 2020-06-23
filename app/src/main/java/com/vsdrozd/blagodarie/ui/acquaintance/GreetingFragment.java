package com.vsdrozd.blagodarie.ui.acquaintance;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ex.diagnosticlib.Diagnostic;
import com.vsdrozd.blagodarie.databinding.GreetingFragmentBinding;


public class GreetingFragment extends Fragment {

    private GreetingFragmentBinding greetingFragmentBinding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Diagnostic.i();
        greetingFragmentBinding = GreetingFragmentBinding.inflate(inflater, container, false);
        return greetingFragmentBinding.getRoot();
    }

    @Override
    public void onActivityCreated (@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Diagnostic.i();
        greetingFragmentBinding.setAcquaintanceNavigator((AcquaintanceNavigator) getActivity());
    }
}
