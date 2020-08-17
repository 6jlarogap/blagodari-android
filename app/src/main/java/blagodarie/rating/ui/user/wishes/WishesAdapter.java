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
final class WishesAdapter
        extends PagedListAdapter<Wish, WishesAdapter.WishViewHolder> {

    @NonNull
    private final OnWishClickListener mOnWishClickListener;

    protected WishesAdapter (@NonNull final OnWishClickListener mOnWishClickListener) {
        super(DIFF_CALLBACK);
        this.mOnWishClickListener = mOnWishClickListener;
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
        final Wish wish = getItem(position);
        if (wish != null) {
            holder.bind(wish, view -> mOnWishClickListener.onClick(wish));
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
                @NonNull final Wish wish,
                @NonNull final View.OnClickListener onWishClickListener
        ) {
            itemView.setOnClickListener(onWishClickListener);
            mBinding.setWish(wish);
        }
    }

    private static DiffUtil.ItemCallback<Wish> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Wish>() {

                @Override
                public boolean areItemsTheSame (
                        Wish oldWish,
                        Wish newWish
                ) {
                    return false;
                }

                @Override
                public boolean areContentsTheSame (
                        Wish oldWish,
                        Wish newWish
                ) {
                    return false;
                }
            };
}
