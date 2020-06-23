package com.vsdrozd.blagodarie.ui.acquaintance;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ex.diagnosticlib.Diagnostic;
import com.vsdrozd.blagodarie.databinding.AgreementFragmentBinding;

public class AgreementFragment extends Fragment {

    private AgreementFragmentBinding agreementFragmentBinding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Diagnostic.i();
        agreementFragmentBinding = AgreementFragmentBinding.inflate(inflater, container, false);
        return agreementFragmentBinding.getRoot();
    }

    @Override
    public void onActivityCreated (@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Diagnostic.i();
        agreementFragmentBinding.setAcquaintanceNavigator((AcquaintanceNavigator) getActivity());
    }
}
