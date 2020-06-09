package com.vsdrozd.blagodarie.ui;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModel;

import com.ex.diagnosticlib.Diagnostic;
import com.vsdrozd.blagodarie.BlagodarieApp;
import com.vsdrozd.blagodarie.DataRepository;

public abstract class BaseActivity<ViewModelType extends ViewModel>
        extends AppCompatActivity {

    private DataRepository mDataRepository;
    private ViewModelType mViewModel;

    @Override
    protected void onCreate (@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Diagnostic.i();

        this.mDataRepository = ((BlagodarieApp) getApplication()).getRepository();
        this.mViewModel = createViewModel();
    }

    protected abstract ViewModelType createViewModel ();

    protected DataRepository getDataRepository () {
        return this.mDataRepository;
    }

    protected ViewModelType getViewModel () {
        return this.mViewModel;
    }
}
