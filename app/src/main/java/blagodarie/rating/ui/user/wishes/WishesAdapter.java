package blagodarie.rating.ui.user.wishes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import blagodarie.rating.R;
import blagodarie.rating.databinding.WishItemBinding;
import blagodarie.rating.model.IWish;

public final class WishesAdapter
        extends PagedListAdapter<IWish, WishesAdapter.WishViewHolder> {

    interface UserActionListener {
        void onClick (@NonNull final IWish wish);
    }

    @NonNull
    private final UserActionListener mUserActionListener;

    protected WishesAdapter (
            @NonNull final UserActionListener userActionListener
    ) {
        super(DIFF_CALLBACK);
        mUserActionListener = userActionListener;
    }

    @NonNull
    @Override
    public WishesAdapter.WishViewHolder onCreateViewHolder (
            @NonNull ViewGroup parent,
            int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final WishItemBinding binding = DataBindingUtil.inflate(inflater, R.layout.wish_item, parent, false);
        return new WishesAdapter.WishViewHolder(binding);
    }


    @Override
    public void onBindViewHolder (
            @NonNull WishViewHolder holder,
            int position
    ) {
        final IWish wish = getItem(position);
        if (wish != null) {
            holder.bind(wish, view -> mUserActionListener.onClick(wish));
        }
    }

    static final class WishViewHolder
            extends RecyclerView.ViewHolder {

        @NonNull
        private final WishItemBinding mBinding;

        WishViewHolder (@NonNull final WishItemBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }

        void bind (
                @NonNull final IWish wish,
                @NonNull final View.OnClickListener onWishClickListener
        ) {
            itemView.setOnClickListener(onWishClickListener);
            mBinding.setWish(wish);
        }
    }

    private static DiffUtil.ItemCallback<IWish> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<IWish>() {

                @Override
                public boolean areItemsTheSame (
                        IWish oldWish,
                        IWish newWish
                ) {
                    return false;
                }

                @Override
                public boolean areContentsTheSame (
                        IWish oldWish,
                        IWish newWish
                ) {
                    return false;
                }
            };
}
