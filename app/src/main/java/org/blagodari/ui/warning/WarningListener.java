package org.blagodari.ui.warning;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

abstract class WarningListener {

    private final MutableLiveData<List<Warning>> mWarnings;

    WarningListener(@NonNull final MutableLiveData<List<Warning>> warnings){
        this.mWarnings = warnings;
    }

    final LiveData<List<Warning>> getWarnings () {
        return this.mWarnings;
    }

    protected void setData(List<Warning> warningList){
        this.mWarnings.setValue(warningList);
    }
}
