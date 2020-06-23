package com.vsdrozd.blagodarie.ui.contactdetail;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.vsdrozd.blagodarie.R;
import com.vsdrozd.blagodarie.databinding.LikeItemBinding;
import com.vsdrozd.blagodarie.db.scheme.Like;
import com.vsdrozd.blagodarie.ui.OnLikeCancelListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class LikeAdapter
        extends RecyclerView.Adapter<LikeAdapter.LikeViewHolder> {

    @NonNull
    private final Long mUserId;

    @NonNull
    private final List<Like> mLikeList = new ArrayList<>();

    @NonNull
    private final OnLikeCancelListener mOnLikeCancelListener;

    LikeAdapter (
            @NonNull final Long userId,
            @NonNull final OnLikeCancelListener onLikeCancelListener
    ) {
        this.mUserId = userId;
        this.mOnLikeCancelListener = onLikeCancelListener;
    }

    @NonNull
    @Override
    public final LikeViewHolder onCreateViewHolder (
            @NonNull final ViewGroup parent,
            final int viewType
    ) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final LikeItemBinding binding = DataBindingUtil.inflate(inflater, R.layout.like_item, parent, false);
        binding.setOnLikeCancelListener(this.mOnLikeCancelListener);
        return new LikeViewHolder(this.mUserId, binding);
    }

    @Override
    public final void onBindViewHolder (
            @NonNull final LikeViewHolder holder,
            final int position
    ) {
        final Like like = this.mLikeList.get(position);
        if (like != null) {
            holder.bind(like);
        }
    }

    @Override
    public final int getItemCount () {
        return this.mLikeList.size();
    }

    public final void setData (@NonNull final Collection<Like> likeCollection) {
        this.mLikeList.clear();
        this.mLikeList.addAll(likeCollection);
        notifyDataSetChanged();
    }

    static final class LikeViewHolder
            extends RecyclerView.ViewHolder {

        @NonNull
        private final Long mUserId;

        @NonNull
        private final LikeItemBinding mBinding;

        LikeViewHolder (

                @NonNull final Long userId,
                @NonNull final LikeItemBinding binding
        ) {
            super(binding.getRoot());
            this.mUserId = userId;
            this.mBinding = binding;
        }

        void bind (@NonNull final Like Like) {
            this.mBinding.setLike(Like);
            this.mBinding.setUserId(this.mUserId);
            this.mBinding.executePendingBindings();
        }
    }
}
