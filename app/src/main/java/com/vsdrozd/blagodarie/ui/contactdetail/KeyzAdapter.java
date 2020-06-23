package com.vsdrozd.blagodarie.ui.contactdetail;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.vsdrozd.blagodarie.R;
import com.vsdrozd.blagodarie.databinding.KeyzItemBinding;
import com.vsdrozd.blagodarie.db.scheme.Keyz;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

final class KeyzAdapter
        extends RecyclerView.Adapter<KeyzAdapter.KeyzViewHolder> {

    @NonNull
    private final List<Keyz> mKeyz = new ArrayList<>();

    @NonNull
    @Override
    public final KeyzViewHolder onCreateViewHolder (@NonNull ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final KeyzItemBinding binding = DataBindingUtil.inflate(inflater, R.layout.keyz_item, parent, false);
        return new KeyzViewHolder(binding);
    }

    @Override
    public void onBindViewHolder (@NonNull KeyzViewHolder holder, int position) {
        holder.bind(mKeyz.get(position));
    }

    @Override
    public int getItemCount () {
        return mKeyz.size();
    }

    public void setData (Collection<Keyz> keyzList) {
        mKeyz.clear();
        mKeyz.addAll(keyzList);
        notifyDataSetChanged();
    }

    static class KeyzViewHolder
            extends RecyclerView.ViewHolder {

        final KeyzItemBinding mBinding;

        KeyzViewHolder (KeyzItemBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }

        void bind(final Keyz keyz) {
            mBinding.setKeyz(keyz);
            mBinding.executePendingBindings();
        }
    }
}
