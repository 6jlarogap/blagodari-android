package blagodarie.rating.ui.wishes;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import java.util.Date;

import blagodarie.rating.R;
import blagodarie.rating.databinding.EditWishActivityBinding;

public final class EditWishActivity
        extends AppCompatActivity {

    private static final String TAG = WishesActivity.class.getSimpleName();

    public static final String EXTRA_WISH = "blagodarie.rating.ui.wishes.EditWishActivity.WISH";

    private Wish mWish;

    private EditWishActivityBinding mActivityBinding;

    @Override
    protected void onCreate (@Nullable final Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        mWish = (Wish) getIntent().getSerializableExtra(EXTRA_WISH);

        if (mWish != null) {
            initBinding();
        } else {
            Toast.makeText(this, R.string.err_msg_missing_wish, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initBinding (
    ) {
        Log.d(TAG, "initBinding");
        mActivityBinding = DataBindingUtil.setContentView(this, R.layout.edit_wish_activity);
        mActivityBinding.setWish(mWish);
    }

    public void onSaveWishClick (@NonNull final View view) {
        mWish.setText(mActivityBinding.etWishText.getText().toString());
        mWish.setTimestamp(new Date());
        final Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_WISH, mWish);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    public static Intent createSelfIntent (
            @NonNull final Context context,
            @NonNull final Wish wish
    ) {
        Log.d(TAG, "createSelfIntent");
        final Intent intent = new Intent(context, EditWishActivity.class);
        intent.putExtra(EXTRA_WISH, wish);
        return intent;
    }
}
