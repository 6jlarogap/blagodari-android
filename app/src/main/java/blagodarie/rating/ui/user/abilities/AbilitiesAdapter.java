package blagodarie.rating.ui.user.abilities;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import blagodarie.rating.R;
import blagodarie.rating.databinding.AbilityItemBinding;
import blagodarie.rating.model.IAbility;

final class AbilitiesAdapter
        extends PagedListAdapter<IAbility, AbilitiesAdapter.AbilityViewHolder> {

    protected AbilitiesAdapter () {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public AbilitiesAdapter.AbilityViewHolder onCreateViewHolder (
            @NonNull final ViewGroup parent,
            final int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final AbilityItemBinding binding = DataBindingUtil.inflate(inflater, R.layout.ability_item, parent, false);
        return new AbilitiesAdapter.AbilityViewHolder(binding);
    }


    @Override
    public void onBindViewHolder (
            @NonNull final AbilityViewHolder holder,
            final int position
    ) {
        final IAbility ability = getItem(position);
        if (ability != null) {
            holder.bind(ability);
        }
    }

    static final class AbilityViewHolder
            extends RecyclerView.ViewHolder {

        @NonNull
        private final AbilityItemBinding mBinding;

        AbilityViewHolder (@NonNull final AbilityItemBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }

        void bind (
                @NonNull final IAbility ability
        ) {
            mBinding.setAbility(ability);
        }
    }

    private static DiffUtil.ItemCallback<IAbility> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<IAbility>() {

                @Override
                public boolean areItemsTheSame (
                        IAbility oldItem,
                        IAbility newItem
                ) {
                    return false;
                }

                @Override
                public boolean areContentsTheSame (
                        IAbility oldItem,
                        IAbility newItem
                ) {
                    return false;
                }
            };
}
