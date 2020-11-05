package blagodarie.rating.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableBoolean
import androidx.navigation.Navigation
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import blagodarie.rating.R
import blagodarie.rating.databinding.WishItemBinding
import blagodarie.rating.model.IWish
import blagodarie.rating.model.entities.Wish

class WishesAdapter(
        val isOwn: ObservableBoolean,
        private val adapterCommunicator: AdapterCommunicator
) : PagedListAdapter<IWish, WishesAdapter.WishViewHolder>(DIFF_CALLBACK) {

    interface AdapterCommunicator {
        fun onDeleteClick(wish: IWish)
    }

    companion object {
        private val DIFF_CALLBACK: DiffUtil.ItemCallback<IWish> = object : DiffUtil.ItemCallback<IWish>() {
            override fun areItemsTheSame(
                    oldItem: IWish,
                    newItem: IWish
            ): Boolean {
                return false
            }

            override fun areContentsTheSame(
                    oldItem: IWish,
                    newItem: IWish
            ): Boolean {
                return false
            }
        }
    }

    override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
    ): WishViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: WishItemBinding = DataBindingUtil.inflate(inflater, R.layout.wish_item, parent, false)
        return WishViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WishViewHolder, position: Int) {
        val wish = getItem(position)
        if (wish != null) {
            holder.bind(wish, isOwn, adapterCommunicator)
        }
    }

    class WishViewHolder(
            val binding: WishItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
                wish: IWish,
                isOwn: ObservableBoolean,
                adapterCommunicator: AdapterCommunicator
        ) {
            itemView.setOnClickListener {
                val action = WishesFragmentDirections.actionWishesFragmentToWishFragment(wish.id.toString())
                Navigation.findNavController(itemView).navigate(action)
            }
            binding.wish = wish
            binding.isOwn = isOwn
            binding.btnEdit.setOnClickListener {
                val action = WishesFragmentDirections.actionWishesFragmentToEditWishFragment(wish as Wish)
                Navigation.findNavController(itemView).navigate(action)
            }
            binding.btnDelete.setOnClickListener {
                adapterCommunicator.onDeleteClick(wish)
            }
        }
    }
}