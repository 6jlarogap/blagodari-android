package com.vsdrozd.blagodarie.ui.contactdetail;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.vsdrozd.blagodarie.DataRepository;
import com.vsdrozd.blagodarie.db.scheme.Contact;
import com.vsdrozd.blagodarie.db.scheme.Keyz;
import com.vsdrozd.blagodarie.db.scheme.KeyzType;
import com.vsdrozd.blagodarie.db.scheme.Like;

import java.util.List;

public final class ContactDetailViewModel
        extends ViewModel {

    private final Long mUserId;
    private final Long mContactId;
    private final LiveData<Contact> mContact;
    private final LiveData<List<Keyz>> mPhoneList;
    private final LiveData<List<Keyz>> mEmailList;
    private final LiveData<List<Like>> mLikeList;

    private ContactDetailViewModel (
            @NonNull final DataRepository dataRepository,
            @NonNull final Long userId,
            @NonNull final Long contactId) {
        super();
        this.mUserId = userId;
        this.mContactId = contactId;
        this.mContact = dataRepository.getContactLiveData(contactId);
        this.mPhoneList = dataRepository.getKeyzLiveDataByContactIdAndTypeId(contactId, KeyzType.Types.PHONE_NUMBER.getId());
        this.mEmailList = dataRepository.getKeyzLiveDataByContactIdAndTypeId(contactId, KeyzType.Types.EMAIL.getId());
        this.mLikeList = dataRepository.getLikeLiveDataByContactIdAndOwnerId(contactId, userId);
    }

    final LiveData<Contact> getContact () {
        return this.mContact;
    }

    final LiveData<List<Like>> getLikes () {
        return this.mLikeList;
    }

    final LiveData<List<Keyz>> getPhones () {
        return this.mPhoneList;
    }

    final LiveData<List<Keyz>> getEmails () {
        return this.mEmailList;
    }

    final Long getUserId () {
        return this.mUserId;
    }

    final Long getContactId () {
        return this.mContactId;
    }

    static class Factory extends ViewModelProvider.NewInstanceFactory {

        @NonNull
        private final DataRepository mRepository;

        @NonNull
        private final Long mUserId;

        @NonNull
        private final Long mContactId;


        Factory (
                @NonNull final DataRepository repository,
                @NonNull final Long userId,
                @NonNull final Long contactId
        ) {
            this.mRepository = repository;
            this.mUserId = userId;
            this.mContactId = contactId;
        }

        @Override
        @NonNull
        public <T extends ViewModel> T create (@NonNull Class<T> modelClass) {
            return (T) new ContactDetailViewModel(
                    this.mRepository,
                    this.mUserId,
                    this.mContactId);
        }
    }

}
