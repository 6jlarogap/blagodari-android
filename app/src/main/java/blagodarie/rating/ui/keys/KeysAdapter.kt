package blagodarie.rating.ui.keys

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableBoolean
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import blagodarie.rating.R
import blagodarie.rating.databinding.KeyItemBinding
import blagodarie.rating.model.IKey
import blagodarie.rating.model.entities.Key

class KeysAdapter(
        val isOwn: ObservableBoolean,
        private val adapterCommunicator: AdapterCommunicator
) : PagedListAdapter<IKey, KeysAdapter.KeyViewHolder>(DIFF_CALLBACK) {

    interface AdapterCommunicator {
        fun onEditKey(key: IKey)
        fun onDeleteKey(key: IKey)
    }

    interface UserActionListener {
        fun onEditClick()
        fun onCopyClick()
        fun onDeleteClick()
    }

    companion object {
        private val DIFF_CALLBACK: DiffUtil.ItemCallback<IKey> = object : DiffUtil.ItemCallback<IKey>() {
            override fun areItemsTheSame(
                    oldItem: IKey,
                    newItem: IKey
            ): Boolean {
                return false
            }

            override fun areContentsTheSame(
                    oldItem: IKey,
                    newItem: IKey
            ): Boolean {
                return false
            }
        }
    }

    override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
    ): KeyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: KeyItemBinding = DataBindingUtil.inflate(inflater, R.layout.key_item, parent, false)
        return KeyViewHolder(binding)
    }

    override fun onBindViewHolder(
            holder: KeyViewHolder,
            position: Int
    ) {
        val key = getItem(position)
        if (key != null) {
            holder.bind(key, isOwn, adapterCommunicator)
        }
    }

    class KeyViewHolder(
            val binding: KeyItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
                key: IKey,
                isOwn: ObservableBoolean,
                adapterCommunicator: AdapterCommunicator
        ) {
            binding.key = key
            binding.isOwn = isOwn
            binding.keyName = String.format(
                    binding.root.context.getString(R.string.txt_key),
                    binding.root.context.getString(key.keyType.nameResId),
                    key.value
            )
            binding.userActionListener = object : UserActionListener {
                override fun onEditClick() {
                    val context: Context = binding.root.context
                    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    val etKeyValue = EditText(context)
                    etKeyValue.setText(key.value)
                    etKeyValue.imeOptions = EditorInfo.IME_ACTION_DONE
                    val dialog = AlertDialog.Builder(context).setTitle(R.string.rqst_enter_key).setView(etKeyValue).setPositiveButton(R.string.btn_update, null).setNegativeButton(R.string.btn_cancel) { _, _ -> imm.hideSoftInputFromWindow(etKeyValue.windowToken, 0) }.create()
                    dialog.show()
                    etKeyValue.isSingleLine = true
                    etKeyValue.setOnEditorActionListener { _, actionId, _ ->
                        var handled = false
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            val value = etKeyValue.text.toString().trim()
                            if (value.isNotBlank()) {
                                imm.hideSoftInputFromWindow(etKeyValue.windowToken, 0)
                                dialog.dismiss()
                                adapterCommunicator.onEditKey(Key(key.id, key.ownerId, value, key.keyType))
                            } else {
                                etKeyValue.error = context.getString(R.string.err_msg_required_to_fill)
                            }
                            handled = true
                        }
                        handled
                    }
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                        val value = etKeyValue.text.toString().trim()
                        if (value.isNotBlank()) {
                            imm.hideSoftInputFromWindow(etKeyValue.windowToken, 0)
                            dialog.dismiss()
                            adapterCommunicator.onEditKey(Key(key.id, key.ownerId, value, key.keyType))
                        } else {
                            etKeyValue.error = context.getString(R.string.err_msg_required_to_fill)
                        }
                    }
                    etKeyValue.requestFocus()
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
                }

                override fun onCopyClick() {
                    val context: Context = binding.root.context
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText(context.getText(R.string.txt_card_number), key.value)
                    if (clipboard != null) {
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, R.string.info_msg_copied_to_clipboard, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onDeleteClick() {
                    val context: Context = binding.root.context
                    AlertDialog.Builder(context).setMessage(R.string.qstn_delete_key).setPositiveButton(R.string.btn_yes) { _, _ -> adapterCommunicator.onDeleteKey(key) }.setNegativeButton(R.string.btn_no, null).create().show()
                }
            }
        }
    }
}