package blagodarie.rating.ui.people;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.UUID;

import blagodarie.rating.R;
import blagodarie.rating.databinding.UserItemBinding;
import blagodarie.rating.model.IProfile;

public class PeopleAdapter
        extends PagedListAdapter<IProfile, PeopleAdapter.UserViewHolder> {

    interface UserActionListener {
        void onUserClick (@NonNull final UUID userId);
    }

    @NonNull
    private final UserActionListener mUserActionListener;

    protected PeopleAdapter (
            @NonNull final UserActionListener userActionListener
    ) {
        super(DIFF_CALLBACK);
        mUserActionListener = userActionListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder (
            @NonNull final ViewGroup parent,
            final int viewType
    ) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final UserItemBinding binding = DataBindingUtil.inflate(inflater, R.layout.user_item, parent, false);
        return new PeopleAdapter.UserViewHolder(binding);
    }


    @Override
    public void onBindViewHolder (
            @NonNull final UserViewHolder holder,
            final int position
    ) {
        final IProfile profileInfo = getItem(position);
        if (profileInfo != null) {
            holder.bind(profileInfo, mUserActionListener);
        }
    }

    static final class UserViewHolder
            extends RecyclerView.ViewHolder {

        @NonNull
        private final UserItemBinding mBinding;

        UserViewHolder (@NonNull final UserItemBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }

        void bind (
                @NonNull final IProfile profileInfo,
                @NonNull final UserActionListener userActionListener
        ) {
            itemView.setOnClickListener(view -> userActionListener.onUserClick(profileInfo.getId()));
            mBinding.setProfileInfo(profileInfo);
        }
    }

    private static DiffUtil.ItemCallback<IProfile> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<IProfile>() {

                @Override
                public boolean areItemsTheSame (
                        @NonNull final IProfile oldItem,
                        @NonNull final IProfile newItem
                ) {
                    return oldItem.getId().equals(newItem.getId());
                }

                @Override
                public boolean areContentsTheSame (
                        @NonNull final IProfile oldItem,
                        @NonNull final IProfile newItem
                ) {
                    return false;
                }
            };
}
