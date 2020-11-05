package blagodarie.rating.ui.user.operations;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.navigation.Navigation;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import blagodarie.rating.R;
import blagodarie.rating.databinding.OperationItemBinding;
import blagodarie.rating.model.IDisplayOperation;

public final class OperationsAdapter
        extends PagedListAdapter<IDisplayOperation, OperationsAdapter.OperationViewHolder> {

    public OperationsAdapter () {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public OperationViewHolder onCreateViewHolder (
            @NonNull ViewGroup parent,
            int viewType
    ) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final OperationItemBinding binding = DataBindingUtil.inflate(inflater, R.layout.operation_item, parent, false);
        return new OperationsAdapter.OperationViewHolder(binding);
    }


    @Override
    public void onBindViewHolder (
            @NonNull OperationViewHolder holder,
            int position
    ) {
        final IDisplayOperation operation = getItem(position);
        if (operation != null) {
            holder.bind(operation);
        }
    }

    static final class OperationViewHolder
            extends RecyclerView.ViewHolder {

        @NonNull
        private final OperationItemBinding mBinding;

        OperationViewHolder (@NonNull final OperationItemBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }

        void bind (
                @NonNull final IDisplayOperation operation
        ) {
            itemView.setOnClickListener(view -> Navigation.findNavController(itemView).navigate(Uri.parse(mBinding.getRoot().getContext().getString(R.string.url_profile, operation.getUserIdFrom().toString()))));
            mBinding.setOperation(operation);
            mBinding.setOperationName(mBinding.getRoot().getContext().getString(operation.getOperationType().getNameResId()));
            mBinding.setPhotoUrl(operation.getPhoto());
        }
    }

    private static DiffUtil.ItemCallback<IDisplayOperation> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<IDisplayOperation>() {

                @Override
                public boolean areItemsTheSame (
                        IDisplayOperation oldItem,
                        IDisplayOperation newItem
                ) {
                    return false;
                }

                @Override
                public boolean areContentsTheSame (
                        IDisplayOperation oldItem,
                        IDisplayOperation newItem
                ) {
                    return false;
                }
            };
}
