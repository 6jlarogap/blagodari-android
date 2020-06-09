package org.blagodari.ui.warning;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import org.blagodari.db.addent.KeyzWithContacts;

import java.util.ArrayList;
import java.util.List;

public final class VagueKeyzWarningListener
        extends WarningListener {

    VagueKeyzWarningListener (@NonNull final LiveData<List<KeyzWithContacts>> keyzWithContactsListLiveData) {
        super(
                (MutableLiveData<List<Warning>>) Transformations.switchMap(keyzWithContactsListLiveData, input -> {
                    final List<Warning> vagueKeyzWarningList = new ArrayList<>();
                    for(KeyzWithContacts keyzWithContacts : input){
                        vagueKeyzWarningList.add(new VagueKeyzWarning(keyzWithContacts));
                    }
                    return new MutableLiveData<>(vagueKeyzWarningList);
                })
        );
    }

}
