package com.vsdrozd.blagodarie.ui.warning;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.vsdrozd.blagodarie.DataRepository;
import com.vsdrozd.blagodarie.db.addent.LikeWithKeyz;
import com.vsdrozd.blagodarie.db.scheme.Contact;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Предупреждение о благодарности, привязанной к ключу/ключам, не привязанному ни к одному контакту.
 */
public final class MissingKeyzWarning
        implements Warning {

    @NonNull
    private final LikeWithKeyz mLikeWithKeyz;

    MissingKeyzWarning (@NonNull final LikeWithKeyz likeWithKeyz) {
        this.mLikeWithKeyz = likeWithKeyz;
    }

    @NonNull
    final LikeWithKeyz getLikeWithKeyz () {
        return this.mLikeWithKeyz;
    }

    @Override
    public boolean resolve (
            @NonNull final AppCompatActivity activity,
            @NonNull final DataRepository dataRepository
    ) {
        ContactListDialog.ContactListDialogCommunicator contactListDialogCommunicator = new ContactListDialog.ContactListDialogCommunicator() {
            @Override
            public void onSelectContact (@NonNull final Contact contact) {
                Completable.
                        fromAction(() -> dataRepository.relateLikeToContact(mLikeWithKeyz.getLike(), contact.getId())).
                        subscribeOn(Schedulers.io()).
                        observeOn(AndroidSchedulers.mainThread()).
                        subscribe();
            }

            @Override
            public void onDelete () {
                Completable.
                    fromAction(() -> dataRepository.removeLike(mLikeWithKeyz.getLike())).
                    subscribeOn(Schedulers.io()).
                    observeOn(AndroidSchedulers.mainThread()).
                    subscribe();
            }
        };
        final ContactListDialog contactListDialog = new ContactListDialog(dataRepository.getContactsByUserId(1L),contactListDialogCommunicator);
        contactListDialog.show(activity.getSupportFragmentManager(),"");
        return false;
    }
}
