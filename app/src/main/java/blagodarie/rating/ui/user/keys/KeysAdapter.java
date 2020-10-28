package blagodarie.rating.ui.user.keys;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import blagodarie.rating.R;
import blagodarie.rating.databinding.KeyItemBinding;
import blagodarie.rating.model.IKey;
import blagodarie.rating.model.entities.Key;

public final class KeysAdapter
        extends PagedListAdapter<IKey, KeysAdapter.KeyViewHolder> {

    public interface AdapterCommunicator {
        void onEditKey (@NonNull final IKey key);

        void onDeleteKey (@NonNull final IKey key);
    }

    public interface UserActionListener {
        void onEditClick ();

        void onCopyClick ();

        void onDeleteClick ();
    }

    @NonNull
    private final AdapterCommunicator mAdapterCommunicator;

    private final boolean mIsOwnProfile;

    protected KeysAdapter (
            final boolean isOwnProfile,
            @NonNull final AdapterCommunicator adapterCommunicator
    ) {
        super(DIFF_CALLBACK);
        mIsOwnProfile = isOwnProfile;
        mAdapterCommunicator = adapterCommunicator;
    }

    @NonNull
    @Override
    public KeysAdapter.KeyViewHolder onCreateViewHolder (
            @NonNull final ViewGroup parent,
            final int viewType
    ) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final KeyItemBinding binding = DataBindingUtil.inflate(inflater, R.layout.key_item, parent, false);
        return new KeysAdapter.KeyViewHolder(mIsOwnProfile, binding);
    }


    @Override
    public void onBindViewHolder (
            @NonNull KeysAdapter.KeyViewHolder holder,
            int position
    ) {
        final IKey key = getItem(position);
        if (key != null) {
            holder.bind(key, mAdapterCommunicator);
        }
    }

    static final class KeyViewHolder
            extends RecyclerView.ViewHolder {

        @NonNull
        private final KeyItemBinding mBinding;

        private final boolean mIsOwnProfile;

        KeyViewHolder (
                final boolean isOwnProfile,
                @NonNull final KeyItemBinding binding
        ) {
            super(binding.getRoot());
            mIsOwnProfile = isOwnProfile;
            mBinding = binding;
        }

        void bind (
                @NonNull final IKey key,
                @NonNull final AdapterCommunicator adapterCommunicator
        ) {
            mBinding.setIsOwnProfile(mIsOwnProfile);
            mBinding.setKeyName(
                    String.format(
                            mBinding.getRoot().getContext().getString(R.string.txt_key),
                            mBinding.getRoot().getContext().getString(key.getKeyType().getNameResId()),
                            key.getValue()
                    )
            );
            mBinding.setKey(key);
            mBinding.setUserActionListener(new UserActionListener() {
                @Override
                public void onEditClick () {
                    final Context context = mBinding.getRoot().getContext();
                    final InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    final EditText etKeyValue = new EditText(context);
                    etKeyValue.setText(key.getValue());
                    final AlertDialog dialog = new AlertDialog.
                            Builder(context).
                            setTitle(R.string.rqst_enter_key).
                            setView(etKeyValue).
                            setPositiveButton(R.string.btn_update, null).
                            setNegativeButton(R.string.btn_cancel, (dialogInterface, i) -> imm.hideSoftInputFromWindow(etKeyValue.getWindowToken(), 0)).
                            create();
                    dialog.show();

                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(view -> {
                        if (!etKeyValue.getText().toString().isEmpty()) {
                            imm.hideSoftInputFromWindow(etKeyValue.getWindowToken(), 0);
                            dialog.dismiss();
                            adapterCommunicator.onEditKey(new Key(key.getId(), key.getOwnerId(), etKeyValue.getText().toString(), key.getKeyType()));
                        } else {
                            etKeyValue.setError(context.getString(R.string.err_msg_required_to_fill));
                        }
                    });
                    etKeyValue.requestFocus();
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                }

                @Override
                public void onCopyClick () {
                    final Context context = mBinding.getRoot().getContext();
                    final ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    final ClipData clip = ClipData.newPlainText(context.getText(R.string.txt_card_number), key.getValue());
                    if (clipboard != null) {
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(context, R.string.info_msg_copied_to_clipboard, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onDeleteClick () {
                    final Context context = mBinding.getRoot().getContext();
                    new AlertDialog.
                            Builder(context).
                            setMessage(R.string.qstn_delete_key).
                            setPositiveButton(R.string.btn_yes, (dialogInterface, i) -> adapterCommunicator.onDeleteKey(key)).
                            setNegativeButton(R.string.btn_no, null).
                            create().
                            show();
                }
            });
        }
    }

    private static DiffUtil.ItemCallback<IKey> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<IKey>() {

                @Override
                public boolean areItemsTheSame (
                        IKey oldItem,
                        IKey newItem
                ) {
                    return false;
                }

                @Override
                public boolean areContentsTheSame (
                        IKey oldItem,
                        IKey newItem
                ) {
                    return false;
                }
            };
}
