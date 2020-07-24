package blagodarie.rating.ui.wishes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import blagodarie.rating.R;
import blagodarie.rating.databinding.WishItemBinding;

public final class WishesAdapter
        extends RecyclerView.Adapter<WishesAdapter.WishViewHolder> {

    @NonNull
    private final List<Wish> mWishes = new ArrayList<>();

    @NonNull
    private final OnWishClickListener mOnWishClickListener;

    public WishesAdapter (@NonNull final OnWishClickListener onWishClickListener) {
        mOnWishClickListener = onWishClickListener;
    }

    @NonNull
    @Override
    public WishViewHolder onCreateViewHolder (@NonNull ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final WishItemBinding binding = DataBindingUtil.inflate(inflater, R.layout.wish_item, parent, false);
        return new WishesAdapter.WishViewHolder(binding);
    }

    @Override
    public void onBindViewHolder (@NonNull WishViewHolder holder, int position) {
        final Wish wish = mWishes.get(position);
        if (wish != null) {
            holder.bind(wish, view -> mOnWishClickListener.onClick(wish));
        }
    }

    @Override
    public int getItemCount () {
        return mWishes.size();
    }

    final void setData (
            @NonNull final List<Wish> wishes
    ) {
        mWishes.clear();
        mWishes.addAll(wishes);
        notifyDataSetChanged();
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
}
