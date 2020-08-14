package blagodarie.rating.ui.user.operations;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import blagodarie.rating.R;
import blagodarie.rating.databinding.OperationItemBinding;

public final class OperationAdapter
        extends PagedListAdapter<Operation, OperationAdapter.OperationViewHolder> {

    protected OperationAdapter () {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public OperationViewHolder onCreateViewHolder (
            @NonNull ViewGroup parent,
            int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final OperationItemBinding binding = DataBindingUtil.inflate(inflater, R.layout.operation_item, parent, false);
        return new OperationAdapter.OperationViewHolder(binding);
    }


    @Override
    public void onBindViewHolder (
            @NonNull OperationViewHolder holder,
            int position
    ) {
        final Operation concert = getItem(position);
        if (concert != null) {
            holder.bind(concert);
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
                @NonNull final Operation operation
        ) {
            mBinding.setOperation(operation);
            mBinding.setOperationName(mBinding.getRoot().getContext().getString(operation.getOperationType().getNameResId()));
            Picasso.get().load(operation.getPhoto()).into(mBinding.ivPhoto);
        }
    }

    private static DiffUtil.ItemCallback<Operation> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Operation>() {

                @Override
                public boolean areItemsTheSame (
                        Operation oldOperation,
                        Operation newOperation
                ) {
                    return false;
                }

                @Override
                public boolean areContentsTheSame (
                        Operation oldOperation,
                        Operation newOperation
                ) {
                    return false;
                }
            };
}
