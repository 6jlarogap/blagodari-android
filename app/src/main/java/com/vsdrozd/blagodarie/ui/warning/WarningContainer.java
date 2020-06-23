package com.vsdrozd.blagodarie.ui.warning;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

import com.vsdrozd.blagodarie.DataRepository;

import java.util.ArrayList;
import java.util.List;

public final class WarningContainer {

    private final List<LiveData<List<Warning>>> mWarningsLiveDataList = new ArrayList<>();
    private final MediatorLiveData<List<Warning>> mWarningsLiveData = new MediatorLiveData<>();
    private final Observer<List<Warning>> mWarningObserver = warnings -> {
        final List<Warning> allWarnings = new ArrayList<>();
        for (LiveData<List<Warning>> warningLiveData : mWarningsLiveDataList) {
            if (warningLiveData.getValue() != null) {
                allWarnings.addAll(warningLiveData.getValue());
            }
        }
        mWarningsLiveData.setValue(allWarnings);
    };

    public WarningContainer (@NonNull final DataRepository dataRepository) {
        addWarnings(new VagueKeyzWarningListener(dataRepository.getVagueKeyzWithContacts()).getWarnings());
        addWarnings(new MissingKeyzWarningListener(dataRepository.getLikeWithMissingKeyz()).getWarnings());
        addWarnings(new VagueLikeWarningListener(dataRepository.getVagueLikeWithKeyz()).getWarnings());
    }

    private void addWarnings (@NonNull final LiveData<List<Warning>> warnings) {
        this.mWarningsLiveDataList.add(warnings);
        this.mWarningsLiveData.addSource(warnings, this.mWarningObserver);
    }

    public final LiveData<List<Warning>> getWarnings () {
        return this.mWarningsLiveData;
    }
}
