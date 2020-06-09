package org.blagodari.ui.warning;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import org.blagodari.R;
import org.blagodari.databinding.ContactListItemBinding;
import org.blagodari.db.scheme.Contact;

import java.util.ArrayList;
import java.util.List;

final class ContactsAdapter
        extends RecyclerView.Adapter<ContactsAdapter.ContactViewHolder> {

    @NonNull
    private final List<Contact> mContacts = new ArrayList<>();

    @NonNull
    private final OnContactClickListener mOnContactClickListener;

    ContactsAdapter (@NonNull final OnContactClickListener onContactClickListener) {
        this.mOnContactClickListener = onContactClickListener;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder (
            @NonNull final ViewGroup parent,
            final int viewType
    ) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final ContactListItemBinding binding = DataBindingUtil.inflate(inflater, R.layout.contact_list_item, parent, false);
        return new ContactViewHolder(binding, this.mOnContactClickListener);
    }

    @Override
    public void onBindViewHolder (
            @NonNull final ContactViewHolder holder,
            final int position
    ) {
        holder.bind(mContacts.get(position));
    }

    @Override
    public int getItemCount () {
        return this.mContacts.size();
    }

    final void setData (@NonNull final List<Contact> contactList) {
        this.mContacts.clear();
        this.mContacts.addAll(contactList);
        notifyDataSetChanged();
    }

    static final class ContactViewHolder extends RecyclerView.ViewHolder {

        @NonNull
        private final ContactListItemBinding mBinding;

        @NonNull
        private final OnContactClickListener mOnContactClickListener;

        ContactViewHolder (
                @NonNull final ContactListItemBinding binding,
                @NonNull final OnContactClickListener onContactClickListener
        ) {
            super(binding.getRoot());
            this.mBinding = binding;
            this.mOnContactClickListener = onContactClickListener;
        }

        void bind (@NonNull final Contact contact) {
            this.itemView.setOnClickListener(v -> this.mOnContactClickListener.onClick(contact));
        this.mBinding.setContact(contact);
            this.mBinding.executePendingBindings();
        }
    }
}
