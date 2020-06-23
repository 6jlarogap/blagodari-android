package com.vsdrozd.blagodarie.ui.warning;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.vsdrozd.blagodarie.DataRepository;
import com.vsdrozd.blagodarie.db.addent.LikeWithKeyz;
import com.vsdrozd.blagodarie.db.scheme.Contact;
import com.vsdrozd.blagodarie.db.scheme.Keyz;
import com.vsdrozd.blagodarie.db.scheme.Like;

import java.util.List;
import java.util.Set;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Предупреждение о благодарности, привязанной к ключам, которые привязаны к разным контактам.
 */
public final class VagueLikeWarning
        implements Warning {

    @NonNull
    private final LikeWithKeyz mLikeWithKeyz;

    VagueLikeWarning (
            @NonNull final LikeWithKeyz likeWithKeyz
    ) {
        this.mLikeWithKeyz = likeWithKeyz;
    }

    @NonNull
    public final Like getLike () {
        return this.mLikeWithKeyz.getLike();
    }

    @NonNull
    final Set<Keyz> getKeyzSet () {
        return this.mLikeWithKeyz.getKeyzSet();
    }

    @Override
    public boolean resolve (
            @NonNull final AppCompatActivity activity,
            @NonNull final DataRepository dataRepository
    ) {
        /*ContactListDialog.ContactListDialogCommunicator contactListDialogCommunicator = new ContactListDialog.ContactListDialogCommunicator() {
            @Override
            public void onSelectContact (@NonNull final Contact contact) {
                Completable.
                        fromAction(() -> dataRepository.relateLikeToContact(mLike, contact.getId())).
                        subscribeOn(Schedulers.io()).
                        observeOn(AndroidSchedulers.mainThread()).
                        subscribe();
            }

            @Override
            public void onDelete () {
                Completable.
                        fromAction(() -> dataRepository.removeLike(mLike)).
                        subscribeOn(Schedulers.io()).
                        observeOn(AndroidSchedulers.mainThread()).
                        subscribe();
            }
        };
        final ContactListDialog contactListDialog = new ContactListDialog(dataRepository.getContactsByUserId(1L),contactListDialogCommunicator);
        contactListDialog.show(activity.getSupportFragmentManager(),"");*/
        return false;
    }
}
