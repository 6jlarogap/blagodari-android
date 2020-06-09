package org.blagodari.ui.warning;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import org.blagodari.R;
import org.blagodari.databinding.WarningItemBinding;
import org.blagodari.db.scheme.Contact;
import org.blagodari.db.scheme.Keyz;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public final class WarningAdapter
        extends RecyclerView.Adapter<WarningAdapter.WarningViewHolder> {

    @NonNull
    private final List<Warning> mWarningList = new ArrayList<>();

    @NonNull
    private final OnWarningClickListener mOnWarningClickListener;

    WarningAdapter (@NonNull final OnWarningClickListener onWarningClickListener) {
        this.mOnWarningClickListener = onWarningClickListener;
    }

    @NonNull
    @Override
    public final WarningViewHolder onCreateViewHolder (
            @NonNull final ViewGroup parent,
            final int viewType
    ) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final WarningItemBinding binding = DataBindingUtil.inflate(inflater, R.layout.warning_item, parent, false);
        final HashMap<Class, String> warningDesriptionPatterns = new HashMap<>();
        warningDesriptionPatterns.put(VagueKeyzWarning.class, parent.getContext().getString(R.string.vague_keyz_warning));
        warningDesriptionPatterns.put(VagueLikeWarning.class, parent.getContext().getString(R.string.vague_like_warning));
        warningDesriptionPatterns.put(MissingKeyzWarning.class, parent.getContext().getString(R.string.missing_keyz_warning));
        return new WarningViewHolder(binding, warningDesriptionPatterns, this.mOnWarningClickListener);
    }

    @Override
    public final void onBindViewHolder (
            @NonNull final WarningViewHolder holder,
            final int position
    ) {
        final Warning warning = this.mWarningList.get(position);
        if (warning != null) {
            holder.bind(warning);
        }
    }

    @Override
    public int getItemCount () {
        return mWarningList.size();
    }

    public final void setData (@NonNull final Collection<Warning> warningCollection) {
        this.mWarningList.clear();
        this.mWarningList.addAll(warningCollection);
        notifyDataSetChanged();
    }

    static final class WarningViewHolder
            extends RecyclerView.ViewHolder {

        @NonNull
        private final WarningItemBinding mBinding;

        @NonNull
        private final HashMap<Class, String> mWarningDesriptionPatternsMap;

        @NonNull
        private final OnWarningClickListener mOnWarningClickListener;

        WarningViewHolder (
                @NonNull final WarningItemBinding binding,
                @NonNull final HashMap<Class, String> warningDesriptionPatternsMap,
                @NonNull final OnWarningClickListener onWarningClickListener
        ) {
            super(binding.getRoot());
            this.mBinding = binding;
            this.mWarningDesriptionPatternsMap = warningDesriptionPatternsMap;
            this.mOnWarningClickListener = onWarningClickListener;
        }

        void bind (@NonNull final Warning warning) {
            this.itemView.setOnClickListener(l -> mOnWarningClickListener.OnClick(warning));
            this.mBinding.setWarning(warning);
            this.mBinding.setDescription(createDescription(warning));
            this.mBinding.executePendingBindings();
        }

        @NonNull
        private String createDescription (@NonNull final Warning warning) {
            final String warningDescription;
            final String descriptionPattern = this.mWarningDesriptionPatternsMap.get(warning.getClass());
            assert descriptionPattern != null : String.format("There is no description pattern for warning %s", VagueKeyzWarning.class);
            if (warning instanceof VagueKeyzWarning) {
                warningDescription = createVagueKeyzDescription((VagueKeyzWarning) warning, descriptionPattern);
            } else if (warning instanceof VagueLikeWarning) {
                warningDescription = createVagueLikeDescription((VagueLikeWarning) warning, descriptionPattern);
            } else if (warning instanceof MissingKeyzWarning) {
                warningDescription = createMissingKeyzDescription((MissingKeyzWarning) warning, descriptionPattern);
            } else {
                throw new IllegalArgumentException(String.format("Unrecognized warning type %s", warning.getClass()));
            }
            return warningDescription;
        }

        @NonNull
        private String createVagueKeyzDescription (
                @NonNull final VagueKeyzWarning warning,
                @NonNull final String descriptionPattern
        ) {
            final StringBuilder contacts = new StringBuilder();
            for (Contact contact : warning.getKeyzWithContacts().getContactList()) {
                if (contacts.length() > 0) {
                    contacts.append(", ");
                }
                contacts.append(contact.getTitle());
            }
            return String.format(descriptionPattern, warning.getKeyzWithContacts().getKeyz().getValue(), contacts);
        }

        @NonNull
        private String createVagueLikeDescription (
                @NonNull final VagueLikeWarning warning,
                @NonNull final String descriptionPattern
        ) {
            final StringBuilder keyzString = new StringBuilder();
            for (Keyz keyz : warning.getKeyzSet()) {
                if (keyzString.length() > 0) {
                    keyzString.append(", ");
                }
                keyzString.append(keyz.getValue());
            }
            return String.format(descriptionPattern, warning.getLike(), keyzString);
        }

        @NonNull
        private String createMissingKeyzDescription (
                @NonNull final MissingKeyzWarning warning,
                @NonNull final String descriptionPattern
        ) {
            final StringBuilder keyzs = new StringBuilder();
            for (Keyz keyz : warning.getLikeWithKeyz().getKeyzSet()) {
                if (keyzs.length() > 0) {
                    keyzs.append(", ");
                }
                keyzs.append(keyz.getValue());
            }
            return String.format(descriptionPattern, warning.getLikeWithKeyz().getLike(), keyzs);
        }
    }
}
