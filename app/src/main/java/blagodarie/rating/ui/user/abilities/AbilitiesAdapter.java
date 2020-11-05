package blagodarie.rating.ui.user.abilities;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableBoolean;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import blagodarie.rating.R;
import blagodarie.rating.databinding.AbilityItemBinding;
import blagodarie.rating.model.IAbility;
import blagodarie.rating.model.entities.Ability;
import blagodarie.rating.ui.AbilitiesFragmentDirections;

public final class AbilitiesAdapter
        extends PagedListAdapter<IAbility, AbilitiesAdapter.AbilityViewHolder> {

    @NonNull
    private final ObservableBoolean mIsOwn;

    public AbilitiesAdapter (
            @NonNull final ObservableBoolean isOwn
    ) {
        super(DIFF_CALLBACK);
        mIsOwn = isOwn;
    }

    @NonNull
    @Override
    public AbilitiesAdapter.AbilityViewHolder onCreateViewHolder (
            @NonNull final ViewGroup parent,
            final int viewType
    ) {
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
            holder.bind(ability, mIsOwn);
        }
    }

    static final class AbilityViewHolder
            extends RecyclerView.ViewHolder {

        @NonNull
        private final AbilityItemBinding mBinding;

        AbilityViewHolder (
                @NonNull final AbilityItemBinding binding
        ) {
            super(binding.getRoot());
            mBinding = binding;
        }

        void bind (
                @NonNull final IAbility ability,
                @NonNull final ObservableBoolean isOwn
        ) {
            mBinding.setAbility(ability);
            mBinding.setIsOwn(isOwn);
            mBinding.btnEdit.setOnClickListener(v -> {
                final NavDirections action = AbilitiesFragmentDirections.actionAbilitiesFragmentToEditAbilityFragment((Ability) ability);
                Navigation.findNavController(itemView).navigate(action);
            });
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
