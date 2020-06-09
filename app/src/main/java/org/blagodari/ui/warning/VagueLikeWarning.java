package org.blagodari.ui.warning;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.blagodari.DataRepository;
import org.blagodari.db.addent.LikeWithKeyz;
import org.blagodari.db.scheme.Keyz;
import org.blagodari.db.scheme.Like;

import java.util.Set;

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
