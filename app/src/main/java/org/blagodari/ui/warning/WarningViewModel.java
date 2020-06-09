package org.blagodari.ui.warning;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.blagodari.DataRepository;

import java.util.List;

public final class WarningViewModel
        extends ViewModel {

    public ObservableBoolean mHaveWarnings = new ObservableBoolean(true);

    @NonNull
    private final DataRepository mDataRepository;

    @NonNull
    private final WarningContainer mWarningContainer;

    private WarningViewModel (@NonNull final DataRepository repository) {
        this.mDataRepository = repository;
        this.mWarningContainer = new WarningContainer(this.mDataRepository);
    }

    final LiveData<List<Warning>> getWarnings () {
        return this.mWarningContainer.getWarnings();
    }

    static final class Factory
            extends ViewModelProvider.NewInstanceFactory {

        @NonNull
        private final DataRepository mRepository;


        Factory (
                @NonNull final DataRepository repository
        ) {
            this.mRepository = repository;
        }

        @Override
        @NonNull
        public <T extends ViewModel> T create (@NonNull Class<T> modelClass) {
            return (T) new WarningViewModel(this.mRepository);
        }
    }
}
