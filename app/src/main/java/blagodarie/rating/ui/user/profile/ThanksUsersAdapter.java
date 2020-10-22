package blagodarie.rating.ui.user.profile;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.UUID;

import blagodarie.rating.R;
import blagodarie.rating.databinding.ThanksUserItemBinding;
import blagodarie.rating.server.GetThanksUsersResponse;

public class ThanksUsersAdapter
        extends PagedListAdapter<GetThanksUsersResponse.ThanksUser, ThanksUsersAdapter.ThanksUsersViewHolder> {

    private static final String TAG = ThanksUsersAdapter.class.getSimpleName();

    interface OnItemClickListener {
        void onClick (@NonNull final UUID userId);
    }

    @NonNull
    private final ThanksUsersAdapter.OnItemClickListener mOnItemClickListener;

    protected ThanksUsersAdapter (
            @NonNull final ThanksUsersAdapter.OnItemClickListener onItemClickListener
    ) {
        super(DIFF_CALLBACK);
        mOnItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ThanksUsersViewHolder onCreateViewHolder (
            @NonNull ViewGroup parent,
            int viewType
    ) {
        Log.d(TAG, "onCreateViewHolder");
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final ThanksUserItemBinding binding = DataBindingUtil.inflate(inflater, R.layout.thanks_user_item, parent, false);
        return new ThanksUsersViewHolder(binding);
    }


    @Override
    public void onBindViewHolder (
            @NonNull ThanksUsersAdapter.ThanksUsersViewHolder holder,
            int position
    ) {
        Log.d(TAG, "onBindViewHolder");
        final GetThanksUsersResponse.ThanksUser thanksUser = getItem(position);
        if (thanksUser != null) {
            holder.bind(thanksUser, mOnItemClickListener);
        }
    }

    static final class ThanksUsersViewHolder
            extends RecyclerView.ViewHolder {

        private static final String TAG = ThanksUsersViewHolder.class.getSimpleName();

        @NonNull
        private final ThanksUserItemBinding mBinding;

        ThanksUsersViewHolder (@NonNull final ThanksUserItemBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }

        void bind (
                @NonNull final GetThanksUsersResponse.ThanksUser thanksUser,
                @NonNull final ThanksUsersAdapter.OnItemClickListener onItemClickListener
        ) {
            Log.d(TAG, "bind");
            itemView.setOnClickListener(view -> onItemClickListener.onClick(thanksUser.getUserId()));
            mBinding.setThanksUser(thanksUser);
            Picasso.get().load(thanksUser.getPhoto()).into(mBinding.ivPhoto);
        }
    }

    private static DiffUtil.ItemCallback<GetThanksUsersResponse.ThanksUser> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<GetThanksUsersResponse.ThanksUser>() {

                @Override
                public boolean areItemsTheSame (
                        GetThanksUsersResponse.ThanksUser oldItem,
                        GetThanksUsersResponse.ThanksUser newItem
                ) {
                    return false;
                }

                @Override
                public boolean areContentsTheSame (
                        GetThanksUsersResponse.ThanksUser oldItem,
                        GetThanksUsersResponse.ThanksUser newItem
                ) {
                    return false;
                }
            };
}
