package blagodarie.rating.ui.user.keys;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import blagodarie.rating.R;
import blagodarie.rating.databinding.KeyItemBinding;

public final class KeysAdapter
        extends PagedListAdapter<Key, KeysAdapter.KeyViewHolder> {

    @NonNull
    private final OnKeyClickListener mOnKeyClickListener;

    protected KeysAdapter (@NonNull final OnKeyClickListener mOnKeyClickListener) {
        super(DIFF_CALLBACK);
        this.mOnKeyClickListener = mOnKeyClickListener;
    }

    @NonNull
    @Override
    public KeysAdapter.KeyViewHolder onCreateViewHolder (
            @NonNull ViewGroup parent,
            int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final KeyItemBinding binding = DataBindingUtil.inflate(inflater, R.layout.key_item, parent, false);
        return new KeysAdapter.KeyViewHolder(binding);
    }


    @Override
    public void onBindViewHolder (
            @NonNull KeysAdapter.KeyViewHolder holder,
            int position
    ) {
        final Key key = getItem(position);
        if (key != null) {
            holder.bind(key, view -> mOnKeyClickListener.onClick(key));
        }
    }

    static final class KeyViewHolder
            extends RecyclerView.ViewHolder {

        @NonNull
        private final KeyItemBinding mBinding;

        KeyViewHolder (@NonNull final KeyItemBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }

        void bind (
                @NonNull final Key key,
                @NonNull final View.OnClickListener onKeyClickListener
        ) {
            itemView.setOnClickListener(onKeyClickListener);
            mBinding.setKey(key);
            mBinding.setKeyName(
                    String.format(
                            mBinding.getRoot().getContext().getString(R.string.txt_key),
                            mBinding.getRoot().getContext().getString(key.getKeyType().getNameResId()),
                            key.getValue()
                    )
            );
        }
    }

    private static DiffUtil.ItemCallback<Key> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Key>() {

                @Override
                public boolean areItemsTheSame (
                        Key oldKey,
                        Key newKey
                ) {
                    return false;
                }

                @Override
                public boolean areContentsTheSame (
                        Key oldKey,
                        Key newKey
                ) {
                    return false;
                }
            };
}
