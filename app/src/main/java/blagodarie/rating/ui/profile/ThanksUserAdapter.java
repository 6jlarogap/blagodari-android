package blagodarie.rating.ui.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import blagodarie.rating.R;
import blagodarie.rating.databinding.ThanksUserItemBinding;

public final class ThanksUserAdapter
        extends RecyclerView.Adapter<ThanksUserAdapter.ThanksUserViewHolder> {

    @NonNull
    private final List<DisplayThanksUser> mThanksUsers = new ArrayList<>();

    @NonNull
    private final View.OnClickListener mOnThanksUserClickListener;

    public ThanksUserAdapter (@NonNull final View.OnClickListener onThanksUserClickListener) {
        mOnThanksUserClickListener = onThanksUserClickListener;
    }

    @NonNull
    @Override
    public ThanksUserViewHolder onCreateViewHolder (
            @NonNull final ViewGroup parent,
            final int viewType
    ) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final ThanksUserItemBinding binding = DataBindingUtil.inflate(inflater, R.layout.thanks_user_item, parent, false);
        return new ThanksUserViewHolder(binding);
    }

    @Override
    public void onBindViewHolder (
            @NonNull final ThanksUserViewHolder holder,
            final int position
    ) {
        final DisplayThanksUser displayThanksUser = mThanksUsers.get(position);
        if (displayThanksUser != null) {
            holder.bind(displayThanksUser, mOnThanksUserClickListener);
        }
    }

    @Override
    public int getItemCount () {
        return mThanksUsers.size();
    }

    final void setData (
            @NonNull final List<DisplayThanksUser> thanksUsers
    ) {
        mThanksUsers.clear();
        mThanksUsers.addAll(thanksUsers);
        notifyDataSetChanged();
    }

    static final class ThanksUserViewHolder
            extends RecyclerView.ViewHolder {

        @NonNull
        private final ThanksUserItemBinding mBinding;

        ThanksUserViewHolder (@NonNull final ThanksUserItemBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }

        void bind (
                @NonNull final DisplayThanksUser displayThanksUser,
                @NonNull final View.OnClickListener onThanksUserClickListener
        ) {
            itemView.setOnClickListener(onThanksUserClickListener);
            mBinding.setThanksUser(displayThanksUser);
            Picasso.get().load(displayThanksUser.getPhoto()).into(mBinding.ivPhoto);
        }
    }
}
