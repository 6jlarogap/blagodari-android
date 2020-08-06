package blagodarie.rating.ui.operations;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.UUID;
import java.util.concurrent.Executors;

import blagodarie.rating.R;
import blagodarie.rating.databinding.OperationsActivityBinding;

public final class OperationsActivity
        extends AppCompatActivity {

    private static final String TAG = OperationsActivity.class.getSimpleName();

    private static final String EXTRA_USER_ID = "blagodarie.rating.ui.operations.OperationsActivity.USER_ID";

    private UUID mUserId;

    private OperationsViewModel mViewModel;

    private OperationsActivityBinding mActivityBinding;

    private OperationAdapter mOperationsAdapter;

    @Override
    protected void onCreate (@Nullable final Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        mUserId = (UUID) getIntent().getSerializableExtra(EXTRA_USER_ID);
        if (mUserId != null) {
            initViewModel();
            initBinding();
            initOperationsAdapter();
        } else {
            Toast.makeText(this, R.string.err_msg_missing_profile_user_id, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initViewModel () {
        Log.d(TAG, "initViewModel");
        mViewModel = new ViewModelProvider(this).get(OperationsViewModel.class);
    }

    private void initBinding () {
        Log.d(TAG, "initBinding");
        mActivityBinding = DataBindingUtil.setContentView(this, R.layout.operations_activity);
        mActivityBinding.setViewModel(mViewModel);
    }

    private void initOperationsAdapter () {
        if (mOperationsAdapter == null) {
            OperationDataSource.OperationSourceFactory sourceFactory = new OperationDataSource.OperationSourceFactory(mUserId);

            PagedList.Config config = new PagedList.Config.Builder()
                    .setEnablePlaceholders(false)
                    .setPageSize(10)
                    .build();

            mViewModel.setOperations(
                    new LivePagedListBuilder<>(sourceFactory, config).
                            setFetchExecutor(Executors.newSingleThreadExecutor()).
                            build()
            );

            mOperationsAdapter = new OperationAdapter();
            mViewModel.getOperations().observe(this, mOperationsAdapter::submitList);

            mActivityBinding.rvOperations.setLayoutManager(new LinearLayoutManager(this));
            mActivityBinding.rvOperations.setAdapter(mOperationsAdapter);
        }
    }

    public static Intent createSelfIntent (
            @NonNull final Context context,
            @NonNull final UUID userId
    ) {
        Log.d(TAG, "createSelfIntent");
        final Intent intent = new Intent(context, OperationsActivity.class);
        intent.putExtra(EXTRA_USER_ID, userId);
        return intent;
    }
}
