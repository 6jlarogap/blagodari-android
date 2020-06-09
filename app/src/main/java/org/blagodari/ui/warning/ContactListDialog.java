package org.blagodari.ui.warning;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;

import org.blagodari.R;
import org.blagodari.databinding.ContactListDialogBinding;
import org.blagodari.db.scheme.Contact;

import java.util.List;

public final class ContactListDialog
        extends DialogFragment {

    public interface ContactListDialogCommunicator {

        void onSelectContact (@NonNull final Contact contact);

        void onDelete ();
    }

    @NonNull
    private final LiveData<List<Contact>> mContacts;

    @NonNull
    private final ContactListDialogCommunicator mContactListDialogCommunicator;

    ContactListDialog (
            @NonNull final LiveData<List<Contact>> contacts,
            @NonNull final ContactListDialogCommunicator contactListDialogCommunicator
    ) {
        this.mContacts = contacts;
        this.mContactListDialogCommunicator = contactListDialogCommunicator;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog (@Nullable final Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        final View view = DataBindingUtil.inflate(requireActivity().getLayoutInflater(), R.layout.contact_list_dialog, null, false).getRoot();
        final ContactListDialogBinding contactListDialogBinding = DataBindingUtil.getBinding(view);
        if (contactListDialogBinding != null) {
            ContactsAdapter contactsAdapter = new ContactsAdapter(contact -> {
                mContactListDialogCommunicator.onSelectContact(contact);
                dismiss();
            });
            this.mContacts.observe(this, contactsAdapter::setData);
            contactListDialogBinding.rvContacts.setLayoutManager(new LinearLayoutManager(requireContext()));
            contactListDialogBinding.rvContacts.setAdapter(contactsAdapter);
        }
        builder.setView(view)
                .setNeutralButton("Удалить", (dialog, id) ->
                        this.mContactListDialogCommunicator.onDelete()
                );
        return builder.create();
    }
}
