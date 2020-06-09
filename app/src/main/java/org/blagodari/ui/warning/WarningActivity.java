package org.blagodari.ui.warning;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ex.diagnosticlib.Diagnostic;
import org.blagodari.BlagodariApp;
import org.blagodari.DataRepository;
import org.blagodari.R;
import org.blagodari.databinding.WarningActivityBinding;

public final class WarningActivity
        extends AppCompatActivity {

    /**
     * ViewModel.
     */
    private WarningViewModel mWarningViewModel;

    /**
     * Источник данных.
     */
    private DataRepository mDataRepository;

    @Override
    protected final void onCreate (@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Diagnostic.i();

        this.mDataRepository = ((BlagodariApp) getApplication()).getRepository();
        initViewModel();

        WarningActivityBinding warningActivityBinding = DataBindingUtil.setContentView(this, R.layout.warning_activity);
        warningActivityBinding.setViewModel(mWarningViewModel);
        WarningAdapter warningAdapter = new WarningAdapter(this::onWarningClick);
        this.mWarningViewModel.getWarnings().observe(this, warningAdapter::setData);
        warningActivityBinding.rvWarnings.setAdapter(warningAdapter);
        warningActivityBinding.rvWarnings.setLayoutManager(new LinearLayoutManager(this));
    }

    private void initViewModel () {
        WarningViewModel.Factory factory = new WarningViewModel.Factory(
                this.mDataRepository
        );

        //создаем ContactsViewModel
        this.mWarningViewModel = new ViewModelProvider(this, factory).get(WarningViewModel.class);
        this.mWarningViewModel.getWarnings().observe(this, warnings -> this.mWarningViewModel.mHaveWarnings.set(!warnings.isEmpty()));
    }

    private void onWarningClick(@NonNull final Warning warning){
        warning.resolve(this, this.mDataRepository);
    }
}
