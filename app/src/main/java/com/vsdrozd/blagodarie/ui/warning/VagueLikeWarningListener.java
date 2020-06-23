package com.vsdrozd.blagodarie.ui.warning;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.vsdrozd.blagodarie.db.addent.KeyzWithContacts;
import com.vsdrozd.blagodarie.db.addent.LikeWithKeyz;

import java.util.ArrayList;
import java.util.List;

final class VagueLikeWarningListener
        extends WarningListener {

    VagueLikeWarningListener (@NonNull final LiveData<List<LikeWithKeyz>> likesWithKeyzListLiveData) {
        super(
                (MutableLiveData<List<Warning>>) Transformations.switchMap(likesWithKeyzListLiveData, input -> {
                    final List<Warning> vagueKeyzWarningList = new ArrayList<>();
                    for(LikeWithKeyz likeWithKeyz : input){
                        vagueKeyzWarningList.add(new VagueLikeWarning(likeWithKeyz));
                    }
                    return new MutableLiveData<>(vagueKeyzWarningList);
                })
        );
    }
}
