package blagodarie.rating.ui

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableBoolean
import androidx.navigation.Navigation
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import blagodarie.rating.R
import blagodarie.rating.databinding.OperationItemBinding
import blagodarie.rating.model.IDisplayOperation
import java.util.*

class OperationsAdapter(
        val isOwn: ObservableBoolean,
        private val onThanksClickListener: OnThanksClickListener
) : PagedListAdapter<IDisplayOperation, OperationsAdapter.OperationViewHolder>(DIFF_CALLBACK) {

    fun interface OnThanksClickListener {
        fun onThanksClick(userId: UUID)
    }

    companion object {
        private val DIFF_CALLBACK: DiffUtil.ItemCallback<IDisplayOperation> = object : DiffUtil.ItemCallback<IDisplayOperation>() {
            override fun areItemsTheSame(
                    oldItem: IDisplayOperation,
                    newItem: IDisplayOperation
            ): Boolean {
                return false
            }

            override fun areContentsTheSame(
                    oldItem: IDisplayOperation,
                    newItem: IDisplayOperation
            ): Boolean {
                return false
            }
        }
    }

    override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
    ): OperationViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: OperationItemBinding = DataBindingUtil.inflate(inflater, R.layout.operation_item, parent, false)
        return OperationViewHolder(binding, isOwn, onThanksClickListener)
    }

    override fun onBindViewHolder(holder: OperationViewHolder, position: Int) {
        val operation = getItem(position)
        if (operation != null) {
            holder.bind(operation)
        }
    }

    class OperationViewHolder(
            val binding: OperationItemBinding,
            private val isOwn: ObservableBoolean,
            private val onThanksClickListener: OnThanksClickListener
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
                operation: IDisplayOperation
        ) {
            itemView.setOnClickListener { Navigation.findNavController(itemView).navigate(Uri.parse(binding.root.context.getString(R.string.url_profile, operation.userIdFrom.toString()))) }
            binding.operation = operation
            binding.operationName = binding.root.context.getString(operation.operationType.nameResId)
            binding.photoUrl = operation.photo
            binding.isOwn = isOwn
            binding.fabThanks.setOnClickListener {
                onThanksClickListener.onThanksClick(operation.userIdFrom)
            }
        }
    }
}