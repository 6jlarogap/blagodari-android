package com.vsdrozd.blagodarie.ui.contacts;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;
import androidx.databinding.DataBindingUtil;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.vsdrozd.blagodarie.R;
import com.vsdrozd.blagodarie.databinding.ContactItemBinding;
import com.vsdrozd.blagodarie.db.scheme.Contact;
import com.vsdrozd.blagodarie.ui.OnLikeCreateListener;

import org.jetbrains.annotations.NotNull;

/**
 * Адаптер списка контактов.
 *
 * @author sergeGabrus
 */
public final class ContactsAdapter
        extends PagedListAdapter<Contact, ContactsAdapter.ContactViewHolder> {

    /**
     * Слушатель нажатия на контакт.
     */
    @NonNull
    private final ContactItemClickListener mContactItemClickListener;

    /**
     * Слушатель нажатия на благодарность
     */
    @NonNull
    private final OnLikeCreateListener mOnLikeCreateListener;

    @NonNull
    private final OnContactSyncListener mOnContactSyncListener;

    private static DiffUtil.ItemCallback<Contact> DIFF_CALLBACK = new DiffUtil.ItemCallback<Contact>() {
        @Override
        public boolean areItemsTheSame (@NonNull final Contact oldItem, @NonNull final Contact newItem) {
            return newItem.getId().equals(oldItem.getId());
        }

        @Override
        public boolean areContentsTheSame (@NonNull final Contact oldItem, @NonNull final Contact newItem) {
            return newItem.getTitle().equals(oldItem.getTitle()) &&
                    (newItem.getPhotoUri() == null ? oldItem.getPhotoUri() == null : newItem.getPhotoUri().equals(oldItem.getPhotoUri())) &&
                    newItem.getFame().equals(oldItem.getFame()) &&
                    newItem.getLikeCount().equals(oldItem.getLikeCount()) &&
                    newItem.getSumLikeCount().equals(oldItem.getSumLikeCount());
        }
    };

    ContactsAdapter (
            @NonNull final ContactItemClickListener contactItemClickListener,
            @NonNull final OnLikeCreateListener onLikeCreateListener,
            @NonNull final OnContactSyncListener onContactSyncListener
    ) {
        super(DIFF_CALLBACK);
        this.mContactItemClickListener = contactItemClickListener;
        this.mOnLikeCreateListener = onLikeCreateListener;
        this.mOnContactSyncListener = onContactSyncListener;
    }

    @NonNull
    @Override
    public final ContactViewHolder onCreateViewHolder (
            @NonNull final ViewGroup parent,
            final int viewType
    ) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final ContactItemBinding binding = DataBindingUtil.inflate(inflater, R.layout.contact_item, parent, false);
        binding.setOnLikeCreateListener(this.mOnLikeCreateListener);
        return new ContactViewHolder(binding, this.mContactItemClickListener);
    }

    @Override
    public final void onBindViewHolder(
            @NotNull final ContactViewHolder holder,
            final int position
    ) {
        final Contact contact = getItem(position);
        if (contact != null) {
            this.mOnContactSyncListener.onSync(contact.getId());
            holder.bind(contact);
        }
    }

    @BindingAdapter ({"app:url"})
    public static void loadImage (final ImageView view, final String url) {
        Picasso.get().load(url).into(view);
    }

    static class ContactViewHolder extends RecyclerView.ViewHolder {

        @NonNull
        private final ContactItemBinding mBinding;

        @NonNull
        private final ContactItemClickListener mContactItemClickListener;

        ContactViewHolder (
                @NonNull final ContactItemBinding binding,
                @NonNull final ContactItemClickListener contactItemClickListener
        ) {
            super(binding.getRoot());
            this.mBinding = binding;
            this.mContactItemClickListener = contactItemClickListener;
        }

        void bind (@NonNull final Contact contact) {
            this.itemView.setOnClickListener(v -> this.mContactItemClickListener.onContactItemClick(v, contact.getId()));
            this.mBinding.setContact(contact);
            this.mBinding.executePendingBindings();
        }
    }
}