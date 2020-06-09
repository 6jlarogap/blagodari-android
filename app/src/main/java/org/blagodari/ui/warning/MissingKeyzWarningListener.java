package org.blagodari.ui.warning;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import org.blagodari.db.addent.LikeWithKeyz;

import java.util.ArrayList;
import java.util.List;

final class MissingKeyzWarningListener
extends WarningListener{

    MissingKeyzWarningListener (@NonNull final LiveData<List<LikeWithKeyz>> likeWithKeyzListLiveData) {
        super(
                (MutableLiveData<List<Warning>>) Transformations.switchMap(likeWithKeyzListLiveData, input -> {
                    final List<Warning> missinKeyzWarningList = new ArrayList<>();
                    for(LikeWithKeyz likeWithKeyz : input){
                        missinKeyzWarningList.add(new MissingKeyzWarning(likeWithKeyz));
                    }
                    return new MutableLiveData<>(missinKeyzWarningList);
                })
        );
    }
}
