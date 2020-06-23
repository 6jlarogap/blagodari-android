package com.vsdrozd.blagodarie.ui.newcontacts;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.vsdrozd.blagodarie.R;
import com.vsdrozd.blagodarie.databinding.NewContactItemBinding;
import com.vsdrozd.blagodarie.db.addent.ContactWithKeyz;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class NewContactsAdapter extends RecyclerView.Adapter<NewContactsAdapter.NewContactViewHolder> {

    @NonNull
    final List<ContactWithKeyz> mContacts = new ArrayList<>();

    public NewContactsAdapter () {
    }

    @NonNull
    @Override
    public NewContactViewHolder onCreateViewHolder (@NonNull ViewGroup parent, int viewType) {

        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final NewContactItemBinding binding = DataBindingUtil.inflate(inflater, R.layout.new_contact_item, parent, false);
        return new NewContactsAdapter.NewContactViewHolder(binding);
    }

    @Override
    public void onBindViewHolder (@NonNull NewContactViewHolder holder, int position) {
        final ContactWithKeyz contactWithKeyz = mContacts.get(position);
        if (contactWithKeyz != null) {
            holder.bind(contactWithKeyz);
        }
    }

    @Override
    public int getItemCount () {
        return mContacts.size();
    }

    public void setData (@NonNull final Collection<ContactWithKeyz> newContacts) {
        //ArrayList<ContactWithKeyz> newContactsList = new ArrayList<>(newContacts);
        //DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new ContactDiffUtilCallBack(newContactsList, mContacts));
        //diffResult.dispatchUpdatesTo(this);
        this.mContacts.clear();
        this.mContacts.addAll(newContacts);
        notifyDataSetChanged();
    }


    public static class NewContactViewHolder extends RecyclerView.ViewHolder {

        @NonNull
        private final NewContactItemBinding mBinding;


        NewContactViewHolder (
                @NonNull final NewContactItemBinding binding
        ) {
            super(binding.getRoot());
            this.mBinding = binding;
        }

        void bind (@NonNull final ContactWithKeyz contactWithKeyz) {
            this.mBinding.setContact(contactWithKeyz.getContact());
            this.mBinding.executePendingBindings();
        }
    }

    private class ContactDiffUtilCallBack extends DiffUtil.Callback {
        List<ContactWithKeyz> newList;
        List<ContactWithKeyz> oldList;

        public ContactDiffUtilCallBack(List<ContactWithKeyz> newList, List<ContactWithKeyz> oldList) {
            this.newList = newList;
            this.oldList = oldList;
        }

        @Override
        public int getOldListSize() {
            return oldList != null ? oldList.size() : 0;
        }

        @Override
        public int getNewListSize() {
            return newList != null ? newList.size() : 0;
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return newList.get(newItemPosition).getContact().getId().equals(oldList.get(oldItemPosition).getContact().getId());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return newList.get(newItemPosition).equals(oldList.get(oldItemPosition));
        }
    }
}
