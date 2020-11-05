package blagodarie.rating.ui

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import blagodarie.rating.R
import blagodarie.rating.databinding.OwnOperationItemBinding
import blagodarie.rating.model.IDisplayOperation
import java.util.*

class OwnOperationsAdapter(val onThanksClickListener: OnThanksClickListener) : PagedListAdapter<IDisplayOperation, OwnOperationsAdapter.OwnOperationViewHolder>(DIFF_CALLBACK) {

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
    ): OwnOperationViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: OwnOperationItemBinding = DataBindingUtil.inflate(inflater, R.layout.own_operation_item, parent, false)
        return OwnOperationViewHolder(binding, onThanksClickListener)
    }

    override fun onBindViewHolder(holder: OwnOperationViewHolder, position: Int) {
        val operation = getItem(position)
        if (operation != null) {
            holder.bind(operation)
        }
    }

    class OwnOperationViewHolder(
            val binding: OwnOperationItemBinding,
            private val onThanksClickListener: OnThanksClickListener
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
                operation: IDisplayOperation
        ) {
            itemView.setOnClickListener { Navigation.findNavController(itemView).navigate(Uri.parse(binding.root.context.getString(R.string.url_profile, operation.userIdFrom.toString()))) }
            binding.operation = operation
            binding.operationName = binding.root.context.getString(operation.operationType.nameResId)
            binding.photoUrl = operation.photo
            binding.fabThanks.setOnClickListener {
                onThanksClickListener.onThanksClick(operation.userIdFrom)
            }
        }
    }
}