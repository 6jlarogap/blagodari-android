package blagodarie.rating.ui.user.keys;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
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

public final class KeysAdapter
        extends PagedListAdapter<Key, KeysAdapter.KeyViewHolder> {

    public interface AdapterCommunicator {
        void onEditKey (@NonNull final Key key);

        void onDeleteKey (@NonNull final Key key);
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
            @NonNull ViewGroup parent,
            int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final KeyItemBinding binding = DataBindingUtil.inflate(inflater, R.layout.key_item, parent, false);
        return new KeysAdapter.KeyViewHolder(mIsOwnProfile, binding);
    }


    @Override
    public void onBindViewHolder (
            @NonNull KeysAdapter.KeyViewHolder holder,
            int position
    ) {
        final Key key = getItem(position);
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
                @NonNull final Key key,
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
                    final EditText etKeyValue = new EditText(context);
                    etKeyValue.setText(key.getValue());
                    new AlertDialog.
                            Builder(context).
                            setTitle(R.string.rqst_enter_key).
                            setView(etKeyValue).
                            setPositiveButton(R.string.btn_update, (dialogInterface, i) -> {
                                if (!etKeyValue.getText().toString().isEmpty()) {
                                    adapterCommunicator.onEditKey(new Key(key.getId(), key.getOwnerId(), etKeyValue.getText().toString(), key.getKeyType()));
                                } else {
                                    etKeyValue.setError(context.getString(R.string.err_msg_required_to_fill));
                                }
                            }).
                            setNegativeButton(R.string.btn_cancel, null).
                            create().
                            show();
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
                            setPositiveButton(R.string.btn_yes, (dialogInterface, i) -> {
                                adapterCommunicator.onDeleteKey(key);
                            }).
                            setNegativeButton(R.string.btn_no, null).
                            create().
                            show();
                }
            });
        }
    }

    private static DiffUtil.ItemCallback<Key> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Key>() {

                @Override
                public boolean areItemsTheSame (
                        Key oldKey,
                        Key newKey
                ) {
                    return false;
                }

                @Override
                public boolean areContentsTheSame (
                        Key oldKey,
                        Key newKey
                ) {
                    return false;
                }
            };
}
